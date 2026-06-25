# DCP Java Library — Implementation Plan

Comprehensive plan for a Java library analogous to `oid4vp`, implementing the **Verifier-side** (and optionally **Credential Service** holder-side) of the [Eclipse Decentralized Claims Protocol (DCP) v1.0.1](https://eclipse-dataspace-dcp.github.io/decentralized-claims-protocol/v1.0.1/) **Verifiable Presentation Protocol** and the **issuer-side offer flow** of the **Credential Issuance Protocol (CIP)**.

## 1. Goal and scope

### In scope

**Verifiable Presentation Protocol (presentation-focused)**

- Validate **Self-Issued ID Tokens** (SI tokens) from clients
- Discover holder **Credential Service** endpoints from DID documents
- Build and send **`PresentationQueryMessage`** to `POST /presentations/query`
- Parse **`PresentationResponseMessage`**, validate VPs/VCs per DCP
- Extract **`PresentationClaims`** from verified presentations
- Pluggable DID resolution, HTTP client, session store, validation backend
- Optional Spring Boot auto-configuration (mirror `oid4vp-spring`)

**Credential Issuance Protocol — offer flow (issuer-side, phase 1b)**

- Build **`CredentialOfferMessage`** with **`CredentialObject`** entries
- **`CredentialOfferDefinition`** interface (mirror of `PresentationQueryDefinition`)
- **`TypeCredentialOfferDefinition`** for credential-type offers
- Build **`CredentialRequestMessage`** to redeem a prior offer (holder-side initiation)
- Wire DTOs: **`CredentialMessage`** (delivery), **`CredentialContainer`**
- Pluggable **`CredentialStorageClient`** (`POST /credentials` on holder CS) and **`IssuerServiceClient`** (`POST /issuance`)
- **`DcpIssuance`** facade (mirror of `DcpPresentation`)

### Out of scope (separate modules / later)

- Full asynchronous issuance pipeline (attestation, approval, signing, status polling)
- Issuer metadata API, credential request status API, credential revocation admin APIs
- Holder-side **`CredentialStorageHandler`** for incoming offers (phase 2+ extension in `de.eecc.dcp.service`)
- Full **Secure Token Service (STS)** implementation (only client DTOs + optional HTTP client for OAuth2 client-credentials)
- **DSP** contract negotiation, catalog federation, ODRL policy evaluation
- VC type/schema definitions (dataspace-specific)
- Identity Hub / EDC connector integration (consumers wire the library in)

### Reference implementation patterns

When implementing code, consult the **Construct-X EDC DCP SPI** module for Java types, naming, and protocol wiring:

- https://github.com/project-construct-x/constructx-edc/tree/develop/spi/dcp-spi

Mirror existing `oid4vp` conventions:

| oid4vp | Planned DCP equivalent |
|--------|------------------------|
| `Oid4Vp` | `DcpPresentation` |
| `PresentationRequest` | `PresentationSession` |
| `PresentationRequestDefinition` | `PresentationQueryDefinition` |
| `PresentationRequestRepository` | `PresentationSessionRepository` |
| `PresentationVerifier` | `CredentialServiceClient` + `PresentationValidator` |
| `DirectPostHandler` | `PresentationReceivedHandler` |
| `DcqlQuery` | DCP scopes + Presentation Exchange definitions |
| `VpTokenResponse` | `PresentationResponseMessage` (+ optional normalized internal model) |
| `Oid4VpError` (sealed) | `DcpError` (sealed) |
| — | `DcpIssuance` (CIP offer facade) |
| — | `CredentialOfferDefinition` |

---

## 2. Normative specifications and web references

### Primary DCP documents

| Document | URL | Relevance |
|----------|-----|-----------|
| DCP v1.0.1 (rendered spec) | https://eclipse-dataspace-dcp.github.io/decentralized-claims-protocol/v1.0.1/ | Master spec |
| DCP GitHub repository | https://github.com/eclipse-dataspace-dcp/decentralized-claims-protocol | Source markdown, schemas, OpenAPI |
| Verifiable Presentation Protocol | https://github.com/eclipse-dataspace-dcp/decentralized-claims-protocol/blob/main/specifications/verifiable.presentation.protocol.md | Core presentation flow, Resolution API |
| Base protocol (SI tokens, JSON-LD) | https://github.com/eclipse-dataspace-dcp/decentralized-claims-protocol/blob/main/specifications/base.protocol.md | SI token claims, validation, context URI |
| Terminology | https://github.com/eclipse-dataspace-dcp/decentralized-claims-protocol/blob/main/specifications/terminology.md | Holder, Verifier, Credential Service, etc. |
| DCP profiles | https://github.com/eclipse-dataspace-dcp/decentralized-claims-protocol/blob/main/specifications/dcp.profiles.md | `vc20-bssl/jwt`, `vc11-sl2021/jwt` |
| DSP profile (catalog trust metadata) | https://github.com/eclipse-dataspace-dcp/decentralized-claims-protocol/blob/main/specifications/dsp.profile.md | `/.well-known/dspace-trust` (optional integration) |
| Credential Issuance Protocol | https://github.com/eclipse-dataspace-dcp/decentralized-claims-protocol/blob/main/specifications/credential.issuance.protocol.md | Offer, request, delivery on CS and issuer endpoints |
| STS OpenAPI (non-normative) | https://github.com/eclipse-dataspace-dcp/decentralized-claims-protocol/blob/main/specifications/identity-trust-sts-api.yaml | OAuth2 client-credentials for SI tokens |
| DCP Technology Compatibility Kit | https://github.com/eclipse-dataspacetck/dcp-tck | Compliance testing target |

### JSON Schemas (published)

| Schema | URL |
|--------|-----|
| PresentationQueryMessage | https://eclipse-dataspace-dcp.github.io/decentralized-claims-protocol/v1.0.1/resources/presentation/presentation-query-message-schema.json |
| PresentationResponseMessage | https://eclipse-dataspace-dcp.github.io/decentralized-claims-protocol/v1.0.1/resources/presentation/presentation-response-message-schema.json |
| CredentialOfferMessage | https://eclipse-dataspace-dcp.github.io/decentralized-claims-protocol/v1.0.1/resources/issuance/credential-offer-message-schema.json |
| CredentialObject | https://eclipse-dataspace-dcp.github.io/decentralized-claims-protocol/v1.0.1/resources/issuance/credential-object-schema.json |
| CredentialRequestMessage | https://eclipse-dataspace-dcp.github.io/decentralized-claims-protocol/v1.0.1/resources/issuance/credential-request-message-schema.json |
| CredentialMessage | https://eclipse-dataspace-dcp.github.io/decentralized-claims-protocol/v1.0.1/resources/issuance/credential-message-schema.json |
| DCP JSON-LD context | https://w3id.org/dspace-dcp/v1.0/dcp.jsonld |

### External dependencies (referenced by DCP)

| Standard | URL | Use in library |
|----------|-----|----------------|
| DID Core | https://www.w3.org/TR/did-core/ | Service discovery, verification relationships |
| JSON-LD 1.1 | https://www.w3.org/TR/json-ld11/ | Message serialization; compact vs expanded `type` |
| JWT (RFC 7519) | https://datatracker.ietf.org/doc/html/rfc7519 | Self-Issued ID Token structure |
| OAuth 2.0 (RFC 6749) | https://datatracker.ietf.org/doc/html/rfc6749 | STS client-credentials (optional) |
| Presentation Exchange 2.1.1 | https://identity.foundation/presentation-exchange/spec/v2.1.1/ | `presentationDefinition`, `presentationSubmission` |
| PE Presentation Definition schema | https://identity.foundation/presentation-exchange/schemas/presentation-definition.json | Validate PE payloads |
| PE Presentation Submission schema | https://identity.foundation/presentation-exchange/schemas/presentation-submission.json | Response validation |
| VC Data Model 2.0 | https://www.w3.org/TR/vc-data-model-2.0/ | Profile `vc20-bssl/jwt` |
| VC Data Model 1.1 | https://www.w3.org/TR/vc-data-model/ | Profile `vc11-sl2021/jwt` |
| VC JOSE (enveloped proofs) | https://www.w3.org/TR/vc-jose-cose/#with-jose | JWT VP/VC parsing |
| BitStringStatusList | https://www.w3.org/TR/vc-bitstring-status-list/ | Revocation for `vc20-bssl/jwt` |
| StatusList2021 (WD) | https://www.w3.org/TR/2023/WD-vc-status-list-20230427/ | Revocation for `vc11-sl2021/jwt` |
| Dataspace Protocol | https://eclipse-dataspace-protocol-base.github.io/DataspaceProtocol/ | Ecosystem context (DSP overlay) |

### Ecosystem / reference implementations (non-normative)

| Resource | URL | Notes |
|----------|-----|-------|
| Eclipse DCP project page | https://projects.eclipse.org/projects/technology.dataspace-dcp | Governance, release 1.0.0 (2025-07-18) |
| EDC Identity Trust / CredentialServiceClient | https://github.com/eclipse-edc/Connector/tree/main/extensions/common/iam/identity-trust | Java reference; `PresentationQueryMessage` naming, JSON-LD compaction |
| Construct-X EDC DCP SPI | https://github.com/project-construct-x/constructx-edc/tree/develop/spi/dcp-spi | Primary in-code implementation reference for DCP SPI types and conventions |
| EDC issue: `@type` vs `type` on wire | https://github.com/eclipse-edc/Connector/issues/5209 | Implement compact JSON-LD on egress; accept both on ingress |

---

## 3. Protocol model vs OID4VP

### OID4VP (current `oid4vp` library) — push model

```
Verifier → wallet URL (openid4vp://) → Wallet → direct_post vp_token → Verifier
Query language: DCQL (dcql_query)
Binding: nonce + client_id (audience)
```

Key entry points: `Oid4Vp.generatePresentationRequest()`, `processDirectPost()`, `extractPresentationClaims()`.

### DCP — pull model

```
Client → Verifier API (Bearer SI token, optional token claim)
Verifier → validates SI token, resolves holder DID
Verifier → discovers Credential Service from DID document
Verifier → POST /presentations/query (Bearer verifier SI token, forwards token claim)
Credential Service → PresentationResponseMessage
Verifier → validates presentations locally
```

**No** `openid4vp://` URL, **no** `state`/`nonce`/`response_uri`/`vp_token` map. Correlation uses SI token claims (`sub`, `jti`, `aud`) and optional opaque access `token`.

### Presentation flow (normative sequence)

From [Verifiable Presentation Protocol — Presentation Flow](https://eclipse-dataspace-dcp.github.io/decentralized-claims-protocol/v1.0.1/#presentation-flow):

1. Client requests SI token (+ optional scopes) from its STS
2. STS returns SI token (may include `token` claim = VP access token)
3. Client calls Verifier protected resource with `Authorization: Bearer <SI token>`
4. Verifier resolves client DID from `sub`
5. Verifier validates SI token (signature, aud, exp, jti replay, etc.)
6. Verifier discovers Credential Service endpoint (`type: CredentialService`)
7. Verifier calls CS with own SI token; forwards client `token` if present
8. CS validates and returns `PresentationResponseMessage`
9. Verifier validates VPs per [Presentation Validation](https://eclipse-dataspace-dcp.github.io/decentralized-claims-protocol/v1.0.1/#presentation-validation)

### Credential offer flow (normative sequence)

From [Credential Issuance Protocol — Credential Offer Flow](https://eclipse-dataspace-dcp.github.io/decentralized-claims-protocol/v1.0.1/#credential-offer-flow):

1. Issuer prepares a **`CredentialOfferMessage`** listing one or more **`CredentialObject`** entries (id, credentialType, optional profile, issuancePolicy)
2. Issuer discovers holder **`CredentialService`** endpoint from holder DID document
3. Issuer POSTs the offer to `{credentialServiceUrl}/credentials` with a Bearer SI token
4. Holder Credential Service stores the offer and returns a **`holderPid`** (process id)
5. Holder (or issuer on behalf of holder) sends **`CredentialRequestMessage`** referencing offered credential object ids to issuer `{issuerServiceUrl}/issuance`
6. Issuer asynchronously processes the request and delivers **`CredentialMessage`** (`status`: `ISSUED` or `REJECTED`) to holder `POST /credentials`

**Redeem** is steps 5–6; this library builds request messages from offer definitions but does not implement issuer signing or delivery yet.

---

## 4. Proposed Maven module layout

New sibling repo or sub-project (recommended naming):

```
dcp-java/
├── pom.xml                         # dcp-parent
├── dcp-core/                       # artifact: dcp
├── dcp-spring/                     # optional auto-config
└── dcp-spring-boot-starter/
```

Package root: `de.eecc.dcp.*`

### `dcp-core` package structure

```
de.eecc.dcp/
├── Constants.java
├── api/
│   ├── DcpPresentation.java           # Presentation facade
│   ├── DcpIssuance.java               # Issuance offer facade
│   ├── DcpBuilder.java
│   ├── DcpOptions.java
│   ├── PresentationReceivedHandler.java
│   └── OfferReceivedHandler.java
├── identity/
│   ├── SelfIssuedIdToken.java         # Parsed JWT claims record
│   ├── SelfIssuedIdTokenParser.java
│   ├── SelfIssuedIdTokenValidator.java
│   ├── SelfIssuedIdTokenFactory.java  # Verifier creates token for CS calls
│   ├── DidDocumentResolver.java       # Pluggable
│   ├── CredentialServiceDiscovery.java
│   └── JtiReplayCache.java            # Pluggable (Caffeine default)
├── message/
│   ├── DcpMessage.java                # Base: @context, type
│   ├── PresentationQueryMessage.java
│   ├── PresentationResponseMessage.java
│   ├── CredentialOfferMessage.java
│   ├── CredentialObject.java
│   ├── CredentialRequestMessage.java
│   ├── CredentialMessage.java
│   └── JsonLdMessageSupport.java        # Compact/expand; type vs @type
├── query/
│   ├── PresentationQueryDefinition.java # Interface (like PresentationRequestDefinition)
│   ├── ScopeQueryDefinition.java
│   ├── PresentationExchangeQueryDefinition.java
│   └── DcpScope.java
├── issuance/
│   ├── CredentialOfferDefinition.java   # Interface (mirror PresentationQueryDefinition)
│   ├── TypeCredentialOfferDefinition.java
│   └── IssuanceMessages.java
├── client/
│   ├── CredentialServiceClient.java     # Pluggable — presentation queries
│   ├── CredentialStorageClient.java     # Pluggable — deliver offers to holder CS
│   ├── IssuerServiceClient.java         # Pluggable — credential requests to issuer
│   └── HttpCredentialServiceClient.java
├── validation/
│   ├── PresentationValidator.java       # Pluggable (default: in-spec steps)
│   ├── PresentationValidationResult.java
│   └── profile/
│       ├── DcpProfile.java              # vc20-bssl/jwt | vc11-sl2021/jwt
│       └── ProfileValidator.java
├── session/
│   ├── PresentationSession.java
│   └── PresentationSessionRepository.java
├── claims/
│   ├── PresentationClaims.java          # Reuse interface shape from oid4vp
│   └── PresentationParser.java
├── sts/                                 # Optional client-only
│   ├── StsTokenRequest.java
│   ├── StsTokenResponse.java
│   └── HttpSecureTokenServiceClient.java
├── exception/
│   ├── DcpException.java
│   └── DcpError.java                    # Sealed hierarchy
└── vp/                                  # VP/VC parsing helpers
    └── VerifiablePresentationParser.java
```

### Holder-side extension package (phase 2+)

```
de.eecc.dcp.service/
├── PresentationQueryHandler.java        # Handle incoming POST /presentations/query
├── CredentialStorageHandler.java        # POST /credentials (from CIP)
├── CredentialMessage.java
├── CredentialContainer.java
└── ScopeRegistry.java                   # Map scope aliases → stored credentials
```

---

## 5. Wire DTOs (detailed)

All DCP protocol messages share:

- `@context`: array or string; normative URI `https://w3id.org/dspace-dcp/v1.0/dcp.jsonld`
- `type`: string message type (compact JSON-LD form on the wire)

Implement Jackson records with `@JsonProperty` and `@JsonInclude(NON_NULL)`.

### 5.1 SelfIssuedIdToken

JWT signed by participant key with `capabilityInvocation` relationship ([validating SI tokens](https://eclipse-dataspace-dcp.github.io/decentralized-claims-protocol/v1.0.1/#validating-self-issued-id-tokens)).

| Claim | Required | Rule |
|-------|----------|------|
| `iss` | yes | MUST equal `sub` (participant DID) |
| `sub` | yes | Holder/client DID |
| `aud` | yes | Verifier DID (when sent to verifier) or target audience |
| `jti` | yes | Replay protection |
| `exp` | yes | Expiry (seconds since epoch) |
| `iat` | yes | Issued-at |
| `nbf` | optional | Not-before |
| `token` | optional | Opaque VP access token for Credential Service |

Library responsibilities:

- Parse JWT header/body (use `nimbus-jose-jwt` or similar)
- Resolve `sub` DID document
- Verify signature against `verificationMethod` with `capabilityInvocation`
- Assert `sub` == DID document `id`
- Check `aud`, `exp`, `nbf`, `jti` (via `JtiReplayCache`)

### 5.2 PresentationQueryMessage

Schema: [presentation-query-message-schema.json](https://eclipse-dataspace-dcp.github.io/decentralized-claims-protocol/v1.0.1/resources/presentation/presentation-query-message-schema.json)

```java
record PresentationQueryMessage(
    List<Object> context,           // @context
    String type,                    // "PresentationQueryMessage"
    List<String> scope,             // XOR presentationDefinition
    JsonNode presentationDefinition // PE object; use JsonNode or PE library type
)
```

Validation rules:

- MUST have `@context` and `type`
- MUST have exactly one of `scope` or `presentationDefinition`
- If both present → treat as error (spec: CS returns 400; client should pre-validate)
- `scope` array MUST be non-empty

### 5.3 PresentationResponseMessage

Schema: [presentation-response-message-schema.json](https://eclipse-dataspace-dcp.github.io/decentralized-claims-protocol/v1.0.1/resources/presentation/presentation-response-message-schema.json)

```java
record PresentationResponseMessage(
    List<Object> context,
    String type,                              // "PresentationResponseMessage"
    List<JsonNode> presentation,              // string JWT or object per item
    JsonNode presentationSubmission           // required when query used PE definition
)
```

- `presentation`: array of VPs (heterogeneous string/object allowed)
- `presentationSubmission`: REQUIRED when request contained `presentationDefinition`

### 5.4 CredentialOfferMessage

Schema: [credential-offer-message-schema.json](https://eclipse-dataspace-dcp.github.io/decentralized-claims-protocol/v1.0.1/resources/issuance/credential-offer-message-schema.json)

```java
record CredentialOfferMessage(
    List<Object> context,
    String type,                        // "CredentialOfferMessage"
    String issuer,                      // issuer DID
    List<CredentialObject> credentials
)
```

Validation rules:

- MUST have `@context`, `type`, `issuer`, and non-empty `credentials`
- Each `CredentialObject` MUST have `id` (URI) and `type` = `CredentialObject`

### 5.5 CredentialRequestMessage

Schema: [credential-request-message-schema.json](https://eclipse-dataspace-dcp.github.io/decentralized-claims-protocol/v1.0.1/resources/issuance/credential-request-message-schema.json)

```java
record CredentialRequestMessage(
    List<Object> context,
    String type,                        // "CredentialRequestMessage"
    String holderPid,
    List<CredentialRequestReference> credentials  // { "id": "<CredentialObject.id>" }
)
```

### 5.6 CredentialMessage (delivery)

Schema: [credential-message-schema.json](https://eclipse-dataspace-dcp.github.io/decentralized-claims-protocol/v1.0.1/resources/issuance/credential-message-schema.json)

Delivered by issuer to holder `POST /credentials` after request processing. `status` is `ISSUED` or `REJECTED`.

### 5.7 DcpScope helpers

Normative scope format: `[alias]:[discriminator]`

| Alias | Example | Meaning |
|-------|---------|---------|
| `org.eclipse.dspace.dcp.vc.type` | `org.eclipse.dspace.dcp.vc.type:Member` | VC by type |
| `org.eclipse.dspace.dcp.vc.id` | `org.eclipse.dspace.dcp.vc.id:8247b87d-...` | VC by id |

```java
record DcpScope(String alias, String discriminator) {
    String toScopeString();
    static DcpScope vcType(String type);
    static DcpScope vcId(String id);
    static DcpScope parse(String scope);
}
```

Scope-to-PE mapping is **implementation-specific** on the Credential Service; the library should allow hosts to register scope aliases for validation expectations only.

### 5.8 Credential Service discovery (from DID document)

Non-normative example in spec:

```json
{
  "id": "did:example:123#identity-hub",
  "type": "CredentialService",
  "serviceEndpoint": "https://cs.example.com"
}
```

DTO: `CredentialServiceEndpoint(String id, URI serviceEndpoint)`.

Discovery constant: `Constants.CREDENTIAL_SERVICE_TYPE = "CredentialService"`.

Resolution API paths: `Constants.PRESENTATIONS_QUERY_PATH`, `Constants.CREDENTIALS_PATH`.

Issuer discovery constant: `Constants.ISSUER_SERVICE_TYPE = "IssuerService"`.

### 5.9 STS DTOs (optional client)

From [identity-trust-sts-api.yaml](https://github.com/eclipse-dataspace-dcp/decentralized-claims-protocol/blob/main/specifications/identity-trust-sts-api.yaml):

- `StsTokenRequest`: `grant_type`, `client_id`, `client_secret`, `audience`, optional `bearer_access_scope`, optional `token`
- `StsTokenResponse`: `access_token`, `expires_in`, `token_type`
- `StsTokenErrorResponse`: `error`, `error_description`

Note: SI token is returned as `access_token` in the non-normative OpenAPI example.

### 5.10 PresentationSession (internal lifecycle DTO)

Not on the wire; analogous to `PresentationRequest`:

| Field | Purpose |
|-------|---------|
| `sessionId` | Internal UUID |
| `verifierDid` | Local participant |
| `holderDid` | From client SI token `sub` |
| `credentialServiceUrl` | Resolved base URL |
| `queryDefinition` | `PresentationQueryDefinition` |
| `clientToken` | Optional forwarded opaque `token` |
| `response` | `PresentationResponseMessage` after query |
| `verificationStatus` | `NOT_RECEIVED`, `RECEIVED`, `SUCCEEDED`, `FAILED` |
| `verificationError` | Structured error |
| `createdAt`, `expiresAt` | Session TTL |
| `consumed` | One-time use flag |

Subclasses may add app fields (e.g. contract offer id) with `@JsonIgnore`, same pattern as `PresentationRequest`.

---

## 6. Core interfaces and facade API

### 6.1 DcpPresentation (facade)

Proposed public methods:

```java
// --- Client token gate (step 3–5 of presentation flow) ---
SelfIssuedIdTokenValidationResult validateClientToken(String bearerJwt);

// --- Session + query (steps 6–8) ---
<T extends PresentationSession> T beginSession(
    SelfIssuedIdToken clientToken,
    PresentationQueryDefinition query,
    BeginSessionOptions<T> options
);

PresentationResponseMessage queryPresentations(PresentationSession session);

// --- Combined flow ---
PresentationFlowResult handlePresentationRequest(
    String clientBearerJwt,
    PresentationQueryDefinition query,
    PresentationReceivedHandler<?> handler
);

// --- Validation + claims (step 9) ---
PresentationValidationResult validatePresentations(
    PresentationQueryDefinition query,
    PresentationResponseMessage response,
    String holderDid
);

PresentationClaims extractPresentationClaims(
    PresentationQueryDefinition query,
    PresentationResponseMessage response
);

// --- Utilities ---
URI resolveCredentialService(String holderDid);
String createVerifierToken(PresentationSession session);  // SI token for CS call
```

### 6.1b DcpIssuance (facade)

Proposed public methods:

```java
// --- Build and deliver offer (steps 1–4 of offer flow) ---
CredentialOfferMessage buildOffer(CredentialOfferDefinition definition);

String deliverOffer(
    String holderDid,
    CredentialOfferMessage offer,
    String issuerBearerJwt
);

// --- Redeem offered credentials (step 5) ---
CredentialRequestMessage buildRequest(CredentialOfferDefinition definition, String holderPid);

String requestCredentials(
    String issuerDid,
    CredentialRequestMessage request,
    String holderBearerJwt
);

// --- Combined issuer flow ---
OfferFlowResult handleCredentialOffer(
    String holderDid,
    CredentialOfferDefinition definition,
    String issuerBearerJwt,
    OfferReceivedHandler handler
);

// --- Utilities ---
URI resolveCredentialService(String holderDid);
URI resolveIssuerService(String issuerDid);
```

### 6.2 Pluggable interfaces

| Interface | Responsibility | Default impl |
|-----------|----------------|--------------|
| `DidDocumentResolver` | Resolve DID → document JSON | HTTP universal resolver adapter (host-provided URL) |
| `CredentialServiceDiscovery` | Extract CS URL from DID doc | Default parsing `service[]` |
| `CredentialServiceClient` | POST query, parse response | `HttpCredentialServiceClient` (Java HttpClient) |
| `SelfIssuedIdTokenValidator` | Full SI token validation | Default per base.protocol |
| `JtiReplayCache` | Store seen `jti` until `exp` | Caffeine TTL cache |
| `PresentationValidator` | 8-step VP validation | `DefaultPresentationValidator` |
| `PresentationSessionRepository` | Session persistence | `CaffeinePresentationSessionRepository` |
| `PresentationReceivedHandler` | App callback after success | — |
| `CredentialStorageClient` | POST offer to holder CS | `HttpCredentialStorageClient` |
| `IssuerServiceClient` | POST request to issuer | `HttpIssuerServiceClient` |
| `OfferReceivedHandler` | App callback after offer accepted | — |

### 6.3 CredentialOfferDefinition

Mirror `PresentationQueryDefinition`:

```java
interface CredentialOfferDefinition {
    CredentialOfferMessage toOfferMessage();

    CredentialRequestMessage toRequestMessage(String holderPid);

    void assertOfferMatches(CredentialOfferMessage message);
}
```

Built-in implementation:

1. **`TypeCredentialOfferDefinition`** — wraps `List<OfferedCredential>` (id, credentialType, profile, optional issuancePolicy)

### 6.4 PresentationQueryDefinition

Mirror `PresentationRequestDefinition`:

```java
interface PresentationQueryDefinition {
    PresentationQueryMessage toQueryMessage();

    /** Validate response matches what was requested (scope or PE). */
    void assertResponseMatches(PresentationResponseMessage response);

    /** Extract claims from verified presentations. */
    PresentationClaims extractPresentationClaims(PresentationResponseMessage response);
}
```

Two built-in implementations:

1. **`ScopeQueryDefinition`** — wraps `List<DcpScope>`
2. **`PresentationExchangeQueryDefinition`** — wraps PE `PresentationDefinition` + optional submission validation rules

### 6.5 PresentationValidator (normative checklist)

Implement [Presentation Validation](https://eclipse-dataspace-dcp.github.io/decentralized-claims-protocol/v1.0.1/#presentation-validation):

1. VP satisfies `scope` or `presentationDefinition`
2. VP signature via holder DID verification method
3. VP verification method has `authentication` relationship
4. VC issuer DID matches VC verification method
5. VC signature valid
6. Revocation check (profile-specific)
7. Expiry / validity claims on VP
8. Optional: cryptographic holder binding (`credentialSubject.id` == VP holder DID)

Split by **DCP profile** (`vc20-bssl/jwt` vs `vc11-sl2021/jwt`) per [dcp.profiles.md](https://github.com/eclipse-dataspace-dcp/decentralized-claims-protocol/blob/main/specifications/dcp.profiles.md).

**Design choice:** allow delegating steps 2–6 to an external vc-verifier HTTP service (like oid4vp) while keeping steps 1, 7, 8 local — configurable via `DcpOptions`.

### 6.6 PresentationReceivedHandler

Analogous to `DirectPostHandler` / `DirectPostResult`:

```java
@FunctionalInterface
interface PresentationReceivedHandler<T extends PresentationSession> {
    PresentationFlowOutcome onVerified(T session, PresentationResponseMessage response);
}

enum PresentationFlowOutcome {
    COMPLETE,           // session marked complete
    ISSUE_SESSION_TOKEN // optional one-time code for downstream OAuth/DSP
    // CUSTOM
}
```

No OID4VP-style `redirect_uri` unless the host adds it in a custom handler response.

---

## 7. JSON-LD serialization strategy

EDC interoperability note ([issue #5209](https://github.com/eclipse-edc/Connector/issues/5209)):

- **Egress (library → CS):** serialize compact JSON-LD (`type`, not `@type`) when using Jackson with proper property names
- **Ingress (CS → library):** accept both `type` and `@type`
- Consider optional dependency on JSON-LD Java library (e.g. Titanium JSON-LD) for strict compaction; phase 1 can use Jackson + tolerant parsing

`@context` handling:

- Default to `[Constants.DCP_CONTEXT, "https://www.w3.org/ns/did/v1"]` in generated messages
- Allow override in `DcpOptions`

---

## 8. Error model (sealed DcpError)

Mirror `Oid4VpError` pattern:

| Error | When |
|-------|------|
| `InvalidSelfIssuedIdToken` | Parse/validation failure (aud, exp, signature, iss≠sub) |
| `ReplayDetected` | Duplicate `jti` |
| `UnknownHolder` | DID resolution failed |
| `CredentialServiceNotFound` | No `CredentialService` in DID document |
| `CredentialServiceError` | HTTP 4xx/5xx from CS |
| `InvalidQueryMessage` | Both scope and presentationDefinition set |
| `InvalidOfferMessage` | Malformed or mismatched CredentialOfferMessage |
| `InvalidCredentialRequest` | Malformed CredentialRequestMessage |
| `IssuerServiceError` | HTTP 4xx/5xx from issuer |
| `UnsupportedPresentationDefinition` | CS returned 501 |
| `PresentationValidationFailed` | VP/VC validation step failed |
| `InvalidPresentationResponse` | Malformed JSON, missing required fields |
| `EmptyPresentationClaims` | Extraction yielded no values |
| `ExpiredSession` | Session TTL exceeded |
| `AlreadyConsumed` | Session reused |
| `InternalError` | Unexpected failures |

Wrap in `DcpException` with `DcpError` cause for Spring handler mapping.

---

## 9. Constants

```java
public final class Constants {
    public static final String DCP_CONTEXT = "https://w3id.org/dspace-dcp/v1.0/dcp.jsonld";
    public static final String TYPE_PRESENTATION_QUERY = "PresentationQueryMessage";
    public static final String TYPE_PRESENTATION_RESPONSE = "PresentationResponseMessage";
    public static final String TYPE_CREDENTIAL_OFFER = "CredentialOfferMessage";
    public static final String TYPE_CREDENTIAL_REQUEST = "CredentialRequestMessage";
    public static final String TYPE_CREDENTIAL_OBJECT = "CredentialObject";
    public static final String TYPE_CREDENTIAL_MESSAGE = "CredentialMessage";
    public static final String CREDENTIAL_SERVICE_TYPE = "CredentialService";
    public static final String ISSUER_SERVICE_TYPE = "IssuerService";
    public static final String SCOPE_ALIAS_VC_TYPE = "org.eclipse.dspace.dcp.vc.type";
    public static final String SCOPE_ALIAS_VC_ID = "org.eclipse.dspace.dcp.vc.id";
    public static final String PATH_PRESENTATIONS_QUERY = "/presentations/query";
    public static final String PATH_CREDENTIALS = "/credentials";
    public static final String PATH_ISSUANCE = "/issuance";
    public static final String PROFILE_VC20_BSSL_JWT = "vc20-bssl/jwt";
    public static final String PROFILE_VC11_SL2021_JWT = "vc11-sl2021/jwt";
    public static final int DEFAULT_SESSION_TTL_SECONDS = 300;
    public static final int DEFAULT_JTI_CACHE_TTL_SECONDS = 3600;
}
```

---

## 10. Implementation phases

### Phase 0 — Project bootstrap (1 week)

- [ ] Create `dcp-java` multi-module Maven parent (mirror `oid4vp-java` structure, Java 25, Jackson, Caffeine, SLF4J)
- [ ] Add `nimbus-jose-jwt` for JWT parsing/signing
- [ ] Define `Constants`, sealed `DcpError`, `DcpException`
- [ ] Copy/adapt release script pattern from oid4vp if same org

**Deliverable:** empty modules compile; CI runs `mvn test`

### Phase 1 — Message DTOs and JSON (1–2 weeks)

- [ ] `PresentationQueryMessage`, `PresentationResponseMessage`, `DcpScope`
- [ ] JSON schema validation (optional: `networknt/json-schema-validator` against published schemas)
- [ ] Unit tests with fixtures from DCP spec examples (construct minimal valid payloads)
- [ ] JSON-LD `type` / `@type` tolerant deserialization

**Deliverable:** round-trip serialize/deserialize tests pass

### Phase 1b — Issuance offer DTOs and definitions (1 week)

- [x] `CredentialOfferMessage`, `CredentialObject`, `CredentialRequestMessage`, `CredentialMessage`
- [x] `CredentialOfferDefinition` + `TypeCredentialOfferDefinition`
- [x] `InvalidOfferMessage`, `InvalidCredentialRequest`, `IssuerServiceError`
- [x] `CredentialStorageClient`, `IssuerServiceClient` interfaces
- [x] `DcpIssuance` facade stub
- [ ] Unit tests with fixtures from published JSON schemas
- [ ] `HttpCredentialStorageClient`, `HttpIssuerServiceClient`

**Deliverable:** issuer can build/deliver offers and build redeem requests; HTTP clients in integration tests

### Phase 2 — Identity layer (2–3 weeks)

- [ ] `SelfIssuedIdToken` parser
- [ ] `DidDocumentResolver` interface + test stub
- [ ] `SelfIssuedIdTokenValidator` (full normative steps)
- [ ] `JtiReplayCache` + Caffeine implementation
- [ ] `CredentialServiceDiscovery`
- [ ] `SelfIssuedIdTokenFactory` for verifier-issued tokens to CS

**Deliverable:** SI token validation tested with mock DID documents and signed JWT fixtures

### Phase 3 — Credential Service client (1–2 weeks)

- [ ] `CredentialServiceClient` interface
- [ ] `HttpCredentialServiceClient` — POST `/presentations/query`, Bearer auth
- [ ] Forward `token` claim from client SI token into verifier SI token
- [ ] Error mapping (400, 401, 501, etc.)

**Deliverable:** integration test against mock HTTP CS server

### Phase 4 — Facade and session management (2 weeks)

- [ ] `PresentationSession`, `PresentationSessionRepository`, Caffeine impl
- [ ] `PresentationQueryDefinition` + scope and PE implementations
- [ ] `DcpPresentation` facade wiring validate → discover → query → store
- [ ] `PresentationReceivedHandler` hook

**Deliverable:** end-to-end test with mock DID + mock CS

### Phase 5 — Presentation validation (3–4 weeks)

- [ ] `PresentationValidator` default implementation (8 steps)
- [ ] Profile support: `vc20-bssl/jwt`, `vc11-sl2021/jwt`
- [ ] JWT VP/VC parsing (enveloped proofs)
- [ ] Revocation: BitStringStatusList + StatusList2021 clients
- [ ] Optional: `DelegatingPresentationValidator` → external HTTP verifier

**Deliverable:** validation unit tests per profile; document which steps are delegated

### Phase 6 — Claims extraction (1 week)

- [ ] Port/adapt `PresentationParser` from oid4vp for JWT/LDP payloads in `presentation[]`
- [ ] `PresentationClaims` interface like in oid4vc
- [ ] `extractPresentationClaims` on facade

**Deliverable:** claims extraction tests with sample VPs

### Phase 7 — Spring Boot starter (1 week)

- [ ] `DcpProperties`: `verifier-did`, `did-resolver-url`, default profile, session TTL
- [ ] `DcpAutoConfiguration`, `@Bean DcpPresentation`
- [ ] `DcpExceptionHandler` (optional, mirror oid4vp-spring)

**Deliverable:** `@SpringBootTest` context loads

### Phase 8 — Holder-side Credential Service helpers (optional, 3+ weeks)

- [ ] `PresentationQueryHandler` for incoming queries
- [ ] `ScopeRegistry` mapping scopes → stored credentials
- [ ] PE `presentationDefinition` matching (if supported)
- [ ] CIP `CredentialMessage` storage endpoint (`POST /credentials`)

**Deliverable:** CS can respond to TCK-style queries

### Phase 9 — TCK and interoperability (ongoing)

- [ ] Run against [DCP TCK](https://github.com/eclipse-dataspacetck/dcp-tck)
- [ ] Interop testing with EDC Identity Hub / mock CS
- [ ] Document `@type`/`type` compatibility

---

## 11. Dependencies

| Dependency | Purpose | Notes |
|------------|---------|-------|
| Jackson | JSON DTOs | Same as oid4vp |
| Caffeine | Session + jti cache | Same as oid4vp |
| SLF4J | Logging | Same as oid4vp |
| Nimbus JOSE + JWT | SI token parse/verify/sign | Industry standard |
| Java HttpClient | CS HTTP calls | JDK built-in |
| networknt/json-schema-validator | Optional schema validation | Against published schemas |
| Titanium JSON-LD | Optional strict JSON-LD | Phase 2+ if interop issues |
| Presentation Exchange library | Optional typed PE models | Or `JsonNode` + manual validation initially |


---

## 12. Testing strategy

### Unit tests

- DTO serialization against published JSON schemas
- SI token validation matrix (expired, wrong aud, replay jti, bad signature)
- Scope string parsing
- Query message XOR validation

### Integration tests

- MockWebServer for Credential Service
- WireMock DID resolver responses with `CredentialService` entry
- End-to-end: client token appears in logs → query → validate → extract claims

### Compliance

- [DCP TCK](https://github.com/eclipse-dataspacetck/dcp-tck) for protocol conformance
- Cross-test with EDC `DefaultCredentialServiceClient` message formats

### Test fixtures

- Generate JWT VPs for both profiles using test keys
- Minimal DID documents with `verificationMethod` + `capabilityInvocation` + `authentication`

---

## 13. Spring configuration (mirror oid4vp)

```yaml
dcp:
  verifier-did: did:web:example.com
  did-resolver-url: https://resolver.example.com
  default-profile: vc20-bssl/jwt
  session-ttl: 300s
  jti-cache-ttl: 3600s
  delegation:
    verifier-url: ""   # optional external vc-verifier; empty = local validation
```

Dependency: `de.eecc.dcp:dcp-spring-boot-starter`

No REST controllers in the library — host application exposes verifier endpoints.

---

## 14. Key design decisions (decide before Phase 3)

| Decision | Options | Recommendation |
|----------|---------|----------------|
| PE modeling | Typed library vs `JsonNode` | Start `JsonNode`; add typed PE when needed |
| Validation location | All local vs delegate crypto to vc-verifier | Hybrid: structural checks local, crypto delegatable |
| DID resolution | Universal resolver vs did:web HTTP | Pluggable; document did:web as primary for dataspaces |
| JSON-LD strictness | Jackson-only vs Titanium compaction | Jackson + tolerant ingress; add Titanium if TCK fails |
| Repo placement | Same monorepo as oid4vp vs separate | Separate artifact coordinates; |
| Holder-side scope | Verifier-only v1 vs include CS handlers | Verifier-only for v0.1; CS in v0.2 |

---

## 15. OID4VP → DCP feature mapping (summary)

| oid4vp feature | DCP equivalent | Status in plan |
|----------------|----------------|----------------|
| `generatePresentationRequest()` | `beginSession()` + build `PresentationQueryMessage` | Phase 4 |
| `toOpenId4VpUrl()` | N/A — use DID discovery | — |
| `processDirectPost()` | `queryPresentations()` + `handlePresentationRequest()` | Phase 4 |
| `pollPresentationStatus()` | Optional session status API (host-defined) | Low priority |
| `verifyVpTokenPresentations()` | `validatePresentations()` | Phase 5 |
| `extractPresentationClaims()` | Same name, different input type | Phase 6 |
| `DcqlQuery` | Scopes + Presentation Exchange | Phase 1/4 |
| `ClientMetadata` | DSP `/.well-known/dspace-trust` (optional) | Out of scope v1 |
| `response_code` OAuth flow | Host-defined session token / DSP auth | Handler hook only |

---

## 16. Risks and mitigations

| Risk | Mitigation |
|------|------------|
| JSON-LD `@type` interop | Tolerant parser; follow EDC compaction on egress |
| PE complexity | Scope-first implementation; PE as phase 1b |
| DID method diversity | Pluggable resolver; test did:web + universal resolver |
| Profile heterogeneity | Enforce homogeneity rule; reject mixed VP arrays |
| Revocation network calls | Cache status lists; configurable timeout |
| Spec drift | Pin to DCP v1.0.1 schemas; track GitHub repo releases |

---

## 17. Success criteria

- [ ] Verifier can validate client SI token, query CS, validate VPs, extract claims
- [ ] Issuer can build credential offers, deliver to holder CS, and build redeem requests
- [ ] Passes DCP TCK presentation- and issuance-offer-related tests
- [ ] API surface feels familiar to oid4vp users (facade, definition interface, repository, sealed errors)
- [ ] Spring Boot starter enables drop-in configuration
- [ ] Documented integration guide for DSP connector / dataspace login flows

---

## 18. References quick list

- DCP v1.0.1: https://eclipse-dataspace-dcp.github.io/decentralized-claims-protocol/v1.0.1/
- DCP GitHub: https://github.com/eclipse-dataspace-dcp/decentralized-claims-protocol
- DCP TCK: https://github.com/eclipse-dataspacetck/dcp-tck
- Presentation Exchange: https://identity.foundation/presentation-exchange/spec/v2.1.1/
- DID Core: https://www.w3.org/TR/did-core/
- VC Data Model 2.0: https://www.w3.org/TR/vc-data-model-2.0/
- DSP: https://eclipse-dataspace-protocol-base.github.io/DataspaceProtocol/
- EDC Identity Trust: https://github.com/eclipse-edc/Connector/tree/main/extensions/common/iam/identity-trust
- Construct-X EDC DCP SPI (in-code reference): https://github.com/project-construct-x/constructx-edc/tree/develop/spi/dcp-spi
- oid4vp module layout: [module-layout.md](module-layout.md)
