package com.project.hrms.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "department")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Department extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "department_id")
    private Long departmentId;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private Employee manager;

    private Boolean isActive = true;
}