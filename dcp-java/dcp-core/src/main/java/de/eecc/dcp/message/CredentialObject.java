package de.eecc.dcp.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * DCP {@code CredentialObject} wire DTO embedded in {@link CredentialOfferMessage}.
 *
 * @see <a href="https://eclipse-dataspace-dcp.github.io/decentralized-claims-protocol/v1.0.1/resources/issuance/credential-object-schema.json">credential-object-schema.json</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CredentialObject(
        String id,
        String type,
        String credentialType,
        String credentialSchema,
        String offerReason,
        List<String> bindingMethods,
        String profile,
        JsonNode issuancePolicy) {
}
