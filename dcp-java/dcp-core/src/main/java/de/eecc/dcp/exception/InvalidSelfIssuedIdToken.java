package de.eecc.dcp.exception;

/**
 * Self-Issued ID Token parse or validation failure (e.g. {@code iss}≠{@code sub}, bad signature,
 * {@code aud}/{@code exp}/{@code jti}).
 *
 * <p>Hosts validate SI tokens externally; throw this when mapping DCP token checks to HTTP 401.
 *
 * @see <a href="https://eclipse-dataspace-dcp.github.io/decentralized-claims-protocol/v1.0.1/#validating-self-issued-id-tokens">Validating SI tokens</a>
 */
public record InvalidSelfIssuedIdToken(String detail) implements DcpError {

    @Override
    public String message() {
        return detail;
    }

    @Override
    public int suggestedHttpStatus() {
        return 401;
    }
}
