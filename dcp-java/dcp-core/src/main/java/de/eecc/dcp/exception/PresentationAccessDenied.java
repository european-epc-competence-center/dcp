package de.eecc.dcp.exception;

/** Holder Credential Service rejected a presentation query under {@code PresentationAccessPolicy}. */
public record PresentationAccessDenied(String detail) implements DcpError {

    @Override
    public String message() {
        return detail;
    }

    @Override
    public int suggestedHttpStatus() {
        return 403;
    }
}
