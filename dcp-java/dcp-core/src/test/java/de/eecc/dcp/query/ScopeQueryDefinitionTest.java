package de.eecc.dcp.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.eecc.dcp.Constants;
import de.eecc.dcp.claims.PresentationClaims;
import de.eecc.dcp.exception.DcpException;
import de.eecc.dcp.exception.InvalidPresentationResponse;
import de.eecc.dcp.exception.InvalidQueryMessage;
import de.eecc.dcp.message.PresentationQueryMessage;
import de.eecc.dcp.message.PresentationResponseMessage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScopeQueryDefinitionTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void buildsScopeQueryMessage() {
        ScopeQueryDefinition definition = ScopeQueryDefinition.of(
                DcpScope.vcType("Member"),
                DcpScope.vcId("8247b87d-8d72-47e1-8128-9ce47e3d829d"));

        PresentationQueryMessage message = definition.toQueryMessage();

        assertThat(message.type()).isEqualTo(Constants.MESSAGE_TYPE_PRESENTATION_QUERY);
        assertThat(message.context()).contains(Constants.DCP_JSON_LD_CONTEXT);
        assertThat(message.scope()).containsExactly(
                "org.eclipse.dspace.dcp.vc.type:Member",
                "org.eclipse.dspace.dcp.vc.id:8247b87d-8d72-47e1-8128-9ce47e3d829d");
        assertThat(message.presentationDefinition()).isNull();
    }

    @Test
    void rejectsEmptyScopes() {
        assertThatThrownBy(() -> new ScopeQueryDefinition(List.of()))
                .isInstanceOf(DcpException.class)
                .satisfies(ex -> assertThat(((DcpException) ex).error()).isInstanceOf(InvalidQueryMessage.class));
    }

    @Test
    void assertResponseMatchesRequiresPresentationArray() {
        ScopeQueryDefinition definition = ScopeQueryDefinition.of(DcpScope.vcType("Member"));

        assertThatThrownBy(() -> definition.assertResponseMatches(emptyResponse()))
                .isInstanceOf(DcpException.class)
                .satisfies(ex -> assertThat(((DcpException) ex).error()).isInstanceOf(InvalidPresentationResponse.class));
    }

    @Test
    void extractPresentationClaimsFromObjectPresentation() {
        ScopeQueryDefinition definition = ScopeQueryDefinition.of(DcpScope.vcType("Member"));

        ObjectNode credential = mapper.createObjectNode()
                .put("type", "MemberCredential")
                .put("issuer", "did:example:issuer");
        ObjectNode presentation = mapper.createObjectNode();
        presentation.set("verifiableCredential", credential);

        PresentationResponseMessage response = new PresentationResponseMessage(
                List.of(Constants.DCP_JSON_LD_CONTEXT),
                Constants.MESSAGE_TYPE_PRESENTATION_RESPONSE,
                List.of(presentation),
                null);

        PresentationClaims claims = definition.extractPresentationClaims(response);

        assertThat(claims.claimValues())
                .containsEntry("presentation.0.verifiableCredential.type", "MemberCredential")
                .containsEntry("presentation.0.verifiableCredential.issuer", "did:example:issuer");
    }

    private PresentationResponseMessage emptyResponse() {
        return new PresentationResponseMessage(
                List.of(Constants.DCP_JSON_LD_CONTEXT),
                Constants.MESSAGE_TYPE_PRESENTATION_RESPONSE,
                List.of(),
                null);
    }
}
