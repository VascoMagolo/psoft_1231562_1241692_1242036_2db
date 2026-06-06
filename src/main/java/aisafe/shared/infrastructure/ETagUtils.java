package aisafe.shared.infrastructure;

public class ETagUtils {

    private ETagUtils() {
    }

    /**
     * Helper method to parse HTTP ETags securely.
     * Extracts the numeric version from standard ETag formats like "5" or W/"5".
     */
    public static Long parseVersion(String etag) {
        if (etag == null || etag.isBlank()) {
            throw new IllegalArgumentException(
                    "If-Match header is missing or empty. Please provide the current resource version.");
        }
        String clean = etag.replace("W/", "").replace("\"", "").trim();
        try {
            return Long.parseLong(clean);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid If-Match ETag format. Expected a numeric entity version.");
        }
    }
}
