package com.github.UsefulHands.reception.features.admin;

import com.github.UsefulHands.reception.common.exception.ResourceNotFoundException;
import com.github.UsefulHands.reception.features.audit.AuditLogService;
import com.github.UsefulHands.reception.features.guest.GuestDto;
import com.github.UsefulHands.reception.features.guest.GuestEntity;
import com.github.UsefulHands.reception.features.user.UserEntity;
import com.github.UsefulHands.reception.features.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
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
        log.info("Registering new Admin: {}", adminDto.getFirstName());

        UserEntity userEntity = userService.createAccount(username, password, "ROLE_ADMIN");
        AdminEntity adminEntity = adminMapper.toEntity(adminDto);
        adminEntity.setUser(userEntity);
        AdminEntity savedAdmin = adminRepository.save(adminEntity);

        log.info("Admin profile saved with ID: {} for User ID: {}", savedAdmin.getId(), userEntity.getId());
        String actor = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
        auditLogService.log("ADMIN_REGISTER", actor, "Registered new Admin: " + username);

        adminDto.setId(savedAdmin.getId());
        adminDto.setUserId(userEntity.getId());
        return adminDto;
    }

    @Transactional
    public AdminDto editAdmin(Long id, AdminDto adminDto) {
        log.info("Editing admin");

        AdminEntity adminEntity = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + id));

        adminMapper.updateEntityFromDto(adminDto, adminEntity);

        AdminEntity updatedAdmin = adminRepository.save(adminEntity);
        return adminMapper.toDto(updatedAdmin);
    }

    public List<AdminDto> getAllAdmins() {
        log.info("Retrieving admins");
        return adminRepository.findAllActiveAdmins()
                .stream()
                .map(adminMapper::toDto)
                .collect(Collectors.toList());
    }

    public AdminDto getAdmin(Long id){
        log.info("Retrieving admin");
        return adminRepository.findByUserId(id)
                .map(adminMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + id));
    }

    @Transactional
    public AdminDto deleteAdmin(Long adminId) {
        AdminEntity admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + adminId));

        if (admin.getUser() != null) {
            admin.getUser().setDeleted(true);
        }

        adminRepository.save(admin);

        log.info("Admin and associated User marked as deleted for Admin ID: {}", adminId);
        return adminMapper.toDto(admin);
    }
}