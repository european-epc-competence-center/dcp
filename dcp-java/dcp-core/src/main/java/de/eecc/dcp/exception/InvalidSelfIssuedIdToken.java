package de.eecc.dcp.exception;

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
