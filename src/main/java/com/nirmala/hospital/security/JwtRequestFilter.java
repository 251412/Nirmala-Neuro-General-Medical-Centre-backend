package com.nirmala.hospital.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final AdminDetailsService adminDetailsService;
    private final JwtTokenUtil jwtTokenUtil;

    public JwtRequestFilter(AdminDetailsService adminDetailsService, JwtTokenUtil jwtTokenUtil) {
        this.adminDetailsService = adminDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String jwtToken = null;
        String email = null;

        // Try to get token from Authorization header first
        final String requestTokenHeader = request.getHeader("Authorization");
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
        } else {
            // Alternatively, try to get from cookies
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("token".equals(cookie.getName())) {
                        jwtToken = cookie.getValue();
                        break;
                    }
                }
            }
        }

        if (jwtToken != null) {
            try {
                email = jwtTokenUtil.getEmailFromToken(jwtToken);
            } catch (Exception e) {
                logger.warn("Unable to parse JWT Token or token has expired");
            }
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.adminDetailsService.loadUserByUsername(email);

            if (jwtTokenUtil.validateToken(jwtToken, userDetails.getUsername())) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        chain.doFilter(request, response);
    }
}
