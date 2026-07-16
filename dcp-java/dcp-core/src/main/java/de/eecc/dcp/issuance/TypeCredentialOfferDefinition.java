package de.eecc.dcp.issuance;

import com.fasterxml.jackson.databind.JsonNode;
import de.eecc.dcp.exception.DcpException;
import de.eecc.dcp.exception.InvalidOfferMessage;
import de.eecc.dcp.exception.InvalidCredentialRequest;
import de.eecc.dcp.message.CredentialObject;
import de.eecc.dcp.message.CredentialOfferMessage;
import de.eecc.dcp.message.CredentialRequestMessage;
import de.eecc.dcp.message.CredentialRequestReference;

import java.util.List;
import java.util.Objects;

/**
 * Credential-type offer per DCP v1.0.1 {@link CredentialObject} semantics.
 */
public final class TypeCredentialOfferDefinition implements CredentialOfferDefinition {

    private final String issuerDid;
    private final List<OfferedCredential> credentials;
    private final List<Object> context;

    public TypeCredentialOfferDefinition(String issuerDid, List<OfferedCredential> credentials) {
        this(issuerDid, credentials, IssuanceMessages.defaultContext());
    }

    public TypeCredentialOfferDefinition(String issuerDid, List<OfferedCredential> credentials, List<Object> context) {
        this.issuerDid = Objects.requireNonNull(issuerDid, "issuerDid");
        this.credentials = List.copyOf(Objects.requireNonNull(credentials, "credentials"));
        this.context = List.copyOf(Objects.requireNonNull(context, "context"));
        IssuanceMessages.validateOffer(issuerDid, credentialObjects());
    }

    public static TypeCredentialOfferDefinition of(String issuerDid, OfferedCredential... credentials) {
        return new TypeCredentialOfferDefinition(issuerDid, List.of(credentials));
    }

    public String issuerDid() {
        return issuerDid;
    }

    public List<OfferedCredential> credentials() {
        return credentials;
    }

    @Override
    public CredentialOfferMessage toOfferMessage() {
        return IssuanceMessages.credentialOffer(context, issuerDid, credentialObjects());
    }

    @Override
    public CredentialRequestMessage toRequestMessage(String holderPid) {
        List<CredentialRequestReference> references =
                credentials.stream().map(OfferedCredential::id).map(CredentialRequestReference::new).toList();
        return IssuanceMessages.credentialRequest(context, holderPid, references);
    }

    @Override
    public void assertOfferMatches(CredentialOfferMessage message) {
        IssuanceMessages.requireOfferMessage(message);
        if (!issuerDid.equals(message.issuer())) {
            throw new DcpException(new InvalidOfferMessage("issuer does not match expected " + issuerDid));
        }
        if (message.credentials().size() != credentials.size()) {
            throw new DcpException(new InvalidOfferMessage(
                    "expected " + credentials.size() + " credential objects, got " + message.credentials().size()));
        }
        for (int i = 0; i < credentials.size(); i++) {
            OfferedCredential expected = credentials.get(i);
            CredentialObject actual = message.credentials().get(i);
            if (!expected.id().equals(actual.id())) {
                throw new DcpException(new InvalidOfferMessage(
                        "credential object id mismatch at index " + i + ": expected " + expected.id()));
            }
            if (expected.credentialType() != null
                    && !expected.credentialType().equals(actual.credentialType())) {
                throw new DcpException(new InvalidOfferMessage(
                        "credentialType mismatch for " + expected.id()));
            }
            if (expected.profile() != null && !expected.profile().equals(actual.profile())) {
                throw new DcpException(new InvalidOfferMessage("profile mismatch for " + expected.id()));
            }
        }
    }

    @Override
    public void assertRequestMatches(CredentialRequestMessage message) {
        IssuanceMessages.requireCredentialRequestMessage(message);
        var offeredIds = credentials.stream().map(OfferedCredential::id).toList();
        for (CredentialRequestReference reference : message.credentials()) {
            if (!offeredIds.contains(reference.id())) {
                throw new DcpException(new InvalidCredentialRequest(
                        "credential id " + reference.id() + " is not part of this offer"));
            }
        }
    }

    private List<CredentialObject> credentialObjects() {
        return credentials.stream()
                .map(credential -> IssuanceMessages.credentialObject(
                        credential.id(),
                        credential.credentialType(),
                        credential.profile(),
                        credential.offerReason(),
                        credential.issuancePolicy()))
                .toList();
    }

    /**
     * One credential entry in an offer.
     *
     * @param id stable URI identifying this credential object in subsequent request/delivery messages
     */
    public record OfferedCredential(
            String id,
            String credentialType,
            String profile,
            String offerReason,
            JsonNode issuancePolicy) {

        public OfferedCredential {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(credentialType, "credentialType");
        }

        public static OfferedCredential ofType(String id, String credentialType) {
            return new OfferedCredential(id, credentialType, null, null, null);
        }

        public static OfferedCredential ofType(String id, String credentialType, String profile) {
            return new OfferedCredential(id, credentialType, profile, null, null);
        }
    }

    @Override
    public String toString() {
        return "TypeCredentialOfferDefinition" + credentials;
    }
}
