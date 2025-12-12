package com.project.hrms.controller;

import com.project.hrms.model.AuditLog;
import com.project.hrms.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    /**
     * 📌 API lấy danh sách audit log (phân trang)
     * - Cho phép client truyền page & size
     * - Trả ra danh sách log gồm:
     *      + action
     *      + entityName
     *      + entityId
     *      + note
     *      + performedAt
     *      + performedBy
     *      + performedByName
     */
    @GetMapping
    public Page<AuditLog> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return auditLogService.getAuditLogs(pageable);
    }
}
