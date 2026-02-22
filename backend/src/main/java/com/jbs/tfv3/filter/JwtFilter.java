package com.jbs.tfv3.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.jbs.tfv3.service.JwtService;
import com.jbs.tfv3.service.UserDtlsService;
import com.jbs.tfv3.service.impl.TokenBlacklistServiceImpl;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDtlsService userDtlsService;

    @Autowired
    private TokenBlacklistServiceImpl tokenBlacklistServiceImpl;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String token = null;
        String email = null;

        logger.info("🔹 Incoming request URI: {}", request.getRequestURI());
        logger.info("🔹 Authorization header: {}", authHeader);

        // ---------------------------------------------------------------------
        // 1️⃣ Extract JWT token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            logger.info("✅ JWT token extracted");

            // -----------------------------------------------------------------
            // 2️⃣ Check if token is blacklisted
            if (tokenBlacklistServiceImpl.isTokenBlacklisted(token)) {
                logger.warn("🚫 Token is blacklisted — rejecting request");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            try {
                email = jwtService.extractEmail(token);
                logger.info("✅ Extracted email from token: {}", email);
            } catch (Exception e) {
                logger.error("❌ Error extracting email from JWT: {}", e.getMessage());
            }
        } else {
            logger.warn("⚠️ Authorization header missing or not starting with 'Bearer '");
        }

        // ---------------------------------------------------------------------
        // 3️⃣ Validate token and set Authentication
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            logger.info("🔹 Loading user details for email: {}", email);
            UserDetails userDetails = userDtlsService.loadUserByUsername(email);

            if (jwtService.validateToken(token, userDetails)) {
                String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));
                logger.info("✅ Token validated successfully. Role from JWT: {}", role);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, List.of(new SimpleGrantedAuthority(role)));

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                logger.info("🔹 Authentication set in SecurityContext: {}", SecurityContextHolder.getContext().getAuthentication());
            } else {
                logger.warn("❌ JWT token validation failed for user: {}", email);
            }
        } else if (email == null) {
            logger.warn("⚠️ No email extracted from token, skipping authentication setup");
        } else {
            logger.debug("🔸 Authentication already present in SecurityContext");
        }

        // ---------------------------------------------------------------------
        logger.info("➡️ Passing request to next filter. Auth in context: {}", SecurityContextHolder.getContext().getAuthentication());
        filterChain.doFilter(request, response);
    }
}
