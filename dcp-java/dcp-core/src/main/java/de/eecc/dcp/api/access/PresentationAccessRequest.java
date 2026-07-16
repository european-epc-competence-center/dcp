package de.eecc.dcp.api.access;

import de.eecc.dcp.Constants;
import de.eecc.dcp.message.PresentationQueryMessage;
import de.eecc.dcp.query.DcpScope;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * What a holder Credential Service evaluates against {@link PresentationAccessPolicy}.
 *
 * <p>{@code credentialTypes} are taken from normative {@code org.eclipse.dspace.dcp.vc.type} scopes
 * today. {@link #properties()} is reserved for future claim/property constraints (empty for now).
 */
public record PresentationAccessRequest(
        String verifierDid,
        Set<String> credentialTypes,
        Map<String, String> properties) {

    public PresentationAccessRequest {
        Objects.requireNonNull(credentialTypes, "credentialTypes");
        Objects.requireNonNull(properties, "properties");
        credentialTypes = Set.copyOf(credentialTypes);
        properties = Map.copyOf(properties);
    }

    public static PresentationAccessRequest of(String verifierDid, Set<String> credentialTypes) {
        return new PresentationAccessRequest(verifierDid, credentialTypes == null ? Set.of() : credentialTypes, Map.of());
    }

    public static PresentationAccessRequest anyCredentials(String verifierDid) {
        return new PresentationAccessRequest(verifierDid, Set.of(), Map.of());
    }

    /**
     * Builds a request from the verifier DID and an inbound query. Credential types are extracted
     * from {@code vc.type} scopes; {@code presentationDefinition}-only queries yield an empty type
     * set (only rules with {@link CredentialConstraint#any()} cover them).
     */
    public static PresentationAccessRequest from(String verifierDid, PresentationQueryMessage query) {
        return of(verifierDid, extractCredentialTypes(query));
    }

    static Set<String> extractCredentialTypes(PresentationQueryMessage query) {
        if (query == null || query.scope() == null || query.scope().isEmpty()) {
            return Set.of();
        }
        Set<String> types = new LinkedHashSet<>();
        for (String scope : query.scope()) {
            if (scope == null || scope.isBlank()) {
                continue;
            }
            try {
                DcpScope parsed = DcpScope.parse(scope);
                if (Constants.SCOPE_ALIAS_VC_TYPE.equals(parsed.alias())) {
                    types.add(parsed.discriminator());
                }
            } catch (RuntimeException ignored) {
                // Malformed scopes are rejected by QueryMessages.requireQueryMessage separately.
            }
        }
        return Set.copyOf(types);
    }
}
