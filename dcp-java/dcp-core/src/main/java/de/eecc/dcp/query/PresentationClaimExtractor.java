package de.eecc.dcp.query;

import com.fasterxml.jackson.databind.JsonNode;
import de.eecc.dcp.claims.MapPresentationClaims;
import de.eecc.dcp.claims.PresentationClaims;
import de.eecc.dcp.exception.DcpException;
import de.eecc.dcp.exception.EmptyPresentationClaims;
import de.eecc.dcp.vp.PresentationParser;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Extracts claim values from JSON-object or JWT presentations via {@link PresentationParser}.
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
            JsonNode root = PresentationParser.presentationRoot(presentation);
            if (root != null && root.isObject()) {
                extractFromObjectPresentation(root, "presentation." + index, claims);
            } else if (presentation.isTextual()) {
                claims.put("presentation." + index, presentation.textValue());
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
            JsonNode vp = presentation.get("vp");
            if (vp != null && vp.isObject()) {
                credentials = vp.get("verifiableCredential");
            }
        }
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
            String compactJwt = PresentationParser.compactJwtFromTextualCredential(credential.textValue());
            if (compactJwt != null) {
                JsonNode payload = PresentationParser.parseJwtPayload(compactJwt);
                if (payload != null && payload.isObject()) {
                    extractFromCredential(payload, prefix, claims);
                    return;
                }
            }
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
