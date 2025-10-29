package com.project.hrms.service;

import com.project.hrms.model.Account;
import com.project.hrms.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

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

        // 2. Lấy quyền (Role) của account
        // Giả sử model Account của bạn có getRole() trả về model Role có getName()
        String roleName = "ROLE_" + account.getRole().getName().toUpperCase();

        // 3. Chuyển đổi Account -> UserDetails của Spring
        return new User(
                account.getUsername(),
                account.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority(roleName))
        );
    }
}