package de.eecc.dcp.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * DCP {@code PresentationResponseMessage} wire DTO.
 *
 * @see <a href="https://eclipse-dataspace-dcp.github.io/decentralized-claims-protocol/v1.0.1/resources/presentation/presentation-response-message-schema.json">presentation-response-message-schema.json</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PresentationResponseMessage(
        @JsonProperty("@context") List<Object> context,
        String type,
        List<JsonNode> presentation,
        JsonNode presentationSubmission) {
}
