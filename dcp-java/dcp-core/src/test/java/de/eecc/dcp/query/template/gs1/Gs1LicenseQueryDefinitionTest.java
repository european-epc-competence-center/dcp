package de.eecc.dcp.query.template.gs1;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.eecc.dcp.Constants;
import de.eecc.dcp.claims.PresentationClaims;
import de.eecc.dcp.message.PresentationQueryMessage;
import de.eecc.dcp.message.PresentationResponseMessage;
import de.eecc.dcp.query.DcpScope;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class Gs1LicenseQueryDefinitionTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void toQueryMessage_requestsGs1LicenseTypesViaDcpScopes() {
        PresentationQueryMessage message = Gs1LicenseQueryDefinition.INSTANCE.toQueryMessage();

        assertThat(message.type()).isEqualTo(Constants.MESSAGE_TYPE_PRESENTATION_QUERY);
        assertThat(message.scope()).containsExactly(
                "org.eclipse.dspace.dcp.vc.type:GS1CompanyPrefixLicenseCredential",
                "org.eclipse.dspace.dcp.vc.type:GS1PrefixLicenseCredential");
        assertThat(message.presentationDefinition()).isNull();
    }

    @Test
    void scopesExposeNormativeVcTypeAlias() {
        assertThat(Gs1LicenseQueryDefinition.INSTANCE.scopes())
                .containsExactly(
                        DcpScope.vcType(Gs1LicenseQueryDefinition.TYPE_COMPANY_PREFIX),
                        DcpScope.vcType(Gs1LicenseQueryDefinition.TYPE_PREFIX));
    }

    @Test
    void extractLicenseValues_ldpPresentation() throws Exception {
        JsonNode node = MAPPER.readTree("""
                {
                  "type": ["VerifiablePresentation"],
                  "verifiableCredential": [{
                    "credentialSubject": {
                      "licenseValue": "0614141"
                    }
                  }]
                }
                """);

        assertThat(Gs1LicenseQueryDefinition.extractLicenseValues(node)).containsExactly("0614141");
    }

    @Test
    void extractLicenseValues_multipleCredentialsInOnePresentation() throws Exception {
        JsonNode node = MAPPER.readTree("""
                {
                  "type": ["VerifiablePresentation"],
                  "verifiableCredential": [
                    {
                      "type": ["VerifiableCredential", "GS1CompanyPrefixLicenseCredential"],
                      "credentialSubject": { "licenseValue": "0614141" }
                    },
                    {
                      "type": ["VerifiableCredential", "GS1CompanyPrefixLicenseCredential"],
                      "credentialSubject": { "licenseValue": "0614142" }
                    }
                  ]
                }
                """);

        assertThat(Gs1LicenseQueryDefinition.extractLicenseValues(node)).containsExactly("0614141", "0614142");
    }

    @Test
    void extractLicenseValues_jwtVpPresentation() throws Exception {
        String payloadJson = """
                {
                  "vp": {
                    "verifiableCredential": [{
                      "credentialSubject": { "licenseValue": "095100" }
                    }]
                  }
                }
                """;
        String jwt = "eyJhbGciOiJub25lIn0."
                + Base64.getUrlEncoder().withoutPadding()
                        .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8))
                + ".sig";
        JsonNode node = MAPPER.readTree("\"" + jwt + "\"");

        assertThat(Gs1LicenseQueryDefinition.extractLicenseValues(node)).containsExactly("095100");
    }

    @Test
    void extractCredentialType_companyPrefixLicense() throws Exception {
        JsonNode node = MAPPER.readTree("""
                {
                  "type": ["VerifiablePresentation"],
                  "verifiableCredential": [{
                    "type": ["VerifiableCredential", "GS1CompanyPrefixLicenseCredential"],
                    "issuer": "did:example:issuer",
                    "credentialSubject": {
                      "id": "did:example:holder",
                      "licenseValue": "0614141"
                    }
                  }]
                }
                """);

        assertThat(Gs1LicenseQueryDefinition.INSTANCE.extractCredentialType(node))
                .isEqualTo(Gs1LicenseQueryDefinition.TYPE_COMPANY_PREFIX);
        assertThat(Gs1LicenseQueryDefinition.INSTANCE.extractCredentialIssuer(node))
                .isEqualTo("did:example:issuer");
        assertThat(Gs1LicenseQueryDefinition.INSTANCE.extractCredentialSubjectId(node))
                .isEqualTo("did:example:holder");
    }

    @Test
    void extractPresentationClaims_fromDcpResponse() throws Exception {
        JsonNode presentation = MAPPER.readTree("""
                {
                  "type": ["VerifiablePresentation"],
                  "verifiableCredential": [{
                    "type": ["VerifiableCredential", "GS1PrefixLicenseCredential"],
                    "credentialSubject": {
                      "licenseValue": "095100",
                      "organization": {
                        "gs1:organizationName": "Acme GS1",
                        "gs1:partyGLN": "9501100000000"
                      }
                    }
                  }]
                }
                """);

        PresentationResponseMessage response = new PresentationResponseMessage(
                List.of(Constants.DCP_JSON_LD_CONTEXT),
                Constants.MESSAGE_TYPE_PRESENTATION_RESPONSE,
                List.of(presentation),
                null);

        PresentationClaims claims = Gs1LicenseQueryDefinition.INSTANCE.extractPresentationClaims(response);

        assertThat(claims.values()).containsExactly("095100");
        assertThat(claims.identifier()).isEqualTo("9501100000000");
        assertThat(claims.name()).isEqualTo("Acme GS1");
        assertThat(claims.credentialType()).isEqualTo(Gs1LicenseQueryDefinition.TYPE_PREFIX);
    }
}
