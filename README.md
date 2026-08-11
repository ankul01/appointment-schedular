# appointment-schedular

Multi-tenant appointment / service scheduler (LLD practice).

## Stack

- Java 21
- Maven
- Spring Boot 3.4

## Build & test

```bash
./mvnw test
./mvnw -DskipTests package
```

Or via Make:

```bash
make test
make build
```

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
