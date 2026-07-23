package de.eecc.dcp.spring;

import de.eecc.dcp.api.DcpIssuance;
import de.eecc.dcp.api.DcpPresentation;
import de.eecc.dcp.api.DcpOptions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class DcpAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(DcpAutoConfiguration.class));

    @Test
    void createsDcpIssuanceBeanFromProperties() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(DcpIssuance.class);
            assertThat(context.getBean(DcpIssuance.class).getOptions().getPresentationAccess().isDenyAll())
                    .isTrue();
        });
    }

    @Test
    void bindsCustomPathsFromProperties() {
        contextRunner
                .withPropertyValues(
                        "dcp.paths.offers=/credentials",
                        "dcp.paths.issuer-request=/issuance")
                .run(context -> {
                    DcpIssuance issuance = context.getBean(DcpIssuance.class);
                    assertThat(issuance.offersUrl("https://cs.example.com"))
                            .isEqualTo("https://cs.example.com/credentials");
                    assertThat(issuance.issuerRequestUrl("https://issuer.example.com"))
                            .isEqualTo("https://issuer.example.com/issuance");
                });
    }

    @Test
    void createsDcpPresentationBeanFromProperties() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(DcpPresentation.class));
    }

    @Test
    void bindsPresentationAccessFromProperties() {
        contextRunner
                .withPropertyValues(
                        "dcp.presentation-access.rules[0].effect=allow",
                        "dcp.presentation-access.rules[0].verifiers[0]=did:web:trusted.example",
                        "dcp.presentation-access.rules[0].credential-types[0]=MembershipCredential",
                        "dcp.presentation-access.rules[1].effect=deny",
                        "dcp.presentation-access.rules[1].verifiers[0]=did:web:blocked.example")
                .run(context -> {
                    DcpPresentation dcp = context.getBean(DcpPresentation.class);
                    var policy = dcp.getOptions().getPresentationAccess();
                    assertThat(policy.isDenyAll()).isFalse();
                    assertThat(policy.getRules()).hasSize(2);
                    assertThat(policy.isAllowed(de.eecc.dcp.api.access.PresentationAccessRequest.of(
                            "did:web:trusted.example", java.util.Set.of("MembershipCredential")))).isTrue();
                    assertThat(policy.isAllowed(de.eecc.dcp.api.access.PresentationAccessRequest.of(
                            "did:web:trusted.example", java.util.Set.of("OtherCredential")))).isFalse();
                    assertThat(policy.isAllowed(de.eecc.dcp.api.access.PresentationAccessRequest.of(
                            "did:web:blocked.example", java.util.Set.of("MembershipCredential")))).isFalse();
                });
    }

    @Test
    void defaultPresentationAccessDeniesAll() {
        contextRunner.run(context -> {
            DcpPresentation dcp = context.getBean(DcpPresentation.class);
            assertThat(dcp.getOptions().getPresentationAccess().isDenyAll()).isTrue();
            assertThat(dcp.getOptions().getPresentationAccess().isAllowed(
                    de.eecc.dcp.api.access.PresentationAccessRequest.of(
                            "did:web:anyone.example", java.util.Set.of("Anything")))).isFalse();
        });
    }

    @Test
    void respectsCustomDcpPresentationBean() {
        DcpPresentation custom = DcpPresentation.create(DcpOptions.builder().build());

        contextRunner
                .withBean("customDcpPresentation", DcpPresentation.class, () -> custom)
                .run(context -> assertThat(context.getBean(DcpPresentation.class)).isSameAs(custom));
    }
}
