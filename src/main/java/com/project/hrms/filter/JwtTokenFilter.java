//package com.project.hrms.filter;
//
//import com.project.shopapp.components.JwtTokenUtils;
//import com.project.shopapp.models.User;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.data.util.Pair;
//import org.springframework.lang.NonNull;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.List;
//
//@Component
//@RequiredArgsConstructor
//public class JwtTokenFilter extends OncePerRequestFilter {
//    @Value("${api.prefix}")
//    private String apiPrefix;
//    private final UserDetailsService userDetailsService;
//    private final JwtTokenUtils jwtTokenUtil;
//
//    @Override
//    protected void doFilterInternal(@NonNull HttpServletRequest request,
//                                    @NonNull HttpServletResponse response,
//                                    @NonNull FilterChain filterChain)
//            throws ServletException, IOException {
//        try {
//            if (isBypassToken(request)) {
//                filterChain.doFilter(request, response); //enable bypass
//                return;
//            }
//            // Lấy token từ header Authorization
//            final String authHeader = request.getHeader("Authorization");
//
//            //  Nếu không có header hoặc không bắt đầu bằng "Bearer " → chặn, trả lỗi 401 (Unauthorized)
//            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
//                return;
//            }
//            //  Cắt chuỗi "Bearer " để lấy phần token thật
//            final String token = authHeader.substring(7);
//            //  Giải mã token để lấy số điện thoại (username)
//            final String phoneNumber = jwtTokenUtil.extractPhoneNumber(token);
//
//            //  Nếu có username và chưa xác thực trong context thì xử lý tiếp
//            if (phoneNumber != null
//                    && SecurityContextHolder.getContext().getAuthentication() == null) {
//                User userDetails = (User) userDetailsService.loadUserByUsername(phoneNumber);
//                //  Kiểm tra token hợp lệ (chưa hết hạn, đúng user, đúng key)
//                if (jwtTokenUtil.validateToken(token, userDetails)) {
//                    //  Tạo đối tượng xác thực cho user đã hợp lệ
//                    UsernamePasswordAuthenticationToken authenticationToken =
//                            new UsernamePasswordAuthenticationToken(
//                                    userDetails,
//                                    null,
//                                    userDetails.getAuthorities()
//                            );
//                    //  Gắn thêm thông tin request hiện tại (IP, session, v.v.)
//                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                    //  Đưa user đã xác thực vào SecurityContext (Spring sẽ hiểu là user đang đăng nhập)
//                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
//                }
//            }
//        // Cho request đi tiếp qua filter chain (dù đã xác thực hay bị bỏ qua)
//            filterChain.doFilter(request, response);
//        } catch (Exception e) {
//            e.printStackTrace(); // log lỗi để biết lỗi thật là gì
//            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
//            SecurityContextHolder.clearContext();
//        }
//    }
//
//    //kiem tra xem duong dan nao can check token, cai nao khong can
//    private boolean isBypassToken(@NonNull HttpServletRequest request) {
//
//        final List<Pair<String, String>> bypassTokens = Arrays.asList(
//                Pair.of(String.format("%s/roles", apiPrefix), "GET"),
//                Pair.of(String.format("%s/products", apiPrefix), "GET"),
//                Pair.of(String.format("%s/categories", apiPrefix), "GET"),
//                Pair.of(String.format("%s/users/register", apiPrefix), "POST"),
//                Pair.of(String.format("%s/users/login", apiPrefix), "POST")
//        );
//        for (Pair<String, String> bypassToken : bypassTokens) {
////            Nếu URL và method của request trùng với 1 mục trong danh sách này ->  bỏ qua xác thực
//            if (request.getServletPath().contains(bypassToken.getFirst()) &&
//                    request.getMethod().equals(bypassToken.getSecond())) {
//                return true;
//            }
//        }
//        return false;
//    }
//}
