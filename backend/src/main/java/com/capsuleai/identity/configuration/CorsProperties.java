package com.capsuleai.identity.configuration;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Browser origins allowed to call the API cross-origin ({@code capsuleai.cors.*}).
 * Each entry is an exact origin exactly as a browser sends it,
 * {@code scheme://host[:port]}: no wildcard, path, trailing slash or spelled-out
 * default port, any of which would never match. Empty means no other origin.
 */
@ConfigurationProperties("capsuleai.cors")
public record CorsProperties(List<String> allowedOrigins) {

    public CorsProperties {
        allowedOrigins = allowedOrigins == null ? List.of() : allowedOrigins.stream().map(String::strip).toList();
        allowedOrigins.forEach(CorsProperties::requireExactOrigin);
    }

    private static void requireExactOrigin(String allowedOrigin) {
        if (!isExactOrigin(allowedOrigin)) {
            throw new IllegalArgumentException("capsuleai.cors.allowed-origins entry '" + allowedOrigin
                    + "' is not an exact origin such as https://app.example.com"
                    + " (http or https, a host and an optional non-default port only)");
        }
    }

    private static boolean isExactOrigin(String allowedOrigin) {
        URI origin;
        try {
            origin = new URI(allowedOrigin);
        } catch (URISyntaxException notAUri) {
            return false;
        }
        String scheme = origin.getScheme();
        boolean webScheme = "https".equals(scheme) || "http".equals(scheme);
        boolean defaultPortSpelledOut = origin.getPort() == ("https".equals(scheme) ? 443 : 80);
        return webScheme
                && origin.getHost() != null
                && origin.getRawUserInfo() == null
                && !defaultPortSpelledOut
                && allowedOrigin.equals(scheme + "://" + origin.getRawAuthority());
    }
}
