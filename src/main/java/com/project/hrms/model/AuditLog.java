package com.project.hrms.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tên bảng hoặc entity liên quan
    private String entityName;

    // Khóa chính của bản ghi bị tác động
    private Long entityId;

    // Hành động CRUD
    @Enumerated(EnumType.STRING)
    private AuditAction action;

    // Ai thực hiện
    private Long performedBy;

    private String performedByName;

    // Thời điểm thực hiện
    private LocalDateTime performedAt;

    // Ghi chú thêm
    private String note;

    @PrePersist
    public void prePersist() {
        performedAt = LocalDateTime.now();
    }

    public enum AuditAction {
        CREATE, READ, UPDATE, DELETE, APPROVE, REJECT, CANCEL
    }
}