package de.eecc.dcp.api.access;

import de.eecc.dcp.exception.DcpException;
import de.eecc.dcp.exception.PresentationAccessDenied;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Holder-side policy: an ordered {@link List} of {@link PresentationAccessRule}s.
 *
 * <p>A request is evaluated by collecting every rule that {@linkplain PresentationAccessRule#matches
 * matches}. If any matching rule is {@link AccessEffect#DENY}, access is denied (deny always
 * overrides allow). Otherwise, if any matching rule is {@link AccessEffect#ALLOW}, access is
 * allowed. If nothing matches (including an empty rule list), access is <strong>denied</strong>.
 *
 * <p>Use {@link PresentationAccessRule#ANY} / {@link CredentialConstraint#ANY} ({@code "*"}) for any
 * verifier or credential type.
 */
@Getter
public final class PresentationAccessPolicy {

    private final List<PresentationAccessRule> rules;

    private PresentationAccessPolicy(List<PresentationAccessRule> rules) {
        this.rules = List.copyOf(rules);
    }

    /** Default policy: empty rule list — every request is denied. */
    public static PresentationAccessPolicy denyAll() {
        return new PresentationAccessPolicy(List.of());
    }

    /** Policy from an explicit rule list. */
    public static PresentationAccessPolicy of(PresentationAccessRule... rules) {
        return of(rules == null ? List.of() : Arrays.asList(rules));
    }

    /** Policy from an explicit rule list. */
    public static PresentationAccessPolicy of(Collection<PresentationAccessRule> rules) {
        return new PresentationAccessPolicy(rules == null ? List.of() : List.copyOf(rules));
    }

    /** Explicit allow-everything policy ({@code *} / {@code *}). */
    public static PresentationAccessPolicy allowAll() {
        return of(PresentationAccessRule.allow()
                .verifiers(PresentationAccessRule.ANY)
                .credentialTypes(CredentialConstraint.ANY)
                .build());
    }

    /** Allow only these verifier DIDs for all credential types. */
    public static PresentationAccessPolicy allowlist(Collection<String> verifierDids) {
        return of(PresentationAccessRule.allow()
                .verifiers(verifierDids)
                .credentialTypes(CredentialConstraint.ANY)
                .build());
    }

    /**
     * Allow every verifier/type except the given DIDs. Deny rules are listed after the allow-all rule;
     * deny still overrides when both match.
     */
    public static PresentationAccessPolicy denylist(Collection<String> verifierDids) {
        return of(
                PresentationAccessRule.allow()
                        .verifiers(PresentationAccessRule.ANY)
                        .credentialTypes(CredentialConstraint.ANY)
                        .build(),
                PresentationAccessRule.deny()
                        .verifiers(verifierDids)
                        .credentialTypes(CredentialConstraint.ANY)
                        .build());
    }

    public static Builder builder() {
        return new Builder();
    }

    /** {@code true} when the rule list is empty (default deny). */
    public boolean isDenyAll() {
        return rules.isEmpty();
    }

    /**
     * Returns every rule that matches {@code request} (verifier + credential constraint). Deny and
     * allow matches may both appear; {@link #requireAllowed} treats any deny as decisive.
     */
    public List<PresentationAccessRule> matchingRules(PresentationAccessRequest request) {
        Objects.requireNonNull(request, "request");
        return rules.stream().filter(rule -> rule.matches(request)).toList();
    }

    public boolean isAllowed(PresentationAccessRequest request) {
        try {
            requireAllowed(request);
            return true;
        } catch (DcpException ex) {
            if (ex.error() instanceof PresentationAccessDenied) {
                return false;
            }
            throw ex;
        }
    }

    /**
     * Asserts that {@code request} is permitted under this policy.
     *
     * @throws DcpException with {@link PresentationAccessDenied} when denied
     */
    public void requireAllowed(PresentationAccessRequest request) {
        Objects.requireNonNull(request, "request");
        String did = request.verifierDid();
        if (did == null || did.isBlank()) {
            throw new DcpException(new PresentationAccessDenied("verifier DID must not be blank"));
        }

        if (request.credentialTypes().isEmpty()) {
            evaluateMatches(matchingRules(request), request);
            return;
        }

        for (String type : request.credentialTypes()) {
            PresentationAccessRequest single = PresentationAccessRequest.of(did, java.util.Set.of(type));
            evaluateMatches(matchingRules(single), single);
        }
    }

    private static void evaluateMatches(List<PresentationAccessRule> matches, PresentationAccessRequest request) {
        boolean denied = false;
        boolean allowed = false;
        for (PresentationAccessRule rule : matches) {
            if (rule.getEffect() == AccessEffect.DENY) {
                denied = true;
            } else if (rule.getEffect() == AccessEffect.ALLOW) {
                allowed = true;
            }
        }
        if (denied) {
            throw new DcpException(new PresentationAccessDenied(
                    "presentation access denied by matching deny rule for verifier " + request.verifierDid()
                            + describeTypes(request)));
        }
        if (allowed) {
            return;
        }
        throw new DcpException(new PresentationAccessDenied(
                "presentation access not granted for verifier " + request.verifierDid()
                        + describeTypes(request)));
    }

    private static String describeTypes(PresentationAccessRequest request) {
        if (request.credentialTypes().isEmpty()) {
            return " (untyped / presentationDefinition query)";
        }
        return " credential types " + request.credentialTypes();
    }

    public static final class Builder {

        private final List<PresentationAccessRule> rules = new ArrayList<>();

        /** Appends a rule to the list (allow or deny). */
        public Builder rule(PresentationAccessRule rule) {
            rules.add(Objects.requireNonNull(rule, "rule"));
            return this;
        }

        /** Appends all rules to the list. */
        public Builder rules(Collection<PresentationAccessRule> more) {
            if (more != null) {
                more.forEach(this::rule);
            }
            return this;
        }

        /** Convenience for {@code rule(PresentationAccessRule.allow()…)}. */
        public Builder allow(PresentationAccessRule rule) {
            return rule(requireEffect(rule, AccessEffect.ALLOW));
        }

        /** Convenience for {@code rule(PresentationAccessRule.deny()…)}. */
        public Builder deny(PresentationAccessRule rule) {
            return rule(requireEffect(rule, AccessEffect.DENY));
        }

        private static PresentationAccessRule requireEffect(PresentationAccessRule rule, AccessEffect expected) {
            Objects.requireNonNull(rule, "rule");
            if (rule.getEffect() != expected) {
                throw new DcpException(new de.eecc.dcp.exception.InternalError(
                        "expected " + expected + " rule, got " + rule.getEffect()));
            }
            return rule;
        }

        public PresentationAccessPolicy build() {
            return new PresentationAccessPolicy(rules);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PresentationAccessPolicy that)) {
            return false;
        }
        return rules.equals(that.rules);
    }

    @Override
    public int hashCode() {
        return rules.hashCode();
    }
}
