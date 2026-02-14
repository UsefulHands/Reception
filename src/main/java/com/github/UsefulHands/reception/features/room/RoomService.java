package com.github.UsefulHands.reception.features.room;

import com.github.UsefulHands.reception.common.exception.DataIntegrityException;
import com.github.UsefulHands.reception.features.audit.AuditLogService;
import com.github.UsefulHands.reception.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;
    private final AuditLogService auditLogService;

    @Transactional
    public RoomDto createRoom(RoomDto roomDto) {
        log.info("Processing room creation for: {}", roomDto.getRoomNumber());

        validateRoomData(roomDto);

        try {
            return roomRepository.findByRoomNumber(roomDto.getRoomNumber())
                    .map(existingRoom -> {
                        if (!existingRoom.isDeleted()) {
                            throw new DataIntegrityException("Room number " + roomDto.getRoomNumber() + " already exists and is active!");
                        }
                        log.info("Re-activating deleted room: {}", roomDto.getRoomNumber());
                        roomMapper.updateEntityFromDto(roomDto, existingRoom);
                        existingRoom.setDeleted(false);
                        RoomEntity saved = roomRepository.saveAndFlush(existingRoom);

                        auditLogService.log("ROOM_REACTIVATE", getSafeActor(),
                                "Re-activated room: " + saved.getRoomNumber());

                        return roomMapper.toDto(saved);
                    })
                    .orElseGet(() -> {
                        log.info("Creating brand new room: {}", roomDto.getRoomNumber());
                        RoomEntity newRoom = roomMapper.toEntity(roomDto);
                        newRoom.setDeleted(false);
                        RoomEntity saved = roomRepository.saveAndFlush(newRoom);

                        auditLogService.log("ROOM_CREATE", getSafeActor(),
                                "Created new room: " + saved.getRoomNumber());

                        return roomMapper.toDto(saved);
                    });
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityException("A room with this number was just created by someone else!");
        }
    }

    @Transactional
    public RoomDto editRoom(Long id, RoomDto roomDto) {
        log.info("Updating room ID: {}", id);

        validateRoomData(roomDto);

        RoomEntity roomEntity = roomRepository.findById(id)
                .filter(room -> !room.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Active room not found with id: " + id));

        String oldRoomNumber = roomEntity.getRoomNumber();

        try {
            roomMapper.updateEntityFromDto(roomDto, roomEntity);
            RoomEntity updatedRoom = roomRepository.saveAndFlush(roomEntity);

            String actor = getSafeActor();
            String logMessage = "Updated room: " + updatedRoom.getRoomNumber();
            if (!oldRoomNumber.equals(updatedRoom.getRoomNumber())) {
                logMessage += " (formerly: " + oldRoomNumber + ")";
            }
            auditLogService.log("ROOM_UPDATE", actor, logMessage);

            return roomMapper.toDto(updatedRoom);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityException("Room number " + roomDto.getRoomNumber() + " is already taken by another room!");
        }
    }

    @Transactional
    public RoomDto deleteRoom(Long id) {
        log.info("Deleting room ID: {}", id);

        RoomEntity room = roomRepository.findById(id)
                .filter(r -> !r.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Active room not found with id: " + id));

        if (room.getStatus() == RoomStatus.OCCUPIED) {
            throw new DataIntegrityException("Cannot delete a room that is currently occupied!");
        }

        if (room.getStatus() == RoomStatus.RESERVED) {
            throw new DataIntegrityException("Cannot delete a room that has reservations!");
        }

        room.setDeleted(true);
        roomRepository.saveAndFlush(room);

        auditLogService.log("ROOM_DELETE", getSafeActor(), "Deleted room number: " + room.getRoomNumber());

        log.info("Room {} successfully marked as deleted", room.getRoomNumber());

        return roomMapper.toDto(room);
    }

    @Transactional(readOnly = true)
    public RoomDto getRoom(Long id) {
        log.debug("Fetching room with ID: {}", id);

        return roomRepository.findById(id)
                .filter(room -> !room.isDeleted())
                .map(roomMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found or it has been deleted."));
    }

    @Transactional(readOnly = true)
    public List<RoomDto> getRooms() {
        log.debug("Fetching all active rooms");

        return roomRepository.findAllActiveRooms()
                .stream()
                .map(roomMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RoomDto> getAvailableRooms() {
        log.debug("Fetching available rooms");

        return roomRepository.findByAvailableTrueAndIsDeletedFalse()
                .stream()
                .map(roomMapper::toDto)
                .collect(Collectors.toList());
    }

    private void validateRoomData(RoomDto roomDto) {
        if (roomDto.getBeds() < 1) {
            throw new DataIntegrityException("Room must have at least 1 bed");
        }

        if (roomDto.getMaxGuests() < 1) {
            throw new DataIntegrityException("Room must accommodate at least 1 guest");
        }

        if (roomDto.getPrice() == null || roomDto.getPrice().doubleValue() <= 0) {
            throw new DataIntegrityException("Room price must be greater than zero");
        }

        if (roomDto.getBedTypes() == null || roomDto.getBedTypes().isEmpty()) {
            throw new DataIntegrityException("Room must have at least one bed type specified");
        }

        if (roomDto.getFloor() == null) {
            throw new DataIntegrityException("Floor information is required");
        }

        if (roomDto.getAreaSqm() != null && roomDto.getAreaSqm() <= 0) {
            throw new DataIntegrityException("Room area must be greater than zero");
        }
    }

    private String getSafeActor() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Principal::getName)
                .orElse("SYSTEM");
    }
}