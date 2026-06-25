package de.eecc.dcp.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DCP {@code CredentialRequestMessage} wire DTO.
 *
 * <p>Holders send this to an issuer to redeem a prior {@link CredentialOfferMessage}.
 *
 * @see <a href="https://eclipse-dataspace-dcp.github.io/decentralized-claims-protocol/v1.0.1/resources/issuance/credential-request-message-schema.json">credential-request-message-schema.json</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CredentialRequestMessage(
        @JsonProperty("@context") List<Object> context,
        String type,
        String holderPid,
        List<CredentialRequestReference> credentials) {
}
