package com.project.hrms.repository;

import com.project.hrms.model.PayrollPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query; // ✅ Thêm Import

import java.time.LocalDate;
import java.util.List;

public interface PayrollPeriodRepository extends JpaRepository<PayrollPeriod, Long>,
        JpaSpecificationExecutor<PayrollPeriod> {

    // Tìm kỳ lương theo trạng thái đóng/mở
    List<PayrollPeriod> findByIsClosed(Boolean isClosed);

    // Tìm kỳ lương có khoảng ngày giao nhau (để tránh tạo kỳ trùng)
    boolean existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate end, LocalDate start);

    // Tìm kỳ lương bao trùm 1 ngày bất kỳ (vd: hôm nay thuộc kỳ nào)
    List<PayrollPeriod> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate date1, LocalDate date2);

    // ---------------------------------------------------------
    // ✅ THÊM PHẦN THIẾU: Tìm kỳ lương hiện tại (dựa vào ngày hôm nay)
    // ---------------------------------------------------------
    @Query("SELECT p FROM PayrollPeriod p WHERE CURRENT_DATE BETWEEN p.startDate AND p.endDate")
    PayrollPeriod findCurrentPeriod();
}
