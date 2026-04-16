package be.freenote.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    @InjectMocks private JwtAuthFilter jwtAuthFilter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateWithBearerToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken("valid-token")).thenReturn(42L);
        when(jwtTokenProvider.isVerified("valid-token")).thenReturn(false);
        when(jwtTokenProvider.getRole("valid-token")).thenReturn("USER");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo(42L);
        assertThat(auth.getAuthorities()).extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void shouldAuthenticateWithCookieWhenNoHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("jwt", "cookie-token")});
        when(jwtTokenProvider.validateToken("cookie-token")).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken("cookie-token")).thenReturn(10L);
        when(jwtTokenProvider.isVerified("cookie-token")).thenReturn(true);
        when(jwtTokenProvider.getRole("cookie-token")).thenReturn("USER");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo(10L);
        assertThat(auth.getAuthorities()).extracting("authority")
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_VERIFIED");
    }

    @Test
    void shouldSetAdminAuthority() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer admin-token");
        when(jwtTokenProvider.validateToken("admin-token")).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken("admin-token")).thenReturn(1L);
        when(jwtTokenProvider.isVerified("admin-token")).thenReturn(true);
        when(jwtTokenProvider.getRole("admin-token")).thenReturn("ADMIN");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth.getAuthorities()).extracting("authority")
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_VERIFIED", "ROLE_ADMIN");
    }

    @Test
    void shouldNotAuthenticateWithoutToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(null);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldNotAuthenticateWithInvalidToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer bad-token");
        when(jwtTokenProvider.validateToken("bad-token")).thenReturn(false);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldPreferHeaderOverCookie() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer header-token");
        // Cookie also present but should not be checked
        when(jwtTokenProvider.validateToken("header-token")).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken("header-token")).thenReturn(1L);
        when(jwtTokenProvider.isVerified("header-token")).thenReturn(false);
        when(jwtTokenProvider.getRole("header-token")).thenReturn("USER");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // getCookies should never be called since header was found
        verify(request, never()).getCookies();
    }

    @Test
    void shouldIgnoreNonJwtCookies() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(new Cookie[]{
                new Cookie("session", "abc"),
                new Cookie("XSRF-TOKEN", "xyz")
        });

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
