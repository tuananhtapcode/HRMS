package com.project.hrms.dto;

import com.project.hrms.dto.FundStructureDTO;
import com.project.hrms.dto.ReminderDTO;
import com.project.hrms.dto.SalaryTrendDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayrollOverviewDTO {
    private BigDecimal totalSalary;
    private BigDecimal personalIncomeTax;
    private BigDecimal insuranceDeduction;
    private Long headcount;
    private BigDecimal averageSalary;
    private Object currentPeriod; // Nên để là PayrollPeriod hoặc PayrollPeriodDTO
    private List<SalaryTrendDTO> salaryTrend;
    private List<FundStructureDTO> fundStructure;
    private BigDecimal plannedBudget;
    private BigDecimal actualBudget;
    private BigDecimal budgetUsagePercent;
    private List<ReminderDTO> reminders;

    // Chỉnh sửa lại method để trả về dữ liệu đúng với lương và thuế đã tính
//    public PayrollOverviewDTO getPayrollOverview() {
//        BigDecimal totalSalary = payrollRepository.sumTotalSalary();
//        BigDecimal personalIncomeTax = payrollRepository.sumPersonalIncomeTax(); // Cập nhật query đúng
//
//        // Trả về thông tin tổng quan
//        return new PayrollOverviewDTO(
//                totalSalary,
//                personalIncomeTax,
//                BigDecimal.ZERO,  // insuranceDeduction là trường tùy chọn, có thể là 0 nếu không có
//                25L,  // headcount
//                totalSalary.divide(new BigDecimal("25"), RoundingMode.HALF_UP), // Tính lương trung bình
//                null, // currentPeriod sẽ được lấy từ PayrollPeriod
//                null, // salaryTrend có thể tính từ lịch sử lương
//                null, // fundStructure có thể lấy từ các salary_component
//                new BigDecimal("2000000"),  // plannedBudget (giả sử từ settings)
//                totalSalary, // actualBudget
//                new BigDecimal("7.5"), // budgetUsagePercent (giả sử tính từ ngân sách)
//                List.of(new ReminderDTO("warning", "Có 2 nhân viên chưa có thông tin ngân hàng"))
//        );
//    }
}
