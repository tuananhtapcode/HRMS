package com.project.hrms.service;

import com.project.hrms.dto.DepartmentDTO;
import com.project.hrms.model.Department;
import com.project.hrms.response.DepartmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface IDepartmentService {
    Department create(DepartmentDTO newDepartmentDTO);

    Department update(Long id, DepartmentDTO departmentDTO);

    void delete(Long departmentId);

    Page<DepartmentResponse> getAllPaged(PageRequest pageRequest);

    Department getById(Long departmentId);

    List<Department> searchByName(String name);

    List<Department> findByManager_EmployeeId(Long managerId);

}
