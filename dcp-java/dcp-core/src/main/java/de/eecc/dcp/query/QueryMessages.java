package de.eecc.dcp.query;

import de.eecc.dcp.Constants;
import de.eecc.dcp.exception.DcpException;
import de.eecc.dcp.exception.InvalidQueryMessage;
import de.eecc.dcp.exception.InvalidPresentationResponse;
import de.eecc.dcp.message.PresentationQueryMessage;
import de.eecc.dcp.message.PresentationResponseMessage;

import java.util.List;
import java.util.Set;

/**
 * Shared helpers for building and validating presentation query messages.
 */
public final class QueryMessages {

    private static final String DID_CONTEXT = "https://www.w3.org/ns/did/v1";

    private QueryMessages() {}

    static List<Object> defaultContext() {
        return List.of(Constants.DCP_JSON_LD_CONTEXT, DID_CONTEXT);
    }

    static List<Object> presentationExchangeContext() {
        return List.of(
                Constants.DCP_JSON_LD_CONTEXT,
                Constants.PRESENTATION_EXCHANGE_SUBMISSION_CONTEXT,
                DID_CONTEXT);
    }

    static PresentationQueryMessage scopeQuery(List<Object> context, List<String> scopes) {
        validateScopeOnly(scopes, null);
        return new PresentationQueryMessage(
                context, Constants.MESSAGE_TYPE_PRESENTATION_QUERY, scopes, null);
    }

    static PresentationQueryMessage presentationExchangeQuery(
            List<Object> context, com.fasterxml.jackson.databind.JsonNode presentationDefinition) {
        validatePresentationDefinitionOnly(null, presentationDefinition);
        return new PresentationQueryMessage(
                context, Constants.MESSAGE_TYPE_PRESENTATION_QUERY, null, presentationDefinition);
    }

    static void validateScopeOnly(List<String> scope, com.fasterxml.jackson.databind.JsonNode presentationDefinition) {
        boolean hasScope = scope != null && !scope.isEmpty();
        boolean hasDefinition = hasPresentationDefinition(presentationDefinition);
        if (hasScope && hasDefinition) {
            throw new DcpException(new InvalidQueryMessage(
                    "Must contain either scope or presentationDefinition, not both"));
        }
        if (!hasScope) {
            throw new DcpException(new InvalidQueryMessage("scope array MUST be non-empty"));
        }
    }

    static void validatePresentationDefinitionOnly(
            List<String> scope, com.fasterxml.jackson.databind.JsonNode presentationDefinition) {
        boolean hasScope = scope != null && !scope.isEmpty();
        boolean hasDefinition = hasPresentationDefinition(presentationDefinition);
        if (hasScope && hasDefinition) {
            throw new DcpException(new InvalidQueryMessage(
                    "Must contain either scope or presentationDefinition, not both"));
        }
        if (!hasDefinition) {
            throw new DcpException(new InvalidQueryMessage("presentationDefinition MUST be present"));
        }
    }

    static boolean hasPresentationDefinition(com.fasterxml.jackson.databind.JsonNode presentationDefinition) {
        return presentationDefinition != null && !presentationDefinition.isNull() && !presentationDefinition.isEmpty();
    }

    static void requireNonEmptyPresentations(PresentationResponseMessage response) {
        if (response == null
                || response.presentation() == null
                || response.presentation().isEmpty()) {
            throw new DcpException(new InvalidPresentationResponse(
                    "presentation array is required and must be non-empty"));
        }
    }

    /** Validates an inbound {@link PresentationQueryMessage} on a holder Credential Service. */
    public static void requireQueryMessage(PresentationQueryMessage message) {
        if (message == null) {
            throw new DcpException(new InvalidQueryMessage("PresentationQueryMessage must not be null"));
        }
        if (!Constants.MESSAGE_TYPE_PRESENTATION_QUERY.equals(message.type())) {
            throw new DcpException(new InvalidQueryMessage(
                    "type MUST be " + Constants.MESSAGE_TYPE_PRESENTATION_QUERY));
        }
        boolean hasScope = message.scope() != null && !message.scope().isEmpty();
        boolean hasDefinition = hasPresentationDefinition(message.presentationDefinition());
        if (hasScope && hasDefinition) {
            throw new DcpException(new InvalidQueryMessage(
                    "Must contain either scope or presentationDefinition, not both"));
        }
        if (!hasScope && !hasDefinition) {
            throw new DcpException(new InvalidQueryMessage(
                    "Must contain either scope or presentationDefinition"));
        }
    }

    static void requirePresentationSubmission(PresentationResponseMessage response) {
        if (response.presentationSubmission() == null
                || response.presentationSubmission().isNull()
                || response.presentationSubmission().isEmpty()) {
            throw new DcpException(new InvalidPresentationResponse(
                    "presentationSubmission is required when the query used presentationDefinition"));
        }
    }

    /**
     * Construct-X EDC appends a non-normative operation segment ({@code read}, {@code write}, {@code *}, {@code all})
     * after the discriminator. DCP v1.0.1 defines scope as {@code [alias]:[discriminator]} only.
     */
    static final Set<String> EDC_SCOPE_OPERATIONS = Set.of("read", "write", "*", "all");
}
