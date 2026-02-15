package com.github.UsefulHands.reception.features.admin;

import com.github.UsefulHands.reception.common.exception.ResourceNotFoundException;
import com.github.UsefulHands.reception.features.admin.dtos.AdminDto;
import com.github.UsefulHands.reception.features.audit.AuditLogService;
import com.github.UsefulHands.reception.features.user.UserEntity;
import com.github.UsefulHands.reception.features.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final AdminRepository adminRepository;
    private final UserService userService;
    private final AdminMapper adminMapper;
    private final AuditLogService auditLogService;

    @Transactional
    public AdminDto registerAdmin(String username, String password, AdminDto adminDto) {
        log.info("Registering/Updating Admin: {}", adminDto.getFirstName());

        UserEntity userEntity = userService.createAccount(username, password, "ROLE_ADMIN");

        AdminEntity adminEntity = adminRepository.findByUserId(userEntity.getId())
                .map(existingAdmin -> {
                    adminMapper.updateEntityFromDto(adminDto, existingAdmin);
                    return existingAdmin;
                })
                .orElseGet(() -> {
                    AdminEntity newAdmin = adminMapper.toEntity(adminDto);
                    newAdmin.setUser(userEntity);
                    return newAdmin;
                });

        AdminEntity savedAdmin = adminRepository.save(adminEntity);

        auditLogService.log("ADMIN_REGISTER", getSafeActor(), "Registered/Updated Admin: " + username);

        AdminDto responseDto = adminMapper.toDto(savedAdmin);
        responseDto.setUserId(userEntity.getId());
        return responseDto;
    }

    @Transactional
    public AdminDto editAdmin(Long id, AdminDto adminDto) {
        AdminEntity adminEntity = adminRepository.findById(id)
                .filter(a -> a.getUser() != null && !a.getUser().isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Active Admin not found with id: " + id));

        adminMapper.updateEntityFromDto(adminDto, adminEntity);
        AdminEntity updatedAdmin = adminRepository.save(adminEntity);

        auditLogService.log("ADMIN_UPDATE", getSafeActor(), "Updated Admin profile: " + updatedAdmin.getFirstName());

        return adminMapper.toDto(updatedAdmin);
    }

    public List<AdminDto> getAdmins() {
        log.info("Retrieving all active admins");
        return adminRepository.findAllActiveAdmins()
                .stream()
                .map(adminMapper::toDto)
                .collect(Collectors.toList());
    }

    public AdminDto getAdmin(Long id){
        log.info("Retrieving admin by Admin ID: {}", id);
        return adminRepository.findById(id)
                .filter(a -> a.getUser() != null && !a.getUser().isDeleted())
                .map(adminMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Active Admin not found with id: " + id));
    }

    @Transactional
    public AdminDto deleteAdmin(Long adminId) {
        AdminEntity admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + adminId));

        if (admin.getUser() != null) {
            admin.getUser().setDeleted(true);
            log.info("Associated User marked as deleted for Admin ID: {}", adminId);
        }

        auditLogService.log("ADMIN_DELETE", getSafeActor(), "Deleted Admin and User: " + admin.getFirstName());

        return adminMapper.toDto(admin);
    }

    private String getSafeActor() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(java.security.Principal::getName)
                .orElse("SYSTEM");
    }
}