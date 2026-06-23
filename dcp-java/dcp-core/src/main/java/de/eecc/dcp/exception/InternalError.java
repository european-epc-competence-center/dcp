package de.eecc.dcp.exception;

public record InternalError(String detail) implements DcpError {

    public InternalError(String detail) {
        this.detail = detail;
    }

    @Override
    public String message() {
        return detail;
    }

    @Override
    public int suggestedHttpStatus() {
        return 500;
    }
}
