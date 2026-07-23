package de.eecc.dcp;

/**
 * Cross-cutting DCP protocol constants used throughout the library.
 *
 * @see <a href="https://eclipse-dataspace-dcp.github.io/decentralized-claims-protocol/v1.0.1/">DCP v1.0.1</a>
 */
public final class Constants {

    public static final String DCP_JSON_LD_CONTEXT = "https://w3id.org/dspace-dcp/v1.0/dcp.jsonld";

    public static final String MESSAGE_TYPE_PRESENTATION_QUERY = "PresentationQueryMessage";
    public static final String MESSAGE_TYPE_PRESENTATION_RESPONSE = "PresentationResponseMessage";

    public static final String MESSAGE_TYPE_CREDENTIAL_OFFER = "CredentialOfferMessage";
    public static final String MESSAGE_TYPE_CREDENTIAL_REQUEST = "CredentialRequestMessage";
    public static final String MESSAGE_TYPE_CREDENTIAL_OBJECT = "CredentialObject";
    public static final String MESSAGE_TYPE_CREDENTIAL_MESSAGE = "CredentialMessage";

    public static final String CREDENTIAL_SERVICE_TYPE = "CredentialService";
    public static final String ISSUER_SERVICE_TYPE = "IssuerService";

    public static final String PRESENTATIONS_QUERY_PATH = "/presentations/query";
    /** Credential Service storage endpoint for issued {@code CredentialMessage} payloads. */
    public static final String CREDENTIALS_PATH = "/credentials";
    /** Credential Service offer endpoint per DCP v1.0.1. */
    public static final String OFFERS_PATH = "/offers";
    /** Issuer Service credential request endpoint per DCP v1.0.1. */
    public static final String ISSUER_CREDENTIALS_PATH = "/credentials";

    /** EDC-style offer path on Credential Service (override via {@link de.eecc.dcp.api.DcpEndpointPaths}). */
    public static final String LEGACY_OFFERS_PATH = "/credentials";
    /** EDC-style request path on Issuer Service (override via {@link de.eecc.dcp.api.DcpEndpointPaths}). */
    public static final String LEGACY_ISSUER_REQUEST_PATH = "/issuance";

    /**
     * @deprecated Use {@link #ISSUER_CREDENTIALS_PATH} or configure {@link de.eecc.dcp.api.DcpEndpointPaths#getIssuerRequest()}.
     */
    @Deprecated
    public static final String ISSUANCE_PATH = LEGACY_ISSUER_REQUEST_PATH;

    public static final String CREDENTIAL_STATUS_ISSUED = "ISSUED";
    public static final String CREDENTIAL_STATUS_REJECTED = "REJECTED";

    public static final String SCOPE_ALIAS_VC_TYPE = "org.eclipse.dspace.dcp.vc.type";
    public static final String SCOPE_ALIAS_VC_ID = "org.eclipse.dspace.dcp.vc.id";

    public static final String PRESENTATION_EXCHANGE_SUBMISSION_CONTEXT =
            "https://identity.foundation/presentation-exchange/submission/v1";

    public static final String DATA_URL_PREFIX = "data:";

    public static final String PROFILE_VC20_BSSL_JWT = "vc20-bssl/jwt";
    public static final String PROFILE_VC11_SL2021_JWT = "vc11-sl2021/jwt";

    private Constants() {}
}
