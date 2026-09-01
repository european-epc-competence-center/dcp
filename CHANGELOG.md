# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed

- **Breaking:** Jackson 3 and Spring Boot 4.1.1. Public DTOs and query/offer APIs now use `tools.jackson.databind.JsonNode` (and `JsonMapper` internally) instead of Jackson 2 `com.fasterxml.jackson.databind.*`. JSON annotations remain `com.fasterxml.jackson.annotation.*`. The Spring starter targets Boot **4.1.1** so hosts can bind library records as `@RequestBody`.

## [1.0.0] - 2026-07-30

### Added

- Multi-module Maven project (`dcp-core`, `dcp-spring`, `dcp-spring-boot-starter`) mirroring [oid4vp](https://github.com/european-epc-competence-center/oid4vp)
- Package scaffolding for DCP presentation library (`de.eecc.dcp.*`)
- Bootstrap types: `Constants`, sealed `DcpError`, `DcpException`
- GitHub Actions CI and Maven Central release workflow
- npm release script for version bumps and changelog management