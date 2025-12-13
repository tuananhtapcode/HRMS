package com.project.hrms.security;

import com.project.hrms.model.Account; // <-- Import Account của bạn
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority; // <-- Import này
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors; // <-- Import này
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationInMs;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // --- HÀM NÀY ĐÃ ĐƯỢC SỬA ---
    public String generateToken(Authentication authentication) {
        // 1. Ép kiểu về Account (Thay vì UserDetails chung chung)
        // Vì Account của bạn đã implements UserDetails rồi
        Account account = (Account) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        // 2. Lấy danh sách Roles từ Account
        // Kết quả sẽ là list string: ["ROLE_ADMIN", "ROLE_USER"...]
        List<String> roles = account.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // 3. Khởi tạo Builder
        JwtBuilder builder = Jwts.builder()
                .setSubject(account.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS512)

                // 4. QUAN TRỌNG: Nhét Role vào Token
                .claim("roles", roles);

        // 5. Tiện thể nhét luôn EmployeeId và Tên (nếu có)
        // Giúp Frontend lấy được ID nhân viên ngay từ token mà không cần gọi API khác
        if (account.getEmployee() != null) {
            builder.claim("employeeId", account.getEmployee().getEmployeeId());
            builder.claim("fullName", account.getEmployee().getFullName());
        }

        return builder.compact();
    }

    // Hàm lấy username từ token (Giữ nguyên)
    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    // Hàm kiểm tra token (Giữ nguyên)
    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (ExpiredJwtException ex) {
            log.warn("JWT expired: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.warn("JWT unsupported: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.warn("JWT malformed: {}", ex.getMessage());
        } catch (SecurityException ex) {
            log.warn("JWT signature invalid: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.warn("JWT is empty or null: {}", ex.getMessage());
        }
        return false;
    }
}