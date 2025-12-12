package com.project.hrms.controller;

import com.project.hrms.dto.RequestApproveDTO;
import com.project.hrms.dto.LeaveRequestDTO;
import com.project.hrms.model.enums.RequestStatus;
import com.project.hrms.response.ApiResponse;
import com.project.hrms.response.LeaveRequestResponse;
import com.project.hrms.service.ILeaveRequestService;
import com.project.hrms.service.LeaveRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/leave-requests")
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    /**
     * ✅ Tạo đơn nghỉ phép
     * - Employee: tự tạo cho chính mình
     * - Admin/Manager: tạo hộ nhân viên khác
     */
    @PostMapping
    public LeaveRequestResponse create(@RequestBody LeaveRequestDTO dto) {
        return leaveRequestService.createRequest(dto);
    }

    /**
     * ✅ Cập nhật đơn nghỉ phép
     * - Chỉ sửa khi trạng thái PENDING
     * - Employee chỉ sửa đơn của chính mình
     * - Admin/Manager sửa được tất cả đơn PENDING
     */
    @PutMapping("/{id}")
    public LeaveRequestResponse update(
            @PathVariable Long id,
            @RequestBody LeaveRequestDTO dto
    ) {
        return leaveRequestService.updateRequest(id, dto);
    }

    /**
     * ✅ Duyệt đơn nghỉ phép
     * - Chỉ Admin/Manager được duyệt
     * - Chỉ đơn PENDING mới được duyệt
     */
    @PostMapping("/{id}/approve")
    public LeaveRequestResponse approve(
            @PathVariable Long id,
            @RequestBody RequestApproveDTO dto
    ) {
        return leaveRequestService.approveRequest(id, dto);
    }

    /**
     * ✅ Từ chối đơn nghỉ phép
     * - Chỉ Admin/Manager được từ chối
     * - Chỉ đơn PENDING mới được reject
     */
    @PostMapping("/{id}/reject")
    public LeaveRequestResponse reject(
            @PathVariable Long id,
            @RequestBody RequestApproveDTO dto
    ) {
        return leaveRequestService.rejectRequest(id, dto);
    }

    /**
     * ✅ Hủy đơn nghỉ phép
     * - Employee: chỉ hủy đơn PENDING của chính mình
     * - Admin/Manager: hủy được mọi đơn
     * - Nếu đơn đã APPROVED → xóa Workday liên quan
     */
    @PostMapping("/{id}/cancel")
    public LeaveRequestResponse cancel(@PathVariable Long id) {
        return leaveRequestService.cancelRequest(id);
    }

    /**
     * ✅ Lấy danh sách tất cả đơn nghỉ (có filter theo status)
     * - Admin/Manager dùng để xem toàn bộ hệ thống
     */
    @GetMapping
    public Page<LeaveRequestResponse> listAll(
            @RequestParam(required = false) RequestStatus status,
            Pageable pageable
    ) {
        return leaveRequestService.listAll(status, pageable);
    }

    /**
     * ✅ Lấy danh sách đơn nghỉ của chính người dùng
     * - Employee xem lịch sử đơn nghỉ của mình
     */
    @GetMapping("/me")
    public Page<LeaveRequestResponse> myRequests(Pageable pageable) {
        return leaveRequestService.getMyRequests(pageable);
    }

    /**
     * ✅ Lấy danh sách đơn nghỉ của 1 nhân viên cụ thể
     * - Admin/Manager dùng để xem đơn của nhân viên
     */
    @GetMapping("/employee/{employeeId}")
    public Page<LeaveRequestResponse> employeeRequests(
            @PathVariable Long employeeId,
            @RequestParam(required = false) RequestStatus status,
            Pageable pageable
    ) {
        return leaveRequestService.getEmployeeRequest(employeeId, status, pageable);
    }

    /**
     * ✅ Lấy chi tiết 1 đơn nghỉ
     */
    @GetMapping("/{id}")
    public LeaveRequestResponse getById(@PathVariable Long id) {
        return leaveRequestService.getById(id);
    }

    /**
     * ✅ Đếm tổng số đơn nghỉ
     */
    @GetMapping("/count")
    public long countAll() {
        return leaveRequestService.countAllRequests();
    }

    /**
     * ✅ Đếm số đơn theo trạng thái
     */
    @GetMapping("/count/status")
    public long countByStatus(@RequestParam RequestStatus status) {
        return leaveRequestService.countRequestsByStatus(status);
    }

    /**
     * ✅ Đếm số đơn của 1 nhân viên theo trạng thái
     */
    @GetMapping("/count/employee/{employeeId}")
    public long countByEmployee(
            @PathVariable Long employeeId,
            @RequestParam(required = false) RequestStatus status
    ) {
        return leaveRequestService.countRequestsByEmployee(employeeId, status);
    }
}