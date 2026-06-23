package de.eecc.dcp.spring;

import de.eecc.dcp.api.DcpOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("dcp")
public record DcpProperties(
        Duration sessionTtl
) {

    public DcpOptions toOptions() {
        var builder = DcpOptions.builder();
        if (sessionTtl != null) {
            builder.sessionTtl(sessionTtl);
        }
        return builder.build();
    }
}
