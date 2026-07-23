package de.eecc.dcp.query.template.constructx;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.eecc.dcp.Constants;
import de.eecc.dcp.exception.DcpException;
import de.eecc.dcp.message.PresentationQueryMessage;
import de.eecc.dcp.message.PresentationResponseMessage;
import de.eecc.dcp.query.DcpScope;
import de.eecc.dcp.query.PresentationQueryDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MembershipQueryDefinitionTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String ISSUER = "did:web:membership-issuer.example";

    @Test
    void toQueryMessage_requestsMembershipCredentialViaDcpScope() {
        PresentationQueryMessage message = MembershipQueryDefinition.INSTANCE.toQueryMessage();

        assertThat(message.type()).isEqualTo(Constants.MESSAGE_TYPE_PRESENTATION_QUERY);
        assertThat(message.scope()).containsExactly(
                "org.eclipse.dspace.dcp.vc.type:MembershipCredential");
        assertThat(message.presentationDefinition()).isNull();
    }

    @Test
    void scopesExposeNormativeVcTypeAlias() {
        assertThat(MembershipQueryDefinition.INSTANCE.scopes())
                .containsExactly(DcpScope.vcType(MembershipQueryDefinition.TYPE_MEMBERSHIP));
    }

    @Test
    void requiresIssuer_preservesTypeScopeAndExposesConstraint() {
        PresentationQueryDefinition query = MembershipQueryDefinition.INSTANCE.requiresIssuer(ISSUER);

        assertThat(query.toQueryMessage().scope()).containsExactly(
                "org.eclipse.dspace.dcp.vc.type:MembershipCredential");
        assertThat(query.requiredIssuers()).containsExactly(ISSUER);
        assertThat(query.root()).isSameAs(MembershipQueryDefinition.INSTANCE);
    }

    @Test
    void assertResponseMatches_acceptsMatchingIssuer() throws Exception {
        PresentationQueryDefinition query = MembershipQueryDefinition.INSTANCE.requiresIssuer(ISSUER);

        query.assertResponseMatches(responseWithIssuer(ISSUER));
    }

    @Test
    void assertResponseMatches_rejectsMismatchedIssuer() throws Exception {
        PresentationQueryDefinition query = MembershipQueryDefinition.INSTANCE.requiresIssuer(ISSUER);

        assertThatThrownBy(() -> query.assertResponseMatches(responseWithIssuer("did:web:other.example")))
                .isInstanceOf(DcpException.class)
                .hasMessageContaining("issuer");
    }

    private static PresentationResponseMessage responseWithIssuer(String issuer) throws Exception {
        JsonNode presentation = MAPPER.readTree("""
                {
                  "type": ["VerifiablePresentation"],
                  "verifiableCredential": [{
                    "type": ["VerifiableCredential", "MembershipCredential"],
                    "issuer": "%s",
                    "credentialSubject": { "id": "did:web:holder.example" }
                  }]
                }
                """.formatted(issuer));

        return new PresentationResponseMessage(
                List.of(Constants.DCP_JSON_LD_CONTEXT),
                Constants.MESSAGE_TYPE_PRESENTATION_RESPONSE,
                List.of(presentation),
                null);
    }
}
