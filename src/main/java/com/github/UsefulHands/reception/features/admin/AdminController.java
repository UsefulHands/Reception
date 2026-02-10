package com.github.UsefulHands.reception.features.admin;

import com.github.UsefulHands.reception.common.response.ApiResponse;
import com.github.UsefulHands.reception.features.guest.GuestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admins")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminDto>> create(@Valid @RequestBody AdminRegistrationRequest request) {
        AdminDto adminDto = adminService.registerAdmin(
                request.getUsername(),
                request.getPassword(),
                request.getAdminDto()
        );
        return ResponseEntity.ok(ApiResponse.success(adminDto, "Admin created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminDto>> editAdmin(@PathVariable Long id, @Valid @RequestBody AdminDto adminDto){
        AdminDto updatedAdmin = adminService.editAdmin(id, adminDto);
        return ResponseEntity.ok(ApiResponse.success(updatedAdmin, "Admin edited"));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AdminDto>>> getAdmins() {
        List<AdminDto> admins = adminService.getAdmins();
        return ResponseEntity.ok(ApiResponse.success(admins, "Admins retrieved"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminDto>> getAdmin(@PathVariable Long id){
        AdminDto adminDto = adminService.getAdmin(id);
        return ResponseEntity.ok(ApiResponse.success(adminDto, "Admin retrieved"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminDto>> deleteAdmin(@PathVariable Long id){
        AdminDto adminDto = adminService.deleteAdmin(id);
        return ResponseEntity.ok(ApiResponse.success(adminDto, "Admin deleted"));
    }
}