package com.project.hrms.service;

import com.project.hrms.dto.AccountDTO;
import com.project.hrms.dto.DepartmentDTO;
import com.project.hrms.exception.DataAlreadyExistsException;
import com.project.hrms.exception.DataNotFoundException;
import com.project.hrms.exception.InvalidParamException;
import com.project.hrms.model.Account;
import com.project.hrms.model.Department;
import com.project.hrms.model.Employee;
import com.project.hrms.model.Role;
import com.project.hrms.repository.AccountRepository;
import com.project.hrms.repository.EmployeeRepository;
import com.project.hrms.repository.RoleRepository;
import com.project.hrms.response.AccountResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService implements IAccountService {

    private final ModelMapper modelMapper;
    private final AccountRepository accountRepository;
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder; // thêm bean này nếu cần mã hóa mật khẩu

    @PostConstruct
    public void setupMapper() {
        // Cấu hình chuyển đổi từ AccountDTO sang Account
        modelMapper.addMappings(new PropertyMap<AccountDTO, Account>() {
            @Override
            protected void configure() {
                // Bỏ qua các trường không nên map tự động
                skip(destination.getAccountId());        // Không map ID vì đây là trường tự sinh
                skip(destination.getRole());            // Role sẽ được set thủ công sau
                skip(destination.getEmployee());        // Employee sẽ được set thủ công sau
                skip(destination.getCreatedAt());       // Các trường thời gian hệ thống tự xử lý
                skip(destination.getUpdatedAt());

                // Map các trường có tên khác nhau (nếu có)
                // Ví dụ: map(source.getPhoneNumber()).setContactNumber(null);
            }
        });

        // Cấu hình chuyển đổi ngược lại từ Account sang AccountDTO
        modelMapper.addMappings(new PropertyMap<Account, AccountDTO>() {
            @Override
            protected void configure() {
                // Map ID của Role và Employee sang DTO
                map(source.getRole().getRoleId()).setRoleId(null);
                map(source.getEmployee().getEmployeeId()).setEmployeeId(null);
            }
        });
    }

    @Override
    public Account create(AccountDTO accountDTO) {
        if (existsByUsername(accountDTO.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (accountDTO.getEmail() != null && existsByEmail(accountDTO.getEmail())) {
            throw new DataAlreadyExistsException("Email already exists");
        }

        Role role = roleRepository.findById(accountDTO.getRoleId())
                .orElseThrow(() -> new DataNotFoundException("Role not found with id: " + accountDTO.getRoleId()));

        Employee employee = employeeRepository.findById(accountDTO.getEmployeeId())
                .orElseThrow(() -> new DataNotFoundException("Employee not found with id: " + accountDTO.getEmployeeId()));

        Account account = modelMapper.map(accountDTO, Account.class);

        if (accountDTO.getPassword() != null) {
            account.setPassword(passwordEncoder.encode(accountDTO.getPassword()));
        }
        account.setRole(role);
        account.setEmployee(employee);
        account.setIsActive(true);

        if (accountRepository.existsByEmployee(employee)) {
            throw new DataAlreadyExistsException("Employee already has an account");
        }

        return accountRepository.save(account);
    }

    @Override
    public AccountResponse update(Long id, AccountDTO accountDTO) {
        Account existingAccount = getById(id);

        // Validate unique fields
        if (accountDTO.getUsername() != null && !accountDTO.getUsername().equals(existingAccount.getUsername())
                && existsByUsername(accountDTO.getUsername())) {
            throw new DataAlreadyExistsException("Username already exists");
        }

        if (accountDTO.getEmail() != null && !accountDTO.getEmail().equals(existingAccount.getEmail())
                && existsByEmail(accountDTO.getEmail())) {
            throw new DataAlreadyExistsException("Email already exists");
        }

        // Validate relations
        Role role = roleRepository.findById(accountDTO.getRoleId())
                .orElseThrow(() -> new DataNotFoundException("Role not found with id: " + accountDTO.getRoleId()));

        Employee employee = employeeRepository.findById(accountDTO.getEmployeeId())
                .orElseThrow(() -> new DataNotFoundException("Employee not found with id: " + accountDTO.getEmployeeId()));



        modelMapper.map(accountDTO, existingAccount);
        existingAccount.setRole(role);
        existingAccount.setEmployee(employee);

        // Handle password
        if (accountDTO.getPassword() != null && !accountDTO.getPassword().isBlank()) {
            existingAccount.setPassword(passwordEncoder.encode(accountDTO.getPassword()));
        }

        Account saved =  accountRepository.save(existingAccount);
        return AccountResponse.fromAccount(saved);

    }

    @Override
    public void delete(Long id) {
        Account account = getById(id);
        if (!account.getIsActive()) {
            throw new IllegalStateException("Account is already inactive");
        }
        account.setIsActive(false);
        // Trả về account đã bị xóa (nếu cần). Controller hiện tại trả 204 No Content nên có thể bỏ qua giá trị trả về.
    }

    @Override
    public Page<AccountResponse> searchAccounts(String keyword, PageRequest pageRequest) {
        if (keyword == null || keyword.trim().isEmpty()) {
            // Nếu không nhập gì, có thể trả toàn bộ account (hoặc ném lỗi tuỳ yêu cầu)
            return accountRepository.findAll(pageRequest)
                    .map(AccountResponse::fromAccount);
        }

        Page<Account> accounts = accountRepository.searchAccounts(keyword.trim(), pageRequest);
        return accounts.map(AccountResponse::fromAccount);
    }


    @Override
    public Page<AccountResponse> getAllPaged(PageRequest pageRequest) {
        return accountRepository.findAll(pageRequest).map(AccountResponse::fromAccount);
    }


    @Override
    public Account getById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Account not found with id: " + id));
    }

    @Override
    public Account getByEmail(String email) {
        return accountRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException("Account not found with email: " + email));
    }

    @Override
    public Account getByUsername(String username) {
        return accountRepository.findByUsername(username)
                .orElseThrow(() -> new DataNotFoundException("Account not found with username: " + username));
    }

    @Override
    public boolean existsByUsername(String username) {
        return accountRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return accountRepository.existsByEmail(email);
    }

    //thay doi mk sau khi login
    @Override
    public Account changePassword(String username, String oldPassword, String newPassword) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new DataNotFoundException("Account not found with username: " + username));

        // ✅ Kiểm tra rỗng/null để tránh NullPointerException
        if (oldPassword == null || newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("Old or new password cannot be null or empty");
        }

        // ✅ Kiểm tra mật khẩu cũ (đã mã hoá)
        if (!passwordEncoder.matches(oldPassword, account.getPassword())) {
            throw new InvalidParamException("Old password is incorrect");
        }

        // ✅ Mã hoá mật khẩu mới và lưu
        account.setPassword(passwordEncoder.encode(newPassword));
        return accountRepository.save(account);
    }

    @Override
    public Account resetPassword(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new DataNotFoundException("Account not found with id: " + accountId));

        // ✅ Dùng mật khẩu mặc định hoặc có thể random sau này
        String defaultPassword = "123456";

        // ✅ Mã hoá mật khẩu mặc định trước khi lưu
        account.setPassword(passwordEncoder.encode(defaultPassword));

        // (Optional) Gửi email thông báo
        // emailService.sendResetPasswordNotification(account.getEmail(), defaultPassword);

        return accountRepository.save(account);
    }

}
