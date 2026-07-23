package de.eecc.dcp.spring;

import de.eecc.dcp.api.DcpEndpointPaths;
import de.eecc.dcp.api.DcpOptions;
import de.eecc.dcp.api.access.AccessEffect;
import de.eecc.dcp.api.access.CredentialConstraint;
import de.eecc.dcp.api.access.PresentationAccessPolicy;
import de.eecc.dcp.api.access.PresentationAccessRule;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@ConfigurationProperties("dcp")
public record DcpProperties(
        @NestedConfigurationProperty Paths paths,
        @NestedConfigurationProperty PresentationAccess presentationAccess
) {

    public DcpProperties {
        if (paths == null) {
            paths = new Paths(null, null, null, null);
        }
        if (presentationAccess == null) {
            presentationAccess = new PresentationAccess(null);
        }
    }

    public record Paths(
            String offers,
            String credentialDelivery,
            String issuerRequest,
            String presentationsQuery) {}

    /**
     * Holder-side presentation access as a list of rules. Empty/absent → deny all. Use {@code *} for
     * any verifier or credential type. Deny rules override allow when both match.
     */
    public record PresentationAccess(List<AccessRule> rules) {}

    /**
     * One rule in the list. {@code effect} is {@code allow} or {@code deny} (default {@code allow}).
     */
    public record AccessRule(String effect, List<String> verifiers, List<String> credentialTypes) {}

    public DcpOptions toOptions() {
        var builder = DcpOptions.builder();
        var pathsBuilder = DcpEndpointPaths.builder();
        if (paths.offers() != null) {
            pathsBuilder.offers(paths.offers());
        }
        if (paths.credentialDelivery() != null) {
            pathsBuilder.credentialDelivery(paths.credentialDelivery());
        }
        if (paths.issuerRequest() != null) {
            pathsBuilder.issuerRequest(paths.issuerRequest());
        }
        if (paths.presentationsQuery() != null) {
            pathsBuilder.presentationsQuery(paths.presentationsQuery());
        }
        builder.paths(pathsBuilder.build());
        builder.presentationAccess(toPresentationAccessPolicy(presentationAccess));
        return builder.build();
    }

    private static PresentationAccessPolicy toPresentationAccessPolicy(PresentationAccess access) {
        if (access.rules() == null || access.rules().isEmpty()) {
            return PresentationAccessPolicy.denyAll();
        }
        List<PresentationAccessRule> rules = new ArrayList<>();
        for (AccessRule rule : access.rules()) {
            rules.add(toRule(rule));
        }
        return PresentationAccessPolicy.of(rules);
    }

    private static PresentationAccessRule toRule(AccessRule rule) {
        AccessEffect effect = parseEffect(rule.effect());
        PresentationAccessRule.Builder builder =
                effect == AccessEffect.DENY ? PresentationAccessRule.deny() : PresentationAccessRule.allow();
        if (rule.verifiers() != null) {
            builder.verifiers(rule.verifiers());
        }
        if (rule.credentialTypes() != null && !rule.credentialTypes().isEmpty()) {
            builder.credentialTypes(rule.credentialTypes());
        } else {
            builder.credentialTypes(CredentialConstraint.ANY);
        }
        return builder.build();
    }

    private static AccessEffect parseEffect(String effect) {
        if (effect == null || effect.isBlank()) {
            return AccessEffect.ALLOW;
        }
        return switch (effect.trim().toLowerCase(Locale.ROOT)) {
            case "allow", "permit" -> AccessEffect.ALLOW;
            case "deny", "block" -> AccessEffect.DENY;
            default -> throw new IllegalArgumentException(
                    "dcp.presentation-access.rules[].effect must be allow or deny, got: " + effect);
        };
    }
}
