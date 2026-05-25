package com.csh.security;

import com.csh.common.constants.Role;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Value("${csh.jwt.header}")
    private String header;

    @Value("${csh.jwt.prefix}")
    private String prefix;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String auth = request.getHeader(header);
        if (auth != null && auth.startsWith(prefix)) {
            String token = auth.substring(prefix.length()).trim();
            try {
                Claims claims = jwtUtil.parse(token);
                Long uid = Long.valueOf(claims.getSubject());
                String username = claims.get("username", String.class);
                String roleStr = claims.get("role", String.class);
                Role role = Role.valueOf(roleStr);
                LoginUser user = new LoginUser(uid, username, null, role, null, 1);
                UsernamePasswordAuthenticationToken token2 =
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(token2);
            } catch (Exception e) {
                log.debug("JWT parse failed: {}", e.getMessage());
            }
        }
        chain.doFilter(request, response);
    }
}
