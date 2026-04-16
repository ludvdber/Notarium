package be.freenote.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class HashUtil {

    private HashUtil() {
    }

    public static String hashEmail(String email, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String salted = salt + email.toLowerCase().trim();
            byte[] hash = digest.digest(salted.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
