package de.eecc.dcp.exception;

public record UnknownSession(String sessionId) implements DcpError {

    @Override
    public String message() {
        return "Unknown or expired presentation session: " + sessionId;
    }

    @Override
    public int suggestedHttpStatus() {
        return 404;
    }
}
