package de.eecc.dcp.query.template.gs1;

import com.fasterxml.jackson.databind.JsonNode;
import de.eecc.dcp.claims.PresentationClaims;
import de.eecc.dcp.message.PresentationQueryMessage;
import de.eecc.dcp.message.PresentationResponseMessage;
import de.eecc.dcp.query.DcpScope;
import de.eecc.dcp.query.PresentationQueryDefinition;
import de.eecc.dcp.query.ScopeQueryDefinition;
import de.eecc.dcp.vp.PresentationParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * DCP presentation query for GS1 Company Prefix or Prefix License credentials.
 *
 * <p>DCP does not use DCQL. Credential types are requested via normative
 * {@code org.eclipse.dspace.dcp.vc.type} scopes (see
 * <a href="https://eclipse-dataspace-dcp.github.io/decentralized-claims-protocol/v1.0.1/#scopes">DCP scopes</a>).
 * The Credential Service maps each scope to its internal Presentation Definition; claim selection is validated
 * after presentation, not in the query message.
 *
 * <p>Pass {@link #INSTANCE} wherever a {@link PresentationQueryDefinition} is required to request
 * {@code GS1CompanyPrefixLicenseCredential} or {@code GS1PrefixLicenseCredential} with
 * {@code licenseValue}, {@code organization.gs1:organizationName}, and {@code organization.gs1:partyGLN}.
 *
 * <p>Mirror of oid4vp {@code Gs1LicenseRequest}.
 */
public final class Gs1LicenseQueryDefinition implements PresentationQueryDefinition {

    private static final Logger log = LoggerFactory.getLogger(Gs1LicenseQueryDefinition.class);

    public static final Gs1LicenseQueryDefinition INSTANCE = new Gs1LicenseQueryDefinition();

    public static final String CLAIM_LICENSE_VALUE = "licenseValue";
    public static final String GS1_LICENSE_VALUE = "gs1:licenseValue";
    public static final String CLAIM_ORGANIZATION_NAME = "organizationName";
    public static final String CLAIM_PARTY_GLN = "partyGLN";

    public static final String TYPE_COMPANY_PREFIX = "GS1CompanyPrefixLicenseCredential";
    public static final String TYPE_PREFIX = "GS1PrefixLicenseCredential";

    public static final String SUBJECT_ORGANIZATION = "organization";
    public static final String GS1_ORGANIZATION_NAME = "gs1:organizationName";
    public static final String GS1_PARTY_GLN = "gs1:partyGLN";

    public static final String ALTERNATIVE_LICENSE_VALUE = "alternativeLicenseValue";

    public static final List<String> CREDENTIAL_TYPES = List.of(TYPE_COMPANY_PREFIX, TYPE_PREFIX);

    private final ScopeQueryDefinition scopeQuery;

    /**
     * GS1 license claims mapped to {@link PresentationClaims}.
     */
    public record Gs1PresentationClaims(
            String partyGln,
            String organizationName,
            List<String> gcps,
            String credentialType
    ) implements PresentationClaims {

        public Gs1PresentationClaims(String partyGln, String organizationName, List<String> gcps) {
            this(partyGln, organizationName, gcps, null);
        }

        @Override
        public String identifier() {
            return partyGln;
        }

        @Override
        public String name() {
            return organizationName;
        }

        @Override
        public List<String> values() {
            return gcps;
        }

        @Override
        public String credentialType() {
            return credentialType;
        }

        @Override
        public Map<String, Object> claimValues() {
            Map<String, Object> claims = new LinkedHashMap<>();
            claims.put(CLAIM_LICENSE_VALUE, List.copyOf(gcps));
            if (organizationName != null) {
                claims.put(CLAIM_ORGANIZATION_NAME, organizationName);
            }
            if (partyGln != null) {
                claims.put(CLAIM_PARTY_GLN, partyGln);
            }
            if (credentialType != null) {
                claims.put("credentialType", credentialType);
            }
            return Map.copyOf(claims);
        }
    }

    private Gs1LicenseQueryDefinition() {
        this.scopeQuery = ScopeQueryDefinition.of(
                DcpScope.vcType(TYPE_COMPANY_PREFIX),
                DcpScope.vcType(TYPE_PREFIX));
    }

    public List<DcpScope> scopes() {
        return scopeQuery.scopes();
    }

    @Override
    public PresentationQueryMessage toQueryMessage() {
        // using the scopeQuery toQueryMessage method to build the query message because better choice for GS1 License Query Definition
        // other query definitions may need to build the query message with presentationDefinition
        return scopeQuery.toQueryMessage();
    }

    @Override
    public void assertResponseMatches(PresentationResponseMessage response) {
        scopeQuery.assertResponseMatches(response);
    }

    @Override
    public PresentationClaims extractPresentationClaims(PresentationResponseMessage response) {
        assertResponseMatches(response);
        return extractPresentationClaims(response.presentation());
    }

    /**
     * Extracts GCP and organisation metadata from verified DCP {@code presentation[]} entries.
     */
    public PresentationClaims extractPresentationClaims(List<JsonNode> presentations) {
        List<String> extractedGcps = new ArrayList<>();
        String firstOrganizationName = null;
        String firstPartyGln = null;
        String firstCredentialType = null;
        LinkedHashSet<String> seenGcps = new LinkedHashSet<>();

        for (JsonNode presentationNode : presentations) {
            List<String> presentationGcps = extractLicenseValues(presentationNode);
            for (String gcp : presentationGcps) {
                String normalized = gcp == null ? "" : gcp.trim();
                if (normalized.isBlank()) {
                    continue;
                }
                if (seenGcps.add(normalized)) {
                    extractedGcps.add(normalized);
                }

                if (firstOrganizationName == null) {
                    firstOrganizationName = extractOrganizationName(presentationNode);
                    firstPartyGln = extractPartyGln(presentationNode);
                    firstCredentialType = extractCredentialType(presentationNode);
                }
            }
        }

        return new Gs1PresentationClaims(
                firstPartyGln, firstOrganizationName, extractedGcps, firstCredentialType);
    }

    public String extractCredentialType(JsonNode presentationNode) {
        return PresentationParser.extractCredentialType(presentationNode, CREDENTIAL_TYPES);
    }

    public String extractCredentialIssuer(JsonNode presentationNode) {
        return PresentationParser.extractIssuer(presentationNode);
    }

    public String extractCredentialSubjectId(JsonNode presentationNode) {
        return PresentationParser.extractSubjectId(presentationNode);
    }

    public static List<String> extractLicenseValues(JsonNode presentationNode) {
        List<JsonNode> subjects = findCredentialSubjectsWithLicense(presentationNode);
        if (subjects.isEmpty()) {
            log.warn("No credentialSubject found in presentation");
            return List.of();
        }

        LinkedHashSet<String> values = new LinkedHashSet<>();
        for (JsonNode subject : subjects) {
            String license = licenseValueFromSubject(subject);
            if (license != null) {
                values.add(license);
            }
        }

        if (values.isEmpty()) {
            log.warn("No licenseValue or alternativeLicenseValue found in presentation credentials");
        }
        return new ArrayList<>(values);
    }

    private static String licenseValueFromSubject(JsonNode credentialSubject) {
        if (credentialSubject == null || credentialSubject.isMissingNode()) {
            return null;
        }

        String license = textClaim(credentialSubject, CLAIM_LICENSE_VALUE);
        if (license != null) {
            return license;
        }

        license = textClaim(credentialSubject, GS1_LICENSE_VALUE);
        if (license != null) {
            return license;
        }

        return textClaim(credentialSubject, ALTERNATIVE_LICENSE_VALUE);
    }

    private static String textClaim(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull() || !value.isTextual()) {
            return null;
        }
        String text = value.asText().trim();
        return text.isBlank() ? null : text;
    }

    private static String extractOrganizationName(JsonNode presentationNode) {
        JsonNode credentialSubject = PresentationParser.findCredentialSubject(presentationNode);
        if (credentialSubject == null || credentialSubject.isMissingNode()) {
            return null;
        }

        JsonNode organization = credentialSubject.get(SUBJECT_ORGANIZATION);
        if (organization == null || organization.isMissingNode()) {
            return null;
        }

        JsonNode nameNode = organization.get(GS1_ORGANIZATION_NAME);
        if (nameNode == null || nameNode.isNull() || !nameNode.isTextual()) {
            return null;
        }

        String value = nameNode.asText().trim();
        return value.isBlank() ? null : value;
    }

    private static String extractPartyGln(JsonNode presentationNode) {
        JsonNode credentialSubject = PresentationParser.findCredentialSubject(presentationNode);
        if (credentialSubject == null || credentialSubject.isMissingNode()) {
            return null;
        }

        JsonNode organization = credentialSubject.get(SUBJECT_ORGANIZATION);
        if (organization == null || organization.isMissingNode()) {
            return null;
        }

        JsonNode glnNode = organization.get(GS1_PARTY_GLN);
        if (glnNode == null || glnNode.isNull() || !glnNode.isTextual()) {
            return null;
        }

        String value = glnNode.asText().trim();
        return value.isBlank() ? null : value;
    }

    private static List<JsonNode> findCredentialSubjectsWithLicense(JsonNode presentationNode) {
        return PresentationParser.collectCredentialSubjects(presentationNode).stream()
                .filter(Gs1LicenseQueryDefinition::hasLicenseClaim)
                .toList();
    }

    private static boolean hasLicenseClaim(JsonNode subject) {
        return textClaim(subject, CLAIM_LICENSE_VALUE) != null
                || textClaim(subject, GS1_LICENSE_VALUE) != null
                || textClaim(subject, ALTERNATIVE_LICENSE_VALUE) != null;
    }
}
