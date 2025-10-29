package com.project.hrms.service;

import com.project.hrms.dto.RegisterRequestDTO;
import com.project.hrms.exception.InvalidParamException;
import com.project.hrms.model.Account;
import com.project.hrms.model.Role;
import com.project.hrms.repository.AccountRepository;
import com.project.hrms.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        Role userRole = roleRepository.findByName("USER")
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
}