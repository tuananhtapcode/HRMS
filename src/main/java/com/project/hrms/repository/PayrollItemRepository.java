package com.project.hrms.repository;

import com.project.hrms.model.PayrollItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayrollItemRepository extends JpaRepository<PayrollItem, Long> {

    // Lấy toàn bộ item theo payroll_id (xem payslip)
    List<PayrollItem> findByPayroll_PayrollId(Long payrollId);

    // Xóa item theo payroll (khi tính lại payroll)
    void deleteByPayroll_PayrollId(Long payrollId);
}
