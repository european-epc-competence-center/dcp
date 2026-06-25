package de.eecc.dcp.claims;

import java.util.List;
import java.util.Map;

/**
 * Claims extracted from verified DCP presentations.
 */
public interface PresentationClaims {

    /** Claim values keyed by claim id or path. */
    Map<String, Object> claimValues();

    /** Primary organisation or subject identifier when present. */
    default String identifier() {
        return null;
    }

    /** Human-readable name when present. */
    default String name() {
        return null;
    }

    /** Primary multi-valued claim when present (for example GS1 license values / GCPs). */
    default List<String> values() {
        return List.of();
    }

    /** Verifiable-credential type when known. */
    default String credentialType() {
        return null;
    }
}
