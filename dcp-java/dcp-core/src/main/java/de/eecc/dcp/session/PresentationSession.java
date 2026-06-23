package de.eecc.dcp.session;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * Tracks an in-flight DCP presentation exchange on the verifier side.
 */
@Getter
@SuperBuilder
public class PresentationSession {

    private final String id;
}
