package be.freenote.security;

import org.springframework.security.core.Authentication;

/**
 * Helpers around the authenticated principal. {@link JwtAuthFilter} stores the user's
 * {@code Long} id as the principal, so every controller that needs "the current user id"
 * goes through here instead of repeating the cast.
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    /** The authenticated user's id. Assumes the request passed {@link JwtAuthFilter}. */
    public static Long currentUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
