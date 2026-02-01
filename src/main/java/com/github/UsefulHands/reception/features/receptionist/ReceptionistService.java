package com.github.UsefulHands.reception.features.receptionist;

import com.github.UsefulHands.reception.common.audit.AuditLogService;
import com.github.UsefulHands.reception.common.exception.ResourceNotFoundException;
import com.github.UsefulHands.reception.features.admin.AdminDto;
import com.github.UsefulHands.reception.features.user.UserEntity;
import com.github.UsefulHands.reception.features.user.UserService;
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
}