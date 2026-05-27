package be.freenote.security;

import be.freenote.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Re-checks the admin role against the database for every /api/admin/** request.
 * JWT claims can be 24h stale — demoted admins must lose access immediately,
 * not when their token expires.
 */
@Component
@RequiredArgsConstructor
public class AdminRoleVerificationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/admin/");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long userId)) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean stillAdmin = userRepository.findById(userId)
                .map(u -> "ADMIN".equals(u.getRole()))
                .orElse(false);

        if (!stillAdmin) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"status\":403,\"message\":\"Access denied\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
