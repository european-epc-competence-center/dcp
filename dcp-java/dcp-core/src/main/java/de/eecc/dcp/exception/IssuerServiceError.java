package de.eecc.dcp.exception;

public record IssuerServiceError(String detail, int httpStatus) implements DcpError {

    public IssuerServiceError(String detail) {
        this(detail, 502);
    }

    @Override
    public String message() {
        return detail;
    }

    @Override
    public int suggestedHttpStatus() {
        return httpStatus >= 400 && httpStatus < 600 ? httpStatus : 502;
    }
}
