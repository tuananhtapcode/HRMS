package com.project.hrms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentDTO{

    @NotBlank(message = "Tên phòng ban không được để trống")
    @Size(max = 100, message = "Tên phòng ban không được vượt quá 100 ký tự")
    private String name;

    @Size(max = 255, message = "Mô tả không được vượt quá 255 ký tự")
    private String description;

//    @JsonProperty("manager_id")
    private Long managerId;
}