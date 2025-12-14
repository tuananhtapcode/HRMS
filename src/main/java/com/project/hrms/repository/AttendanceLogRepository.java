package com.project.hrms.repository;

import com.project.hrms.model.AttendanceLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AttendanceLogRepository extends JpaRepository<AttendanceLog, Long> {

    // Lấy tất cả log của nhân viên trong 1 khoảng thời gian, sắp xếp tăng dần
    List<AttendanceLog> findByEmployee_EmployeeIdAndTimeBetweenOrderByTimeAsc(
            Long employeeId, LocalDateTime startTime, LocalDateTime endTime);

    // --- CÁC HÀM MỚI (Của bạn - Dùng cho API hiển thị danh sách/phân trang) ---

    // 1. Tìm kiếm có phân trang theo nhân viên và ngày
    Page<AttendanceLog> findByEmployee_EmployeeIdAndTimeBetween(Long employeeId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    // 2. Tìm kiếm có phân trang chỉ theo nhân viên
    Page<AttendanceLog> findByEmployee_EmployeeId(Long employeeId, Pageable pageable);

    // 3. (Optional) Nếu muốn lấy tất cả log của hệ thống thì dùng hàm có sẵn findAll(Pageable) của JpaRepository
}