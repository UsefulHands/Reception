package com.github.UsefulHands.reception.features.room;

import com.github.UsefulHands.reception.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoomDto>> create(@Valid @RequestBody RoomDto roomDto) {
        RoomDto createdRoom = roomService.createRoom(roomDto);
        return ResponseEntity.ok(ApiResponse.success(createdRoom, "Room created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<RoomDto>> editRoom(@PathVariable Long id, @Valid @RequestBody RoomDto roomDto) {
        RoomDto updatedRoom = roomService.editRoom(id, roomDto);
        return ResponseEntity.ok(ApiResponse.success(updatedRoom, "Room updated successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoomDto>>> getRooms(@RequestParam(required = false) Boolean available) {
        List<RoomDto> rooms = (available != null && available)
                ? roomService.getAvailableRooms()
                : roomService.getRooms();
        return ResponseEntity.ok(ApiResponse.success(rooms, "Rooms retrieved"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomDto>> getRoom(@PathVariable Long id) {
        RoomDto roomDto = roomService.getRoom(id);
        return ResponseEntity.ok(ApiResponse.success(roomDto, "Room details retrieved"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Room deleted successfully"));
    }
}