package de.eecc.dcp.query;

import de.eecc.dcp.Constants;
import de.eecc.dcp.exception.DcpException;
import de.eecc.dcp.exception.InvalidQueryMessage;

import java.util.Objects;

/**
 * Normative DCP scope value in the form {@code [alias]:[discriminator]}.
 *
 * @see <a href="https://eclipse-dataspace-dcp.github.io/decentralized-claims-protocol/v1.0.1/#scopes">DCP scopes</a>
 */
public record DcpScope(String alias, String discriminator) {

    public DcpScope {
        Objects.requireNonNull(alias, "alias");
        Objects.requireNonNull(discriminator, "discriminator");
        if (alias.isBlank()) {
            throw new DcpException(new InvalidQueryMessage("scope alias must not be blank"));
        }
        if (discriminator.isBlank()) {
            throw new DcpException(new InvalidQueryMessage("scope discriminator must not be blank"));
        }
    }

    public String toScopeString() {
        return alias + ":" + discriminator;
    }

    public static DcpScope vcType(String type) {
        return new DcpScope(Constants.SCOPE_ALIAS_VC_TYPE, type);
    }

    public static DcpScope vcId(String id) {
        return new DcpScope(Constants.SCOPE_ALIAS_VC_ID, id);
    }

    /**
     * Parses a scope string per DCP v1.0.1 ({@code alias:discriminator}, split on the first colon).
     *
     * <p>Construct-X EDC commonly uses a third {@code :operation} segment (e.g. {@code :read}); that suffix is
     * stripped here because it is not defined in the DCP specification.
     */
    public static DcpScope parse(String scope) {
        if (scope == null || scope.isBlank()) {
            throw new DcpException(new InvalidQueryMessage("scope must not be blank"));
        }

        int separator = scope.indexOf(':');
        if (separator <= 0 || separator == scope.length() - 1) {
            throw new DcpException(new InvalidQueryMessage(
                    "scope must be in the form [alias]:[discriminator], got: " + scope));
        }

        String parsedAlias = scope.substring(0, separator);
        String parsedDiscriminator = normalizeDiscriminator(scope.substring(separator + 1));
        return new DcpScope(parsedAlias, parsedDiscriminator);
    }

    static String normalizeDiscriminator(String discriminator) {
        int lastSeparator = discriminator.lastIndexOf(':');
        if (lastSeparator <= 0 || lastSeparator == discriminator.length() - 1) {
            return discriminator;
        }

        String operation = discriminator.substring(lastSeparator + 1);
        if (QueryMessages.EDC_SCOPE_OPERATIONS.contains(operation)) {
            // EDC non-normative operation suffix; spec uses [alias]:[discriminator] only.
            return discriminator.substring(0, lastSeparator);
        }
        return discriminator;
    }
}
