package de.eecc.dcp.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DCP {@code CredentialMessage} wire DTO for credential delivery to a holder Credential Service.
 *
 * @see <a href="https://eclipse-dataspace-dcp.github.io/decentralized-claims-protocol/v1.0.1/resources/issuance/credential-message-schema.json">credential-message-schema.json</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CredentialMessage(
        @JsonProperty("@context") List<Object> context,
        String type,
        String issuerPid,
        String holderPid,
        List<CredentialContainer> credentials,
        String format,
        String status,
        String rejectionReason) {
}
