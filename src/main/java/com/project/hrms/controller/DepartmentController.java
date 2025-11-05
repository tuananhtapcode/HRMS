package com.project.hrms.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}/departments")
@RequiredArgsConstructor
public class DepartmentController {
    @PostMapping
    public String createDepartment() {
        return "Create Department endpoint";
    }

    @GetMapping
    public String getAllDepartments() {
        return "Get All Departments endpoint";
    }

    @GetMapping("/{id}")
    public String getDepartmentById() {
        return "Get Department by ID endpoint";
    }

}
