package de.eecc.dcp.client;

import de.eecc.dcp.message.CredentialRequestMessage;

/**
 * Sends credential requests to an issuer service.
 */
public interface IssuerServiceClient {

    /**
     * POST {@code /issuance} (or issuer-specific path) with a {@link CredentialRequestMessage}.
     *
     * @return issuer process id ({@code issuerPid}) when returned by the service
     */
    String requestCredentials(String issuerServiceUrl, CredentialRequestMessage request, String bearerToken);
}
