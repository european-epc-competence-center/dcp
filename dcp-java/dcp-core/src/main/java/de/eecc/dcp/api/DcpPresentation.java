package de.eecc.dcp.api;

import de.eecc.dcp.claims.PresentationClaims;
import de.eecc.dcp.message.PresentationQueryMessage;
import de.eecc.dcp.message.PresentationResponseMessage;
import de.eecc.dcp.query.PresentationQueryDefinition;
import de.eecc.dcp.query.QueryMessages;
import lombok.Getter;

/**
 * Verifier-side DCP presentation protocol helpers.
 *
 * <p>This library builds and validates wire messages; the host application owns HTTP routes and
 * transports payloads to and from Credential Services (mirrors oid4vp's separation of protocol logic
 * from REST controllers).
 *
 * <p>Typical verifier flow:
 * <ol>
 *   <li>{@link #buildQueryMessage(PresentationQueryDefinition)} — serialize and POST to
 *       {@link #presentationsQueryUrl(String)} (app supplies Bearer SI token)</li>
 *   <li>{@link #verifyAndExtractClaims(PresentationQueryDefinition, PresentationResponseMessage)}
 *       — validate response shape and read claims</li>
 * </ol>
 */
@Getter
public final class DcpPresentation {

    private final DcpOptions options;

    private DcpPresentation(DcpOptions options) {
        this.options = options;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static DcpPresentation create(DcpOptions options) {
        return builder().options(options).build();
    }

    /** Resolves {@code {credentialServiceUrl}/presentations/query} using configured paths. */
    public String presentationsQueryUrl(String credentialServiceBaseUrl) {
        return DcpUrls.join(credentialServiceBaseUrl, options.getPaths().getPresentationsQuery());
    }

    /** Builds the wire {@link PresentationQueryMessage} for outbound POST to the Credential Service. */
    public PresentationQueryMessage buildQueryMessage(PresentationQueryDefinition definition) {
        return definition.toQueryMessage();
    }

    /** Validates an inbound query on a holder Credential Service. */
    public void verifyQueryMessage(PresentationQueryMessage message) {
        QueryMessages.requireQueryMessage(message);
    }

    /** Validates that a Credential Service response matches the query definition. */
    public void verifyResponse(PresentationQueryDefinition definition, PresentationResponseMessage response) {
        definition.assertResponseMatches(response);
    }

    /** Validates the response and extracts claims in one step. */
    public PresentationClaims verifyAndExtractClaims(
            PresentationQueryDefinition definition, PresentationResponseMessage response) {
        definition.assertResponseMatches(response);
        return definition.extractPresentationClaims(response);
    }

    public static final class Builder {

        private DcpOptions options = DcpOptions.builder().build();

        public Builder options(DcpOptions options) {
            this.options = options;
            return this;
        }

        public DcpPresentation build() {
            return new DcpPresentation(options);
        }
    }
}
