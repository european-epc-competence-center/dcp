package de.eecc.dcp.client;

import de.eecc.dcp.api.DcpEndpointPaths;
import de.eecc.dcp.api.DcpUrls;
import de.eecc.dcp.message.CredentialOfferMessage;

/**
 * Delivers credential offers to a holder Credential Service.
 *
 * <p>Implementations should POST to {@code DcpUrls.join(credentialServiceBaseUrl, paths.getOffers())}.
 */
public interface CredentialStorageClient {

    /**
     * POST offer payload to the holder Credential Service.
     *
     * @return holder process id ({@code holderPid}) when the service returns one
     */
    String deliverOffer(
            String credentialServiceBaseUrl,
            DcpEndpointPaths paths,
            CredentialOfferMessage offer,
            String bearerToken);

    default String deliverOffer(String credentialServiceBaseUrl, CredentialOfferMessage offer, String bearerToken) {
        return deliverOffer(credentialServiceBaseUrl, DcpEndpointPaths.builder().build(), offer, bearerToken);
    }

    default String offersUrl(String credentialServiceBaseUrl, DcpEndpointPaths paths) {
        return DcpUrls.join(credentialServiceBaseUrl, paths.getOffers());
    }
}
