package com.github.UsefulHands.reception.features.reservation;

import com.github.UsefulHands.reception.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
@Validated
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping("/grid")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<Map<Long, List<ReservationGridDto>>>> getGrid(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        Map<Long, List<ReservationGridDto>> data = reservationService.getGridData(start, end);
        return ResponseEntity.ok(ApiResponse.success(data, "Grid data retrieved successfully"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<ReservationDto>> createReservation(@Valid @RequestBody ReservationCreateRequest request) {
        ReservationDto created = reservationService.createWithNewUser(request);
        return ResponseEntity.ok(ApiResponse.success(created, "Reservation and guest account created successfully"));
    }

    @PostMapping("/my-booking")
    @PreAuthorize("hasRole('GUEST')")
    public ResponseEntity<ApiResponse<ReservationDto>> createReservationForLoggedGuest(@Valid @RequestBody ReservationCreateRequest request) {
        ReservationDto created = reservationService.createForExistingGuest(request);
        return ResponseEntity.ok(ApiResponse.success(created, "Your reservation has been successfully created!"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<ReservationDto>> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody ReservationUpdateRequest request) {

        ReservationDto updated = reservationService.updateReservation(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Reservation updated successfully"));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'GUEST')")
    public ResponseEntity<ApiResponse<Void>> cancelReservation(@PathVariable Long id) {
        reservationService.cancelReservation(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Reservation has been cancelled"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Reservation has been permanently deleted (soft-delete)"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<ReservationDto>> getReservation(@PathVariable Long id) {
        ReservationDto res = reservationService.getReservation(id);
        return ResponseEntity.ok(ApiResponse.success(res, "Reservation details retrieved successfully"));
    }
}