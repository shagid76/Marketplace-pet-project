package com.marketplace.backend.security.jwt;

import com.marketplace.backend.model.User.Role;
import com.marketplace.backend.model.User.User;
import com.marketplace.backend.security.CustomUserDetails;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final List<String> SKIP_PATHS = List.of(
            "/api/auth/sign-in",
            "/api/auth/registration",
            "/api/auth/refresh",
            "/api/payments/webhook"
    );

    private final JwtService jwtService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return SKIP_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (token != null) {
            Optional<Claims> claims = jwtService.validateAndExtract(token);

            if (claims.isEmpty()) {
                sendUnauthorized(response, "Invalid or expired token");
                return;
            }

            if (!JwtService.TokenType.ACCESS.name().equals(claims.get().get("type", String.class))) {
                sendUnauthorized(response, "Invalid token type");
                return;
            }

            authenticateFromClaims(claims.get(), request);
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateFromClaims(Claims claims, HttpServletRequest request) {
        User user = buildUserFromClaims(claims);
        CustomUserDetails userDetails = new CustomUserDetails(user, true, true);

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private User buildUserFromClaims(Claims claims) {
        User user = new User();
        user.setEmail(claims.getSubject());
        user.setId(claims.get("userId", String.class));

        List<String> rawRoles = claims.get("roles", List.class);
        if (rawRoles != null) {
            Set<Role> roles = rawRoles.stream()
                    .map(Role::valueOf)
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        return user;
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}