package de.eecc.dcp.api;

import lombok.Builder;
import lombok.Getter;

import java.time.Duration;

/**
 * Library-wide configuration for {@link DcpPresentation} and {@link DcpIssuance}.
 */
@Getter
@Builder
public class DcpOptions {

    @Builder.Default
    private final Duration sessionTtl = Duration.ofSeconds(de.eecc.dcp.Constants.DEFAULT_SESSION_TTL_SECONDS);

    @Builder.Default
    private final DcpEndpointPaths paths = DcpEndpointPaths.builder().build();
}
