package com.project.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor // <-- Sửa lỗi "Expected no arguments but found 2"
@NoArgsConstructor
public class ReminderDTO {
    private String type;
    private String message;
}