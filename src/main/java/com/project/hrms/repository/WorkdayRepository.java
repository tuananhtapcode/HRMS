package com.project.hrms.repository;

import com.project.hrms.model.Workday;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface WorkdayRepository extends JpaRepository<Workday,Long> {
    List<Workday> findByDate(LocalDate date);

    // tìm theo nhân viên và ngày cụ thể.
    List<Workday> findByEmployeeIdAndDate(Long employeeId, LocalDate date);

    // kiểm tra có tồn tại ngày làm việc trong khoảng
    boolean existsByEmployeeIdAndDateBetween(Long employeeId, LocalDate start, LocalDate end);

    //lấy danh sách ngày làm việc trong khoảng (for cancel cleanup)
    List<Workday> findByEmployeeIdAndDateBetween(Long employeeId, LocalDate start, LocalDate end);
}
