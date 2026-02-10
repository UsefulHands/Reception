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
    public List<AuditLogDto> getLogs() {
        log.info("Fetching all audit logs");
        return auditLogMapper.toDtoList(auditLogRepository.findAllByOrderByCreatedAtDesc());
    }

    @Async("auditTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String username, String details) {
        try {
            // 255 karakter sınırına takılmamak için (SQL TEXT değilse)
            String safeDetails = (details != null && details.length() > 255)
                    ? details.substring(0, 252) + "..."
                    : details;

            AuditLogEntity auditLog = AuditLogEntity.builder()
                    .action(action)
                    .performedBy(username != null ? username : "SYSTEM")
                    .details(safeDetails)
                    .createdAt(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("CRITICAL: Failed to write audit log to database!", e);
        }
    }
}