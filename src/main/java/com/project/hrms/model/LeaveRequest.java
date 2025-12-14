package com.project.hrms.model;

import com.project.hrms.model.enums.LeaveType;
import com.project.hrms.model.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "leave_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long leaveRequestId;

    //ID nhân viên, FK tới bảng employee
    @Column(nullable = false)
    private Long employeeId;

    // ID tài khoản duyệt đơn, FK tới bảng account
    private Long accountApproverId;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LeaveType leaveType;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.PENDING;

    private LocalDateTime approvedAt;

    private String approvedNote;

    // số ngày nghỉ được tính tự động
    @Column(nullable = false)
    private Integer totalDays = 0;
}
