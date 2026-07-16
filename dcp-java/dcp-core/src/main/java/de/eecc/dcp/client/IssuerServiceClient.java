package de.eecc.dcp.client;

import de.eecc.dcp.api.DcpEndpointPaths;
import de.eecc.dcp.api.DcpUrls;
import de.eecc.dcp.message.CredentialMessage;
import de.eecc.dcp.message.CredentialRequestMessage;

/**
 * Sends credential requests and deliveries between Issuer Service and Credential Service.
 */
public interface IssuerServiceClient {

    /**
     * POST {@link CredentialRequestMessage} to the Issuer Service.
     *
     * @return issuer process id ({@code issuerPid}) when returned by the service
     */
    String requestCredentials(
            String issuerServiceBaseUrl,
            DcpEndpointPaths paths,
            CredentialRequestMessage request,
            String bearerToken);

    default String requestCredentials(
            String issuerServiceBaseUrl, CredentialRequestMessage request, String bearerToken) {
        return requestCredentials(issuerServiceBaseUrl, DcpEndpointPaths.builder().build(), request, bearerToken);
    }

    /** POST {@link CredentialMessage} delivery to the holder Credential Service. */
    void deliverCredentials(
            String credentialServiceBaseUrl,
            DcpEndpointPaths paths,
            CredentialMessage message,
            String bearerToken);

    default void deliverCredentials(
            String credentialServiceBaseUrl, CredentialMessage message, String bearerToken) {
        deliverCredentials(credentialServiceBaseUrl, DcpEndpointPaths.builder().build(), message, bearerToken);
    }

    default String issuerRequestUrl(String issuerServiceBaseUrl, DcpEndpointPaths paths) {
        return DcpUrls.join(issuerServiceBaseUrl, paths.getIssuerRequest());
    }

    default String credentialDeliveryUrl(String credentialServiceBaseUrl, DcpEndpointPaths paths) {
        return DcpUrls.join(credentialServiceBaseUrl, paths.getCredentialDelivery());
    }
}
