package de.eecc.dcp.issuance;

import de.eecc.dcp.message.CredentialOfferMessage;
import de.eecc.dcp.message.CredentialRequestMessage;

/**
 * Describes what an issuer offers in a DCP credential issuance exchange.
 *
 * <p>Mirror of {@link de.eecc.dcp.query.PresentationQueryDefinition} for the offer side of CIP.
 */
public interface CredentialOfferDefinition {

    /** Builds the wire offer message for {@code POST /credentials} on the holder Credential Service. */
    CredentialOfferMessage toOfferMessage();

    /**
     * Builds a redeem request referencing offered credential object ids.
     *
     * @param holderPid holder process id assigned when the offer was accepted
     */
    CredentialRequestMessage toRequestMessage(String holderPid);

    /** Validates that an incoming offer structurally matches what was defined. */
    void assertOfferMatches(CredentialOfferMessage message);
}
