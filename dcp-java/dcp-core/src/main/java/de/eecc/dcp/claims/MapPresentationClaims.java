package de.eecc.dcp.claims;

import java.util.Map;

/**
 * Simple {@link PresentationClaims} backed by a map.
 */
public record MapPresentationClaims(Map<String, Object> claimValues) implements PresentationClaims {
}
