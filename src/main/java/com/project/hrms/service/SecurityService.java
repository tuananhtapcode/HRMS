// src/main/java/com/project/hrms/service/SecurityService.java
package com.project.hrms.service;

import com.project.hrms.model.Account;
import com.project.hrms.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("securityService") // Đặt tên "securityService" để @PreAuthorize có thể tìm thấy
@RequiredArgsConstructor
public class SecurityService {

    private final AccountRepository accountRepository;

    /**
     * Kiểm tra xem người dùng đang đăng nhập có phải là chủ sở hữu
     * của hồ sơ nhân viên (employeeId) này không.
     */
    public boolean isOwner(Authentication authentication, Long employeeId) {
        if (authentication == null || employeeId == null) {
            return false;
        }

        String username = authentication.getName();
        Account account = accountRepository.findByUsername(username).orElse(null);

        if (account == null || account.getEmployee() == null) {
            return false;
        }

        // So sánh employeeId của người đang login
        // với employeeId đang được yêu cầu
        return account.getEmployee().getEmployeeId().equals(employeeId);
    }
}