package com.project.hrms.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.hrms.model.Account;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AccountResponse {

    @JsonProperty("account_id")
    private Long accountId;

    private String username;

    private String email;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("role_name")
    private String roleName;

    @JsonProperty("employee_name")
    private String employeeName;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    /**
     * ✅ Static factory method: chuyển từ Entity → DTO Response
     */
    public static AccountResponse fromAccount(Account account) {
        if (account == null) return null;

        return AccountResponse.builder()
                .accountId(account.getAccountId())
                .username(account.getUsername())
                .email(account.getEmail())
                .phoneNumber(account.getPhoneNumber())
                .isActive(account.getIsActive())
                .roleName(account.getRole() != null ? account.getRole().getName() : null)
                .employeeName(account.getEmployee() != null ? account.getEmployee().getFullName() : null)
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
