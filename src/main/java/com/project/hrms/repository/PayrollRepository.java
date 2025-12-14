package com.project.hrms.repository;

import com.project.hrms.dto.PayrollListItemDTO;
import com.project.hrms.dto.PayrollPeriodPaymentSummaryDTO;
import com.project.hrms.dto.SalaryTrendDTO;
import com.project.hrms.model.Payroll;
import com.project.hrms.model.enums.PaymentMethod;
import com.project.hrms.model.enums.PayrollStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PayrollRepository extends JpaRepository<Payroll, Long>, JpaSpecificationExecutor<Payroll> {

    Optional<Payroll> findByEmployee_EmployeeIdAndPayrollPeriod_PayrollPeriodId(Long employeeId, Long payrollPeriodId);

    @EntityGraph(attributePaths = {"employee", "payrollPeriod", "payrollItems", "payrollItems.salaryComponent"})
    Optional<Payroll> findWithItemsByPayrollId(Long payrollId);

    @Query("""
        select new com.project.hrms.dto.PayrollListItemDTO(
            p.payrollId, e.employeeId, e.fullName, p.totalSalary, p.taxAmount, p.insuranceAmount
        )
        from Payroll p
        join p.employee e
        where p.payrollPeriod.payrollPeriodId = :periodId
    """)
    Page<PayrollListItemDTO> findPayrollListByPeriod(@Param("periodId") Long periodId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.totalSalary), 0) FROM Payroll p")
    BigDecimal sumTotalSalary();

    @Query("SELECT COALESCE(SUM(p.taxAmount), 0) FROM Payroll p")
    BigDecimal sumPersonalIncomeTax();

    @Query("SELECT COALESCE(SUM(p.insuranceAmount), 0) FROM Payroll p")
    BigDecimal sumInsuranceDeduction();

    @Query("""
        SELECT new com.project.hrms.dto.SalaryTrendDTO(p.payrollPeriod.name, SUM(p.totalSalary))
        FROM Payroll p
        GROUP BY p.payrollPeriod.name, p.payrollPeriod.startDate
        ORDER BY p.payrollPeriod.startDate ASC
    """)
    List<SalaryTrendDTO> getSalaryTrend();

    // =========================
    // BULK APPROVE BY PERIOD
    // =========================
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Payroll p
           set p.status = :toStatus,
               p.approvedAt = :approvedAt,
               p.approvedBy = :approvedBy,
               p.approvedByName = :approvedByName
         where p.payrollPeriod.payrollPeriodId = :periodId
           and p.status in :fromStatuses
    """)
    int bulkApproveByPeriod(
            @Param("periodId") Long periodId,
            @Param("fromStatuses") Collection<PayrollStatus> fromStatuses,
            @Param("toStatus") PayrollStatus toStatus,
            @Param("approvedAt") LocalDateTime approvedAt,
            @Param("approvedBy") Long approvedBy,
            @Param("approvedByName") String approvedByName
    );

    // =========================
    // BULK PAY FULL BY PERIOD (paidAmount = totalSalary)
    // =========================
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Payroll p
           set p.status = :toStatus,
               p.paidAt = :paidAt,
               p.paidBy = :paidBy,
               p.paidByName = :paidByName,
               p.paymentMethod = :method,
               p.transactionRef = :txRef,
               p.paymentNote = :note,
               p.paidAmount = p.totalSalary
         where p.payrollPeriod.payrollPeriodId = :periodId
           and p.status = :fromStatus
    """)
    int bulkPayFullByPeriod(
            @Param("periodId") Long periodId,
            @Param("fromStatus") PayrollStatus fromStatus,
            @Param("toStatus") PayrollStatus toStatus,
            @Param("paidAt") LocalDateTime paidAt,
            @Param("paidBy") Long paidBy,
            @Param("paidByName") String paidByName,
            @Param("method") PaymentMethod method,
            @Param("txRef") String txRef,
            @Param("note") String note
    );

    // =========================
    // SUMMARY BY PERIOD (for manager screen)
    // =========================
    @Query("""
        SELECT new com.project.hrms.dto.PayrollPeriodPaymentSummaryDTO(
            p.payrollPeriod.payrollPeriodId,
            p.payrollPeriod.name,
            COUNT(p.payrollId),
            COALESCE(SUM(p.totalSalary),0),
            COALESCE(SUM(p.paidAmount),0),
            SUM(CASE WHEN p.status = com.project.hrms.model.enums.PayrollStatus.PAID THEN 1 ELSE 0 END),
            SUM(CASE WHEN p.status = com.project.hrms.model.enums.PayrollStatus.APPROVED THEN 1 ELSE 0 END),
            SUM(CASE WHEN p.status = com.project.hrms.model.enums.PayrollStatus.CALCULATED THEN 1 ELSE 0 END)
        )
        FROM Payroll p
        WHERE p.payrollPeriod.payrollPeriodId = :periodId
        GROUP BY p.payrollPeriod.payrollPeriodId, p.payrollPeriod.name
    """)
    PayrollPeriodPaymentSummaryDTO getPaymentSummaryByPeriod(@Param("periodId") Long periodId);

    // =========================
    // PAY SINGLE (optional overridePaidAmount)
    // =========================
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Payroll p
           set p.status = :toStatus,
               p.paidAt = :paidAt,
               p.paidBy = :paidBy,
               p.paidByName = :paidByName,
               p.paymentMethod = :method,
               p.transactionRef = :txRef,
               p.paymentNote = :note,
               p.paidAmount = COALESCE(:overridePaidAmount, p.totalSalary)
         where p.payrollId = :payrollId
           and p.status = :fromStatus
    """)
    int paySingle(
            @Param("payrollId") Long payrollId,
            @Param("fromStatus") PayrollStatus fromStatus,
            @Param("toStatus") PayrollStatus toStatus,
            @Param("paidAt") LocalDateTime paidAt,
            @Param("paidBy") Long paidBy,
            @Param("paidByName") String paidByName,
            @Param("method") PaymentMethod method,
            @Param("txRef") String txRef,
            @Param("note") String note,
            @Param("overridePaidAmount") BigDecimal overridePaidAmount
    );
}
