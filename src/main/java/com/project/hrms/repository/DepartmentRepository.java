package com.project.hrms.repository;

import com.project.hrms.model.Department;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Department findByManager_EmployeeId(Long managerId);

    Boolean existsByName(String name);

    boolean existsByCode(String code);

    List<Department> findByNameContainingIgnoreCase(String name);

    // --- THÊM HÀM NÀY ---
    // Query này thực hiện:
    // 1. Lấy danh sách phòng ban (d)
    // 2. Join với bảng nhân viên (e)
    // 3. Chỉ đếm nhân viên đang ACTIVE (tuỳ logic, nếu muốn đếm hết thì bỏ đoạn AND e.status...)
    // 4. Group by theo phòng ban để đếm
    @Query("SELECT d.name, COUNT(e) " +
            "FROM Department d " +
            "LEFT JOIN Employee e ON e.department.departmentId = d.departmentId AND e.status = 'ACTIVE' " +
            "GROUP BY d.departmentId, d.name")
    List<Object[]> countEmployeesPerDepartment();
}
