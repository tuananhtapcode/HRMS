package com.project.hrms.service;

import com.project.hrms.model.Account;
import com.project.hrms.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Tìm Account trong DB
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with username: " + username));

        // 2. Kiểm tra active
        if (Boolean.FALSE.equals(account.getIsActive())) {
            throw new UsernameNotFoundException("Account is deactivated: " + username);
        }

        // 3. TRẢ VỀ TRỰC TIẾP ACCOUNT (Thay đổi quan trọng nhất)
        // Vì Account đã implements UserDetails rồi, nên return nó là hợp lệ.
        // Spring Security sẽ giữ nguyên cục Account này trong suốt phiên làm việc.
        return account;
    }
}