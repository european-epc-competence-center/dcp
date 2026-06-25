package de.eecc.dcp.message;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Issued credential payload container in a {@link CredentialMessage}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CredentialContainer(String payload, String credentialType, String format) {
}
