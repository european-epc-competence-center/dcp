package de.eecc.dcp.exception;

/** Typed DCP processing errors for host-specific HTTP or domain mapping. */
public sealed interface DcpError permits
        InternalError,
        InvalidSelfIssuedIdToken,
        CredentialServiceError,
        IssuerServiceError,
        InvalidQueryMessage,
        InvalidOfferMessage,
        InvalidCredentialRequest,
        InvalidPresentationResponse,
        EmptyPresentationClaims,
        PresentationValidationFailed,
        PresentationAccessDenied {

    String message();

    int suggestedHttpStatus();
}
