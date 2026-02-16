package com.ssafy.global.filter;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ssafy.global.jwt.JwtTokenProvider;
import com.ssafy.global.security.CustomUserDetails;
import com.ssafy.global.security.CustomUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

        private final JwtTokenProvider jwtTokenProvider;
        private final CustomUserDetailsService userDetailsService;

        @Override
        protected void doFilterInternal(HttpServletRequest request,
                        HttpServletResponse response,
                        FilterChain filterChain)
                        throws ServletException, IOException {

                String header = request.getHeader("Authorization");

                if (header != null && header.startsWith("Bearer ")) {
                        String token = header.substring(7);

                        String username = jwtTokenProvider.getUsername(token);

                        // DB에서 사용자 정보 조회 → CustomUserDetails 반환
                        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService
                                        .loadUserByUsername(username);

                        // Authentication 생성
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                        userDetails, // principal
                                        null,
                                        userDetails.getAuthorities());

                        authentication.setDetails(
                                        new WebAuthenticationDetailsSource().buildDetails(request));

                        // 🔥 SecurityContext에 저장
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                }

                filterChain.doFilter(request, response);
        }

        @Override
        protected boolean shouldNotFilterAsyncDispatch() {
                return false;
        }
}
