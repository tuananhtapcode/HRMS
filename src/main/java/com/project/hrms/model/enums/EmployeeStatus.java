package com.project.hrms.model.enums;

public enum EmployeeStatus {
    ACTIVE("Đang làm việc"),
    ON_LEAVE("Tạm nghỉ"),
    RESIGNED("Đã nghỉ việc"),
    TERMINATED("Đã chấm dứt");

    private final String vietnameseName;

    EmployeeStatus(String vietnameseName) {
        this.vietnameseName = vietnameseName;
    }

    public String getVietnameseName() {
        return vietnameseName;
    }
}