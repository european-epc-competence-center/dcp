package de.eecc.dcp.api;

import de.eecc.dcp.Constants;
import de.eecc.dcp.exception.DcpException;
import de.eecc.dcp.exception.InternalError;
import de.eecc.dcp.issuance.TypeCredentialOfferDefinition;
import de.eecc.dcp.message.PresentationQueryMessage;
import de.eecc.dcp.query.DcpScope;
import de.eecc.dcp.query.ScopeQueryDefinition;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DcpFacadeTest {

    @Nested
    class EndpointPaths {

        @Test
        void defaultsMatchDcpSpec() {
            DcpEndpointPaths paths = DcpEndpointPaths.builder().build();

            assertThat(paths.getOffers()).isEqualTo(Constants.OFFERS_PATH);
            assertThat(paths.getCredentialDelivery()).isEqualTo(Constants.CREDENTIALS_PATH);
            assertThat(paths.getIssuerRequest()).isEqualTo(Constants.ISSUER_CREDENTIALS_PATH);
            assertThat(paths.getPresentationsQuery()).isEqualTo(Constants.PRESENTATIONS_QUERY_PATH);
        }
    }

    @Nested
    class Urls {

        @Test
        void joinsBaseUrlAndPath() {
            assertThat(DcpUrls.join("https://cs.example.com", "/offers"))
                    .isEqualTo("https://cs.example.com/offers");
            assertThat(DcpUrls.join("https://cs.example.com/", "/offers"))
                    .isEqualTo("https://cs.example.com/offers");
            assertThat(DcpUrls.join("https://cs.example.com", "offers"))
                    .isEqualTo("https://cs.example.com/offers");
        }

        @Test
        void rejectsBlankBaseUrl() {
            assertThatThrownBy(() -> DcpUrls.join(" ", "/offers"))
                    .isInstanceOf(DcpException.class)
                    .satisfies(ex -> assertThat(((DcpException) ex).error()).isInstanceOf(InternalError.class));
        }
    }

    @Nested
    class Presentation {

        private final DcpPresentation dcp = DcpPresentation.create(DcpOptions.builder().build());

        @Test
        void resolvesPresentationsQueryUrl() {
            assertThat(dcp.presentationsQueryUrl("https://cs.example.com"))
                    .isEqualTo("https://cs.example.com/presentations/query");
        }

        @Test
        void buildsAndVerifiesQueryFlow() {
            ScopeQueryDefinition query = ScopeQueryDefinition.of(DcpScope.vcType("MembershipCredential"));
            PresentationQueryMessage message = dcp.buildQueryMessage(query);

            dcp.verifyQueryMessage(message);
            assertThat(message.scope()).containsExactly("org.eclipse.dspace.dcp.vc.type:MembershipCredential");
        }
    }

    @Nested
    class Issuance {

        private final DcpIssuance issuance = DcpIssuance.create(DcpOptions.builder().build());

        @Test
        void resolvesSpecDefaultUrls() {
            assertThat(issuance.offersUrl("https://cs.example.com")).isEqualTo("https://cs.example.com/offers");
            assertThat(issuance.credentialDeliveryUrl("https://cs.example.com"))
                    .isEqualTo("https://cs.example.com/credentials");
            assertThat(issuance.issuerRequestUrl("https://issuer.example.com"))
                    .isEqualTo("https://issuer.example.com/credentials");
        }

        @Test
        void resolvesCustomPathOverrides() {
            DcpIssuance custom = DcpIssuance.create(DcpOptions.builder()
                    .paths(DcpEndpointPaths.builder()
                            .offers("/custom-offers")
                            .issuerRequest("/custom-issuance")
                            .build())
                    .build());

            assertThat(custom.offersUrl("https://cs.example.com")).isEqualTo("https://cs.example.com/custom-offers");
            assertThat(custom.issuerRequestUrl("https://issuer.example.com"))
                    .isEqualTo("https://issuer.example.com/custom-issuance");
        }

        @Test
        void buildsOfferAndRequestMessages() {
            TypeCredentialOfferDefinition offer = TypeCredentialOfferDefinition.of(
                    "did:web:issuer.example",
                    TypeCredentialOfferDefinition.OfferedCredential.ofType("urn:uuid:1", "Member"));

            var offerMessage = issuance.buildOfferMessage(offer);
            issuance.verifyOffer(offer, offerMessage);

            var request = issuance.buildRequestMessage(offer, "holder-pid");
            issuance.verifyRequest(offer, request);
        }
    }
}
