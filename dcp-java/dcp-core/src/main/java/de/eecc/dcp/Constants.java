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

    public static final String CREDENTIAL_SERVICE_TYPE = "CredentialService";
    public static final String PRESENTATIONS_QUERY_PATH = "/presentations/query";
    public static final String CREDENTIALS_PATH = "/credentials";

    public static final String PROFILE_VC20_BSSL_JWT = "vc20-bssl/jwt";
    public static final String PROFILE_VC11_SL2021_JWT = "vc11-sl2021/jwt";

    public static final int DEFAULT_SESSION_TTL_SECONDS = 300;

    private Constants() {}
}
