package com.project.hrms.response;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class DepartmentListResponse {
    private List<DepartmentResponse> departmentResponseList;
    private int totalPages;
}
