package com.github.UsefulHands.reception.features.reservation;

import com.github.UsefulHands.reception.common.exception.DataIntegrityException;
import com.github.UsefulHands.reception.common.exception.ResourceNotFoundException;
import com.github.UsefulHands.reception.features.audit.AuditLogService;
import com.github.UsefulHands.reception.features.guest.GuestEntity;
import com.github.UsefulHands.reception.features.guest.GuestRepository;
import com.github.UsefulHands.reception.features.guest.GuestService;
import com.github.UsefulHands.reception.features.reservation.dtos.ReservationCreateRequest;
import com.github.UsefulHands.reception.features.reservation.dtos.ReservationDto;
import com.github.UsefulHands.reception.features.reservation.dtos.ReservationGridDto;
import com.github.UsefulHands.reception.features.reservation.dtos.ReservationUpdateRequest;
import com.github.UsefulHands.reception.features.room.RoomEntity;
import com.github.UsefulHands.reception.features.room.RoomRepository;
import com.github.UsefulHands.reception.features.user.UserEntity;
import com.github.UsefulHands.reception.features.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final GuestRepository guestRepository;
    private final UserService userService;
    private final RoomRepository roomRepository;
    private final ReservationMapper reservationMapper;
    private final GuestService guestService;
    private final AuditLogService auditLogService;

    @Transactional
    public ReservationDto createWithNewUser(ReservationCreateRequest request) {
        validateReservationDates(request.checkInDate(), request.checkOutDate());

        RoomEntity room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        validateRoomStatus(room);
        checkRoomAvailability(request.roomId(), request.checkInDate(), request.checkOutDate());

        String username = request.firstNameOrEmpty().toLowerCase() + "Guest";
        String password = request.lastNameOrEmpty().toLowerCase() + "123";
        UserEntity user = userService.createAccount(username, password, "ROLE_GUEST");

        GuestEntity guest = guestRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    GuestEntity newGuest = new GuestEntity();
                    newGuest.setUser(user);
                    return newGuest;
                });
        guest.setFirstName(request.guestFirstName());
        guest.setLastName(request.guestLastName());
        guest.setPhoneNumber(request.phoneNumber());
        guest.setIdentityNumber(request.identityNumber());
        guest = guestRepository.save(guest);

        return createReservationInternal(guest, request);
    }

    @Transactional
    public ReservationDto createForExistingGuest(ReservationCreateRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        GuestEntity guest = guestService.findGuestByUserUsername(username);

        validateReservationDates(request.checkInDate(), request.checkOutDate());

        RoomEntity room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        validateRoomStatus(room);
        checkRoomAvailability(request.roomId(), request.checkInDate(), request.checkOutDate());

        return createReservationInternal(guest, request);
    }

    private void validateReservationDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new DataIntegrityException("Check-in and check-out dates are required");
        }

        if (checkIn.isAfter(checkOut) || checkIn.isEqual(checkOut)) {
            throw new DataIntegrityException("Check-out date must be at least 1 day after check-in date");
        }

        long daysBetween = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (daysBetween < 1) {
            throw new DataIntegrityException("Minimum stay is 1 night");
        }

        if (checkIn.isBefore(LocalDate.now())) {
            throw new DataIntegrityException("Check-in date cannot be in the past");
        }

        if (checkIn.isAfter(LocalDate.now().plusYears(1))) {
            throw new DataIntegrityException("Cannot book more than 1 year in advance");
        }

        if (daysBetween > 30) {
            throw new DataIntegrityException("Maximum stay duration is 30 nights");
        }
    }

    private void validateRoomStatus(RoomEntity room) {
        if (room.getStatus() == null) {
            return;
        }

        String status = room.getStatus().toString().toUpperCase();
        if ("MAINTENANCE".equals(status) || "OUT_OF_SERVICE".equals(status)) {
            throw new DataIntegrityException("Room is not available for booking");
        }
    }

    private ReservationDto createReservationInternal(GuestEntity guest, ReservationCreateRequest request) {
        RoomEntity room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        long days = ChronoUnit.DAYS.between(request.checkInDate(), request.checkOutDate());
        if (days <= 0) days = 1;
        BigDecimal totalPrice = room.getPrice().multiply(BigDecimal.valueOf(days));

        ReservationEntity res = ReservationEntity.builder()
                .guest(guest)
                .room(room)
                .checkInDate(request.checkInDate())
                .checkOutDate(request.checkOutDate())
                .status(request.status() != null ? request.status() : ReservationStatus.CONFIRMED)
                .totalPrice(totalPrice)
                .amountPaid(BigDecimal.ZERO)
                .isDeleted(false)
                .build();

        ReservationEntity saved = reservationRepository.save(res);

        auditLogService.log("RESERVATION_CREATE", getSafeActor(),
                "Room: " + room.getRoomNumber() + " reserved for " + guest.getFirstName() +
                        " | Status: " + res.getStatus() + " | Dates: " + request.checkInDate() + " to " + request.checkOutDate());

        return reservationMapper.toDto(saved);
    }

    @Transactional
    public ReservationDto updateReservation(Long id, ReservationUpdateRequest request) {
        log.info("Updating reservation ID: {}", id);

        validateReservationDates(request.checkInDate(), request.checkOutDate());

        ReservationEntity res = reservationRepository.findById(id)
                .filter(r -> !r.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Active reservation not found with id: " + id));

        RoomEntity room = res.getRoom();
        if (!res.getRoom().getId().equals(request.roomId())) {
            room = roomRepository.findById(request.roomId())
                    .filter(r -> !r.isDeleted())
                    .orElseThrow(() -> new ResourceNotFoundException("New room not found or deleted"));

            validateRoomStatus(room);
        }

        boolean isOverlapping = reservationRepository.findOverlappingReservations(
                room.getId(), request.checkInDate(), request.checkOutDate()
        ).stream().anyMatch(r -> !r.getId().equals(id));

        if (isOverlapping) {
            throw new DataIntegrityException("Room is not available for the selected dates. Please choose different dates.");
        }

        if (request.status() != null) {
            validateStatusTransition(res.getStatus(), request.status());
            res.setStatus(request.status());
        }

        res.setRoom(room);
        res.setCheckInDate(request.checkInDate());
        res.setCheckOutDate(request.checkOutDate());

        long days = ChronoUnit.DAYS.between(request.checkInDate(), request.checkOutDate());
        if (days <= 0) days = 1;
        res.setTotalPrice(room.getPrice().multiply(BigDecimal.valueOf(days)));

        ReservationEntity updated = reservationRepository.save(res);

        auditLogService.log("RESERVATION_UPDATE", getSafeActor(),
                "Updated Res ID: " + id + " | Room: " + room.getRoomNumber() + " | Status: " + updated.getStatus());

        return reservationMapper.toDto(updated);
    }

    private void validateStatusTransition(ReservationStatus currentStatus, ReservationStatus newStatus) {
        if (currentStatus == newStatus) {
            return;
        }

        boolean isValidTransition = switch (currentStatus) {
            case PENDING -> newStatus == ReservationStatus.CONFIRMED ||
                    newStatus == ReservationStatus.CANCELLED;
            case CONFIRMED -> newStatus == ReservationStatus.CHECKED_IN ||
                    newStatus == ReservationStatus.CANCELLED ||
                    newStatus == ReservationStatus.NO_SHOW;
            case CHECKED_IN -> newStatus == ReservationStatus.CHECKED_OUT;
            case CHECKED_OUT, CANCELLED, NO_SHOW -> false;
        };

        if (!isValidTransition) {
            throw new DataIntegrityException(
                    String.format("Cannot transition reservation status from %s to %s", currentStatus, newStatus)
            );
        }
    }

    private void checkRoomAvailability(Long roomId, LocalDate start, LocalDate end) {
        List<ReservationEntity> overlaps = reservationRepository.findOverlappingReservations(roomId, start, end);
        if (!overlaps.isEmpty()) {
            throw new DataIntegrityException("Room is not available for the selected dates. Please choose different dates.");
        }
    }

    @Transactional
    public void cancelReservation(Long id) {
        ReservationEntity res = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        if (res.getStatus() == ReservationStatus.CHECKED_OUT) {
            throw new DataIntegrityException("Cannot cancel a reservation that has already been checked out");
        }

        if (res.getStatus() == ReservationStatus.CANCELLED) {
            throw new DataIntegrityException("Reservation is already cancelled");
        }

        res.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(res);

        auditLogService.log("RESERVATION_CANCEL", getSafeActor(), "ID: " + id + " cancelled.");
    }

    @Transactional
    public void deleteReservation(Long id) {
        log.info("Deleting reservation with ID: {}", id);

        ReservationEntity res = reservationRepository.findById(id)
                .filter(r -> !r.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Active reservation not found with id: " + id));

        if (res.getStatus() == ReservationStatus.CONFIRMED &&
                res.getCheckInDate().isBefore(LocalDate.now().plusDays(1))) {
            log.warn("Attempt to delete an active or near-term reservation: {}", id);
        }

        res.setDeleted(true);
        res.setStatus(ReservationStatus.CANCELLED);

        reservationRepository.save(res);

        auditLogService.log(
                "RESERVATION_DELETE",
                getSafeActor(),
                "Deleted Reservation ID: " + id + " for Guest: " + res.getGuest().getLastName()
        );

        log.info("Reservation {} successfully marked as deleted.", id);
    }

    private String getSafeActor() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(java.security.Principal::getName)
                .orElse("SYSTEM");
    }

    @Transactional(readOnly = true)
    public Map<Long, List<ReservationGridDto>> getGridData(LocalDate start, LocalDate end) {
        return reservationRepository.findAllByDateRange(start, end).stream()
                .map(res -> new ReservationGridDto(
                        res.getId(),
                        res.getRoom().getId(),
                        res.getCheckInDate(),
                        res.getCheckOutDate(),
                        res.getStatus().name(),
                        res.getRoom().getRoomNumber(),
                        res.getGuest().getFirstName(),
                        res.getGuest().getLastName()
                ))
                .collect(Collectors.groupingBy(ReservationGridDto::roomId));
    }

    @Transactional(readOnly = true)
    public List<ReservationGridDto> getPublicGridData(Long roomId, LocalDate start, LocalDate end) {
        return reservationRepository.findOverlappingReservations(roomId, start, end).stream()
                .map(res -> new ReservationGridDto(
                        null,
                        res.getRoom().getId(),
                        res.getCheckInDate(),
                        res.getCheckOutDate(),
                        "OCCUPIED",
                        res.getRoom().getRoomNumber(),
                        "",
                        ""
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReservationDto getReservation(Long id) {
        log.info("Fetching reservation details for ID: {}", id);

        return reservationRepository.findById(id)
                .filter(res -> !res.isDeleted())
                .map(res -> {
                    ReservationDto dto = reservationMapper.toDto(res);

                    if (res.getGuest() != null) {
                        dto.setGuestFullName(res.getGuest().getFirstName() + " " + res.getGuest().getLastName());
                        dto.setGuestFirstName(res.getGuest().getFirstName());
                        dto.setGuestLastName(res.getGuest().getLastName());
                        dto.setPhoneNumber(res.getGuest().getPhoneNumber());
                        dto.setIdentityNumber(res.getGuest().getIdentityNumber());
                    }
                    if (res.getRoom() != null) {
                        dto.setRoomNumber(res.getRoom().getRoomNumber());
                    }

                    return dto;
                })
                .orElseThrow(() -> new ResourceNotFoundException("Active reservation not found with id: " + id));
    }
}