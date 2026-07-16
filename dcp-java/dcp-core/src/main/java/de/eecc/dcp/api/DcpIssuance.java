package de.eecc.dcp.api;

import de.eecc.dcp.issuance.CredentialOfferDefinition;
import de.eecc.dcp.message.CredentialMessage;
import de.eecc.dcp.message.CredentialOfferMessage;
import de.eecc.dcp.message.CredentialRequestMessage;
import de.eecc.dcp.issuance.IssuanceMessages;
import lombok.Getter;

/**
 * Issuer-side DCP credential offer protocol helpers.
 *
 * <p>This library builds and validates wire messages; the host application owns HTTP routes and
 * transports payloads between Issuer Service and Credential Service endpoints.
 *
 * <p>Typical offer flow:
 * <ol>
 *   <li>{@link #buildOfferMessage(CredentialOfferDefinition)} — POST to {@link #offersUrl(String)}</li>
 *   <li>Credential Service returns {@code holderPid}</li>
 *   <li>{@link #buildRequestMessage(CredentialOfferDefinition, String)} — POST to
 *       {@link #issuerRequestUrl(String)}</li>
 *   <li>Issuer delivers {@link CredentialMessage} to {@link #credentialDeliveryUrl(String)}</li>
 * </ol>
 */
@Getter
public final class DcpIssuance {

    private final DcpOptions options;

    private DcpIssuance(DcpOptions options) {
        this.options = options;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static DcpIssuance create(DcpOptions options) {
        return builder().options(options).build();
    }

    /** Resolves the Credential Service URL for inbound credential offers. */
    public String offersUrl(String credentialServiceBaseUrl) {
        return DcpUrls.join(credentialServiceBaseUrl, options.getPaths().getOffers());
    }

    /** Resolves the Credential Service URL for {@link CredentialMessage} delivery. */
    public String credentialDeliveryUrl(String credentialServiceBaseUrl) {
        return DcpUrls.join(credentialServiceBaseUrl, options.getPaths().getCredentialDelivery());
    }

    /** Resolves the Issuer Service URL for {@link CredentialRequestMessage} ingress. */
    public String issuerRequestUrl(String issuerServiceBaseUrl) {
        return DcpUrls.join(issuerServiceBaseUrl, options.getPaths().getIssuerRequest());
    }

    /** Builds the wire {@link CredentialOfferMessage} for outbound POST to the holder Credential Service. */
    public CredentialOfferMessage buildOfferMessage(CredentialOfferDefinition definition) {
        return definition.toOfferMessage();
    }

    /** Builds a redeem request after the holder Credential Service accepted an offer. */
    public CredentialRequestMessage buildRequestMessage(CredentialOfferDefinition definition, String holderPid) {
        return definition.toRequestMessage(holderPid);
    }

    /** Validates an inbound offer against the expected definition (holder Credential Service). */
    public void verifyOffer(CredentialOfferDefinition definition, CredentialOfferMessage message) {
        definition.assertOfferMatches(message);
    }

    /** Validates an inbound redeem request against the expected definition (Issuer Service). */
    public void verifyRequest(CredentialOfferDefinition definition, CredentialRequestMessage message) {
        definition.assertRequestMatches(message);
    }

    /** Validates a {@link CredentialMessage} delivered to the holder Credential Service. */
    public void verifyDelivery(CredentialMessage message) {
        IssuanceMessages.requireCredentialMessage(message);
    }

    /**
     * Validates an offer and delegates to the application after the holder Credential Service accepted it.
     *
     * @param holderPid process id returned by the Credential Service
     */
    public OfferFlowOutcome acceptOffer(
            CredentialOfferDefinition definition,
            CredentialOfferMessage message,
            String holderPid,
            OfferReceivedHandler handler) {
        verifyOffer(definition, message);
        return handler.onOfferAccepted(holderPid, message);
    }

    public static final class Builder {

        private DcpOptions options = DcpOptions.builder().build();

        public Builder options(DcpOptions options) {
            this.options = options;
            return this;
        }

        public DcpIssuance build() {
            return new DcpIssuance(options);
        }
    }
}
