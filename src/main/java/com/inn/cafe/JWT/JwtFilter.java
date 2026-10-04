package com.inn.cafe.JWT;

import com.inn.cafe.POJO.User;
import com.inn.cafe.dao.UserDao;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Authenticates a request from its Bearer token.
 *
 * <p>Holds no per-request state of its own: the authenticated {@link User} entity is
 * stored as the details of the request's {@code Authentication}, which Spring scopes to
 * the request. Role checks read the role from that database row, never from the token,
 * so a forged or stale {@code role} claim grants nothing.
 */
@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDao userDao;

    @Autowired
    private CustomerUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticate(header.substring(7), request);
        }
        chain.doFilter(request, response);
    }

    private void authenticate(String token, HttpServletRequest request) {
        Claims claims;
        try {
            claims = jwtUtil.extractAllClaims(token); // verifies signature and expiry
        } catch (JwtException | IllegalArgumentException ex) {
            // An expired or malformed token makes the request anonymous rather than
            // failing it: public endpoints still work, protected ones return 401.
            log.debug("Ignoring invalid token: {}", ex.getMessage());
            return;
        }
        User user = userDao.findByEmailId(claims.getSubject());
        if (user == null || !"true".equalsIgnoreCase(user.getStatus())) {
            return; // deleted or disabled since the token was issued
        }
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user.getEmail(), null, List.of());
        auth.setDetails(user);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    public boolean isAdmin() {
        User user = userDetailsService.getUserDetail();
        return user != null && "admin".equalsIgnoreCase(user.getRole());
    }

    public boolean isUser() {
        User user = userDetailsService.getUserDetail();
        return user != null && "user".equalsIgnoreCase(user.getRole());
    }

    public String getCurrentUser() {
        User user = userDetailsService.getUserDetail();
        return user == null ? null : user.getEmail();
    }
}
