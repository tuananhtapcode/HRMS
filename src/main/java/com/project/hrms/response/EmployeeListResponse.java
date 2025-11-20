package com.project.hrms.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeListResponse {
    private List<EmployeeResponse> employees;
    private int totalPages;
    private long totalElements; // tổng số bản ghi (nếu cần)
}
