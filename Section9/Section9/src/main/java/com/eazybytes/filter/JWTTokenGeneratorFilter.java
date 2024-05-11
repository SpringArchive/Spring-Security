package com.eazybytes.filter;

import com.eazybytes.constants.SecurityConstants;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

// JWT 생성 필
public class JWTTokenGeneratorFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // 인증 유저 정보 가져오기
        if (null != authentication) {
            SecretKey key = Keys.hmacShaKeyFor(SecurityConstants.JWT_KEY.getBytes(StandardCharsets.UTF_8));
            String jwt = Jwts.builder().setIssuer("Eazy Bank").setSubject("JWT Token")
                    .claim("username", authentication.getName())    // username 담기
                    .claim("authorities", populateAuthorities(authentication.getAuthorities())) // Authorization 담기
                    .setIssuedAt(new Date())    // 생성일
                    .setExpiration(new Date((new Date()).getTime() + 30000000)) // 만료일
                    .signWith(key).compact(); // JWT key 서명 + JWT 생성
            response.setHeader(SecurityConstants.JWT_HEADER, jwt);  // 응답 객체의 헤더에 JWT 담기
        }

        filterChain.doFilter(request, response); // 다음 필터로
    }

    @Override   // 이 필터는 오로지 로그인 과정에서만 실행되도록 하는 메서드, 후속 요청에서 토큰이 계속 생성되는 것을 방지
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return !request.getServletPath().equals("/user");   // "/user":  로그인 주소, 이 주소로 들어올 때만 허
    }

    // 내 모든 권한 읽어오기
    private String populateAuthorities(Collection<? extends GrantedAuthority> collection) {
        Set<String> authoritiesSet = new HashSet<>();
        for (GrantedAuthority authority : collection) {
            authoritiesSet.add(authority.getAuthority());
        }

        return String.join(",", authoritiesSet);
    }
}
