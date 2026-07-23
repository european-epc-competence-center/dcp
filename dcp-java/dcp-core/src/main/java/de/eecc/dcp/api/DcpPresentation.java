package de.eecc.dcp.api;

import de.eecc.dcp.api.access.PresentationAccessPolicy;
import de.eecc.dcp.api.access.PresentationAccessRequest;
import de.eecc.dcp.claims.PresentationClaims;
import de.eecc.dcp.message.PresentationQueryMessage;
import de.eecc.dcp.message.PresentationResponseMessage;
import de.eecc.dcp.query.PresentationQueryDefinition;
import de.eecc.dcp.query.QueryMessages;
import lombok.Getter;

/**
 * DCP presentation protocol helpers (verifier outbound queries and holder inbound checks).
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
 *
 * <p>Typical holder Credential Service flow for {@code POST /presentations/query}:
 * <ol>
 *   <li>Validate the verifier SI token (host / external verifier) and read the verifier DID ({@code sub})</li>
 *   <li>{@link #verifyQueryMessage(PresentationQueryMessage, String)} — wire shape +
 *       {@link PresentationAccessPolicy}</li>
 *   <li>Assemble and return a {@link PresentationResponseMessage} (host responsibility)</li>
 * </ol>
 *
 * <p>Presentation delivery is automatic (no interactive user consent in DCP). Configure
 * {@link PresentationAccessPolicy} via {@link DcpOptions} so only trusted verifiers (and credential
 * types) receive presentations; the default is deny-all until allow rules are configured.
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

    /**
     * Validates inbound query wire shape only. Prefer
     * {@link #verifyQueryMessage(PresentationQueryMessage, String)} on holder Credential Services so
     * the configured {@link PresentationAccessPolicy} is applied.
     */
    public void verifyQueryMessage(PresentationQueryMessage message) {
        QueryMessages.requireQueryMessage(message);
    }

    /**
     * Validates an inbound query on a holder Credential Service: wire shape plus presentation access
     * policy (verifier DID and credential types from {@code vc.type} scopes). Pass the verifier DID
     * from the validated SI token ({@code sub} / {@code iss}).
     */
    public void verifyQueryMessage(PresentationQueryMessage message, String verifierDid) {
        assertPresentationAllowed(PresentationAccessRequest.from(verifierDid, message));
        QueryMessages.requireQueryMessage(message);
    }

    /**
     * Asserts that {@code request} is permitted under {@link DcpOptions#getPresentationAccess()}.
     */
    public void assertPresentationAllowed(PresentationAccessRequest request) {
        options.getPresentationAccess().requireAllowed(request);
    }

    /**
     * Asserts that {@code verifierDid} may receive presentations for any credential type under the
     * configured policy (useful before types are known).
     */
    public void assertVerifierAllowed(String verifierDid) {
        assertPresentationAllowed(PresentationAccessRequest.anyCredentials(verifierDid));
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
