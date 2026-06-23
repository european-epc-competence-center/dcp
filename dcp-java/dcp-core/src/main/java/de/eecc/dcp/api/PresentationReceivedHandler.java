package de.eecc.dcp.api;

import com.fasterxml.jackson.databind.JsonNode;
import de.eecc.dcp.session.PresentationSession;

/**
 * Callback invoked after a verified presentation is received from a Credential Service.
 */
@FunctionalInterface
public interface PresentationReceivedHandler {

    /**
     * Handle a verified presentation for the given session.
     *
     * @param session     the active presentation session
     * @param presentation verified presentation payload
     */
    void onPresentationReceived(PresentationSession session, JsonNode presentation);
}
