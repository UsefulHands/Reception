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
        try {
            return roomRepository.findByRoomNumber(roomDto.getRoomNumber())
                    .map(existingRoom -> {
                        if (!existingRoom.isDeleted()) {
                            throw new DataIntegrityException("Room number " + roomDto.getRoomNumber() + " already exists and is active!");
                        }
                        log.info("Re-activating deleted room: {}", roomDto.getRoomNumber());
                        roomMapper.updateEntityFromDto(roomDto, existingRoom);
                        existingRoom.setDeleted(false);
                        return roomMapper.toDto(roomRepository.saveAndFlush(existingRoom));
                    })
                    .orElseGet(() -> {
                        log.info("Creating brand new room: {}", roomDto.getRoomNumber());
                        RoomEntity newRoom = roomMapper.toEntity(roomDto);
                        newRoom.setDeleted(false);
                        return roomMapper.toDto(roomRepository.saveAndFlush(newRoom));
                    });
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityException("A room with this number was just created by someone else!");
        }
    }

    @Transactional
    public RoomDto editRoom(Long id, RoomDto roomDto) {
        RoomEntity roomEntity = roomRepository.findById(id)
                .filter(room -> !room.isDeleted()) // Silinmiş oda güncellenemez
                .orElseThrow(() -> new ResourceNotFoundException("Active room not found with id: " + id));

        try {
            roomMapper.updateEntityFromDto(roomDto, roomEntity);
            RoomEntity updatedRoom = roomRepository.saveAndFlush(roomEntity); // Hata fırlatması için flush

            String actor = getSafeActor();
            auditLogService.log("ROOM_UPDATE", actor, "Updated room: " + updatedRoom.getRoomNumber());

            return roomMapper.toDto(updatedRoom);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityException("Room number " + roomDto.getRoomNumber() + " is already taken by another room!");
        }
    }

    @Transactional
    public RoomDto deleteRoom(Long id) {
        RoomEntity room = roomRepository.findById(id)
                .filter(r -> !r.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Active room not found with id: " + id));

        if (room.getStatus() == RoomStatus.OCCUPIED) {
            throw new DataIntegrityException("Cannot delete a room that is currently occupied!");
        }

        room.setDeleted(true);
        roomRepository.saveAndFlush(room);

        auditLogService.log("ROOM_DELETE", getSafeActor(), "Deleted room number: " + room.getRoomNumber());
        return roomMapper.toDto(room);
    }

    public RoomDto getRoom(Long id) {
        return roomRepository.findById(id)
                .filter(room -> !room.isDeleted())
                .map(roomMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found or it has been deleted."));
    }

    public List<RoomDto> getRooms() {
        return roomRepository.findAllActiveRooms()
                .stream()
                .map(roomMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<RoomDto> getAvailableRooms() {
        return roomRepository.findByAvailableTrueAndIsDeletedFalse()
                .stream()
                .map(roomMapper::toDto)
                .collect(Collectors.toList());
    }

    private String getSafeActor() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Principal::getName)
                .orElse("SYSTEM");
    }
}