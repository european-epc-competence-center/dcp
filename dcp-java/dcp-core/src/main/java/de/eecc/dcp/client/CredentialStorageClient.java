package de.eecc.dcp.client;

import de.eecc.dcp.message.CredentialOfferMessage;

/**
 * Delivers credential offers to a holder Credential Service.
 */
public interface CredentialStorageClient {

    /**
     * POST {@code /credentials} with a {@link CredentialOfferMessage}.
     *
     * @return holder process id ({@code holderPid}) when the service returns one (e.g. {@code Location} header body)
     */
    String deliverOffer(String credentialServiceUrl, CredentialOfferMessage offer, String bearerToken);
}
