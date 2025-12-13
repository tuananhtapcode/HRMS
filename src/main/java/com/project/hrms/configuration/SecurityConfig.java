package com.project.hrms.configuration;

import com.project.hrms.repository.AccountRepository;
import com.project.hrms.security.JwtAuthenticationFilter;
import com.project.hrms.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
//@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;


    @Bean
    public PasswordEncoder passwordEncoder() {
        // Dùng BCrypt để mã hóa mật khẩu
        return new BCryptPasswordEncoder();
    }

//    @Bean
//    public AuthenticationProvider authenticationProvider() {
//        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
//        authProvider.setUserDetailsService(customUserDetailsService);
//        authProvider.setPasswordEncoder(passwordEncoder());
//        return authProvider;
//    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        // Lấy AuthenticationManager từ Spring
        return config.getAuthenticationManager();
    }

    //dùng bỏ qua authen cho tất cả các endpoint
//    @Value("${security.disabled:true}")
//    private boolean securityDisabled;
//
//    @Bean
//    public WebSecurityCustomizer webSecurityCustomizer() {
//        if (securityDisabled) {
//            return (web) -> web.ignoring().requestMatchers("/**");
//        }
//        return (web) -> {}; // Không ignore gì cả
//    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Tắt CSRF vì dùng API (JWT)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Không tạo session
                .authorizeHttpRequests(auth -> auth
                        // Cho phép các endpoint không cần xác thực
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/accounts/**").permitAll() // Cho phép truy cập tất cả API của accounts
                        .requestMatchers("/api/v1/accounts/*").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll() // Cho phép truy cập Swagger UI
                        // Tất cả các request khác đều phải được xác thực
                        .anyRequest().authenticated()
                );

        // Thêm filter JWT của chúng ta vào trước filter mặc định của Spring
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}