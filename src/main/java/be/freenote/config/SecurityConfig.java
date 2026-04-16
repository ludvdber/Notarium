package be.freenote.config;

import be.freenote.security.JwtAuthFilter;
import be.freenote.security.CustomOAuth2UserService;
import be.freenote.security.OAuth2LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // CSRF: use cookie-based token so the SPA can read it and send it back as header
        var csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        var csrfHandler = new CsrfTokenRequestAttributeHandler();
        csrfHandler.setCsrfRequestAttributeName(null); // disable deferred loading

        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfTokenRepository)
                .csrfTokenRequestHandler(csrfHandler)
                // Exempt webhook + public GET endpoints from CSRF
                .ignoringRequestMatchers(
                    "/api/webhooks/**",
                    "/api/auth/**",
                    "/api/dev/**"
                )
            )
            .cors(cors -> cors.configure(http))
            .headers(headers -> headers
                .cacheControl(cache -> cache.disable()) // Controllers set Cache-Control explicitly where needed
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives(
                        "default-src 'self'; " +
                        "script-src 'self'; " +
                        "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                        "font-src 'self' https://fonts.gstatic.com; " +
                        "img-src 'self' data: blob:; " +
                        "connect-src 'self'; " +
                        "frame-ancestors 'none'; " +
                        "base-uri 'self'; " +
                        "form-action 'self'"
                    )
                )
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/auth/logout").permitAll()
                .requestMatchers("/api/dev/**").permitAll()
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/sections").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/courses").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/stats").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/leaderboard").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/news").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/documents/search").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/documents/popular").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/documents/{id}").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/documents/{id}/ratings/average").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/professors").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/delegates").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/delegates/user/{userId}").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/users/me").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/users/featured").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/users/{id}").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/webhooks/kofi").permitAll()

                // Admin endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // Verified-only endpoints
                .requestMatchers(HttpMethod.POST, "/api/documents").hasRole("VERIFIED")
                .requestMatchers(HttpMethod.GET, "/api/documents/{id}/file").hasRole("VERIFIED")
                .requestMatchers(HttpMethod.POST, "/api/documents/{docId}/ratings").hasRole("VERIFIED")
                .requestMatchers(HttpMethod.POST, "/api/documents/{documentId}/reports").hasRole("VERIFIED")

                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write(
                            "{\"status\":401,\"message\":\"Authentication required\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write(
                            "{\"status\":403,\"message\":\"Access denied\"}");
                })
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
                .successHandler(oAuth2LoginSuccessHandler)
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
