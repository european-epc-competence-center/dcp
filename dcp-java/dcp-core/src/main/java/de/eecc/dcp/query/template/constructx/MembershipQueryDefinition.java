package de.eecc.dcp.query.template.constructx;

import de.eecc.dcp.Constants;
import de.eecc.dcp.message.PresentationQueryMessage;
import de.eecc.dcp.message.PresentationResponseMessage;
import de.eecc.dcp.query.DcpScope;
import de.eecc.dcp.query.PresentationQueryDefinition;
import de.eecc.dcp.query.ScopeQueryDefinition;

import java.util.List;

/**
 * DCP presentation query for Construct-X {@code MembershipCredential} (VC Data Model 2.0).
 *
 * <p>Requests the credential type via the normative {@code org.eclipse.dspace.dcp.vc.type} scope.
 * Claim selection is left to the Credential Service mapping; template-specific claim extraction can be
 * added later.
 *
 * <p>Pass {@link #INSTANCE} wherever a {@link PresentationQueryDefinition} is required. Constrain the
 * accepted issuer DID with {@link #requiresIssuer(String)}:
 *
 * <pre>{@code
 * PresentationQueryDefinition query = MembershipQueryDefinition.INSTANCE
 *         .requiresIssuer("did:web:membership-issuer.example");
 * }</pre>
 *
 * @see Constants#PROFILE_VC20_BSSL_JWT
 */
public final class MembershipQueryDefinition implements PresentationQueryDefinition {

    public static final MembershipQueryDefinition INSTANCE = new MembershipQueryDefinition();

    /** Construct-X membership credential type (VC DM 2.0). */
    public static final String TYPE_MEMBERSHIP = "MembershipCredential";

    public static final List<String> CREDENTIAL_TYPES = List.of(TYPE_MEMBERSHIP);

    private final ScopeQueryDefinition scopeQuery;

    private MembershipQueryDefinition() {
        this.scopeQuery = ScopeQueryDefinition.of(DcpScope.vcType(TYPE_MEMBERSHIP));
    }

    public List<DcpScope> scopes() {
        return scopeQuery.scopes();
    }

    @Override
    public PresentationQueryMessage toQueryMessage() {
        return scopeQuery.toQueryMessage();
    }

    @Override
    public void assertResponseStructure(PresentationResponseMessage response) {
        scopeQuery.assertResponseStructure(response);
    }
}
