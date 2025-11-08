package com.project.hrms.response;

import com.project.hrms.model.Department;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DepartmentResponse {

    private Long departmentId;
    private String code;
    private String name;
    private String description;
    private Long managerId;
    private String managerName;
    private String createdAt;
    private String updatedAt;

    public static DepartmentResponse fromDepartment(Department department) {
        return DepartmentResponse.builder()
                .departmentId(department.getDepartmentId())
                .code(department.getCode())
                .name(department.getName())
                .description(department.getDescription())
                .managerId(department.getManager() != null ? department.getManager().getEmployeeId() : null)
                .managerName(department.getManager() != null ? department.getManager().getFullName() : null)
                .createdAt(department.getCreatedAt() != null ? department.getCreatedAt().toString() : null)
                .updatedAt(department.getUpdatedAt() != null ? department.getUpdatedAt().toString() : null)
                .build();
    }
}
