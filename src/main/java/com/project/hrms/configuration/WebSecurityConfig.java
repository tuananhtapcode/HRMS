//package com.project.hrms.configuration;
//
//import com.project.shopapp.Filter.JwtTokenFilter;
//import com.project.shopapp.models.Role;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.web.servlet.config.annotation.EnableWebMvc;
//
//import static org.springframework.http.HttpMethod.*;
//
//@Configuration //Đánh dấu class này là Spring configuration class
////@EnableMethodSecurity
//@EnableWebSecurity //Kích hoạt module Spring Security cho web
//@EnableWebMvc //Bật Spring MVC configuration
//@RequiredArgsConstructor
//public class WebSecurityConfig {
//    private final JwtTokenFilter jwtTokenFilter;
//    @Value("${api.prefix}")
//    private String apiPrefix;
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
//                .authorizeHttpRequests(auth -> auth
//                        // Public routes
//                        .requestMatchers(
//                                String.format("%s/users/register", apiPrefix),
//                                String.format("%s/users/login", apiPrefix)
//                        ).permitAll()
//
//                        // Roles
//                        .requestMatchers(GET, String.format("%s/roles/**", apiPrefix)).permitAll()
//
//                        // Categories
//                        .requestMatchers(GET, String.format("%s/categories/**", apiPrefix)).permitAll()
//                        .requestMatchers(POST, String.format("%s/categories/**", apiPrefix)).hasRole(Role.ADMIN)
//                        .requestMatchers(PUT, String.format("%s/categories/**", apiPrefix)).hasRole(Role.ADMIN)
//                        .requestMatchers(DELETE, String.format("%s/categories/**", apiPrefix)).hasRole(Role.ADMIN)
//
//                        // Products
//                        .requestMatchers(GET, String.format("%s/products/**", apiPrefix)).permitAll()
//                        .requestMatchers(GET, String.format("%s/products/images/**", apiPrefix)).permitAll()
//                        .requestMatchers(POST, String.format("%s/products/**", apiPrefix)).hasRole(Role.ADMIN)
//                        .requestMatchers(PUT, String.format("%s/products/**", apiPrefix)).hasRole(Role.ADMIN)
//                        .requestMatchers(DELETE, String.format("%s/products/**", apiPrefix)).hasRole(Role.ADMIN)
//
//                        // Orders
//                        .requestMatchers(GET, String.format("%s/orders/**", apiPrefix)).hasAnyRole(Role.USER, Role.ADMIN)
//                        .requestMatchers(POST, String.format("%s/orders/**", apiPrefix)).hasRole(Role.USER)
//                        .requestMatchers(PUT, String.format("%s/orders/**", apiPrefix)).hasRole(Role.ADMIN)
//                        .requestMatchers(DELETE, String.format("%s/orders/**", apiPrefix)).hasRole(Role.ADMIN)
//
//                        // Order details
//                        .requestMatchers(GET, String.format("%s/order_details/**", apiPrefix)).permitAll()
//                        .requestMatchers(POST, String.format("%s/order_details/**", apiPrefix)).hasRole(Role.USER)
//                        .requestMatchers(PUT, String.format("%s/order_details/**", apiPrefix)).hasRole(Role.ADMIN)
//                        .requestMatchers(DELETE, String.format("%s/order_details/**", apiPrefix)).hasRole(Role.ADMIN)
//
//                        // Default rule
//                        .anyRequest().authenticated()
//                )
//                .csrf(AbstractHttpConfigurer::disable);
////                .cors(cors -> {
////                    CorsConfiguration config = new CorsConfiguration();
////                    config.setAllowedOrigins(List.of("*"));
////                    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
////                    config.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token"));
////                    config.setExposedHeaders(List.of("x-auth-token"));
////                    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
////                    source.registerCorsConfiguration("/**", config);
////                    cors.configurationSource(source);
////                });
//
//        return http.build();
//    }
//}
