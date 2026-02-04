package com.github.UsefulHands.reception.features.receptionist;

import com.github.UsefulHands.reception.common.response.ApiResponse;
import com.github.UsefulHands.reception.features.admin.AdminDto;
import com.github.UsefulHands.reception.features.guest.GuestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/receptionists")
@RequiredArgsConstructor
public class ReceptionistController {

    private final ReceptionistService receptionistService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReceptionistDto>> create(@Valid @RequestBody ReceptionistRegistrationRequest request) {
        ReceptionistDto result = receptionistService.registerReceptionist(
                request.getUsername(),
                request.getPassword(),
                request.getReceptionistDetails()
        );
        return ResponseEntity.ok(ApiResponse.success(result, "Receptionist created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReceptionistDto>> editReceptionist(@PathVariable Long id, @Valid @RequestBody ReceptionistDto receptionistDto){
        ReceptionistDto updatedReceptionist = receptionistService.editReceptionist(id, receptionistDto);
        return ResponseEntity.ok(ApiResponse.success(updatedReceptionist, "Receptionist edited"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ReceptionistDto>>> getReceptionists() {
        List<ReceptionistDto> receptionists = receptionistService.getAllReceptionists();
        return ResponseEntity.ok(ApiResponse.success(receptionists, "Receptionists retrieved"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReceptionistDto>> getReceptioist(@PathVariable Long id){
        ReceptionistDto receptionistDto = receptionistService.getReceptionist(id);
        return ResponseEntity.ok(ApiResponse.success(receptionistDto, "Receptionist retrieved"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReceptionistDto>> deleteReceptionist(@PathVariable Long id){
        ReceptionistDto receptionistDto = receptionistService.deleteReceptionist(id);
        return ResponseEntity.ok(ApiResponse.success(receptionistDto, "Receptionist deleted"));
    }
}