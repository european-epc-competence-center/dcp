package de.eecc.dcp.spring;

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
    void createsDcpPresentationBeanFromProperties() {
        contextRunner
                .withPropertyValues("dcp.session-ttl=PT10M")
                .run(context -> {
                    assertThat(context).hasSingleBean(DcpPresentation.class);
                    assertThat(context.getBean(DcpPresentation.class).getOptions().getSessionTtl().toMinutes())
                            .isEqualTo(10);
                });
    }

    @Test
    void respectsCustomDcpPresentationBean() {
        DcpPresentation custom = DcpPresentation.create(DcpOptions.builder().build());

        contextRunner
                .withPropertyValues("dcp.session-ttl=PT5M")
                .withBean("customDcpPresentation", DcpPresentation.class, () -> custom)
                .run(context -> assertThat(context.getBean(DcpPresentation.class)).isSameAs(custom));
    }
}
