package com.project.hrms.repository;

import com.project.hrms.model.MonthlyTimesheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonthlyTimesheetRepository extends JpaRepository<MonthlyTimesheet, Long> {

    // 1. Lấy tất cả (Logic cũ - Dùng cho HR xem toàn bộ)
    List<MonthlyTimesheet> findByMonthAndYear(int month, int year);

    // 2. Lấy theo PHÒNG BAN (Logic mới - Dùng để Filter)
    List<MonthlyTimesheet> findByMonthAndYearAndEmployee_Department_DepartmentId(int month, int year, Long departmentId);

    // 3. Tìm theo tên nhân viên (Search)
    List<MonthlyTimesheet> findByMonthAndYearAndEmployee_FullNameContainingIgnoreCase(int month, int year, String name);

    // [BẮT BUỘC PHẢI THÊM CÁI NÀY]
    // Để Service check xem nhân viên A tháng này đã có dòng nào chưa để Update
    List<MonthlyTimesheet> findByEmployee_EmployeeIdAndMonthAndYear(Long employeeId, int month, int year);
    MonthlyTimesheet findTopByEmployee_EmployeeIdAndMonthAndYearOrderByIdDesc(Long employeeId, int month, int year);

    @Query("""
   select distinct mt.employee.employeeId
   from MonthlyTimesheet mt
   where mt.month = :month and mt.year = :year
""")
    List<Long> findDistinctEmployeeIdsByMonthYear(@Param("month") int month, @Param("year") int year);

}