package com.project.hrms.controller;

import com.project.hrms.dto.ActivateAccountDTO;
import com.project.hrms.dto.LoginRequestDTO;
import com.project.hrms.dto.LoginResponseDTO;
import com.project.hrms.dto.RegisterRequestDTO;
import com.project.hrms.response.MessageResponse;
import com.project.hrms.security.JwtTokenProvider;
import com.project.hrms.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AuthService authService;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequest,
                                   BindingResult bindingResult) {
        if (bindingResult.hasErrors())
        {
            List<String> errorMessages = bindingResult.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(errorMessages);
        }

        // 1. Xác thực username & password qua AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        // 2. Lưu thông tin xác thực vào SecurityContext(để session hoạt động)
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Tạo JWT token
        String jwt = tokenProvider.generateToken(authentication);

        // 4. Trả về token
        return ResponseEntity.ok(new LoginResponseDTO(jwt));
    }

    @PostMapping("/activate")
    public ResponseEntity<?> activateAccount(
            @Valid @RequestBody ActivateAccountDTO dto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            List<String> errorMessages = bindingResult.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(errorMessages);
        }

        authService.activateAccount(dto);
        return ResponseEntity.ok(new MessageResponse("Kích hoạt tài khoản thành công!"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequestDTO registerRequest) {

        // 1. Gọi service để tạo user
        authService.registerUser(registerRequest);

        // 2. Trả về thông báo thành công
        return ResponseEntity.ok("User registered successfully!");
    }
}