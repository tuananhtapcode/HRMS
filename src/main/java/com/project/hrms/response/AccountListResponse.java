package com.project.hrms.response;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AccountListResponse {
    private List<AccountResponse> accounts;
    private int totalPages;
}
