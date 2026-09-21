package com.example.jobboard.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String target = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> switch (role) {
                    case "ROLE_ADMIN" -> "/admin/dashboard";
                    case "ROLE_EMPLOYER" -> "/employer/dashboard";
                    case "ROLE_CANDIDATE" -> "/candidate/dashboard";
                    default -> "/";
                })
                .findFirst()
                .orElse("/");
        response.sendRedirect(request.getContextPath() + target);
    }
}
