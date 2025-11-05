//package com.project.hrms.security.service;
//
//import com.project.hrms.model.Account;
//import com.project.hrms.repository.AccountRepository;
//import com.project.hrms.security.model.CustomUserDetails;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class CustomUserDetailsService implements UserDetailsService {
//
//    private final AccountRepository accountRepository;
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        Account account = accountRepository.findByUsername(username)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
//        return new CustomUserDetails(account);
//    }

// ✅ Nếu account bị khóa hoặc vô hiệu hóa
//        if (Boolean.FALSE.equals(account.getIsActive())) {
//        throw new UsernameNotFoundException("Account is deactivated: " + username);
//        }

// ✅ Gán quyền cho người dùng (role từ DB)
//GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + account.getRole().getRoleName().toUpperCase());

// ✅ Trả về đối tượng User của Spring Security
//        return new User(
//        account.getUsername(),
//                account.getPassword(),
//                Collections.singleton(authority)
//        );
//}
