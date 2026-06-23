package de.eecc.dcp.query;

import de.eecc.dcp.message.PresentationQueryMessage;

/**
 * Describes what a verifier asks for in a DCP presentation exchange.
 */
public interface PresentationQueryDefinition {

    PresentationQueryMessage toQueryMessage();
}
