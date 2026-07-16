package de.eecc.dcp.api.access;

import de.eecc.dcp.api.DcpOptions;
import de.eecc.dcp.api.DcpPresentation;
import de.eecc.dcp.exception.DcpException;
import de.eecc.dcp.exception.PresentationAccessDenied;
import de.eecc.dcp.message.PresentationQueryMessage;
import de.eecc.dcp.query.DcpScope;
import de.eecc.dcp.query.ScopeQueryDefinition;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PresentationAccessPolicyTest {

    @Nested
    class DenyAllDefault {

        @Test
        void deniesEveryRequestWhenUnconfigured() {
            PresentationAccessPolicy policy = PresentationAccessPolicy.denyAll();

            assertThat(policy.isDenyAll()).isTrue();
            assertThat(policy.isAllowed(PresentationAccessRequest.of(
                    "did:web:anyone.example", Set.of("MembershipCredential")))).isFalse();
        }

        @Test
        void allowAllRequiresExplicitStarRules() {
            PresentationAccessPolicy policy = PresentationAccessPolicy.allowAll();

            assertThat(policy.isAllowed(PresentationAccessRequest.of(
                    "did:web:anyone.example", Set.of("MembershipCredential")))).isTrue();
            assertThat(policy.isAllowed(PresentationAccessRequest.anyCredentials("did:web:anyone.example")))
                    .isTrue();
        }

        @Test
        void rejectsBlankVerifierDid() {
            assertThatThrownBy(() -> PresentationAccessPolicy.allowAll()
                            .requireAllowed(PresentationAccessRequest.anyCredentials(" ")))
                    .isInstanceOf(DcpException.class)
                    .satisfies(ex -> assertThat(((DcpException) ex).error())
                            .isInstanceOf(PresentationAccessDenied.class));
        }
    }

    @Nested
    class VerifierAndTypes {

        @Test
        void allowlistRestrictsVerifierAndOptionalTypes() {
            PresentationAccessPolicy policy = PresentationAccessPolicy.builder()
                    .allow(PresentationAccessRule.allow()
                            .verifiers("did:web:trusted.example")
                            .credentialTypes("MembershipCredential", "OrgCredential")
                            .build())
                    .build();

            assertThat(policy.isAllowed(PresentationAccessRequest.of(
                    "did:web:trusted.example", Set.of("MembershipCredential")))).isTrue();
            assertThat(policy.isAllowed(PresentationAccessRequest.of(
                    "did:web:trusted.example", Set.of("MembershipCredential", "OrgCredential")))).isTrue();
            assertThat(policy.isAllowed(PresentationAccessRequest.of(
                    "did:web:trusted.example", Set.of("OtherCredential")))).isFalse();
            assertThat(policy.isAllowed(PresentationAccessRequest.of(
                    "did:web:other.example", Set.of("MembershipCredential")))).isFalse();
        }

        @Test
        void starMatchesAnyVerifierOrCredentialType() {
            PresentationAccessPolicy policy = PresentationAccessPolicy.builder()
                    .allow(PresentationAccessRule.allow()
                            .verifiers(PresentationAccessRule.ANY)
                            .credentialTypes(CredentialConstraint.ANY)
                            .build())
                    .build();

            assertThat(policy.isAllowed(PresentationAccessRequest.of(
                    "did:web:anyone.example", Set.of("Anything")))).isTrue();
        }

        @Test
        void omittedCredentialTypesDefaultsToStar() {
            PresentationAccessPolicy policy = PresentationAccessPolicy.builder()
                    .allow(PresentationAccessRule.allow()
                            .verifiers("did:web:trusted.example")
                            .build())
                    .build();

            assertThat(policy.isAllowed(PresentationAccessRequest.of(
                    "did:web:trusted.example", Set.of("Anything")))).isTrue();
        }

        @Test
        void emptyVerifiersMatchNothing() {
            PresentationAccessPolicy policy = PresentationAccessPolicy.builder()
                    .allow(PresentationAccessRule.allow()
                            .credentialTypes(CredentialConstraint.ANY)
                            .build())
                    .build();

            assertThat(policy.isAllowed(PresentationAccessRequest.of(
                    "did:web:anyone.example", Set.of("MembershipCredential")))).isFalse();
        }

        @Test
        void denylistBlocksListedVerifiersAndAllowsOthers() {
            PresentationAccessPolicy policy = PresentationAccessPolicy.denylist(
                    java.util.List.of("did:web:blocked.example"));

            assertThat(policy.isAllowed(PresentationAccessRequest.of(
                    "did:web:blocked.example", Set.of("MembershipCredential")))).isFalse();
            assertThat(policy.isAllowed(PresentationAccessRequest.of(
                    "did:web:anyone.example", Set.of("MembershipCredential")))).isTrue();
        }

        @Test
        void matchingRulesReturnsAllHitsAndDenyOverridesAllow() {
            PresentationAccessRule allowTrusted = PresentationAccessRule.allow()
                    .verifiers("did:web:trusted.example")
                    .credentialTypes(CredentialConstraint.ANY)
                    .build();
            PresentationAccessRule denySensitive = PresentationAccessRule.deny()
                    .verifiers("did:web:trusted.example")
                    .credentialTypes("SensitiveCredential")
                    .build();
            PresentationAccessPolicy policy = PresentationAccessPolicy.of(allowTrusted, denySensitive);

            PresentationAccessRequest membership = PresentationAccessRequest.of(
                    "did:web:trusted.example", Set.of("MembershipCredential"));
            assertThat(policy.matchingRules(membership)).containsExactly(allowTrusted);
            assertThat(policy.isAllowed(membership)).isTrue();

            PresentationAccessRequest sensitive = PresentationAccessRequest.of(
                    "did:web:trusted.example", Set.of("SensitiveCredential"));
            assertThat(policy.matchingRules(sensitive)).containsExactly(allowTrusted, denySensitive);
            assertThat(policy.isAllowed(sensitive)).isFalse();
        }

        @Test
        void ofRulesListIsThePolicy() {
            List<PresentationAccessRule> rules = List.of(
                    PresentationAccessRule.allow()
                            .verifiers("did:web:a.example")
                            .credentialTypes("TypeA")
                            .build());
            PresentationAccessPolicy policy = PresentationAccessPolicy.of(rules);

            assertThat(policy.getRules()).isEqualTo(rules);
            assertThat(policy.isAllowed(PresentationAccessRequest.of("did:web:a.example", Set.of("TypeA"))))
                    .isTrue();
        }

        @Test
        void perTypeAllowRulesCanCombine() {
            PresentationAccessPolicy policy = PresentationAccessPolicy.builder()
                    .allow(PresentationAccessRule.allow()
                            .verifiers("did:web:trusted.example")
                            .credentialTypes("TypeA")
                            .build())
                    .allow(PresentationAccessRule.allow()
                            .verifiers("did:web:trusted.example")
                            .credentialTypes("TypeB")
                            .build())
                    .build();

            assertThat(policy.isAllowed(PresentationAccessRequest.of(
                    "did:web:trusted.example", Set.of("TypeA", "TypeB")))).isTrue();
            assertThat(policy.isAllowed(PresentationAccessRequest.of(
                    "did:web:trusted.example", Set.of("TypeA", "TypeC")))).isFalse();
        }
    }

    @Nested
    class QueryExtraction {

        @Test
        void extractsVcTypeScopesFromQuery() {
            PresentationQueryMessage message = ScopeQueryDefinition.of(
                            DcpScope.vcType("MembershipCredential"),
                            DcpScope.vcId("urn:uuid:1"))
                    .toQueryMessage();

            PresentationAccessRequest request =
                    PresentationAccessRequest.from("did:web:v.example", message);

            assertThat(request.credentialTypes()).containsExactly("MembershipCredential");
            assertThat(request.properties()).isEmpty();
        }
    }

    @Nested
    class DcpPresentationIntegration {

        @Test
        void defaultOptionsDenyInboundQuery() {
            DcpPresentation dcp = DcpPresentation.create(DcpOptions.builder().build());
            PresentationQueryMessage message = dcp.buildQueryMessage(
                    ScopeQueryDefinition.of(DcpScope.vcType("MembershipCredential")));

            assertThatThrownBy(() -> dcp.verifyQueryMessage(message, "did:web:any-verifier.example"))
                    .isInstanceOf(DcpException.class)
                    .satisfies(ex -> assertThat(((DcpException) ex).error())
                            .isInstanceOf(PresentationAccessDenied.class));
        }

        @Test
        void configuredRulesBlockDisallowedType() {
            DcpPresentation dcp = DcpPresentation.create(DcpOptions.builder()
                    .presentationAccess(PresentationAccessPolicy.builder()
                            .allow(PresentationAccessRule.allow()
                                    .verifiers("did:web:trusted.example")
                                    .credentialTypes("MembershipCredential")
                                    .build())
                            .build())
                    .build());

            PresentationQueryMessage allowed = dcp.buildQueryMessage(
                    ScopeQueryDefinition.of(DcpScope.vcType("MembershipCredential")));
            PresentationQueryMessage blocked = dcp.buildQueryMessage(
                    ScopeQueryDefinition.of(DcpScope.vcType("OtherCredential")));

            assertThatCode(() -> dcp.verifyQueryMessage(allowed, "did:web:trusted.example"))
                    .doesNotThrowAnyException();
            assertThatThrownBy(() -> dcp.verifyQueryMessage(blocked, "did:web:trusted.example"))
                    .isInstanceOf(DcpException.class)
                    .satisfies(ex -> assertThat(((DcpException) ex).error())
                            .isInstanceOf(PresentationAccessDenied.class));
        }
    }
}
