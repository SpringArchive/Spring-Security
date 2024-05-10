package com.eazybytes.config;

import com.eazybytes.filter.CsrfCookieFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;

@Configuration
public class ProjectSecurityConfig {

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();   // CsrfTokenRequestHandler 객체를 구현하기 위한 객체
        requestHandler.setCsrfRequestAttributeName("_csrf");    // 위 줄은 핸들러가 디폴트로 지원하지만 가독성을 높이기 위함

        http.securityContext((context) -> context
                        .requireExplicitSave(false))    // 사용자가 세션 데이터를 명시적으로 저장해야만 변경 사항이 영구적으로 유지되도록 하는 설정
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))    // 내가 만든 SessionManagement를 따라서 JSessionID를 만드는 설정 -> 첫 로그인 이후에 계속될 자격증명 요청을 해결하기 위해
                .cors(corsCustomizer -> corsCustomizer.configurationSource(request -> { // 인터페이스 메서드(getCorsConfiguration(HttpServletRequest request)) 오버라이딩
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(Collections.singletonList("http://localhost:4200"));   // 위 주소(프론트 URL)로 들어오는 요청은 CORS allow
                    config.setAllowedMethods(Collections.singletonList("*"));   // 스픠링 코드 내의 모든 메서드 allow
                    config.setAllowCredentials(true);   // 인증 정보 allow
                    config.setAllowedHeaders(Collections.singletonList("*"));   // 모든 헤더 allow
                    config.setMaxAge(3600L);    // 캐싱 기한: 1시간
                    return config;
                })).csrf((csrf) -> csrf.csrfTokenRequestHandler(requestHandler)
                        .ignoringRequestMatchers("/contact", "/register")   // 공공 API CSRF 해제, 하지만 /notices와 같은 공공 API는 GET으로 정보를 불러와야 할 경우이므로 CSRF 방어를 해야 함
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))    // CookieCsrfTokenRepository는 Csrf 토큰을 쿠키로 유지하는 역할, withHttpOnlyFalse는 쿠키를 HTTP Only로 설정하지 않고, JS를 통해 쿠키에 접근할 수 있게 하여 토큰을 읽을 수 있게 하는 역할
                        .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)    // 내가 만든 필터를 BasicAuthenticationFilter 뒤에 연결
                .authorizeHttpRequests((requests)->requests
//                        .requestMatchers("/myAccount").hasAuthority("VIEWACCOUNT")    // Authority
//                        .requestMatchers("/myBalance").hasAnyAuthority("VIEWACCOUNT", "VIEWBALANCE")
//                        .requestMatchers("/myLoans").hasAuthority("VIEWLOANS")
//                        .requestMatchers("/myCards").hasAuthority("VIEWCARDS")
                        .requestMatchers("/myAccount").hasRole("USER")
                        .requestMatchers("/myBalance").hasAnyRole("ADMIN", "USER")  // ROLE: ROLE은 ROLE_ 접두사가 반드시 있어야하며 URL 지정할 땐 접두사를 제외한 단어만 지정해야 함
                        .requestMatchers("/myLoans").hasRole("USER")
                        .requestMatchers("/myCards").hasRole("USER")
                        .requestMatchers("/user").authenticated()
                        .requestMatchers("/notices", "/contact", "/register").permitAll())  // Public API URL
                .formLogin(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
