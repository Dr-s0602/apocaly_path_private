package com.apocaly.apocaly_path_private.security.jwt.filter;

import com.apocaly.apocaly_path_private.security.jwt.util.JWTUtil;
import com.apocaly.apocaly_path_private.user.model.entity.User;
import com.apocaly.apocaly_path_private.user.model.output.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");

        if( authorization == null || !authorization.startsWith("Bearer ")){
            filterChain.doFilter(request,response);
            return;
        }

        String token = authorization.split(" ")[1];

        if(jwtUtil.isTokenExpired(token)){
            filterChain.doFilter(request,response);
            return;
        }

        String userEmail = jwtUtil.getUserEmailFromToken(token);
        boolean is_admin = jwtUtil.isAdminFromToken(token);

        User user = new User();
        user.setEmail(userEmail);
        user.setPassword("tempPassword");
        user.setIsAdmin(is_admin);

        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request,response);
    }
}
