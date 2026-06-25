package de.eecc.dcp.api;

import de.eecc.dcp.message.CredentialOfferMessage;

/**
 * Application callback after a holder Credential Service accepts a credential offer.
 */
@FunctionalInterface
public interface OfferReceivedHandler {

    OfferFlowOutcome onOfferAccepted(String holderPid, CredentialOfferMessage offer);
}
