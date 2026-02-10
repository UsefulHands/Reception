package com.github.UsefulHands.reception.features.reservation;

import com.github.UsefulHands.reception.common.exception.DataIntegrityException;
import com.github.UsefulHands.reception.common.exception.ResourceNotFoundException;
import com.github.UsefulHands.reception.features.audit.AuditLogService;
import com.github.UsefulHands.reception.features.guest.GuestEntity;
import com.github.UsefulHands.reception.features.guest.GuestRepository;
import com.github.UsefulHands.reception.features.guest.GuestService;
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
        checkRoomAvailability(request.roomId(), request.checkInDate(), request.checkOutDate());

        String username = request.firstName() + "Guest";
        String password = request.firstName().toLowerCase() + "123";
        UserEntity user = userService.createAccount(username, password, "ROLE_GUEST");

        GuestEntity guest = guestRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    GuestEntity newGuest = new GuestEntity();
                    newGuest.setUser(user);
                    return newGuest;
                });
        guest.setFirstName(request.firstName());
        guest.setLastName(request.lastName());
        guest.setPhoneNumber(request.phoneNumber());
        guest.setIdentityNumber(request.identityNumber());
        guest = guestRepository.save(guest);

        return createReservationInternal(guest, request);
    }

    @Transactional
    public ReservationDto createForExistingGuest(ReservationCreateRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        GuestEntity guest = guestService.findGuestByUserUsername(username);

        checkRoomAvailability(request.roomId(), request.checkInDate(), request.checkOutDate());

        return createReservationInternal(guest, request);
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
                // SABİT YERİNE BURAYI GÜNCELLEDİK:
                .status(request.status() != null ? request.status() : ReservationStatus.CONFIRMED)
                .totalPrice(totalPrice)
                .amountPaid(BigDecimal.ZERO)
                .isDeleted(false)
                .build();

        ReservationEntity saved = reservationRepository.save(res);

        auditLogService.log("RESERVATION_CREATE", getSafeActor(),
                "Room: " + room.getRoomNumber() + " reserved for " + guest.getFirstName() +
                        " | Status: " + res.getStatus()); // Loga statüyü ekledik

        return reservationMapper.toDto(saved);
    }

    @Transactional
    public ReservationDto updateReservation(Long id, ReservationUpdateRequest request) {
        log.info("Updating reservation ID: {}", id);

        ReservationEntity res = reservationRepository.findById(id)
                .filter(r -> !r.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Active reservation not found with id: " + id));

        RoomEntity room = res.getRoom();
        if (!res.getRoom().getId().equals(request.roomId())) {
            room = roomRepository.findById(request.roomId())
                    .filter(r -> !r.isDeleted())
                    .orElseThrow(() -> new ResourceNotFoundException("New room not found or deleted"));
        }

        boolean isOverlapping = reservationRepository.findOverlappingReservations(
                room.getId(), request.checkInDate(), request.checkOutDate()
        ).stream().anyMatch(r -> !r.getId().equals(id));

        if (isOverlapping) {
            throw new DataIntegrityException("Room is not available in this date!");
        }

        // GÜNCELLEMELER
        res.setRoom(room);
        res.setCheckInDate(request.checkInDate());
        res.setCheckOutDate(request.checkOutDate());

        // STATÜ GÜNCELLEMESİ BURAYA GELDİ:
        if (request.status() != null) {
            res.setStatus(request.status());
        }

        long days = ChronoUnit.DAYS.between(request.checkInDate(), request.checkOutDate());
        if (days <= 0) days = 1;
        res.setTotalPrice(room.getPrice().multiply(BigDecimal.valueOf(days)));

        ReservationEntity updated = reservationRepository.save(res);

        // Audit Log'u daha detaylı yapalım ki takip kolay olsun
        auditLogService.log("RESERVATION_UPDATE", getSafeActor(),
                "Updated Res ID: " + id + " | Room: " + room.getRoomNumber() + " | Status: " + updated.getStatus());

        return reservationMapper.toDto(updated);
    }

    private void checkRoomAvailability(Long roomId, LocalDate start, LocalDate end) {
        List<ReservationEntity> overlaps = reservationRepository.findOverlappingReservations(roomId, start, end);
        if (!overlaps.isEmpty()) {
            throw new DataIntegrityException("Room is reserved");
        }
    }

    @Transactional
    public void cancelReservation(Long id) {
        ReservationEntity res = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

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

        if (res.getStatus() == ReservationStatus.CONFIRMED && res.getCheckInDate().isBefore(LocalDate.now().plusDays(1))) {
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
                        res.getCheckInDate(),
                        res.getCheckOutDate(),
                        res.getStatus().name(),
                        res.getRoom().getRoomNumber(),
                        res.getGuest().getFirstName(),
                        res.getGuest().getLastName()
                ))
                .collect(Collectors.groupingBy(dto -> {
                    return reservationRepository.findById(dto.id()).get().getRoom().getId();
                }));
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
                    }
                    if (res.getRoom() != null) {
                        dto.setRoomNumber(res.getRoom().getRoomNumber());
                    }

                    return dto;
                })
                .orElseThrow(() -> new ResourceNotFoundException("Active reservation not found with id: " + id));
    }
}