package de.eecc.dcp.issuance;

import de.eecc.dcp.Constants;
import de.eecc.dcp.exception.DcpException;
import de.eecc.dcp.exception.InvalidCredentialRequest;
import de.eecc.dcp.exception.InvalidOfferMessage;
import de.eecc.dcp.message.CredentialOfferMessage;
import de.eecc.dcp.message.CredentialRequestMessage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TypeCredentialOfferDefinitionTest {

    private static final String ISSUER = "did:web:issuer.example";
    private static final String OBJECT_ID = "urn:uuid:8247b87d-8d72-47e1-8128-9ce47e3d829d";

    @Test
    void buildsCredentialOfferMessage() {
        TypeCredentialOfferDefinition definition = TypeCredentialOfferDefinition.of(
                ISSUER,
                TypeCredentialOfferDefinition.OfferedCredential.ofType(
                        OBJECT_ID, "MembershipCredential", Constants.PROFILE_VC20_BSSL_JWT));

        CredentialOfferMessage message = definition.toOfferMessage();

        assertThat(message.type()).isEqualTo(Constants.MESSAGE_TYPE_CREDENTIAL_OFFER);
        assertThat(message.context()).contains(Constants.DCP_JSON_LD_CONTEXT);
        assertThat(message.issuer()).isEqualTo(ISSUER);
        assertThat(message.credentials()).hasSize(1);
        assertThat(message.credentials().getFirst().id()).isEqualTo(OBJECT_ID);
        assertThat(message.credentials().getFirst().credentialType()).isEqualTo("MembershipCredential");
        assertThat(message.credentials().getFirst().type()).isEqualTo(Constants.MESSAGE_TYPE_CREDENTIAL_OBJECT);
        assertThat(message.credentials().getFirst().profile()).isEqualTo(Constants.PROFILE_VC20_BSSL_JWT);
    }

    @Test
    void buildsCredentialRequestMessageFromOffer() {
        TypeCredentialOfferDefinition definition = TypeCredentialOfferDefinition.of(
                ISSUER, TypeCredentialOfferDefinition.OfferedCredential.ofType(OBJECT_ID, "MembershipCredential"));

        CredentialRequestMessage request = definition.toRequestMessage("holder-pid-1");

        assertThat(request.type()).isEqualTo(Constants.MESSAGE_TYPE_CREDENTIAL_REQUEST);
        assertThat(request.holderPid()).isEqualTo("holder-pid-1");
        assertThat(request.credentials()).extracting(ref -> ref.id()).containsExactly(OBJECT_ID);
    }

    @Test
    void rejectsEmptyCredentials() {
        assertThatThrownBy(() -> new TypeCredentialOfferDefinition(ISSUER, List.of()))
                .isInstanceOf(DcpException.class)
                .satisfies(ex -> assertThat(((DcpException) ex).error()).isInstanceOf(InvalidOfferMessage.class));
    }

    @Test
    void assertOfferMatchesValidatesIssuerAndCredentials() {
        TypeCredentialOfferDefinition definition = TypeCredentialOfferDefinition.of(
                ISSUER, TypeCredentialOfferDefinition.OfferedCredential.ofType(OBJECT_ID, "MembershipCredential"));

        definition.assertOfferMatches(definition.toOfferMessage());

        CredentialOfferMessage wrongIssuer = new CredentialOfferMessage(
                List.of(Constants.DCP_JSON_LD_CONTEXT),
                Constants.MESSAGE_TYPE_CREDENTIAL_OFFER,
                "did:web:other.example",
                definition.toOfferMessage().credentials());

        assertThatThrownBy(() -> definition.assertOfferMatches(wrongIssuer))
                .isInstanceOf(DcpException.class)
                .satisfies(ex -> assertThat(((DcpException) ex).error()).isInstanceOf(InvalidOfferMessage.class));
    }

    @Test
    void assertRequestMatchesRejectsUnknownCredentialId() {
        TypeCredentialOfferDefinition definition = TypeCredentialOfferDefinition.of(
                ISSUER, TypeCredentialOfferDefinition.OfferedCredential.ofType(OBJECT_ID, "MembershipCredential"));

        CredentialRequestMessage request = new CredentialRequestMessage(
                List.of(Constants.DCP_JSON_LD_CONTEXT),
                Constants.MESSAGE_TYPE_CREDENTIAL_REQUEST,
                "holder-pid-1",
                List.of(new de.eecc.dcp.message.CredentialRequestReference("urn:uuid:unknown")));

        assertThatThrownBy(() -> definition.assertRequestMatches(request))
                .isInstanceOf(DcpException.class)
                .satisfies(ex -> assertThat(((DcpException) ex).error()).isInstanceOf(InvalidCredentialRequest.class));
    }

    @Test
    void rejectsBlankHolderPidOnRequest() {
        TypeCredentialOfferDefinition definition = TypeCredentialOfferDefinition.of(
                ISSUER, TypeCredentialOfferDefinition.OfferedCredential.ofType(OBJECT_ID, "MembershipCredential"));

        assertThatThrownBy(() -> definition.toRequestMessage(" "))
                .isInstanceOf(DcpException.class)
                .satisfies(ex -> assertThat(((DcpException) ex).error()).isInstanceOf(InvalidCredentialRequest.class));
    }
}
