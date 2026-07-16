package de.eecc.dcp.api;

import de.eecc.dcp.Constants;
import lombok.Builder;
import lombok.Getter;

/**
 * Relative HTTP paths for DCP Credential Service and Issuer Service endpoints.
 *
 * <p>Defaults match DCP v1.0.1. Override individual paths when your deployment uses alternate routes
 * (for example EDC Identity Hub with {@link #edcCompat()}).
 */
@Getter
@Builder
public class DcpEndpointPaths {

    /** Credential Service: {@code CredentialOfferMessage} ingress. Spec default {@code /offers}. */
    @Builder.Default
    private final String offers = Constants.OFFERS_PATH;

    /** Credential Service: {@code CredentialMessage} delivery after issuance. Spec default {@code /credentials}. */
    @Builder.Default
    private final String credentialDelivery = Constants.CREDENTIALS_PATH;

    /** Issuer Service: {@code CredentialRequestMessage} ingress. Spec default {@code /credentials}. */
    @Builder.Default
    private final String issuerRequest = Constants.ISSUER_CREDENTIALS_PATH;

    /** Credential Service: {@code PresentationQueryMessage} ingress. Spec default {@code /presentations/query}. */
    @Builder.Default
    private final String presentationsQuery = Constants.PRESENTATIONS_QUERY_PATH;

    /**
     * Paths commonly used by Eclipse Dataspace Components deployments: offers on {@code /credentials},
     * requests on {@code /issuance}.
     */
    public static DcpEndpointPaths edcCompat() {
        return DcpEndpointPaths.builder()
                .offers(Constants.LEGACY_OFFERS_PATH)
                .issuerRequest(Constants.LEGACY_ISSUER_REQUEST_PATH)
                .build();
    }
}
