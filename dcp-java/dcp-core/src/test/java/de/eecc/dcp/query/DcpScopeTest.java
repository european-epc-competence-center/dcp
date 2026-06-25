package de.eecc.dcp.query;

import de.eecc.dcp.Constants;
import de.eecc.dcp.exception.DcpException;
import de.eecc.dcp.exception.InvalidQueryMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DcpScopeTest {

    @Test
    void vcTypeFactoryUsesNormativeAlias() {
        DcpScope scope = DcpScope.vcType("Member");

        assertThat(scope.alias()).isEqualTo(Constants.SCOPE_ALIAS_VC_TYPE);
        assertThat(scope.discriminator()).isEqualTo("Member");
        assertThat(scope.toScopeString()).isEqualTo("org.eclipse.dspace.dcp.vc.type:Member");
    }

    @Test
    void vcIdFactoryUsesNormativeAlias() {
        DcpScope scope = DcpScope.vcId("8247b87d-8d72-47e1-8128-9ce47e3d829d");

        assertThat(scope.alias()).isEqualTo(Constants.SCOPE_ALIAS_VC_ID);
        assertThat(scope.toScopeString())
                .isEqualTo("org.eclipse.dspace.dcp.vc.id:8247b87d-8d72-47e1-8128-9ce47e3d829d");
    }

    @Test
    void parseSpecTwoPartScope() {
        DcpScope scope = DcpScope.parse("org.eclipse.dspace.dcp.vc.type:Member");

        assertThat(scope.alias()).isEqualTo(Constants.SCOPE_ALIAS_VC_TYPE);
        assertThat(scope.discriminator()).isEqualTo("Member");
    }

    @Test
    void parseStripsEdcOperationSuffix() {
        // Construct-X EDC uses alias:discriminator:operation; DCP spec defines alias:discriminator only.
        DcpScope scope = DcpScope.parse("org.eclipse.dspace.dcp.vc.type:AlumniCredential:read");

        assertThat(scope.discriminator()).isEqualTo("AlumniCredential");
        assertThat(scope.toScopeString()).isEqualTo("org.eclipse.dspace.dcp.vc.type:AlumniCredential");
    }

    @Test
    void parseRetainsFqctDiscriminatorWithHash() {
        DcpScope scope = DcpScope.parse(
                "org.eclipse.dspace.dcp.vc.type:https://example.com/contexts/v1#TestCredential:read");

        assertThat(scope.discriminator()).isEqualTo("https://example.com/contexts/v1#TestCredential");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "invalid", "alias-only:", ":missing-alias"})
    void parseRejectsInvalidScope(String scope) {
        assertThatThrownBy(() -> DcpScope.parse(scope))
                .isInstanceOf(DcpException.class)
                .satisfies(ex -> assertThat(((DcpException) ex).error()).isInstanceOf(InvalidQueryMessage.class));
    }
}
