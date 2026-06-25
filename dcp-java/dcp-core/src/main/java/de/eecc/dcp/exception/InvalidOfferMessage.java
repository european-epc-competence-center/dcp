package de.eecc.dcp.exception;

public record InvalidOfferMessage(String detail) implements DcpError {

    @Override
    public String message() {
        return detail;
    }

    @Override
    public int suggestedHttpStatus() {
        return 400;
    }
}
