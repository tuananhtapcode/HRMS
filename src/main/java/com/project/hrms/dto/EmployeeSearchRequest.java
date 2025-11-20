package com.project.hrms.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeSearchRequest {
    private String name;
    private String code;
    private Long departmentId;
    private Long managerId;
    private Long positionId;

    private String gender;      // "MALE", "FEMALE", "OTHER"
    private String status;      // "ACTIVE", "INACTIVE", "ON_LEAVE"

    private LocalDate hireDateFrom;
    private LocalDate hireDateTo;
    private LocalDate birthDateFrom;
    private LocalDate birthDateTo;

//    private String employeeType; // "FULLTIME", "PARTTIME", "INTERN"

    private String phoneNumber;
}
