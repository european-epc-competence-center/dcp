package de.eecc.dcp.query;

import de.eecc.dcp.message.PresentationQueryMessage;

import java.util.List;
import java.util.Objects;

/**
 * Scope-based presentation query per DCP v1.0.1.
 */
public final class ScopeQueryDefinition implements PresentationQueryDefinition {

    private final List<DcpScope> scopes;
    private final List<Object> context;

    public ScopeQueryDefinition(List<DcpScope> scopes) {
        this(scopes, QueryMessages.defaultContext());
    }

    public ScopeQueryDefinition(List<DcpScope> scopes, List<Object> context) {
        this.scopes = List.copyOf(Objects.requireNonNull(scopes, "scopes"));
        this.context = List.copyOf(Objects.requireNonNull(context, "context"));
        QueryMessages.validateScopeOnly(scopeStrings(), null);
    }

    public static ScopeQueryDefinition of(DcpScope... scopes) {
        return new ScopeQueryDefinition(List.of(scopes));
    }

    public List<DcpScope> scopes() {
        return scopes;
    }

    @Override
    public PresentationQueryMessage toQueryMessage() {
        return QueryMessages.scopeQuery(context, scopeStrings());
    }

    // Spec: CS MAY return fewer presentations than scopes requested; default assertResponseStructure
    // requires at least one presentation.

    private List<String> scopeStrings() {
        return scopes.stream().map(DcpScope::toScopeString).toList();
    }

    @Override
    public String toString() {
        return "ScopeQueryDefinition" + scopes;
    }
}
