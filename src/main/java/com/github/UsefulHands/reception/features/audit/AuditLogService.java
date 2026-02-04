package com.github.UsefulHands.reception.features.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    @Transactional(readOnly = true)
    public List<AuditLogDto> getAllLogs() {
        log.info("Fetching all audit logs");
        List<AuditLogEntity> logs = auditLogRepository.findAllByOrderByCreatedAtDesc();
        return auditLogMapper.toDtoList(logs);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String username, String details) {
        AuditLogEntity auditLog = AuditLogEntity.builder()
                .action(action)
                .performedBy(username)
                .details(details)
                .createdAt(LocalDateTime.now())
                .build();
        auditLogRepository.save(auditLog);
    }
}
