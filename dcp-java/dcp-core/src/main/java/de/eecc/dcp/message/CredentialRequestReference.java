package de.eecc.dcp.message;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Credential object reference in a {@link CredentialRequestMessage}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CredentialRequestReference(String id) {
}
