package de.eecc.dcp.api.access;

import de.eecc.dcp.exception.DcpException;
import de.eecc.dcp.exception.InternalError;
import lombok.Getter;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * One allow/deny rule: which verifiers may receive which credentials.
 *
 * <p>Use {@link #ANY} ({@code "*"}) in {@link #verifiers()} for any verifier. Use
 * {@link CredentialConstraint#ANY} in credential types for any credential. Empty verifier sets match
 * nothing — specify {@code *} explicitly when intended.
 */
@Getter
public final class PresentationAccessRule {

    /** Wildcard matching every verifier DID. */
    public static final String ANY = "*";

    private final AccessEffect effect;
    private final Set<String> verifiers;
    private final CredentialConstraint credentials;

    private PresentationAccessRule(AccessEffect effect, Set<String> verifiers, CredentialConstraint credentials) {
        this.effect = Objects.requireNonNull(effect, "effect");
        this.verifiers = Set.copyOf(verifiers);
        this.credentials = Objects.requireNonNull(credentials, "credentials");
    }

    public static Builder allow() {
        return new Builder(AccessEffect.ALLOW);
    }

    public static Builder deny() {
        return new Builder(AccessEffect.DENY);
    }

    public boolean matches(PresentationAccessRequest request) {
        return matchesVerifier(request.verifierDid()) && credentials.covers(request);
    }

    public boolean matchesVerifier(String verifierDid) {
        if (verifierDid == null || verifierDid.isBlank()) {
            return false;
        }
        if (verifiers.isEmpty()) {
            return false;
        }
        if (verifiers.contains(ANY)) {
            return true;
        }
        return verifiers.contains(verifierDid.trim());
    }

    public static final class Builder {

        private final AccessEffect effect;
        private final Set<String> verifiers = new LinkedHashSet<>();
        private CredentialConstraint credentials = CredentialConstraint.any();

        private Builder(AccessEffect effect) {
            this.effect = effect;
        }

        public Builder verifiers(String... verifierDids) {
            return verifiers(verifierDids == null ? List.of() : List.of(verifierDids));
        }

        public Builder verifiers(Collection<String> verifierDids) {
            if (verifierDids == null) {
                return this;
            }
            for (String did : verifierDids) {
                if (did == null || did.isBlank()) {
                    throw new DcpException(new InternalError("verifier DID entries must not be blank"));
                }
                verifiers.add(did.trim());
            }
            return this;
        }

        /**
         * Restrict to these credential types. Omit, pass empty, or include
         * {@link CredentialConstraint#ANY} for all types.
         */
        public Builder credentialTypes(String... types) {
            return credentialTypes(types == null ? List.of() : List.of(types));
        }

        public Builder credentialTypes(Collection<String> types) {
            this.credentials = CredentialConstraint.types(types);
            return this;
        }

        /**
         * Replace the credential constraint (use for custom/future property constraints).
         */
        public Builder credentials(CredentialConstraint credentials) {
            this.credentials = Objects.requireNonNull(credentials, "credentials");
            return this;
        }

        public PresentationAccessRule build() {
            return new PresentationAccessRule(effect, verifiers, credentials);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PresentationAccessRule that)) {
            return false;
        }
        return effect == that.effect
                && verifiers.equals(that.verifiers)
                && credentials.equals(that.credentials);
    }

    @Override
    public int hashCode() {
        return Objects.hash(effect, verifiers, credentials);
    }
}
