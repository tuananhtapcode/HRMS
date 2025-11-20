package com.project.hrms.repository;

import com.project.hrms.dto.EmployeeSearchRequest;
import com.project.hrms.model.Department;
import com.project.hrms.model.Employee;
import com.project.hrms.model.enums.EmployeeStatus;
import com.project.hrms.model.enums.Gender;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class EmployeeSpecification {

    public static Specification<Employee> filter(EmployeeSearchRequest req) {
        return (root, query, cb) -> {
            Predicate p = cb.conjunction();

            // --- Full name LIKE ---
            if (req.getName() != null && !req.getName().isBlank()) {
                String name = req.getName().trim().toLowerCase();
                p = cb.and(p, cb.like(cb.lower(root.get("fullName")), "%" + name + "%"));
            }

            // --- Employee code LIKE ---
            if (req.getCode() != null && !req.getCode().isBlank()) {
                String code = req.getCode().trim().toLowerCase();
                p = cb.and(p, cb.like(cb.lower(root.get("employeeCode")), "%" + code + "%"));
            }

            // --- Department filter ---
            if (req.getDepartmentId() != null) {
                p = cb.and(p,
                        cb.equal(root.get("department").get("departmentId"), req.getDepartmentId()));
            }

            // --- Manager filter (Department.manager) ---
            if (req.getManagerId() != null) {
                // Null-safe join
                p = cb.and(p,
                        cb.equal(root.get("department").get("manager").get("employeeId"), req.getManagerId()));
            }

            // --- Job position ---
            if (req.getPositionId() != null) {
                p = cb.and(p,
                        cb.equal(root.get("jobPosition").get("jobPositionId"), req.getPositionId()));
            }

            // --- Gender (enum-safe) ---
            if (req.getGender() != null && !req.getGender().isBlank()) {
                try {
                    Gender genderEnum = Gender.valueOf(req.getGender().toUpperCase());
                    p = cb.and(p, cb.equal(root.get("gender"), genderEnum));
                } catch (IllegalArgumentException ignored) {}
            }

            // --- Status (enum-safe) ---
            if (req.getStatus() != null && !req.getStatus().isBlank()) {
                try {
                    EmployeeStatus statusEnum = EmployeeStatus.valueOf(req.getStatus().toUpperCase());
                    p = cb.and(p, cb.equal(root.get("status"), statusEnum));
                } catch (IllegalArgumentException ignored) {}
            }

            // --- Hire date range ---
            if (req.getHireDateFrom() != null) {
                p = cb.and(p, cb.greaterThanOrEqualTo(root.get("hireDate"), req.getHireDateFrom()));
            }
            if (req.getHireDateTo() != null) {
                p = cb.and(p, cb.lessThanOrEqualTo(root.get("hireDate"), req.getHireDateTo()));
            }

            // --- Birth date range ---
            if (req.getBirthDateFrom() != null) {
                p = cb.and(p, cb.greaterThanOrEqualTo(root.get("dateOfBirth"), req.getBirthDateFrom()));
            }
            if (req.getBirthDateTo() != null) {
                p = cb.and(p, cb.lessThanOrEqualTo(root.get("dateOfBirth"), req.getBirthDateTo()));
            }

            // --- Phone number LIKE ---
            if (req.getPhoneNumber() != null && !req.getPhoneNumber().isBlank()) {
                String phone = req.getPhoneNumber().trim().toLowerCase();
                p = cb.and(p, cb.like(cb.lower(root.get("phoneNumber")), "%" + phone + "%"));
            }

            return p;
        };
    }
}
