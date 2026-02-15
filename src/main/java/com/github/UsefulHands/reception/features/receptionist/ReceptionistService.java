package com.github.UsefulHands.reception.features.receptionist;

import com.github.UsefulHands.reception.common.exception.ResourceNotFoundException;
import com.github.UsefulHands.reception.features.audit.AuditLogService;
import com.github.UsefulHands.reception.features.receptionist.dtos.ReceptionistDto;
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
public class ReceptionistService {

    private final ReceptionistRepository receptionistRepository;
    private final UserService userService;
    private final ReceptionistMapper receptionistMapper;
    private final AuditLogService auditLogService;

    @Transactional
    public ReceptionistDto registerReceptionist(String username, String password, ReceptionistDto receptionistDto) {
        log.info("Registering/Updating Receptionist: {}", receptionistDto.getFirstName());

        UserEntity userEntity = userService.createAccount(username, password, "ROLE_RECEPTIONIST");

        ReceptionistEntity receptionistEntity = receptionistRepository.findByUserId(userEntity.getId())
                .map(existing -> {
                    receptionistMapper.updateEntityFromDto(receptionistDto, existing);
                    return existing;
                })
                .orElseGet(() -> {
                    ReceptionistEntity newReceptionist = receptionistMapper.toEntity(receptionistDto);
                    newReceptionist.setUser(userEntity);
                    return newReceptionist;
                });

        ReceptionistEntity savedReceptionist = receptionistRepository.saveAndFlush(receptionistEntity);

        auditLogService.log("RECEPTION_REGISTER", getSafeActor(), "Registered/Updated Receptionist: " + username);

        ReceptionistDto responseDto = receptionistMapper.toDto(savedReceptionist);
        responseDto.setUserId(userEntity.getId());
        return responseDto;
    }

    @Transactional
    public ReceptionistDto editReceptionist(Long id, ReceptionistDto receptionistDto) {
        ReceptionistEntity entity = receptionistRepository.findById(id)
                .filter(r -> r.getUser() != null && !r.getUser().isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Active Receptionist not found with id: " + id));

        try {
            receptionistMapper.updateEntityFromDto(receptionistDto, entity);
            ReceptionistEntity updated = receptionistRepository.saveAndFlush(entity);

            auditLogService.log("RECEPTION_UPDATE", getSafeActor(), "Updated Receptionist: " + updated.getId());

            return receptionistMapper.toDto(updated);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new com.github.UsefulHands.reception.common.exception.DataIntegrityException(
                    "Employee ID already in use!");
        }
    }

    public List<ReceptionistDto> getReceptionists() {
        log.info("Retrieving all active receptionists");
        return receptionistRepository.findAllActiveReceptionists()
                .stream()
                .map(receptionistMapper::toDto)
                .collect(Collectors.toList());
    }

    public ReceptionistDto getReceptionist(Long id) {
        // Genelde profil sorguları Admin ID (PK) üzerinden yapılır
        return receptionistRepository.findById(id)
                .filter(r -> r.getUser() != null && !r.getUser().isDeleted())
                .map(receptionistMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Active Receptionist not found with id: " + id));
    }

    @Transactional
    public ReceptionistDto deleteReceptionist(Long receptionistId) {
        ReceptionistEntity receptionist = receptionistRepository.findById(receptionistId)
                .orElseThrow(() -> new ResourceNotFoundException("Receptionist not found with id: " + receptionistId));

        if (receptionist.getUser() != null) {
            receptionist.getUser().setDeleted(true);
        }

        receptionistRepository.saveAndFlush(receptionist);

        auditLogService.log("RECEPTION_DELETE", getSafeActor(), "Deleted Receptionist ID: " + receptionistId);

        log.info("Receptionist and associated User marked as deleted for ID: {}", receptionistId);
        return receptionistMapper.toDto(receptionist);
    }

    private String getSafeActor() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(java.security.Principal::getName)
                .orElse("SYSTEM");
    }
}