# appointment-schedular

Multi-tenant appointment / service scheduler (LLD practice).

## Stack

- Java 21
- Maven (wrapper with SHA-256 pinned distribution)
- Spring Boot 3.5.16

## Build & test

```bash
./mvnw test
make build          # runs tests, then packages
make verify         # full Maven verify lifecycle
```

`make package` skips tests on purpose — do **not** use it as a quality gate. CI runs `./mvnw test` and fails on HIGH/CRITICAL dependency findings.

## Security tooling

- Dependabot weekly updates (Maven + GitHub Actions)
- GitHub Actions CI: unit tests + Trivy filesystem CVE scan (fails on HIGH/CRITICAL)
- Optional GitHub Dependency Review on PRs (needs **Dependency graph** enabled under
  [Code security settings](https://github.com/ankul01/appointment-schedular/settings/security_analysis))
- Maven Wrapper `distributionSha256Sum` pins the downloaded Maven zip

## Layout

```text
src/main/java/scheduling/
  api/       # AvailabilityService, SchedulingService, …
  domain/    # enums, value types, entities
  core/      # orchestration implementations
  spi/port/  # repositories / ports
  infra/     # adapters
src/test/java/scheduling/
docs/tasks.md
```
