package de.eecc.dcp;

import de.eecc.dcp.api.DcpOptions;
import de.eecc.dcp.api.DcpPresentation;
import de.eecc.dcp.exception.DcpException;
import de.eecc.dcp.exception.InternalError;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ModuleBootstrapTest {

    @Test
    void exposesDcpConstants() {
        assertThat(Constants.DCP_JSON_LD_CONTEXT)
                .isEqualTo("https://w3id.org/dspace-dcp/v1.0/dcp.jsonld");
        assertThat(Constants.MESSAGE_TYPE_PRESENTATION_QUERY)
                .isEqualTo("PresentationQueryMessage");
    }

    @Test
    void createsDcpPresentationFromOptions() {
        DcpOptions options = DcpOptions.builder()
                .sessionTtl(Duration.ofMinutes(10))
                .build();

        DcpPresentation presentation = DcpPresentation.create(options);

        assertThat(presentation.getOptions().getSessionTtl()).isEqualTo(Duration.ofMinutes(10));
    }

    @Test
    void dcpExceptionWrapsTypedError() {
        assertThatThrownBy(() -> {
            throw new DcpException(new InternalError("bootstrap failure"));
        })
                .isInstanceOf(DcpException.class)
                .satisfies(ex -> assertThat(((DcpException) ex).error()).isInstanceOf(InternalError.class));
    }
}
