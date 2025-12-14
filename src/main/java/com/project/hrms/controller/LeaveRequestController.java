package com.project.hrms.controller;

import com.project.hrms.dto.RequestApproveDTO;
import com.project.hrms.dto.LeaveRequestDTO;
import com.project.hrms.model.enums.RequestStatus;
import com.project.hrms.response.LeaveRequestResponse;
import com.project.hrms.service.ILeaveRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/leave-requests")
@RequiredArgsConstructor
public class LeaveRequestController {

    private final ILeaveRequestService leaveRequestService;

    /**
     * ✅ Tạo đơn nghỉ phép
     * - Employee: tự tạo cho chính mình
     * - Admin/Manager: tạo hộ nhân viên khác
     */
    @PostMapping
    public LeaveRequestResponse create(@Valid @RequestBody LeaveRequestDTO dto) {
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
            @Valid
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

    @GetMapping("/count/pending")
    public long countPending() {
        return leaveRequestService.countPending();
    }

    @GetMapping("/count/approved")
    public long countApproved() {
        return leaveRequestService.countApproved();
    }

    @GetMapping("/count/rejected")
    public long countRejected() {
        return leaveRequestService.countRejected();
    }

    @GetMapping("/count/cancelled")
    public long countCancelled() {
        return leaveRequestService.countCancelled();
    }

    /* ================= ADMIN / MANAGER ================= */

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/pending")
    public Page<LeaveRequestResponse> getAllPending(Pageable pageable) {
        return leaveRequestService.getAllPending(pageable);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/approved")
    public Page<LeaveRequestResponse> getAllApproved(Pageable pageable) {
        return leaveRequestService.getAllApproved(pageable);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/rejected")
    public Page<LeaveRequestResponse> getAllRejected(Pageable pageable) {
        return leaveRequestService.getAllRejected(pageable);
    }

    /* ================= EMPLOYEE ================= */

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/my/pending")
    public Page<LeaveRequestResponse> getMyPending(Pageable pageable) {
        return leaveRequestService.getMyPending(pageable);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/my/approved")
    public Page<LeaveRequestResponse> getMyApproved(Pageable pageable) {
        return leaveRequestService.getMyApproved(pageable);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/my/rejected")
    public Page<LeaveRequestResponse> getMyRejected(Pageable pageable) {
        return leaveRequestService.getMyRejected(pageable);
    }
}