# DCP Java Library

Java library for the verifier-side and issuer-side of the [Eclipse Decentralized Claims Protocol (DCP) v1.0.1](https://eclipse-dataspace-dcp.github.io/decentralized-claims-protocol/v1.0.1/): the **Verifiable Presentation Protocol** and the **Credential Issuance Protocol** offer flow.

> **Status (0.1.x):** Wire DTOs, presentation query definitions, credential offer definitions, claims extraction helpers, and Spring Boot auto-configuration are available. The `DcpPresentation` and `DcpIssuance` facade methods for Self-Issued ID token validation, HTTP clients, and VP validation are being added incrementally — see [implementation-plan.md](implementation-plan.md).

## Requirements

- Java 25 JDK
- Maven 3.9+

## Installation

Include the corresponding Maven dependency in your `pom.xml`:

```xml
<dependency>
  <groupId>de.eecc.dcp</groupId>
  <artifactId>dcp</artifactId>
  <version>0.1.0</version>
</dependency>
```

For Spring Boot applications, use the starter:

```xml
<dependency>
  <groupId>de.eecc.dcp</groupId>
  <artifactId>dcp-spring-boot-starter</artifactId>
  <version>0.1.0</version>
</dependency>
```

Check [Maven Central](https://central.sonatype.com/artifact/de.eecc.dcp/dcp/versions) for the latest package version.

## Quick Start

A verifier validates a client **Self-Issued ID Token**, discovers the holder's **Credential Service** from their DID document, sends a **`PresentationQueryMessage`** to `POST /presentations/query`, and validates the returned **Verifiable Presentations**.

Create a library instance and define what credentials you need:

```java
import de.eecc.dcp.api.DcpOptions;
import de.eecc.dcp.api.DcpPresentation;
import de.eecc.dcp.message.PresentationQueryMessage;
import de.eecc.dcp.query.DcpScope;
import de.eecc.dcp.query.ScopeQueryDefinition;

import java.time.Duration;

DcpPresentation dcp = DcpPresentation.create(DcpOptions.builder()
        .sessionTtl(Duration.ofMinutes(5))
        .build());

ScopeQueryDefinition query = ScopeQueryDefinition.of(
        DcpScope.vcType("MembershipCredential"));

PresentationQueryMessage message = query.toQueryMessage();
// Serialize message to JSON and POST to the holder Credential Service
// at {credentialServiceUrl}/presentations/query with a Bearer SI token.
```

`verifierDid` and DID resolution are configured when the identity layer is wired in; hosts typically provide a `DidDocumentResolver` pointing at a universal resolver or Identity Hub.

### Presentation Query Definitions

A presentation query definition describes **what** the verifier asks for. This is the DCP equivalent of oid4vp's DCQL-based `PresentationRequestDefinition`:

| oid4vp | DCP |
|--------|-----|
| `DcqlQuery` in the authorization request | `scope` array or `presentationDefinition` in `PresentationQueryMessage` |
| Wallet matches credential types + claim paths | Credential Service maps scopes to stored credentials (mapping is implementation-specific) |
| `PresentationRequestDefinition` | `PresentationQueryDefinition` |

DCP does **not** put claim paths on the wire. Normative scope aliases request credentials **by type** (`org.eclipse.dspace.dcp.vc.type:Member`) or **by id** (`org.eclipse.dspace.dcp.vc.id:…`). Alternatively, a full [Presentation Exchange](https://identity.foundation/presentation-exchange/) `presentationDefinition` can express richer constraints when the Credential Service supports it.

Implement `PresentationQueryDefinition` or use the built-in scope and PE implementations:

```java
import de.eecc.dcp.query.DcpScope;
import de.eecc.dcp.query.PresentationQueryDefinition;
import de.eecc.dcp.query.ScopeQueryDefinition;

// By VC type (normative alias org.eclipse.dspace.dcp.vc.type)
PresentationQueryDefinition byType = ScopeQueryDefinition.of(
        DcpScope.vcType("MembershipCredential"));

// By VC id (normative alias org.eclipse.dspace.dcp.vc.id)
PresentationQueryDefinition byId = ScopeQueryDefinition.of(
        DcpScope.vcId("8247b87d-8d72-47e1-8128-9ce47e3d829d"));

// Custom scope alias (implementation-specific mapping on the Credential Service)
PresentationQueryDefinition custom = ScopeQueryDefinition.of(
        DcpScope.parse("org.example.dcp.vc.type:MyCredential"));
```

After the Credential Service responds, validate the response shape and extract claims with the same definition:

```java
import de.eecc.dcp.claims.PresentationClaims;
import de.eecc.dcp.message.PresentationResponseMessage;

PresentationResponseMessage response = /* from CS */;

byType.assertResponseMatches(response);
PresentationClaims claims = byType.extractPresentationClaims(response);
Object issuer = claims.claimValues().get("presentation.0.verifiableCredential.issuer");
```

#### Scope format

DCP defines scopes as `[alias]:[discriminator]`, for example `org.eclipse.dspace.dcp.vc.type:Member`. Construct-X EDC often appends a non-normative operation suffix (`:read`, `:write`, `:*`); `DcpScope.parse()` accepts those strings and normalizes them to the spec format.

#### Presentation Exchange queries

For Presentation Exchange, wrap a `presentationDefinition` JSON object:

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import de.eecc.dcp.query.PresentationExchangeQueryDefinition;

var mapper = new ObjectMapper();
var presentationDefinition = mapper.readTree("""
        {
          "id": "membership-pd",
          "input_descriptors": []
        }
        """);

PresentationQueryDefinition peQuery =
        new PresentationExchangeQueryDefinition(presentationDefinition);
```

When the query used `presentationDefinition`, the response **must** include `presentationSubmission` (validated by `assertResponseMatches`).

#### Built-in template: GS1 License Presentation

The library ships a ready-made definition for GS1 Company Prefix and Prefix License credentials (`Gs1LicenseQueryDefinition.INSTANCE`), mirroring oid4vp's `Gs1LicenseRequest`:

```java
import de.eecc.dcp.claims.PresentationClaims;
import de.eecc.dcp.message.PresentationQueryMessage;
import de.eecc.dcp.message.PresentationResponseMessage;
import de.eecc.dcp.query.template.gs1.Gs1LicenseQueryDefinition;

PresentationQueryMessage message = Gs1LicenseQueryDefinition.INSTANCE.toQueryMessage();
// POST message to Credential Service …

PresentationResponseMessage response = /* from CS */;
PresentationClaims claims = Gs1LicenseQueryDefinition.INSTANCE.extractPresentationClaims(response);
List<String> gcps = claims.values();
String partyGln = claims.identifier();
```

The query requests both `GS1CompanyPrefixLicenseCredential` and `GS1PrefixLicenseCredential` via DCP `vc.type` scopes. Your Credential Service must map those scope strings to the corresponding stored credentials.

### Credential offer flow

An issuer prepares a **`CredentialOfferMessage`**, delivers it to the holder's **Credential Service** at `POST /credentials`, and the holder later redeems it with a **`CredentialRequestMessage`** to the issuer's **`/issuance`** endpoint.

Define what to offer and build the wire message:

```java
import de.eecc.dcp.Constants;
import de.eecc.dcp.api.DcpIssuance;
import de.eecc.dcp.api.DcpOptions;
import de.eecc.dcp.issuance.TypeCredentialOfferDefinition;
import de.eecc.dcp.message.CredentialOfferMessage;
import de.eecc.dcp.message.CredentialRequestMessage;

DcpIssuance issuance = DcpIssuance.create(DcpOptions.builder().build());

TypeCredentialOfferDefinition offer = TypeCredentialOfferDefinition.of(
        "did:web:issuer.example",
        TypeCredentialOfferDefinition.OfferedCredential.ofType(
                "urn:uuid:8247b87d-8d72-47e1-8128-9ce47e3d829d",
                "MembershipCredential",
                Constants.PROFILE_VC20_BSSL_JWT));

CredentialOfferMessage message = offer.toOfferMessage();
// POST message to holder Credential Service at {credentialServiceUrl}/credentials
// with a Bearer SI token issued by the issuer.

// After the holder accepts the offer and returns holderPid:
CredentialRequestMessage request = offer.toRequestMessage("holder-pid-from-cs");
// POST request to issuer at {issuerServiceUrl}/issuance with holder Bearer SI token.
```

`CredentialOfferDefinition` mirrors `PresentationQueryDefinition`: use `toOfferMessage()` for the wire payload, `assertOfferMatches(message)` when receiving offers on the holder side, and `toRequestMessage(holderPid)` to redeem.

### Verifier presentation flow

**Flow**

1. The client obtains a Self-Issued ID Token from its Secure Token Service and sends it to your verifier (for example as `Authorization: Bearer …` on a DSP or HTTP API call).
2. You validate the SI token (`iss` == `sub`, signature, `aud`, `exp`, `jti` replay protection).
3. You resolve the holder DID document and discover the `CredentialService` endpoint (`type: CredentialService`).
4. You build a `PresentationQueryMessage` from your `PresentationQueryDefinition` and POST it to `{serviceEndpoint}/presentations/query`, forwarding the opaque `token` claim from the client SI token when present.
5. You validate returned VPs per [DCP presentation validation](https://eclipse-dataspace-dcp.github.io/decentralized-claims-protocol/v1.0.1/#presentation-validation) and extract claims.

**Server-side example** (target facade API; adapt paths and token issuance to your application):

```java
import de.eecc.dcp.api.DcpPresentation;
import de.eecc.dcp.api.PresentationReceivedHandler;
import de.eecc.dcp.claims.PresentationClaims;
import de.eecc.dcp.query.ScopeQueryDefinition;
import de.eecc.dcp.query.DcpScope;
import de.eecc.dcp.session.PresentationSession;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

ScopeQueryDefinition query = ScopeQueryDefinition.of(DcpScope.vcType("MembershipCredential"));

@Getter
@SuperBuilder
class ContractPresentationSession extends PresentationSession {
    // Application fields (never sent on the wire), e.g. contract offer id
    private String contractOfferId;
}

// Incoming request with client Bearer SI token
// dcp.handlePresentationRequest(clientBearerJwt, query, (session, response) -> {
//     PresentationClaims claims = query.extractPresentationClaims(response);
//     // issue DSP token, persist session, etc.
// });
```

Lower-level access: `query.toQueryMessage()` for the wire payload, `query.assertResponseMatches(response)` for structural checks, and `CredentialServiceClient` for HTTP calls once the client implementation is registered.

## Spring Boot

Add the starter dependency and configure session defaults in `application.yml`:

```yaml
dcp:
  session-ttl: 5m
```

`DcpPresentation` and `DcpExceptionHandler` are auto-configured. Inject the facade where needed:

```java
import de.eecc.dcp.api.DcpPresentation;
import de.eecc.dcp.claims.PresentationClaims;
import de.eecc.dcp.message.PresentationQueryMessage;
import de.eecc.dcp.message.PresentationResponseMessage;
import de.eecc.dcp.query.template.gs1.Gs1LicenseQueryDefinition;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dcp/presentations")
class PresentationController {

    private final DcpPresentation dcp;

    PresentationController(DcpPresentation dcp) {
        this.dcp = dcp;
    }

    @GetMapping("/query/gs1")
    PresentationQueryMessage gs1Query() {
        return Gs1LicenseQueryDefinition.INSTANCE.toQueryMessage();
    }

    @PostMapping("/claims/gs1")
    PresentationClaims extractGs1Claims(@RequestBody PresentationResponseMessage response) {
        var definition = Gs1LicenseQueryDefinition.INSTANCE;
        definition.assertResponseMatches(response);
        return definition.extractPresentationClaims(response);
    }
}
```

`DcpExceptionHandler` maps `DcpException` to HTTP responses using each `DcpError`'s `suggestedHttpStatus()`.

## Development

Clone the repository and run the build from the Maven parent:

```bash
git clone https://github.com/european-epc-competence-center/dcp.git
cd dcp/dcp-java
mvn test
mvn package
```

To install locally into your Maven repository:

```bash
mvn install
```

Then reference `0.1.0-SNAPSHOT` from a dependent project:

```xml
<dependency>
  <groupId>de.eecc.dcp</groupId>
  <artifactId>dcp</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### Project Structure

```
dcp-java/
├── dcp-core/                       # artifact: dcp
│   └── src/main/java/de/eecc/dcp/
│       ├── api/                    # DcpPresentation facade, *Options, handlers
│       ├── identity/               # Self-Issued ID tokens, DID resolution
│       ├── message/                # PresentationQuery/ResponseMessage DTOs
│       ├── query/                  # PresentationQueryDefinition implementations
│       │   └── template/           # Built-in query templates (e.g. gs1)
│       ├── issuance/               # CredentialOfferDefinition implementations
│       ├── client/                 # CredentialServiceClient, CredentialStorageClient, IssuerServiceClient
│       ├── validation/             # PresentationValidator, DCP profiles
│       ├── session/                # PresentationSession, repository
│       ├── claims/                 # PresentationClaims extraction
│       ├── sts/                    # Optional Secure Token Service client
│       ├── exception/              # DcpError, DcpException
│       ├── vp/                     # VP/VC parsing helpers
│       └── service/                # Holder-side extension (phase 2+)
├── dcp-spring/                     # Spring Boot auto-configuration
└── dcp-spring-boot-starter/
```

See [implementation-plan.md](implementation-plan.md) for the full design and phased rollout.

## Repository Overview

```
/
├── dcp-java/             # Java library (Maven, Java 25)
├── scripts/              # Release tooling
├── implementation-plan.md
├── README.md
└── LICENSE
```

## License

Apache License 2.0 — see [LICENSE](LICENSE).
