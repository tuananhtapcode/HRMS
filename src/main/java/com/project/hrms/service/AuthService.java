package com.project.hrms.service;

import com.project.hrms.dto.ActivateAccountDTO;
import com.project.hrms.dto.RegisterRequestDTO;
import com.project.hrms.exception.DataNotFoundException;
import com.project.hrms.exception.InvalidParamException;
import com.project.hrms.model.Account;
import com.project.hrms.model.Role;
import com.project.hrms.repository.AccountRepository;
import com.project.hrms.repository.RoleRepository;
import com.project.hrms.security.model.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public Account registerUser(RegisterRequestDTO dto) {
        // 1. Kiểm tra username đã tồn tại chưa
        if (accountRepository.existsByUsername(dto.getUsername())) {
            throw new InvalidParamException("Username is already taken!");
        }

        // 2. Kiểm tra email đã tồn tại chưa (nếu email là unique)
        if (accountRepository.existsByEmail(dto.getEmail())) {
            throw new InvalidParamException("Email is already in use!");
        }

        // 3. Tìm Role mặc định (ví dụ: "USER")
        // Đảm bảo bạn có 1 Role tên "USER" trong bảng `role`
        Role userRole = roleRepository.findByName("Quản Lý")
                .orElseThrow(() -> new RuntimeException("Error: Default User Role not found."));

        // 4. Tạo Account mới
        Account account = new Account();
        account.setUsername(dto.getUsername());
        account.setEmail(dto.getEmail());
        // Mã hóa mật khẩu
        account.setPassword(passwordEncoder.encode(dto.getPassword()));
        account.setRole(userRole);
        account.setIsActive(true);
        // Bạn có thể set các trường mặc định khác

        // 5. Lưu vào database
        return accountRepository.save(account);
    }

    public void activateAccount(ActivateAccountDTO dto) {
        Account account = accountRepository.findByActivationToken(dto.getToken())
                .orElseThrow(() -> new DataNotFoundException("Token không hợp lệ hoặc đã được sử dụng."));

        if (account.getActivationTokenExpires().isBefore(LocalDateTime.now())) {
            throw new InvalidParamException("Token đã hết hạn, vui lòng liên hệ HR để cấp lại.");
        }

        account.setIsActive(true);
        account.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        account.setActivationToken(null);
        account.setActivationTokenExpires(null);

        accountRepository.save(account);
    }

    public Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            throw new IllegalStateException("Không tìm thấy user trong SecurityContext");
        }

        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
        return user.getUserId();
    }

    public String getCurrentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().iterator().next().getAuthority();
    }

    public boolean isAdminOrManager() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String role = auth.getAuthorities().iterator().next().getAuthority();
        return role.equals("ADMIN") || role.equals("MANAGER");
    }


}