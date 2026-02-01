package com.github.UsefulHands.reception.features.receptionist;

import com.github.UsefulHands.reception.common.response.ApiResponse;
import com.github.UsefulHands.reception.features.admin.AdminDto;
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
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<ReceptionistDto>> create(@Valid @RequestBody ReceptionistRegistrationRequest request) {
        ReceptionistDto result = receptionistService.registerReceptionist(
                request.getUsername(),
                request.getPassword(),
                request.getReceptionistDto()
        );
        return ResponseEntity.ok(ApiResponse.success(result, "Receptionist created"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<ReceptionistDto>>> getReceptionists() {
        List<ReceptionistDto> receptionists = receptionistService.getAllReceptionists();
        return ResponseEntity.ok(ApiResponse.success(receptionists, "Receptionists retrieved"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<ReceptionistDto>> getReceptioist(@PathVariable Long id){
        ReceptionistDto receptionistDto = receptionistService.getReceptionist(id);
        return ResponseEntity.ok(ApiResponse.success(receptionistDto, "Receptionist retrieved"));
    }
}