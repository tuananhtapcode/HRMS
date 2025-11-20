package com.project.hrms.security;

import com.project.hrms.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Mỗi request HTTP đi qua sẽ chạy qua filter này đúng 1 lần
     * để kiểm tra xem header có chứa JWT hợp lệ không.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // 🔹 1. Lấy token JWT từ header "Authorization"
            String jwt = extractJwtFromRequest(request);

            // 🔹 2. Kiểm tra token có hợp lệ không (chữ ký, hết hạn, định dạng, ...)
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {

                // 🔹 3. Giải mã token → lấy username (được lưu khi tạo token)
                String username = tokenProvider.getUsernameFromJWT(jwt);

                // 🔹 4. Load thông tin người dùng từ DB để kiểm tra quyền hạn
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                // 🔹 5. Tạo đối tượng Authentication (chứa thông tin user + quyền)
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null, // không cần mật khẩu ở đây
                                userDetails.getAuthorities()
                        );

                // 🔹 6. Gắn thông tin chi tiết request (IP, session, ...) vào authentication
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 🔹 7. Lưu authentication vào SecurityContextHolder
                // => Spring Security hiểu user này đã đăng nhập
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("✅ Authenticated user '{}', set security context.", username);
            } else {
                // Trường hợp không có token hoặc token không hợp lệ
                log.debug("⚠️ No valid JWT token found for request URI: {}", request.getRequestURI());
            }

        } catch (Exception ex) {
            // Không nên throw exception ra ngoài — tránh chặn luồng filter chain
            log.error("❌ Failed to authenticate user: {}", ex.getMessage());
        }

        // 🔹 8. Tiếp tục cho request đi qua filter tiếp theo
        filterChain.doFilter(request, response);
    }

    /**
     * Hàm tiện ích: lấy chuỗi JWT từ header "Authorization"
     * Ví dụ: "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        // Kiểm tra token có tồn tại và đúng format "Bearer <token>"
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // bỏ phần "Bearer "
        }
        return null;
    }
}
