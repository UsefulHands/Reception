package com.github.UsefulHands.reception.features.room;

import com.github.UsefulHands.reception.features.audit.AuditLogService;
import com.github.UsefulHands.reception.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
        log.info("Creating new room: {}", roomDto.getRoomNumber());

        if(roomRepository.findByRoomNumber(roomDto.getRoomNumber()).isPresent()) {
            throw new RuntimeException("Room number " + roomDto.getRoomNumber() + " already exists!");
        }

        RoomEntity savedRoom = roomRepository.save(roomMapper.toEntity(roomDto));

        // AUDIT LOG - @Async metodunu çağırıyoruz
        auditLogService.log(
                "ROOM_CREATE",
                getCurrentUsername(),
                "Created room number: " + savedRoom.getRoomNumber()
        );

        return roomMapper.toDto(savedRoom);
    }

    public List<RoomDto> getAvailableRooms() {
        log.info("Retrieving available rooms");
        return roomRepository.findByAvailableTrue()
                .stream()
                .map(roomMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public RoomDto editRoom(Long id, RoomDto roomDto) {
        RoomEntity roomEntity = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));

        roomMapper.updateEntityFromDto(roomDto, roomEntity);
        RoomEntity updatedRoom = roomRepository.save(roomEntity);

        // AUDIT LOG
        auditLogService.log(
                "ROOM_UPDATE",
                getCurrentUsername(),
                "Updated room: " + updatedRoom.getRoomNumber() + " details."
        );

        return roomMapper.toDto(updatedRoom);
    }

    @Transactional
    public RoomDto deleteRoom(Long id) {
        RoomEntity room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));

        RoomDto deletedRoomDto = roomMapper.toDto(room);
        String roomNumber = room.getRoomNumber();

        roomRepository.delete(room);

        roomRepository.flush();

        auditLogService.log(
                "ROOM_DELETE",
                getCurrentUsername(),
                "Deleted room number: " + roomNumber
        );

        return deletedRoomDto;
    }

    // Helper method to get current user from Security Context
    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    // GET metodları (Audit logsuz) aynı kalıyor...
    public List<RoomDto> getAllRooms() { return roomRepository.findAll().stream().map(roomMapper::toDto).collect(Collectors.toList()); }
    public RoomDto getRoom(Long id) { return roomRepository.findById(id).map(roomMapper::toDto).orElseThrow(() -> new ResourceNotFoundException("Not found")); }
}