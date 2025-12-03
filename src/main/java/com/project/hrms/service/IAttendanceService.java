package com.project.hrms.service;

import com.project.hrms.response.AttendanceResponse;

public interface IAttendanceService {
    AttendanceResponse performCheckIn(String username);
    AttendanceResponse performCheckOut(String username);
}