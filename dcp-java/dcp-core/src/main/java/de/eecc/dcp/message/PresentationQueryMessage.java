package de.eecc.dcp.message;

import java.util.List;

/**
 * DCP {@code PresentationQueryMessage} wire DTO.
 *
 * <p>Fields and JSON-LD mapping will be implemented in Phase 1.
 */
public record PresentationQueryMessage(
        List<Object> context,
        String type) {
}
