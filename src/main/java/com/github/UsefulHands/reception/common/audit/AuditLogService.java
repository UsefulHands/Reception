package com.github.UsefulHands.reception.common.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String username, String details) {
        try {
            String sql = "INSERT INTO audit_logs (action, username, details, created_at) VALUES (?, ?, ?, NOW())";
            jdbcTemplate.update(sql, action, username, details);
            log.info("Audit Log saved: {} for user: {}", action, username);
        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage());
        }
    }
}