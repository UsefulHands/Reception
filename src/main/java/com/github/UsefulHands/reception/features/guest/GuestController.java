package com.github.UsefulHands.reception.features.guest;

import com.github.UsefulHands.reception.common.response.ApiResponse;
import com.github.UsefulHands.reception.features.receptionist.ReceptionistDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/guests")
@RequiredArgsConstructor
public class GuestController {

    private final GuestService guestService;

    @PostMapping
    public ResponseEntity<ApiResponse<GuestDto>> create(@Valid @RequestBody GuestRegistrationRequest request) {
        GuestDto guestDto = guestService.registerGuest(
                request.getGuestDetails(),
                request.getUsername(),
                request.getPassword()
        );
        return ResponseEntity.ok(ApiResponse.success(guestDto, "Guest created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<GuestDto>> editGuest(@PathVariable Long id, @Valid @RequestBody GuestDto guestDto){
        GuestDto updatedGuest = guestService.editGuest(id, guestDto);
        return ResponseEntity.ok(ApiResponse.success(updatedGuest, "Guest edited"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<GuestDto>>> getGuests() {
        List<GuestDto> guests = guestService.getAllGuests();
        return ResponseEntity.ok(ApiResponse.success(guests, "Guests retrieved"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<GuestDto>> getGuest(@PathVariable Long id){
        GuestDto guestDto = guestService.getGuest(id);
        return ResponseEntity.ok(ApiResponse.success(guestDto, "Guest retrieved"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<GuestDto>> deleteGuest(@PathVariable Long id){
        GuestDto guestDto = guestService.deleteGuest(id);
        return ResponseEntity.ok(ApiResponse.success(guestDto, "Guest deleted"));
    }
}