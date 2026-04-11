package be.notarium.config;

import be.notarium.security.JwtAuthFilter;
import be.notarium.security.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configure(http))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
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
                .requestMatchers(HttpMethod.GET, "/api/users/featured").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/users/{id}").permitAll()

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
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
