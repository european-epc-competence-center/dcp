package de.eecc.dcp.spring;

import de.eecc.dcp.api.DcpEndpointPaths;
import de.eecc.dcp.api.DcpOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;

@ConfigurationProperties("dcp")
public record DcpProperties(
        Duration sessionTtl,
        @NestedConfigurationProperty Paths paths
) {

    public DcpProperties {
        if (paths == null) {
            paths = new Paths(null, null, null, null);
        }
    }

    public record Paths(
            String offers,
            String credentialDelivery,
            String issuerRequest,
            String presentationsQuery) {}

    public DcpOptions toOptions() {
        var builder = DcpOptions.builder();
        if (sessionTtl != null) {
            builder.sessionTtl(sessionTtl);
        }
        var pathsBuilder = DcpEndpointPaths.builder();
        if (paths.offers() != null) {
            pathsBuilder.offers(paths.offers());
        }
        if (paths.credentialDelivery() != null) {
            pathsBuilder.credentialDelivery(paths.credentialDelivery());
        }
        if (paths.issuerRequest() != null) {
            pathsBuilder.issuerRequest(paths.issuerRequest());
        }
        if (paths.presentationsQuery() != null) {
            pathsBuilder.presentationsQuery(paths.presentationsQuery());
        }
        builder.paths(pathsBuilder.build());
        return builder.build();
    }
}
