package de.eecc.dcp.query;

import de.eecc.dcp.claims.PresentationClaims;
import de.eecc.dcp.message.PresentationQueryMessage;
import de.eecc.dcp.message.PresentationResponseMessage;

/**
 * Describes what a verifier asks for in a DCP presentation exchange.
 *
 * <p>Mirror of oid4vp's {@code PresentationRequestDefinition}.
 */
public interface PresentationQueryDefinition {

    PresentationQueryMessage toQueryMessage();

    /** Validates that the response structurally matches what was requested (scope or Presentation Exchange). */
    void assertResponseMatches(PresentationResponseMessage response);

    /**
     * Validates the response then extracts claims from presentations.
     *
     * <p>Override when extraction is template-specific (e.g. GS1 license claims).
     */
    default PresentationClaims extractPresentationClaims(PresentationResponseMessage response) {
        assertResponseMatches(response);
        return PresentationClaimExtractor.extract(response.presentation());
    }
}
