package de.eecc.dcp.api.access;

import de.eecc.dcp.exception.DcpException;
import de.eecc.dcp.exception.InternalError;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Constraint on which credentials a presentation-access rule covers.
 *
 * <p>Use {@link #ANY} ({@code "*"}) for every credential type. Additional sealed implementations can
 * add claim/property filters later without changing {@link PresentationAccessRule}.
 */
public sealed interface CredentialConstraint permits CredentialTypeConstraint {

    /** Wildcard matching every credential type (and untyped / presentationDefinition queries). */
    String ANY = "*";

    /** Matches every credential request. */
    static CredentialConstraint any() {
        return types(ANY);
    }

    /**
     * Matches the given credential types. Empty/null → {@link #ANY}. Include {@link #ANY} to match
     * all types.
     */
    static CredentialConstraint types(Collection<String> credentialTypes) {
        if (credentialTypes == null || credentialTypes.isEmpty()) {
            return new CredentialTypeConstraint(Set.of(ANY));
        }
        Set<String> normalized = new LinkedHashSet<>();
        for (String type : credentialTypes) {
            if (type == null || type.isBlank()) {
                throw new DcpException(new InternalError("credential type entries must not be blank"));
            }
            normalized.add(type.trim());
        }
        return new CredentialTypeConstraint(Set.copyOf(normalized));
    }

    static CredentialConstraint types(String... credentialTypes) {
        return types(credentialTypes == null ? List.of() : List.of(credentialTypes));
    }

    /**
     * Whether this constraint covers {@code request}.
     *
     * <p>A constraint containing {@link #ANY} covers every request. Otherwise the request must declare
     * credential types and each requested type must be listed.
     */
    boolean covers(PresentationAccessRequest request);

    /** {@code true} when this constraint contains {@link #ANY}. */
    boolean isAny();
}
