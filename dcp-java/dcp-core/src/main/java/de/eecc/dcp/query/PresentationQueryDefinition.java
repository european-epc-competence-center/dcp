package de.eecc.dcp.query;

import com.fasterxml.jackson.databind.JsonNode;
import de.eecc.dcp.claims.PresentationClaims;
import de.eecc.dcp.exception.DcpException;
import de.eecc.dcp.exception.InvalidPresentationResponse;
import de.eecc.dcp.message.PresentationQueryMessage;
import de.eecc.dcp.message.PresentationResponseMessage;
import de.eecc.dcp.vp.PresentationParser;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Describes what a verifier asks for in a DCP presentation exchange.
 *
 * <p>Mirror of oid4vp's {@code PresentationRequestDefinition}.
 *
 * <p>Optional constraints ({@link #requiresIssuer(String)}, {@link #requiresSubjectId(String)}, …)
 * are enforced by {@link #assertQueryConstraints(PresentationResponseMessage)} when validating a
 * presentation response. DCP scope queries cannot encode all constraints on the wire; Credential
 * Services map type scopes locally, and this library asserts the caller's constraints after
 * presentation.
 */
public interface PresentationQueryDefinition {

    PresentationQueryMessage toQueryMessage();

    /**
     * Validates that the response matches this query: structural shape plus all declared constraints.
     */
    default void assertResponseMatches(PresentationResponseMessage response) {
        root().assertResponseStructure(response);
        assertQueryConstraints(response);
    }

    /**
     * Structural response checks for this query (non-empty presentations, PE submission, …).
     *
     * <p>Override in scope / Presentation Exchange / template definitions. Do not put optional
     * caller constraints here; those belong in {@link #assertQueryConstraints(PresentationResponseMessage)}.
     */
    default void assertResponseStructure(PresentationResponseMessage response) {
        QueryMessages.requireNonEmptyPresentations(response);
    }

    /**
     * Asserts every constraint declared on this query that is present (non-empty).
     *
     * <p>Default checks {@link #requiredIssuers()} and {@link #requiredSubjectIds()}. Override to
     * add further constraint types; call {@code PresentationQueryDefinition.super.assertQueryConstraints(response)}
     * when extending.
     */
    default void assertQueryConstraints(PresentationResponseMessage response) {
        assertConstraintMatched(
                response, requiredIssuers(), PresentationParser::extractIssuer, "issuer");
        assertConstraintMatched(
                response, requiredSubjectIds(), PresentationParser::extractSubjectId, "subject id");
    }

    /**
     * Validates the response then extracts claims from presentations.
     *
     * <p>Override when extraction is template-specific (e.g. GS1 license claims).
     */
    default PresentationClaims extractPresentationClaims(PresentationResponseMessage response) {
        assertResponseMatches(response);
        return PresentationClaimExtractor.extract(response.presentation());
    }

    /**
     * Root template when this definition was produced by chaining optional constraints.
     */
    default PresentationQueryDefinition root() {
        return this;
    }

    /**
     * Required credential issuer DIDs (OR semantics). Empty means no issuer constraint.
     */
    default List<String> requiredIssuers() {
        return List.of();
    }

    /**
     * Required credential subject ids (OR semantics). Empty means no subject constraint.
     */
    default List<String> requiredSubjectIds() {
        return List.of();
    }

    /**
     * Returns a definition that requires the presented credential issuer to be one of the given DIDs.
     */
    default PresentationQueryDefinition requiresIssuers(List<String> requiredIssuers) {
        return withConstraints(root(), requiredIssuers, requiredSubjectIds());
    }

    /**
     * Returns a definition that requires the presented credential subject id to be one of the given values.
     */
    default PresentationQueryDefinition requiresSubjectIds(List<String> requiredSubjectIds) {
        return withConstraints(root(), requiredIssuers(), requiredSubjectIds);
    }

    /** Convenience for {@link #requiresIssuers(List)} with a single issuer DID. */
    default PresentationQueryDefinition requiresIssuer(String requiredIssuer) {
        return requiresIssuers(List.of(requiredIssuer));
    }

    /** Convenience for {@link #requiresSubjectIds(List)} with a single subject id. */
    default PresentationQueryDefinition requiresSubjectId(String requiredSubjectId) {
        return requiresSubjectIds(List.of(requiredSubjectId));
    }

    private static PresentationQueryDefinition withConstraints(
            PresentationQueryDefinition root,
            List<String> requiredIssuers,
            List<String> requiredSubjectIds) {
        List<String> issuers = normalizeConstraintValues(requiredIssuers);
        List<String> subjectIds = normalizeConstraintValues(requiredSubjectIds);
        if (issuers.isEmpty() && subjectIds.isEmpty()) {
            return root;
        }
        return new PresentationQueryDefinition() {
            @Override
            public PresentationQueryDefinition root() {
                return root;
            }

            @Override
            public PresentationQueryMessage toQueryMessage() {
                return root.toQueryMessage();
            }

            @Override
            public PresentationClaims extractPresentationClaims(PresentationResponseMessage response) {
                assertResponseMatches(response);
                return root.extractPresentationClaims(response);
            }

            @Override
            public List<String> requiredIssuers() {
                return issuers;
            }

            @Override
            public List<String> requiredSubjectIds() {
                return subjectIds;
            }
        };
    }

    private static void assertConstraintMatched(
            PresentationResponseMessage response,
            List<String> requiredValues,
            Function<JsonNode, String> extractor,
            String constraintName) {
        if (requiredValues == null || requiredValues.isEmpty()) {
            return;
        }

        List<JsonNode> presentations = response.presentation();
        if (presentations == null || presentations.isEmpty()) {
            return;
        }

        for (JsonNode presentation : presentations) {
            String actual = extractor.apply(presentation);
            if (actual == null || !requiredValues.contains(actual)) {
                throw new DcpException(new InvalidPresentationResponse(
                        "presented credential " + constraintName
                                + " does not match required " + constraintName + "(s) "
                                + requiredValues));
            }
        }
    }

    private static List<String> normalizeConstraintValues(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .distinct()
                .toList();
    }
}
