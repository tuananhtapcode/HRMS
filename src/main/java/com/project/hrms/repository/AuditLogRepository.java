package com.project.hrms.repository;

import com.project.hrms.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository  extends JpaRepository<AuditLog, Long> {
}
