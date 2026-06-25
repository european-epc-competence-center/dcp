package de.eecc.dcp.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.eecc.dcp.Constants;
import de.eecc.dcp.exception.DcpException;
import de.eecc.dcp.exception.InvalidPresentationResponse;
import de.eecc.dcp.exception.InvalidQueryMessage;
import de.eecc.dcp.message.PresentationQueryMessage;
import de.eecc.dcp.message.PresentationResponseMessage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PresentationExchangeQueryDefinitionTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void buildsPresentationExchangeQueryMessage() {
        ObjectNode presentationDefinition = mapper.createObjectNode()
                .put("id", "test-pd")
                .put("name", "Test");

        PresentationExchangeQueryDefinition definition =
                new PresentationExchangeQueryDefinition(presentationDefinition);

        PresentationQueryMessage message = definition.toQueryMessage();

        assertThat(message.type()).isEqualTo(Constants.MESSAGE_TYPE_PRESENTATION_QUERY);
        assertThat(message.context()).contains(Constants.PRESENTATION_EXCHANGE_SUBMISSION_CONTEXT);
        assertThat(message.scope()).isNull();
        assertThat(message.presentationDefinition()).isEqualTo(presentationDefinition);
    }

    @Test
    void rejectsMissingPresentationDefinition() {
        assertThatThrownBy(() -> new PresentationExchangeQueryDefinition(mapper.createObjectNode()))
                .isInstanceOf(DcpException.class)
                .satisfies(ex -> assertThat(((DcpException) ex).error()).isInstanceOf(InvalidQueryMessage.class));
    }

    @Test
    void assertResponseMatchesRequiresPresentationSubmission() {
        PresentationExchangeQueryDefinition definition = new PresentationExchangeQueryDefinition(
                mapper.createObjectNode().put("id", "test-pd"));

        PresentationResponseMessage responseWithoutSubmission = new PresentationResponseMessage(
                List.of(Constants.DCP_JSON_LD_CONTEXT),
                Constants.MESSAGE_TYPE_PRESENTATION_RESPONSE,
                List.of(mapper.createObjectNode().put("holder", "did:example:holder")),
                null);

        assertThatThrownBy(() -> definition.assertResponseMatches(responseWithoutSubmission))
                .isInstanceOf(DcpException.class)
                .satisfies(ex -> assertThat(((DcpException) ex).error()).isInstanceOf(InvalidPresentationResponse.class));
    }

    @Test
    void assertResponseMatchesChecksDefinitionId() {
        PresentationExchangeQueryDefinition definition = new PresentationExchangeQueryDefinition(
                mapper.createObjectNode().put("id", "test-pd"));

        ObjectNode submission = mapper.createObjectNode().put("definition_id", "other-pd");
        PresentationResponseMessage response = new PresentationResponseMessage(
                List.of(Constants.DCP_JSON_LD_CONTEXT),
                Constants.MESSAGE_TYPE_PRESENTATION_RESPONSE,
                List.of(mapper.createObjectNode()),
                submission);

        assertThatThrownBy(() -> definition.assertResponseMatches(response))
                .isInstanceOf(DcpException.class)
                .satisfies(ex -> assertThat(((DcpException) ex).error()).isInstanceOf(InvalidPresentationResponse.class));
    }

    @Test
    void assertResponseMatchesAcceptsMatchingDefinitionId() {
        PresentationExchangeQueryDefinition definition = new PresentationExchangeQueryDefinition(
                mapper.createObjectNode().put("id", "test-pd"));

        ObjectNode submission = mapper.createObjectNode().put("definition_id", "test-pd");
        PresentationResponseMessage response = new PresentationResponseMessage(
                List.of(Constants.DCP_JSON_LD_CONTEXT),
                Constants.MESSAGE_TYPE_PRESENTATION_RESPONSE,
                List.of(mapper.createObjectNode().put("holder", "did:example:holder")),
                submission);

        definition.assertResponseMatches(response);
    }
}
