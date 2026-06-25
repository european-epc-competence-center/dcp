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

    /** Extracts claims from verified presentations. */
    PresentationClaims extractPresentationClaims(PresentationResponseMessage response);
}
