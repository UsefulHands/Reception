package com.github.UsefulHands.reception.features.receptionist;

import com.github.UsefulHands.reception.common.exception.ResourceNotFoundException;
import com.github.UsefulHands.reception.features.admin.AdminDto;
import com.github.UsefulHands.reception.features.audit.AuditLogService;
import com.github.UsefulHands.reception.features.guest.GuestDto;
import com.github.UsefulHands.reception.features.guest.GuestEntity;
import com.github.UsefulHands.reception.features.user.UserEntity;
import com.github.UsefulHands.reception.features.user.UserService;
import jakarta.validation.Valid;
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
public class ReceptionistService {

    private final ReceptionistRepository receptionistRepository;
    private final UserService userService;
    private final ReceptionistMapper receptionistMapper;
    private final AuditLogService auditLogService;

    @Transactional
    public ReceptionistDto registerReceptionist(String username, String password, ReceptionistDto receptionistDto) {
        log.info("Registering new Admin: {}", receptionistDto.getFirstName());

        UserEntity userEntity = userService.createAccount(username, password, "ROLE_RECEPTIONIST");
        ReceptionistEntity entity = receptionistMapper.toEntity(receptionistDto);
        entity.setUser(userEntity);
        ReceptionistEntity savedReceptionist = receptionistRepository.save(entity);

        log.info("Receptionist profile saved with ID: {} for User ID: {}", savedReceptionist.getId(), userEntity.getId());
        String actor = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
        auditLogService.log("RECEPTION_REGISTER", actor, "Registered new Receptionist: " + username);

        receptionistDto.setId(savedReceptionist.getId());
        receptionistDto.setUserId(userEntity.getId());
        return receptionistMapper.toDto(savedReceptionist);
    }

    @Transactional
    public ReceptionistDto editReceptionist(Long id, ReceptionistDto receptionistDto) {
        log.info("Editing receptionist");

        ReceptionistEntity receptionistEntity = receptionistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receptionist not found with id: " + id));

        receptionistMapper.updateEntityFromDto(receptionistDto, receptionistEntity);

        ReceptionistEntity updatedReceptionist = receptionistRepository.save(receptionistEntity);
        return receptionistMapper.toDto(updatedReceptionist);
    }

    public List<ReceptionistDto> getAllReceptionists() {
        log.info("Retrieving receptionists");
        return receptionistRepository.findAll()
                .stream()
                .map(receptionistMapper::toDto)
                .collect(Collectors.toList());
    }

    public ReceptionistDto getReceptionist(Long id){
        log.info("Retrieving receptionist");
        return receptionistRepository.findByUserId(id)
                .map(receptionistMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Receptionist not found with id: " + id));
    }

    @Transactional
    public ReceptionistDto deleteReceptionist(Long receptionistId) {
        ReceptionistEntity receptionist = receptionistRepository.findById(receptionistId)
                .orElseThrow(() -> new ResourceNotFoundException("Receptionist not found with id: " + receptionistId));

        if (receptionist.getUser() != null) {
            receptionist.getUser().setDeleted(true);
        }

        receptionistRepository.save(receptionist);

        log.info("Receptionist and associated User marked as deleted for Guest ID: {}", receptionistId);
        return receptionistMapper.toDto(receptionist);
    }
}