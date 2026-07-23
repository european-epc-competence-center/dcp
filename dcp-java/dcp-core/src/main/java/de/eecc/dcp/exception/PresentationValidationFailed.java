package de.eecc.dcp.exception;

/**
 * Verifiable Presentation / Credential validation failed under the applicable DCP profile
 * (signature, status, proof, schema, …).
 *
 * <p>Hosts perform cryptographic verification externally; throw this when mapping profile checks
 * to HTTP 400.
 *
 * @see <a href="https://github.com/eclipse-dataspace-dcp/decentralized-claims-protocol/blob/main/specifications/dcp.profiles.md">DCP profiles</a>
 */
public record PresentationValidationFailed(String detail) implements DcpError {

    @Override
    public String message() {
        return detail;
    }

    @Override
    public int suggestedHttpStatus() {
        return 400;
    }
}
