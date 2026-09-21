package com.inn.cafe.JWT;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomerUserDetailsService service;

    // These used to be plain instance fields on this @Component -- but a
    // filter bean is a singleton shared across every concurrent request, so
    // whichever request last set userName/claims left them in place for the
    // NEXT request to read, even a token-less one. A guest request would
    // then reuse a previous, unrelated user's name, call
    // loadUserByUsername() with it, and call validateToken(null, ...),
    // which throws (JJWT rejects a null token) -- turning every public,
    // permitAll endpoint into an intermittent 401/500 depending on what
    // request happened to run on this thread before it. ThreadLocal keeps
    // each request's values isolated, matching how SecurityContextHolder
    // itself is scoped.
    private static final ThreadLocal<Claims> claimsHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> userNameHolder = new ThreadLocal<>();

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {

        if (httpServletRequest.getServletPath().matches("/user/login|/user/forgotPassword|/user/signup")) {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }

        // Always reset for this request/thread first -- a token-less
        // request must never see a previous request's leftovers.
        claimsHolder.remove();
        userNameHolder.remove();

        try {
            String authorizationHeader = httpServletRequest.getHeader("Authorization");
            String token = null;
            String userName = null;

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                token = authorizationHeader.substring(7);
                userName = jwtUtil.extractUsername(token);
                userNameHolder.set(userName);
                claimsHolder.set(jwtUtil.extractAllClaims(token));
            }

            if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = service.loadUserByUsername(userName);
                if (jwtUtil.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }
            }
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        } finally {
            claimsHolder.remove();
            userNameHolder.remove();
        }
    }
    public boolean isAdmin(){
        Claims claims = claimsHolder.get();
        return claims != null && "admin".equalsIgnoreCase((String) claims.get("role"));
    }

    public boolean isUser(){
        Claims claims = claimsHolder.get();
        return claims != null && "user".equalsIgnoreCase((String) claims.get("role"));
    }

    public String getCurrentUser(){
        return userNameHolder.get();
    }
}
