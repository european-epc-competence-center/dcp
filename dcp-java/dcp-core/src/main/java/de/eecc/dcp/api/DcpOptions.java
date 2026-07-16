package de.eecc.dcp.api;

import de.eecc.dcp.api.access.PresentationAccessPolicy;
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

    /**
     * Holder-side allow/deny rules for which verifiers may receive which credentials.
     * Defaults to {@link PresentationAccessPolicy#denyAll()} — configure explicit allow rules
     * (use {@code *} for any verifier or credential type).
     */
    @Builder.Default
    private final PresentationAccessPolicy presentationAccess = PresentationAccessPolicy.denyAll();
}
