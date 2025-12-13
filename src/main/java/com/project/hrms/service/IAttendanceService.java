package com.project.hrms.service;

import com.project.hrms.dto.AttendanceTapDTO;
import com.project.hrms.response.AttendanceResponse;

public interface IAttendanceService {
    AttendanceResponse performCheckIn(String username);
    AttendanceResponse performCheckOut(String username);
    AttendanceResponse tapAttendance(String username, AttendanceTapDTO dto);
}