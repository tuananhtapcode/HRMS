package com.project.hrms.repository;

import com.project.hrms.dto.PayrollListItemDTO;
import com.project.hrms.model.Payroll;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.project.hrms.dto.SalaryTrendDTO;

import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.math.BigDecimal; // ✅ Thêm import này
import java.util.List;       // ✅ Thêm import này

public interface PayrollRepository extends JpaRepository<Payroll, Long>,
        JpaSpecificationExecutor<Payroll> {

    Optional<Payroll> findByEmployee_EmployeeIdAndPayrollPeriod_PayrollPeriodId(Long employeeId, Long payrollPeriodId);

    // ✅ Tối ưu: fetch payrollItems + salaryComponent 1 phát
    @EntityGraph(attributePaths = {
            "employee",
            "payrollPeriod",
            "payrollItems",
            "payrollItems.salaryComponent"
    })
    Optional<Payroll> findWithItemsByPayrollId(Long payrollId);

    @Query("""
   select new com.project.hrms.dto.PayrollListItemDTO(p.payrollId, e.employeeId, e.fullName, p.totalSalary, p.taxAmount, p.insuranceAmount)
   from Payroll p
   join p.employee e
   where p.payrollPeriod.payrollPeriodId = :periodId
   """)
    Page<PayrollListItemDTO> findPayrollListByPeriod(@Param("periodId") Long periodId, Pageable pageable);

    // -------------------------------------------------------------------------
    // ✅ PHẦN CẦN SỬA LẠI TÊN TRƯỜNG CHO KHỚP VỚI ENTITY PAYROLL
    // -------------------------------------------------------------------------

    @Query("SELECT COALESCE(SUM(p.totalSalary), 0) FROM Payroll p")
    BigDecimal sumTotalSalary();

    // 🔴 SỬA TỪ 'p.tax' THÀNH TÊN TRƯỜNG THỰC TẾ (Ví dụ: p.taxAmount hoặc p.personalIncomeTax)
    @Query("SELECT COALESCE(SUM(p.taxAmount), 0) FROM Payroll p")
    BigDecimal sumPersonalIncomeTax();

    // 🔴 SỬA TỪ 'p.insurance' THÀNH TÊN TRƯỜNG THỰC TẾ (Ví dụ: p.insuranceAmount)
    @Query("SELECT COALESCE(SUM(p.insuranceAmount), 0) FROM Payroll p")
    BigDecimal sumInsuranceDeduction();

    // 4. Lấy xu hướng lương theo kỳ
    @Query("""
        SELECT new com.project.hrms.dto.SalaryTrendDTO(p.payrollPeriod.name, SUM(p.totalSalary))
        FROM Payroll p
        GROUP BY p.payrollPeriod.name, p.payrollPeriod.startDate
        ORDER BY p.payrollPeriod.startDate ASC
    """)
    List<SalaryTrendDTO> getSalaryTrend();
}

