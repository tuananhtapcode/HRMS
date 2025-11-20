package com.project.hrms.controller;

import com.project.hrms.dto.DepartmentDTO;
import com.project.hrms.model.Department;
import com.project.hrms.response.DepartmentListResponse;
import com.project.hrms.response.DepartmentResponse;
import com.project.hrms.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    //synchronized đảm bảo 1 thread tạo phòng ban tại 1 thời điểm duy nhất, ap dung voi create
    public synchronized  ResponseEntity<?> createDepartment(@Valid @RequestBody DepartmentDTO dto,
                                                               BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors())
            {
                List<String> errorMessages = bindingResult.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(errorMessages);
            }
            Department department = departmentService.create(dto);
            return ResponseEntity.ok(DepartmentResponse.fromDepartment(department));
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartmentResponse> updateDepartment(
            @PathVariable Long id,
            @RequestBody DepartmentDTO dto) {
        Department updated = departmentService.update(id, dto);
        return ResponseEntity.ok(DepartmentResponse.fromDepartment(updated));
    }

    @GetMapping
    public ResponseEntity<?> getAllDepartments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
//        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("departmentId").ascending());
        Page<DepartmentResponse> results = departmentService.getAllPaged(pageRequest);
        return ResponseEntity.ok(DepartmentListResponse.builder()
                .departmentResponseList(results.getContent())
                .totalPages(results.getTotalPages())
                .build());
//        return ResponseEntity.ok(departmentService.getAllPaged(pageRequest).getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponse> getDepartmentById(@PathVariable Long id) {
        Department department = departmentService.getById(id);
        return ResponseEntity.ok(DepartmentResponse.fromDepartment(department));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDepartment(@PathVariable Long id) {
        departmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<DepartmentResponse>> searchDepartments(@RequestParam String name) {
        List<Department> results = departmentService.searchByName(name);
        return ResponseEntity.ok(results.stream()
                .map(DepartmentResponse::fromDepartment)
                .toList());
    }

//    @GetMapping("/manager/{managerId}")
//    public ResponseEntity<DepartmentResponse> getDepartmentsByManager(@PathVariable Long managerId) {
//        Department departments = departmentService.findByManager_EmployeeId(managerId);
//
//        DepartmentResponse response = . (DepartmentResponse::fromDepartment);
//
//        return ResponseEntity.ok(response);
//    }

}
