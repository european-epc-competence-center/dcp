package de.eecc.dcp.api;

import de.eecc.dcp.exception.DcpException;
import de.eecc.dcp.exception.InternalError;

/**
 * Joins a service base URL with a configured DCP path segment.
 */
public final class DcpUrls {

    private DcpUrls() {}

    public static String join(String baseUrl, String path) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new DcpException(new InternalError("service base URL must not be blank"));
        }
        if (path == null || path.isBlank()) {
            throw new DcpException(new InternalError("path must not be blank"));
        }
        String normalizedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return normalizedBase + normalizedPath;
    }
}
