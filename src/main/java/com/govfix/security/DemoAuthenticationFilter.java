package com.govfix.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class DemoAuthenticationFilter extends OncePerRequestFilter {

    public static final String ROLE_HEADER = "X-GovFix-Role";
    public static final String USER_HEADER = "X-GovFix-User";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestedRole = request.getHeader(ROLE_HEADER);
        String requestedUser = request.getHeader(USER_HEADER);

        // Normalize requested role or default to CITIZEN
        String roleName = (requestedRole != null && !requestedRole.trim().isEmpty())
                ? requestedRole.trim().toUpperCase()
                : "CITIZEN";

        String username = (requestedUser != null && !requestedUser.trim().isEmpty())
                ? requestedUser.trim()
                : getDefaultUsernameForRole(roleName);

        // Ensure ROLE_ prefix for Spring Security
        String authority = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(authority));

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private String getDefaultUsernameForRole(String role) {
        switch (role.toUpperCase().replace("ROLE_", "")) {
            case "OFFICER": return "officer_sharma";
            case "SUPERVISOR": return "supervisor_kulkarni";
            case "ADMIN": return "admin_govfix";
            case "AUDITOR": return "auditor_joshi";
            default: return "citizen_rahul";
        }
    }
}
