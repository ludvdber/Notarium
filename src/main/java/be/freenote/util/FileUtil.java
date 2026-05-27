package be.freenote.util;

public final class FileUtil {

    private FileUtil() {
    }

    public static String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "unnamed";
        }
        return fileName.replaceAll("[^a-zA-Z0-9.\\-_]", "_");
    }
}
