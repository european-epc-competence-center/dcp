# DCP Java Library

Java library for the verifier-side (and optionally holder-side) of the [Eclipse Decentralized Claims Protocol (DCP) v1.0.1](https://eclipse-dataspace-dcp.github.io/decentralized-claims-protocol/v1.0.1/) Verifiable Presentation Protocol.

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

## Development

```bash
cd dcp-java
mvn test
mvn package
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
│       ├── client/                 # CredentialServiceClient
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
├── README.md
└── LICENSE
```

## License

Apache License 2.0 — see [LICENSE](LICENSE).
