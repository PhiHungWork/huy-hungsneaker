package com.example.QuanLiThuChi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/", "/home", "/css/**", "/js/**", "/img/**", "/about-us", "/bo-suu-tap", "/chinh-sach").permitAll()
                .requestMatchers("/products/admin", "/products/admin/**").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers("/products", "/products/**").permitAll()
                .requestMatchers("/cart", "/cart/**").permitAll()
                .requestMatchers("/login", "/error").permitAll()
                .requestMatchers("/orders/checkout", "/orders/place", "/orders/success/**", "/orders/track", "/orders/track/**").permitAll()
                .requestMatchers("/dashboard", "/categories/**", "/orders/**", "/reports/**").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler((request, response, authentication) -> {
                    boolean isAdmin = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_STAFF"));
                    if (isAdmin) {
                        response.sendRedirect("/dashboard");
                    } else {
                        response.sendRedirect("/");
                    }
                })
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
