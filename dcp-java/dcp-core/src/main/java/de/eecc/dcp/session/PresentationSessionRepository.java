package de.eecc.dcp.session;

import java.util.Optional;

/**
 * Stores and retrieves {@link PresentationSession} instances.
 */
public interface PresentationSessionRepository {

    void save(PresentationSession session);

    Optional<PresentationSession> findById(String id);

    void delete(String id);
}
