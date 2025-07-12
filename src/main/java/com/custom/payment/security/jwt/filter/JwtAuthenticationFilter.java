package com.custom.payment.security.jwt.filter;

import com.custom.payment.security.jwt.JwtTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;

    private static final List<String> WHITELIST = List.of(
            "/api/v1/login",
            "/login",
            "/api/v1/actuator/prometheus",
            "/api/v1/v3/api-docs",
            "/api/v1/swagger-ui",
            "/api/v1/swagger-ui/"
    );

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException, IOException {

        var uri = request.getRequestURI();

        if (WHITELIST.stream().anyMatch(uri::startsWith)) {
            filterChain.doFilter(request, response);
            return;
        }

        var token = extractTokenFromRequest(request);
        if (token != null && jwtTokenService.validateToken(token)) {
            Authentication authentication = new UsernamePasswordAuthenticationToken(null, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> body = new HashMap<>();
            body.put("error", "Invalid token");
            body.put("status", 401);
            body.put("timestamp", LocalDateTime.now().toString());

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(mapper.writeValueAsString(body));
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
