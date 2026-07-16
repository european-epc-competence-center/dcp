package de.eecc.dcp.api.access;

import java.util.Objects;
import java.util.Set;

/**
 * Credential-type filter for a presentation-access rule. Use {@link CredentialConstraint#ANY}
 * ({@code "*"}) for all types. Future property constraints can be added as other
 * {@link CredentialConstraint} implementations.
 *
 * @param credentialTypes non-empty set of VC type names and/or {@code *}
 */
public record CredentialTypeConstraint(Set<String> credentialTypes) implements CredentialConstraint {

    public CredentialTypeConstraint {
        Objects.requireNonNull(credentialTypes, "credentialTypes");
        if (credentialTypes.isEmpty()) {
            throw new IllegalArgumentException(
                    "credentialTypes must not be empty; use \"" + CredentialConstraint.ANY + "\" for any");
        }
        credentialTypes = Set.copyOf(credentialTypes);
    }

    @Override
    public boolean covers(PresentationAccessRequest request) {
        if (isAny()) {
            return true;
        }
        Set<String> requested = request.credentialTypes();
        if (requested.isEmpty()) {
            return false;
        }
        return credentialTypes.containsAll(requested);
    }

    @Override
    public boolean isAny() {
        return credentialTypes.contains(CredentialConstraint.ANY);
    }
}
