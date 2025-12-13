package com.project.hrms.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "system_setting")
@Data
public class SystemSetting {

    @Id
    @Column(name = "setting_key")
    private String settingKey;

    @Column(name = "setting_value")
    private String value;

    private String description;

    @Column(name = "group_name")
    private String groupName;
}