package com.prestamosfacil.infrastructure.security.config;

import com.prestamosfacil.infrastructure.security.constants.SecurityConstants;
import com.prestamosfacil.infrastructure.adapter.in.rest.auth.filter.JwtAuthFilter;
import com.prestamosfacil.infrastructure.adapter.in.rest.auth.filter.CookieCsrfFilter;
import com.prestamosfacil.infrastructure.adapter.in.rest.auth.filter.LoginRateLimitFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String API_AUTH = SecurityConstants.API_AUTH_PATH;
    private static final String API_STAFF = SecurityConstants.API_STAFF_PATH;

    private final JwtAuthFilter jwtAuthFilter;
    private final LoginRateLimitFilter loginRateLimitFilter;
    private final CookieCsrfFilter cookieCsrfFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          LoginRateLimitFilter loginRateLimitFilter,
                          CookieCsrfFilter cookieCsrfFilter,
                          CorsConfigurationSource corsConfigurationSource) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.loginRateLimitFilter = loginRateLimitFilter;
        this.cookieCsrfFilter = cookieCsrfFilter;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
                .cacheControl(cache -> cache.disable())
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS).permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                .requestMatchers(
                    API_AUTH + "/register", API_AUTH + "/login",
                    API_AUTH + "/refresh", API_AUTH + "/password-reset/request",
                    API_AUTH + "/password-reset/confirm",
                    API_STAFF + "/login", API_STAFF + "/refresh"
                ).permitAll()
                .requestMatchers(API_STAFF + "/register").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/loan-types").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/customers").hasAuthority("CUSTOMER_CREATE")
                .requestMatchers(HttpMethod.GET, "/api/v1/loan-types/admin").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.POST, "/api/v1/loan-types").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.PUT, "/api/v1/loan-types/*").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/loan-types/*/status").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.POST, "/api/v1/loan-types/reorder").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/loan-types/*").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.GET, "/api/v1/loan-applications").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/v1/loan-applications/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/v1/loan-applications/*/approve").hasAuthority("LOAN_APPLICATION_APPROVE")
                .requestMatchers(HttpMethod.POST, "/api/v1/loan-applications/*/reject").hasAuthority("LOAN_APPLICATION_REJECT")
                .requestMatchers(HttpMethod.POST, "/api/v1/loan-applications/*/automatic-evaluation").hasAuthority("LOAN_APPLICATION_EVALUATE")
                .requestMatchers("/api/v1/reports/**").hasAuthority("REPORT_APPROVED_LOANS_READ")
                .requestMatchers(API_STAFF + "/**").hasRole("STAFF")
                .requestMatchers(HttpMethod.POST, "/api/v1/loan-applications").authenticated()
                .anyRequest().authenticated()
            )
            .addFilterBefore(cookieCsrfFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(loginRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
