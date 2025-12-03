package com.project.hrms.service;

import com.project.hrms.dto.TimesheetSummaryDTO;
import java.util.List;

public interface ITimesheetService {
    List<TimesheetSummaryDTO> getMonthlyTimesheetSummary(int month, int year, Long departmentId);
    void runDailyProcessManually(String dateStr); // API phụ để test
}