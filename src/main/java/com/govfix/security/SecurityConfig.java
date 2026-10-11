package com.govfix.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final DemoAuthenticationFilter demoAuthenticationFilter;

    public SecurityConfig(DemoAuthenticationFilter demoAuthenticationFilter) {
        this.demoAuthenticationFilter = demoAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)) // For H2 console
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Static web frontend
                        .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/assets/**", "/uploads/**", "/favicon.ico").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/api/health").permitAll()
                        .requestMatchers("/api/dashboard/stats").permitAll()
                        .requestMatchers("/api/garbage-tracking/**", "/api/vehicles/**", "/api/waste-reports/**").permitAll()
                        .requestMatchers("/api/demo/**").permitAll()

                        // Role-specific API restrictions
                        .requestMatchers(HttpMethod.POST, "/api/workflows/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/workflows/**").hasRole("ADMIN")
                        .requestMatchers("/api/workflows/**").hasAnyRole("ADMIN", "SUPERVISOR", "OFFICER", "AUDITOR")

                        .requestMatchers("/api/audit/**").hasAnyRole("AUDITOR", "ADMIN", "SUPERVISOR", "OFFICER")
                        .requestMatchers("/api/escalations/**").hasAnyRole("SUPERVISOR", "ADMIN", "AUDITOR")

                        .requestMatchers(HttpMethod.POST, "/api/complaints/*/assign").hasAnyRole("SUPERVISOR", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/complaints/*/inspection").hasAnyRole("OFFICER", "SUPERVISOR")
                        .requestMatchers(HttpMethod.POST, "/api/complaints/*/evidence").hasAnyRole("OFFICER", "SUPERVISOR")

                        // General complaint interactions
                        .requestMatchers("/api/complaints/**").authenticated()
                        .requestMatchers("/api/ai/**").authenticated()

                        .anyRequest().permitAll()
                )
                .addFilterBefore(demoAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

