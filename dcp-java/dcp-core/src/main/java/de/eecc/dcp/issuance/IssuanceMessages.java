package de.eecc.dcp.issuance;

import de.eecc.dcp.Constants;
import de.eecc.dcp.exception.DcpException;
import de.eecc.dcp.exception.InvalidCredentialRequest;
import de.eecc.dcp.exception.InvalidOfferMessage;
import de.eecc.dcp.message.CredentialObject;
import de.eecc.dcp.message.CredentialOfferMessage;
import de.eecc.dcp.message.CredentialRequestMessage;
import de.eecc.dcp.message.CredentialRequestReference;
import de.eecc.dcp.message.CredentialMessage;

import java.util.List;
import java.util.Objects;

/**
 * Shared helpers for building and validating credential issuance messages.
 */
public final class IssuanceMessages {

    private static final String DID_CONTEXT = "https://www.w3.org/ns/did/v1";

    private IssuanceMessages() {}

    static List<Object> defaultContext() {
        return List.of(Constants.DCP_JSON_LD_CONTEXT, DID_CONTEXT);
    }

    static CredentialOfferMessage credentialOffer(
            List<Object> context, String issuerDid, List<CredentialObject> credentials) {
        validateOffer(issuerDid, credentials);
        return new CredentialOfferMessage(
                context, Constants.MESSAGE_TYPE_CREDENTIAL_OFFER, issuerDid, credentials);
    }

    static CredentialRequestMessage credentialRequest(
            List<Object> context, String holderPid, List<CredentialRequestReference> credentials) {
        validateCredentialRequest(holderPid, credentials);
        return new CredentialRequestMessage(
                context, Constants.MESSAGE_TYPE_CREDENTIAL_REQUEST, holderPid, credentials);
    }

    static CredentialObject credentialObject(
            String id,
            String credentialType,
            String profile,
            String offerReason,
            com.fasterxml.jackson.databind.JsonNode issuancePolicy) {
        Objects.requireNonNull(id, "id");
        if (id.isBlank()) {
            throw new DcpException(new InvalidOfferMessage("credential object id must not be blank"));
        }
        return new CredentialObject(
                id,
                Constants.MESSAGE_TYPE_CREDENTIAL_OBJECT,
                credentialType,
                null,
                offerReason,
                null,
                profile,
                issuancePolicy);
    }

    static void validateOffer(String issuerDid, List<CredentialObject> credentials) {
        if (issuerDid == null || issuerDid.isBlank()) {
            throw new DcpException(new InvalidOfferMessage("issuer MUST be present"));
        }
        if (credentials == null || credentials.isEmpty()) {
            throw new DcpException(new InvalidOfferMessage("credentials array MUST be non-empty"));
        }
        for (CredentialObject credential : credentials) {
            if (credential.id() == null || credential.id().isBlank()) {
                throw new DcpException(new InvalidOfferMessage("each credential object MUST have an id"));
            }
            if (!Constants.MESSAGE_TYPE_CREDENTIAL_OBJECT.equals(credential.type())) {
                throw new DcpException(new InvalidOfferMessage(
                        "credential object type MUST be " + Constants.MESSAGE_TYPE_CREDENTIAL_OBJECT));
            }
        }
    }

    static void validateCredentialRequest(String holderPid, List<CredentialRequestReference> credentials) {
        if (holderPid == null || holderPid.isBlank()) {
            throw new DcpException(new InvalidCredentialRequest("holderPid MUST be present"));
        }
        if (credentials == null || credentials.isEmpty()) {
            throw new DcpException(new InvalidCredentialRequest("credentials array MUST be non-empty"));
        }
        for (CredentialRequestReference credential : credentials) {
            if (credential.id() == null || credential.id().isBlank()) {
                throw new DcpException(new InvalidCredentialRequest("each credential reference MUST have an id"));
            }
        }
    }

    static void requireOfferMessage(CredentialOfferMessage message) {
        if (message == null) {
            throw new DcpException(new InvalidOfferMessage("CredentialOfferMessage must not be null"));
        }
        if (!Constants.MESSAGE_TYPE_CREDENTIAL_OFFER.equals(message.type())) {
            throw new DcpException(new InvalidOfferMessage(
                    "type MUST be " + Constants.MESSAGE_TYPE_CREDENTIAL_OFFER));
        }
        validateOffer(message.issuer(), message.credentials());
    }

    static void requireCredentialRequestMessage(CredentialRequestMessage message) {
        if (message == null) {
            throw new DcpException(new InvalidCredentialRequest("CredentialRequestMessage must not be null"));
        }
        if (!Constants.MESSAGE_TYPE_CREDENTIAL_REQUEST.equals(message.type())) {
            throw new DcpException(new InvalidCredentialRequest(
                    "type MUST be " + Constants.MESSAGE_TYPE_CREDENTIAL_REQUEST));
        }
        validateCredentialRequest(message.holderPid(), message.credentials());
    }

    /** Validates an inbound {@link CredentialMessage} on a holder Credential Service. */
    public static void requireCredentialMessage(CredentialMessage message) {
        if (message == null) {
            throw new DcpException(new InvalidCredentialRequest("CredentialMessage must not be null"));
        }
        if (!Constants.MESSAGE_TYPE_CREDENTIAL_MESSAGE.equals(message.type())) {
            throw new DcpException(new InvalidCredentialRequest(
                    "type MUST be " + Constants.MESSAGE_TYPE_CREDENTIAL_MESSAGE));
        }
        if (message.issuerPid() == null || message.issuerPid().isBlank()) {
            throw new DcpException(new InvalidCredentialRequest("issuerPid MUST be present"));
        }
        if (message.holderPid() == null || message.holderPid().isBlank()) {
            throw new DcpException(new InvalidCredentialRequest("holderPid MUST be present"));
        }
        if (message.status() == null || message.status().isBlank()) {
            throw new DcpException(new InvalidCredentialRequest("status MUST be present"));
        }
        if (!Constants.CREDENTIAL_STATUS_ISSUED.equals(message.status())
                && !Constants.CREDENTIAL_STATUS_REJECTED.equals(message.status())) {
            throw new DcpException(new InvalidCredentialRequest(
                    "status MUST be " + Constants.CREDENTIAL_STATUS_ISSUED + " or "
                            + Constants.CREDENTIAL_STATUS_REJECTED));
        }
        if (Constants.CREDENTIAL_STATUS_ISSUED.equals(message.status())
                && (message.credentials() == null || message.credentials().isEmpty())) {
            throw new DcpException(new InvalidCredentialRequest(
                    "credentials array MUST be non-empty when status is ISSUED"));
        }
    }
}
