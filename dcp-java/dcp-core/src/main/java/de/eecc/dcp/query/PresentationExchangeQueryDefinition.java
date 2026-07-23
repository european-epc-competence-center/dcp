package de.eecc.dcp.query;

import com.fasterxml.jackson.databind.JsonNode;
import de.eecc.dcp.exception.DcpException;
import de.eecc.dcp.exception.InvalidPresentationResponse;
import de.eecc.dcp.message.PresentationQueryMessage;
import de.eecc.dcp.message.PresentationResponseMessage;

import java.util.List;
import java.util.Objects;

/**
 * Presentation Exchange based query using a {@code presentationDefinition} payload.
 *
 * <p>Construct-X EDC's {@code CredentialQueryResolverImpl} does not yet support PE queries; this implementation
 * follows the DCP specification which requires {@code presentationSubmission} in the response.
 */
public final class PresentationExchangeQueryDefinition implements PresentationQueryDefinition {

    private final JsonNode presentationDefinition;
    private final List<Object> context;

    public PresentationExchangeQueryDefinition(JsonNode presentationDefinition) {
        this(presentationDefinition, QueryMessages.presentationExchangeContext());
    }

    public PresentationExchangeQueryDefinition(JsonNode presentationDefinition, List<Object> context) {
        this.presentationDefinition = Objects.requireNonNull(presentationDefinition, "presentationDefinition");
        this.context = List.copyOf(Objects.requireNonNull(context, "context"));
        QueryMessages.validatePresentationDefinitionOnly(null, presentationDefinition);
    }

    public JsonNode presentationDefinition() {
        return presentationDefinition;
    }

    @Override
    public PresentationQueryMessage toQueryMessage() {
        return QueryMessages.presentationExchangeQuery(context, presentationDefinition);
    }

    @Override
    public void assertResponseStructure(PresentationResponseMessage response) {
        QueryMessages.requireNonEmptyPresentations(response);
        QueryMessages.requirePresentationSubmission(response);
        assertDefinitionIdMatches(response.presentationSubmission());
    }

    private void assertDefinitionIdMatches(JsonNode presentationSubmission) {
        JsonNode definitionId = presentationSubmission.get("definition_id");
        if (definitionId == null || definitionId.isNull() || definitionId.asText().isBlank()) {
            throw new DcpException(new InvalidPresentationResponse(
                    "presentationSubmission.definition_id is required"));
        }

        JsonNode queryDefinitionId = presentationDefinition.get("id");
        if (queryDefinitionId != null
                && !queryDefinitionId.isNull()
                && !queryDefinitionId.asText().isBlank()
                && !definitionId.asText().equals(queryDefinitionId.asText())) {
            throw new DcpException(new InvalidPresentationResponse(
                    "presentationSubmission.definition_id does not match presentationDefinition.id"));
        }
    }
}
