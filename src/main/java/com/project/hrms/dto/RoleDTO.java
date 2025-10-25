package com.project.hrms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleDTO {

    private String name;

    private String description;

    public static final String ADMIN = "ADMIN";
    public static final String USER = "USER";
}
