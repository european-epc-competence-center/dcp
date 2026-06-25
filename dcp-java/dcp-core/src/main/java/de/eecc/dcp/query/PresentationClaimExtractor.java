package de.eecc.dcp.query;

import com.fasterxml.jackson.databind.JsonNode;
import de.eecc.dcp.claims.MapPresentationClaims;
import de.eecc.dcp.claims.PresentationClaims;
import de.eecc.dcp.exception.DcpException;
import de.eecc.dcp.exception.EmptyPresentationClaims;
import de.eecc.dcp.message.PresentationQueryMessage;
import de.eecc.dcp.message.PresentationResponseMessage;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Extracts claim values from JSON object presentations until the dedicated {@code PresentationParser} lands in Phase 6.
 */
final class PresentationClaimExtractor {

    private PresentationClaimExtractor() {}

    static PresentationClaims extract(List<JsonNode> presentations) {
        Map<String, Object> claims = new LinkedHashMap<>();
        int index = 0;
        for (JsonNode presentation : presentations) {
            if (presentation == null || presentation.isNull()) {
                continue;
            }
            if (presentation.isTextual()) {
                // JWT envelope — full parsing deferred to Phase 6 (PresentationParser).
                claims.put("presentation." + index, presentation.textValue());
            } else {
                extractFromObjectPresentation(presentation, "presentation." + index, claims);
            }
            index++;
        }

        if (claims.isEmpty()) {
            throw new DcpException(new EmptyPresentationClaims("no claims could be extracted from presentations"));
        }
        return new MapPresentationClaims(Map.copyOf(claims));
    }

    private static void extractFromObjectPresentation(JsonNode presentation, String prefix, Map<String, Object> claims) {
        JsonNode holder = presentation.get("holder");
        if (holder != null && !holder.isNull()) {
            claims.put(prefix + ".holder", holder.isTextual() ? holder.textValue() : holder);
        }

        JsonNode credentials = presentation.get("verifiableCredential");
        if (credentials == null || credentials.isNull()) {
            return;
        }

        if (credentials.isArray()) {
            for (int i = 0; i < credentials.size(); i++) {
                extractFromCredential(credentials.get(i), prefix + ".verifiableCredential[" + i + "]", claims);
            }
        } else {
            extractFromCredential(credentials, prefix + ".verifiableCredential", claims);
        }
    }

    private static void extractFromCredential(JsonNode credential, String prefix, Map<String, Object> claims) {
        if (credential == null || credential.isNull()) {
            return;
        }
        if (credential.isTextual()) {
            claims.put(prefix, credential.textValue());
            return;
        }

        JsonNode type = credential.get("type");
        if (type != null && !type.isNull()) {
            claims.put(prefix + ".type", jsonValue(type));
        }

        JsonNode issuer = credential.get("issuer");
        if (issuer != null && !issuer.isNull()) {
            claims.put(prefix + ".issuer", issuer.isTextual() ? issuer.textValue() : issuer);
        }

        JsonNode subject = credential.path("credentialSubject");
        if (!subject.isMissingNode() && !subject.isNull()) {
            claims.put(prefix + ".credentialSubject", jsonValue(subject));
        }
    }

    private static Object jsonValue(JsonNode node) {
        if (node.isTextual()) {
            return node.textValue();
        }
        if (node.isArray()) {
            var values = new java.util.ArrayList<Object>();
            for (JsonNode element : node) {
                values.add(jsonValue(element));
            }
            return values;
        }
        return node;
    }
}
