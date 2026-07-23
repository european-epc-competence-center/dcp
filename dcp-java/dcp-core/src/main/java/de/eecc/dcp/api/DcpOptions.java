package de.eecc.dcp.api;

import de.eecc.dcp.api.access.PresentationAccessPolicy;
import lombok.Builder;
import lombok.Getter;

/**
 * Library-wide configuration for {@link DcpPresentation} and {@link DcpIssuance}.
 */
@Getter
@Builder
public class DcpOptions {

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
