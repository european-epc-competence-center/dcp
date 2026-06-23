package de.eecc.dcp.client;

import de.eecc.dcp.message.PresentationQueryMessage;
import de.eecc.dcp.message.PresentationResponseMessage;

/**
 * Sends presentation queries to a holder Credential Service.
 */
public interface CredentialServiceClient {

    PresentationResponseMessage query(String credentialServiceUrl, PresentationQueryMessage query, String bearerToken);
}
