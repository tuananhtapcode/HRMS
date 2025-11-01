package com.project.hrms.controller;

import com.project.hrms.dto.AccountDTO;
import com.project.hrms.dto.ChangePasswordRequest;
import com.project.hrms.model.Account;
import com.project.hrms.response.AccountListResponse;
import com.project.hrms.response.AccountResponse;
import com.project.hrms.response.MessageResponse;
import com.project.hrms.service.IAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
            return ResponseEntity.badRequest().body(errorMessages);
        }
        AccountResponse accountResponse = accountService.update(id, accountDTO);
        return ResponseEntity.ok(accountResponse);

    }

    @DeleteMapping("/{id}")
    //@PreAuthorize("hasRole('ADMIN')")  // Comment lại để bỏ qua xác thực token
    public ResponseEntity<?> deleteAccount(@PathVariable Long id) {
        accountService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    //@PreAuthorize("hasAnyRole('ADMIN', 'USER')")  // Comment lại để bỏ qua xác thực token
    public ResponseEntity<Account> getAccountById(@PathVariable Long id) {
        Account account = accountService.getById(id);
        return ResponseEntity.ok(account);
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
        Page<AccountResponse> accountResponsePage = accountService.getAllPaged(pageRequest);
        int totalPages = accountResponsePage.getTotalPages();
        List<AccountResponse> accounts = accountResponsePage.getContent();
        //cach 1
        return ResponseEntity.ok(AccountListResponse.builder()
                .accounts(accounts)
                .totalPages(totalPages)
                .build());
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchAccounts(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {

        PageRequest pageRequest = PageRequest.of(page, limit);
        Page<AccountResponse> results = accountService.searchAccounts(keyword, pageRequest);

        //cach 2
        return ResponseEntity.ok(AccountListResponse.builder()
                .accounts(results.getContent())
                .totalPages(results.getTotalPages())
                .build());
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
    public ResponseEntity<MessageResponse> changePassword(
            @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("New password and confirmation do not match"));
        }
        // Lấy username hiện tại từ token đăng nhập
        String username = authentication.getName();

        accountService.changePassword(username, request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
    }

    @PostMapping("/{id}/reset-password")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> resetPassword(@PathVariable Long id) {
        accountService.resetPassword(id);
        return ResponseEntity.ok().build();
    }

}
