package com.project.hrms.controller;

import com.project.hrms.dto.AccountDTO;
import com.project.hrms.dto.ChangePasswordRequest;
import com.project.hrms.model.Account;
import com.project.hrms.response.AccountListResponse;
import com.project.hrms.response.AccountResponse;
import com.project.hrms.response.ApiResponse;
import com.project.hrms.service.IAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
//import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
//@Tag(name = "Account Controller", description = "API quản lý tài khoản người dùng")
@CrossOrigin(origins = "*")
public class AccountController {

    private final IAccountService accountService;

    @PutMapping("/{id}")
    //@PreAuthorize("hasRole('ADMIN')")  // Comment lại để bỏ qua xác thực token
    public ResponseEntity<?> updateAccount(
            @PathVariable Long id,
            @Valid @RequestBody AccountDTO accountDTO,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors())
        {
            List<String> errorMessages = bindingResult.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(ApiResponse.fail(BAD_REQUEST, String.join("; ", errorMessages)));
        }
        AccountResponse accountResponse = accountService.update(id, accountDTO);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật tài khoản thành công", accountResponse));

    }

    @DeleteMapping("/{id}")
    //@PreAuthorize("hasRole('ADMIN')")  // Comment lại để bỏ qua xác thực token
    public ResponseEntity<?> deleteAccount(@PathVariable Long id) {
        accountService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa tài khoản thành công", null));
//        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    //@PreAuthorize("hasAnyRole('ADMIN', 'USER')")  // Comment lại để bỏ qua xác thực token
    public ResponseEntity<?> getAccountById(@PathVariable Long id) {
        Account account = accountService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy tài khoản thành công", AccountResponse.fromAccount(account)));
    }

    @GetMapping
    //@PreAuthorize("hasRole('ADMIN')")  // Comment lại để bỏ qua xác thực token
    public ResponseEntity<?> getAllAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit)
    {
        PageRequest pageRequest = PageRequest.of(page, limit,
                Sort.by("createdAt").descending());
        //lay danh sach tat ca tai khoan da chia trang
        Page<AccountResponse> accountResponsePage =  accountService.getAllPaged(pageRequest);

        AccountListResponse listResponse = AccountListResponse.builder()
                .accounts(accountResponsePage.getContent())
                .totalPages(accountResponsePage.getTotalPages())
                .totalElements(accountResponsePage.getTotalElements())
                .build();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách tài khoản thành công", listResponse));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchAccounts(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {

        PageRequest pageRequest = PageRequest.of(page, limit);
        Page<AccountResponse> results = accountService.searchAccounts(keyword, pageRequest);

        AccountListResponse listResponse = AccountListResponse.builder()
                .accounts(results.getContent())
                .totalPages(results.getTotalPages())
                .totalElements(results.getTotalElements())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Tìm kiếm tài khoản thành công", listResponse));
    }

    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Object>> checkUsernameExists(@RequestParam String username) {
        boolean exists = accountService.existsByUsername(username);
        return ResponseEntity.ok(Map.of(
                "username", username,
                "exists", exists
        ));
    }

    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmailExists(@RequestParam String email) {
        boolean exists = accountService.existsByEmail(email);
        return ResponseEntity.ok(Map.of(
                "email", email,
                "exists", exists
        ));
    }

    // ✅ User đổi mật khẩu của chính mình. Đang loi vu token
    @PostMapping("/change-password")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(BAD_REQUEST,
                            "Mật khẩu xác nhận không khớp"));
        }
        // Lấy username hiện tại từ token đăng nhập
        String username = authentication.getName();

        accountService.changePassword(username, request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công", null));
    }

    @PostMapping("/{id}/reset-password")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> resetPassword(@PathVariable Long id) {
        accountService.resetPassword(id);
        return ResponseEntity.ok(ApiResponse.success("Đặt lại mật khẩu mặc định thành công", null));
    }
}