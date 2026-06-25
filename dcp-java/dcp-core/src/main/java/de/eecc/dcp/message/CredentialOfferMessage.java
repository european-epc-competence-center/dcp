package de.eecc.dcp.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DCP {@code CredentialOfferMessage} wire DTO.
 *
 * <p>Issuers send this to a holder Credential Service at {@code POST /credentials}.
 *
 * @see <a href="https://eclipse-dataspace-dcp.github.io/decentralized-claims-protocol/v1.0.1/resources/issuance/credential-offer-message-schema.json">credential-offer-message-schema.json</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CredentialOfferMessage(
        @JsonProperty("@context") List<Object> context,
        String type,
        String issuer,
        List<CredentialObject> credentials) {
}
