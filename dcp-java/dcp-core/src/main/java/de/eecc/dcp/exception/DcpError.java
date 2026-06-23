package de.eecc.dcp.exception;

/** Typed DCP processing errors for host-specific HTTP or domain mapping. */
public sealed interface DcpError permits
        InternalError,
        InvalidSelfIssuedIdToken,
        CredentialServiceError,
        PresentationValidationFailed,
        UnknownSession {

    String message();

    int suggestedHttpStatus();
}
