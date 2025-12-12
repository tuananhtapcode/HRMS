package com.project.hrms.controller;

import com.project.hrms.dto.OvertimeRequestDTO;
import com.project.hrms.dto.RequestApproveDTO;
import com.project.hrms.model.enums.RequestStatus;
import com.project.hrms.response.ApiResponse;
import com.project.hrms.response.OvertimeRequestResponse;
import com.project.hrms.service.IOvertimeRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/overtime-requests")
@RequiredArgsConstructor
public class OvertimeRequestController {

    private final IOvertimeRequestService overtimeService;

    /* ----------------------------------------------------
     * 1) Nhân viên tạo yêu cầu OT
     * ---------------------------------------------------- */
    @PostMapping
    public ResponseEntity<ApiResponse<OvertimeRequestResponse>> create(
            @Valid @RequestBody OvertimeRequestDTO dto
    ) {
        var res = overtimeService.createRequest(dto);
        return ResponseEntity.ok(ApiResponse.success("Created", res));
    }

    /* ----------------------------------------------------
     * 2) Cập nhật yêu cầu OT
     * ---------------------------------------------------- */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OvertimeRequestResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody OvertimeRequestDTO dto
    ) {
        var res = overtimeService.updateRequest(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Updated", res));
    }

    /* ----------------------------------------------------
     * 3) Manager duyệt yêu cầu OT
     * ---------------------------------------------------- */
    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<OvertimeRequestResponse>> approve(
            @PathVariable Long id,
            @RequestBody RequestApproveDTO dto
    ) {
        var res = overtimeService.approveRequest(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Approved", res));
    }

    /* ----------------------------------------------------
     * 4) Manager từ chối yêu cầu OT
     * ---------------------------------------------------- */
    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<OvertimeRequestResponse>> reject(
            @PathVariable Long id,
            @RequestBody RequestApproveDTO dto
    ) {
        var res = overtimeService.rejectRequest(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Rejected", res));
    }

    /* ----------------------------------------------------
     * 5) Huỷ yêu cầu OT
     * ---------------------------------------------------- */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OvertimeRequestResponse>> cancel(
            @PathVariable Long id
    ) {
        var res = overtimeService.cancelRequest(id);
        return ResponseEntity.ok(ApiResponse.success("Cancelled", res));
    }

    /* ----------------------------------------------------
     * 6) Lấy danh sách OT (cho admin/manager)
     * ---------------------------------------------------- */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<OvertimeRequestResponse>>> listAll(
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var res = overtimeService.listAll(status, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success("Get all overtime request", res));
    }

    /* ----------------------------------------------------
     * 7) Nhân viên xem danh sách OT của chính mình
     * ---------------------------------------------------- */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Page<OvertimeRequestResponse>>> listMyRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        var res = overtimeService.getMyRequests(pageable);
        return ResponseEntity.ok(ApiResponse.success("Get my overtime request", res));
    }

    /* ----------------------------------------------------
     * 8) Admin xem tất cả request của 1 nhân viên
     * ---------------------------------------------------- */
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<Page<OvertimeRequestResponse>>> listByEmployee(
            @PathVariable Long employeeId,
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var res = overtimeService.getEmployeeRequest(employeeId, status, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success("Admin xem tất cả request của 1 nhân viên", res));
    }

    /* ----------------------------------------------------
     * 9) Xem chi tiết đơn OT
     * ---------------------------------------------------- */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OvertimeRequestResponse>> getById(
            @PathVariable Long id
    ) {
        var res = overtimeService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Chi tiết đơn OT", res));
    }

    /* ===================== COUNT API ===================== */

    @GetMapping("/count/all")
    public ResponseEntity<ApiResponse<Long>> countAll() {
        return ResponseEntity.ok(ApiResponse.success("OK", overtimeService.countAllRequests()));
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> countByStatus(
            @RequestParam(required = false) RequestStatus status) {
        return ResponseEntity.ok(ApiResponse.success("OK", overtimeService.countRequestsByStatus(status)));
    }

    @GetMapping("/employee/{id}/count")
    public ResponseEntity<ApiResponse<Long>> countByEmployee(
            @PathVariable Long id,
            @RequestParam(required = false) RequestStatus status) {
        return ResponseEntity.ok(ApiResponse.success("OK", overtimeService.countRequestsByEmployee(id, status)));
    }

    /* ===================== DASHBOARD KPIs ===================== */

    @GetMapping("/dashboard/pending")
    public ResponseEntity<ApiResponse<Long>> countPending() {
        return ResponseEntity.ok(ApiResponse.success("OK", overtimeService.countPending()));
    }

    @GetMapping("/dashboard/approved")
    public ResponseEntity<ApiResponse<Long>> countApproved() {
        return ResponseEntity.ok(ApiResponse.success("OK", overtimeService.countApproved()));
    }

    @GetMapping("/dashboard/rejected")
    public ResponseEntity<ApiResponse<Long>> countRejected() {
        return ResponseEntity.ok(ApiResponse.success("OK", overtimeService.countRejected()));
    }

    @GetMapping("/dashboard/cancelled")
    public ResponseEntity<ApiResponse<Long>> countCancelled() {
        return ResponseEntity.ok(ApiResponse.success("OK", overtimeService.countCancelled()));
    }

    /* ===================== DASHBOARD OT HOURS ===================== */

    @GetMapping("/dashboard/total-minutes")
    public ResponseEntity<ApiResponse<Integer>> getTotalMinutesAll() {
        return ResponseEntity.ok(ApiResponse.success("OK", overtimeService.getTotalOvertimeMinutesAll()));
    }

    @GetMapping("/dashboard/employee/{id}/total-minutes")
    public ResponseEntity<ApiResponse<Integer>> getTotalMinutesEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("OK", overtimeService.getTotalOvertimeMinutesByEmployee(id)));
    }

    /* ===================== DASHBOARD MONTHLY ===================== */

    @GetMapping("/dashboard/monthly")
    public ResponseEntity<ApiResponse<Map<Integer, Integer>>> monthlyStats(
            @RequestParam int year) {
        return ResponseEntity.ok(ApiResponse.success("OK", overtimeService.getMonthlyStats(year)));
    }
}
