package de.eecc.dcp.client;

import de.eecc.dcp.api.DcpEndpointPaths;
import de.eecc.dcp.api.DcpUrls;
import de.eecc.dcp.message.PresentationQueryMessage;
import de.eecc.dcp.message.PresentationResponseMessage;

/**
 * Sends presentation queries to a holder Credential Service.
 *
 * <p>Implementations should POST to {@code DcpUrls.join(credentialServiceBaseUrl, paths.getPresentationsQuery())}.
 * The host application may also call {@link de.eecc.dcp.api.DcpPresentation#presentationsQueryUrl(String)} directly
 * and transport the {@link PresentationQueryMessage} JSON itself.
 */
public interface CredentialServiceClient {

    PresentationResponseMessage query(
            String credentialServiceBaseUrl,
            DcpEndpointPaths paths,
            PresentationQueryMessage query,
            String bearerToken);

    /** Convenience when spec-default paths apply. */
    default PresentationResponseMessage query(
            String credentialServiceBaseUrl, PresentationQueryMessage query, String bearerToken) {
        return query(credentialServiceBaseUrl, DcpEndpointPaths.builder().build(), query, bearerToken);
    }

    /** Resolves the target URL for a query without performing HTTP. */
    default String queryUrl(String credentialServiceBaseUrl, DcpEndpointPaths paths) {
        return DcpUrls.join(credentialServiceBaseUrl, paths.getPresentationsQuery());
    }
}
