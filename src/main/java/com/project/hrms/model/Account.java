package com.project.hrms.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "account")
@Data
@NoArgsConstructor
@AllArgsConstructor
// 1. Phải implement UserDetails
public class Account extends BaseEntity implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = true)
    private String password;

    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // Quan hệ ManyToOne với bảng Role
    @ManyToOne(fetch = FetchType.EAGER) // Nên để EAGER để lúc login lấy luôn được Role
    @JoinColumn(name = "role_id")
    private Role role;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "activation_token")
    private String activationToken;

    @Column(name = "activation_token_expires")
    private LocalDateTime activationTokenExpires;

    // --- PHẦN QUAN TRỌNG NHẤT: LẤY QUYỀN TỪ BẢNG ROLE ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null) {
            return Collections.emptyList();
        }
        // Lấy code từ bảng Role (VD: "ADMIN") và thêm tiền tố "ROLE_"
        // Kết quả: "ROLE_ADMIN" (Chuẩn Spring Security)
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.getCode().toUpperCase()));
    }

    // --- CÁC HÀM BẮT BUỘC KHÁC CỦA USERDETAILS ---
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Hoặc check field is_active
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(isActive);
    }
}