package de.eecc.dcp.exception;

/**
 * Unchecked exception for DCP processing failures.
 */
public class DcpException extends RuntimeException {

    private final DcpError error;

    public DcpException(DcpError error) {
        super(error.message());
        this.error = error;
    }

    public DcpException(DcpError error, Throwable cause) {
        super(error.message(), cause);
        this.error = error;
    }

    public DcpError error() {
        return error;
    }
}
