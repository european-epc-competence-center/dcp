package de.eecc.dcp.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * DCP {@code PresentationQueryMessage} wire DTO.
 *
 * @see <a href="https://eclipse-dataspace-dcp.github.io/decentralized-claims-protocol/v1.0.1/resources/presentation/presentation-query-message-schema.json">presentation-query-message-schema.json</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PresentationQueryMessage(
        @JsonProperty("@context") List<Object> context,
        String type,
        List<String> scope,
        JsonNode presentationDefinition) {
}
