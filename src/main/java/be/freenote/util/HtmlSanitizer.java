package be.freenote.util;

/**
 * HTML-entity escaping for user-supplied text persisted then rendered in the SPA.
 * Single source of truth — services must call this instead of rolling their own escaper.
 */
public final class HtmlSanitizer {

    private HtmlSanitizer() {}

    /**
     * Trims the input, returns {@code null} when blank, and escapes the five characters
     * that could break out of an HTML/attribute context.
     */
    public static String escape(String input) {
        if (input == null) return null;
        String trimmed = input.trim();
        if (trimmed.isEmpty()) return null;
        return trimmed
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}
