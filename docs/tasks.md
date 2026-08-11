# Appointment Scheduler — Implementation Tasks

Track implementation of [Multi-tenant Appointment / Service Scheduler LLD](../../docs/lld/multi-tenant-appointment-scheduler.md) (Draft v1.1).

**How to use:** check boxes as work lands. Tasks are sized to be independently implementable; follow **Depends on** when order matters. Each task lists LLD sections and acceptance criteria.

**Tests (required):** every task must ship focused unit tests under `src/test/java` covering its acceptance criteria. Prefer plain JUnit 5 for domain/value types; use Spring Boot test support when wiring beans/context. A task is not done until `./mvnw test` stays green.

**Legend**

| Status | Meaning |
| --- | --- |
| `[ ]` | Not started |
| `[~]` | In progress |
| `[x]` | Done |

---

## Progress

| Phase | Focus | Tasks |
| --- | --- | --- |
| 0 | Scaffold & contracts | T0.1–T0.4 |
| 1 | Domain model | T1.1–T1.5 |
| 2 | Persistence schema | T2.1–T2.3 |
| 3 | Repositories (SQL) | T3.1–T3.4 |
| 4 | Tenant & rules | T4.1–T4.4 |
| 5 | Hold / claim correctness | T5.1–T5.3 |
| 6 | Scheduling write path | T6.1–T6.4 |
| 7 | Availability read path | T7.1–T7.4 |
| 8 | Advisor & auth | T8.1–T8.3 |
| 9 | Jobs & scale | T9.1–T9.4 |
| 10 | Hardening & verification | T10.1–T10.4 |

---

## Phase 0 — Scaffold & API contracts

### T0.1 — Project skeleton
- [x] Create package layout aligned with LLD components (`api`, `domain`, `core`, `spi` / repos, `infra`)
- [x] Maven + Spring Boot build (`pom.xml`, `./mvnw`, `SchedulingApplication`)
- [x] Unit tests: Spring context smoke test (`SchedulingApplicationTest`)
- **LLD:** §5
- **Done when:** empty modules / packages exist for AvailabilityService, SchedulingService, SlotLockManager, BookingRuleEngine, SlotRepository, AppointmentRepository; `./mvnw test` runs

### T0.2 — Core enums & value types
- [x] Implement `ResourceType`, `SlotStatus`, `AppointmentStatus`, `RequiredResourceMode`, `BookingActor`
- [x] Implement `TimeRange`, `SlotView`, `HoldResult`, `BookableWindow`
- [x] Unit tests under `src/test/java/scheduling/domain`
- **LLD:** §6.2, §6.4, §7.2
- **Done when:** types compile; `AppointmentStatus` has only `CONFIRMED` / `CANCELLED` (no `RESCHEDULED`); domain unit tests pass

### T0.3 — Request / error contracts
- [ ] Implement `BookingRequest` (incl. `holdToken`, `actor`, `bypassLeadTime`, `expectedVersions`)
- [ ] Implement exceptions: `SlotTakenException`, `ValidationException`, `UnknownTenantException`, `AppointmentNotFoundException`, `HoldExpiredException`, `IdempotencyConflictException`
- [ ] Unit tests for request construction / exception semantics
- **LLD:** §7.2, §7.3
- **Done when:** DTO + error types match LLD names/semantics; tests pass

### T0.4 — Service interfaces
- [ ] Define `AvailabilityService`, `SchedulingService`, `SlotLockManager`, `BookingRuleEngine`
- [ ] Define `SlotRepository`, `AppointmentRepository` method signatures
- [ ] Compile-only / contract tests (method signatures present; cancel/reschedule take `requestId`)
- **LLD:** §6.3, §7.1
- **Depends on:** T0.2, T0.3
- **Done when:** interfaces match LLD; cancel/reschedule take `requestId`; tests pass

---

## Phase 1 — Domain model

### T1.1 — Slot entity & state machine
- [ ] Model `Slot` (`version`, `holdExpiresAt`, `holdToken`)
- [ ] Document / enforce transitions: AVAILABLE ↔ HELD ↔ BOOKED
- **LLD:** §6.2, §6.5
- **Done when:** domain rejects illegal status transitions in unit tests

### T1.2 — Appointment entity
- [ ] Model `Appointment` with `requestId`, `slotIds`, `serviceCenterId`, status
- [ ] Support confirm, cancel, `replaceSlots` (reschedule keeps `CONFIRMED`)
- [ ] Unit tests covering this task's acceptance criteria
- **LLD:** §6.2, §8.5, R13
- **Done when:** reschedule updates slots on same row; status stays `CONFIRMED`

### T1.3 — Tenant / center / resource / service type models
- [ ] `Tenant`, `ServiceCenter` (timezone, `slotMinutes`), `Resource`, `ServiceType`
- [ ] `Customer` entity
- [ ] Unit tests covering this task's acceptance criteria
- **LLD:** §6.1, §10.1
- **Done when:** models carry `tenantId` where required

### T1.4 — BookableWindow pairing contract
- [ ] Implement `BookableWindow` (`windowId`, bay/tech slot lists, `expectedVersions`)
- [ ] Helper to validate consecutive N-slot windows and bay+tech pairing
- **LLD:** §6.4, R4
- **Depends on:** T0.2, T1.1
- **Done when:** unit tests cover valid/invalid pairing and duration continuity

### T1.5 — TenantContext & resolver port
- [ ] `TenantContext` + `TenantResolver`; unknown tenant → `UnknownTenantException`
- [ ] Unit tests covering this task's acceptance criteria
- **LLD:** §11.1
- **Done when:** resolve fails closed for unknown tenant

---

## Phase 2 — Persistence schema

### T2.1 — Core tenant tables
- [ ] DDL: `tenant`, `customer`, `service_center`, `resource`, `service_type`, `resource_certification`
- [ ] Indexes: `idx_customer_tenant`, `idx_center_tenant`, `idx_resource_center`
- [ ] Unit tests covering this task's acceptance criteria
- **LLD:** §10.1, R8, R9
- **Done when:** migration applies cleanly

### T2.2 — Calendar & slot tables
- [ ] DDL: `business_calendar`, `holiday_calendar`, `slot`
- [ ] Unique `uq_slot_resource_start`; partial avail index `idx_slot_avail_scan`
- [ ] Slot columns: `version`, `hold_expires_at`, `hold_token`, status check constraint
- [ ] Unit tests covering this task's acceptance criteria
- **LLD:** §10.1, R1, R7, R8
- **Depends on:** T2.1
- **Done when:** schema matches LLD; no partial unique on BOOKED-only (R17)

### T2.3 — Appointment, mutation, cache-version, outbox
- [ ] DDL: `appointment`, `appointment_slot`, `scheduling_mutation`, `service_center_calendar_version`, `outbox`
- [ ] Unique `(tenant_id, request_id)` on appointment
- [ ] Unique `uq_active_slot_occupancy` on `(tenant_id, slot_id)` for occupancy integrity
- [ ] Unit tests covering this task's acceptance criteria
- **LLD:** §10.1, R3, R6, R12
- **Depends on:** T2.1, T2.2
- **Done when:** all LLD tables + critical unique indexes exist

---

## Phase 3 — Repositories (conditional SQL)

### T3.1 — SlotRepository: claim / claimHeld / hold / release
- [ ] `claim`: AVAILABLE **or** expired HELD → BOOKED (clears hold fields)
- [ ] `claimHeld`: HELD + matching `hold_token` + not expired + version
- [ ] `hold`: AVAILABLE → HELD with server-generated `holdToken`
- [ ] `release`: BOOKED|HELD → AVAILABLE
- [ ] Unit tests covering this task's acceptance criteria
- **LLD:** §9.2, §10.2, R1, R2
- **Depends on:** T0.4, T2.2
- **Done when:** SQL matches LLD; unit/integration tests cover win/lose races and wrong token

### T3.2 — SlotRepository: availability scan
- [ ] Query AVAILABLE + expired HELD for center/range/resource type
- [ ] Exclude active HELD from results
- [ ] Unit tests covering this task's acceptance criteria
- **LLD:** §8.1, §10.2
- **Depends on:** T3.1
- **Done when:** scan returns reclaimable expired holds; omits active holds

### T3.3 — AppointmentRepository: idempotent insert-or-get
- [ ] `insertOrGetByRequestId` **inside** transaction (`ON CONFLICT DO NOTHING` + select)
- [ ] Detect same `requestId` + different payload → `IdempotencyConflictException`
- [ ] `linkSlots`, `load`, `update`, `runInTransaction`
- [ ] Unit tests covering this task's acceptance criteria
- **LLD:** §8.3, §10.2, R3
- **Depends on:** T2.3
- **Done when:** concurrent same `requestId` yields one appointment; conflict payload throws

### T3.4 — Mutation ledger & calendar version
- [ ] `mutationSeen` / `recordMutation` on `scheduling_mutation`
- [ ] Bump / read `service_center_calendar_version`
- [ ] Enqueue outbox rows in same txn as appointment writes
- [ ] Unit tests covering this task's acceptance criteria
- **LLD:** §8.4, §8.5, §10.1, R12
- **Depends on:** T2.3
- **Done when:** cancel/reschedule replay is a no-op; calendar version increments on write

---

## Phase 4 — Tenant isolation & booking rules

### T4.1 — Tenant-scoped repository discipline
- [ ] Every repo method takes `tenantId` first; queries always filter by it
- [ ] Unit tests covering this task's acceptance criteria
- **LLD:** §11.1, N5
- **Depends on:** T3.1–T3.4
- **Done when:** cross-tenant fixture tests cannot see foreign data

### T4.2 — BookingRuleEngine (Strategy skeleton)
- [ ] `BookingRule` interface + `TenantAwareRuleEngine` chaining rules
- [ ] Unit tests covering this task's acceptance criteria
- **LLD:** §11.2
- **Depends on:** T0.4, T1.5
- **Done when:** engine runs ordered rules; first failure throws `ValidationException`

### T4.3 — Core rules: hours, lead, horizon, certs, duration
- [ ] `BusinessHoursRule` (+ holidays)
- [ ] `LeadTimeRule`, `HorizonRule`
- [ ] `CertificationRule`
- [ ] `DurationAndPairingRule` (consecutive slots + BAY_AND_TECHNICIAN window)
- **LLD:** §11.2, §6.4, R4, R8
- **Depends on:** T4.2, T1.4
- **Done when:** each rule has focused unit tests; hours never bypassable

### T4.4 — CapacityRule
- [ ] Max concurrent BOOKED bay slots overlapping instant T for center
- [ ] Unit tests covering this task's acceptance criteria
- **LLD:** §11.2, R14
- **Depends on:** T4.2, T3.1
- **Done when:** booking above capacity rejected; under capacity allowed

---

## Phase 5 — Hold path

### T5.1 — SlotLockManager
- [ ] `hold(tenantId, slotId, ttl, expectedVersion)` → `HoldResult` with `holdToken`
- [ ] `release(tenantId, slotId, holdToken)` with token check
- [ ] Unit tests covering this task's acceptance criteria
- **LLD:** §7.1, §8.2, R1, R19
- **Depends on:** T3.1
- **Done when:** lost race returns `success=false`; naming is `holdToken` everywhere

### T5.2 — Expired-hold reclaim on claim path
- [ ] Ensure `claim` books expired HELD without requiring sweeper
- [ ] Unit tests covering this task's acceptance criteria
- **LLD:** §8.2, §9.2, R2
- **Depends on:** T3.1
- **Done when:** test books expired HELD via `claim` without prior sweeper run

### T5.3 — Hold expiry sweeper job
- [ ] Background job every ~60s: expired HELD → AVAILABLE (clear token)
- [ ] Unit tests covering this task's acceptance criteria
- **LLD:** §12.2
- **Depends on:** T3.1
- **Done when:** sweeper is idempotent and races safely with `claimHeld`

---

## Phase 6 — Scheduling write path

### T6.1 — DefaultSchedulingService.book
- [ ] Resolve tenant → validate rules → txn: insertOrGet → claim slots ascending → link → outbox → bump calendar version
- [ ] Support hold confirm via `claimHeld` when `holdToken` present
- [ ] Multi-slot / bay+tech: single txn, ascending `slot_id` order
- [ ] Unit tests covering this task's acceptance criteria
- **LLD:** §8.3, §15.1, R3, R5
- **Depends on:** T3.1, T3.3, T3.4, T4.3, T5.1
- **Done when:** race on last slot has one winner; retry same `requestId` returns original; partial multi-slot failure rolls back

### T6.2 — cancel (idempotent)
- [ ] Mark cancelled, release slots ascending, record mutation, outbox, bump calendar version
- [ ] Replay same `requestId` is no-op
- [ ] Unit tests covering this task's acceptance criteria
- **LLD:** §8.4, R12
- **Depends on:** T3.4, T6.1
- **Done when:** double cancel safe; slots become AVAILABLE

### T6.3 — reschedule (claim-new-then-release-old)
- [ ] Idempotent on `requestId`; same appointment row stays `CONFIRMED`
- [ ] Fail closed if new claim fails (old slots unchanged)
- [ ] Unit tests covering this task's acceptance criteria
- **LLD:** §8.5, R13
- **Depends on:** T6.1, T3.4
- **Done when:** new slot taken aborts cleanly; success moves occupancy; replay returns same appt

### T6.4 — Concurrent cancel vs reschedule
- [ ] Use `appointment.version` optimistic lock; loser retries or fails clearly
- [ ] Unit tests covering this task's acceptance criteria
- **LLD:** §13
- **Depends on:** T6.2, T6.3
- **Done when:** concurrent cancel+reschedule never leaves inconsistent slot occupancy

---

## Phase 7 — Availability read path

### T7.1 — Single-resource availability
- [ ] Find consecutive `durationSlots` windows for BAY_ONLY / TECHNICIAN_ONLY
- [ ] Filter certified resources; include expired HELD as bookable candidates
- [ ] Return `SlotView` / windows with `expectedVersions`
- [ ] Unit tests covering this task's acceptance criteria
- **LLD:** §8.1, F1
- **Depends on:** T3.2, T4.3
- **Done when:** oil-change (1 slot) and multi-slot jobs return correct windows

### T7.2 — BAY_AND_TECHNICIAN pairing in availability
- [ ] Emit one `BookableWindow` per valid (bay, technician) pair at start T
- [ ] Unit tests covering this task's acceptance criteria
- **LLD:** §6.4, §8.1, R4
- **Depends on:** T1.4, T7.1
- **Done when:** unpaired free bay/tech alone does not produce a window

### T7.3 — Availability cache with calendarVersion
- [ ] Cache key: `(tenantId, centerId, serviceTypeId, range bucket, calendarVersion)`
- [ ] TTL 15–60s; bump version on book/cancel/reschedule (no broad blind flush required)
- [ ] Unit tests covering this task's acceptance criteria
- **LLD:** §8.1, §12.2, R16
- **Depends on:** T3.4, T7.1
- **Done when:** after book, new reads miss stale version; claim still authoritative if cache stale

### T7.4 — Read replica lag routing (optional config)
- [ ] Route availability to replica only if lag ≤ budget (e.g. 2s); else primary
- [ ] Claims always primary
- [ ] Unit tests covering this task's acceptance criteria
- **LLD:** §12.2, R15
- **Depends on:** T7.1
- **Done when:** lag over budget forces primary; documented config knob

---

## Phase 8 — Advisor flows & authorization

### T8.1 — Advisor book with rule bypass
- [ ] `actor=ADVISOR` + `bypassLeadTime` skips LeadTime/Horizon only
- [ ] Audit log: advisor_id, rule, appointment_id, timestamp
- [ ] BusinessHours + Certification never bypassed
- [ ] Unit tests covering this task's acceptance criteria
- **LLD:** §7.4, §11.3, R10
- **Depends on:** T4.3, T6.1
- **Done when:** advisor can book inside lead window; hours/certs still enforced; audit written

### T8.2 — blockSlot
- [ ] Advisor marks slot BOOKED via internal blocked appointment (no customer)
- [ ] Availability flags blocked slots for advisors
- [ ] Unit tests covering this task's acceptance criteria
- **LLD:** §7.4
- **Depends on:** T6.1, T7.1
- **Done when:** blocked slot not customer-bookable; advisor can unblock via cancel path

### T8.3 — Role-based authorization matrix
- [ ] Enforce JWT claims: `tenant_id`, `role`, `customer_id` / `advisor_id`
- [ ] Matrix: findAvailable / book / cancel / reschedule / blockSlot / config
- [ ] Unit tests covering this task's acceptance criteria
- **LLD:** §11.4, R11
- **Depends on:** T6.x, T8.1, T8.2
- **Done when:** customer cannot act cross-tenant or on others’ appointments; advisor scoped to center

---

## Phase 9 — Materializer, outbox, scale

### T9.1 — Slot materializer job
- [ ] Generate AVAILABLE slots for horizon N days in center local TZ → store UTC
- [ ] Skip closed holidays; `INSERT ON CONFLICT DO NOTHING`
- [ ] Onboard single resource; hours-change regenerates future AVAILABLE only
- [ ] DST: use `ZonedDateTime`; skip/flag ambiguous fall-back times
- [ ] Unit tests covering this task's acceptance criteria
- **LLD:** §12.1, R7
- **Depends on:** T2.2
- **Done when:** materializer is idempotent; never overwrites BOOKED/active HELD

### T9.2 — Outbox publisher
- [ ] Emit `AppointmentBooked` / `Cancelled` / `Rescheduled` after durable commit
- [ ] Poll unpublished rows; mark `published_at`
- [ ] Unit tests covering this task's acceptance criteria
- **LLD:** §5, §8.3–§8.5
- **Depends on:** T3.4
- **Done when:** events appear only after successful txn; at-least-once publish with idempotent consumers assumed

### T9.3 — Cache invalidation wiring
- [ ] All write paths bump `calendarVersion` after commit
- [ ] Unit tests covering this task's acceptance criteria
- **LLD:** §12.2, R16
- **Depends on:** T6.1–T6.3, T7.3
- **Done when:** book/cancel/reschedule/block all bump version

### T9.4 — Tenant rate limit on book (optional)
- [ ] Optional per-tenant book rate limit; DB remains correctness layer
- [ ] Unit tests covering this task's acceptance criteria
- **LLD:** §12.2
- **Depends on:** T6.1
- **Done when:** excess books rejected without affecting claim correctness

---

## Phase 10 — Hardening & verification

### T10.1 — Concurrency race suite
- [ ] Two clients, last slot → exactly one success
- [ ] Same `requestId` concurrent → one appointment
- [ ] Multi-slot ascending order; no deadlock under parallel bay+tech books
- [ ] Hold stolen / wrong token → `HoldExpiredException`
- [ ] Unit tests covering this task's acceptance criteria
- **LLD:** §9.1, §13, R5
- **Depends on:** T5.x, T6.1
- **Done when:** automated tests cover table in §9.1 and §13 critical rows

### T10.2 — Idempotency & failure-mode suite
- [ ] Duplicate requestId same/different payload
- [ ] Process die mid-txn → rollback restores slot
- [ ] Reschedule abort leaves old reservation
- [ ] Unique index violation maps to `SlotTakenException`
- **LLD:** §13, N4
- **Depends on:** T6.x
- **Done when:** §13 scenarios have regression tests

### T10.3 — End-to-end happy paths
- [ ] Availability → hold → book
- [ ] Book → cancel → slot free again
- [ ] Book → reschedule → new window occupied
- [ ] Advisor block + customer book rejection
- [ ] Unit tests covering this task's acceptance criteria
- **LLD:** F1–F9
- **Depends on:** Phases 5–8
- **Done when:** demo/script or integration test covers F1–F9

### T10.4 — LLD decision checklist (sign-off)
- [ ] Walk §16 Design Decisions Summary; confirm each choice is implemented or explicitly deferred
- [ ] Unit tests covering this task's acceptance criteria
- **LLD:** §16
- **Depends on:** all above
- **Done when:** checklist filed in this doc or REVIEW notes; no silent drift from v1.1

---

## Suggested implementation order

```text
T0.* → T1.* → T2.* → T3.1–T3.4
                 ↓
            T4.* + T5.*
                 ↓
              T6.1–T6.4
                 ↓
           T7.* ∥ T8.* ∥ T9.1–T9.2
                 ↓
              T10.*
```

Parallel tracks after T3:

| Track A (correctness) | Track B (reads) | Track C (ops) |
| --- | --- | --- |
| T4 rules | T7 availability | T9.1 materializer |
| T5 hold | T7.3 cache | T5.3 / T9.2 jobs |
| T6 book/cancel/reschedule | T8 advisor/auth | T9.3–T9.4 |

---

## Out of scope for v1 (do not track as must-have)

Per LLD §1.2 / §14 — document only if extending later:

- Full payment orchestration (hold → pay → claimHeld saga sketch only)
- Complex technician skill graphs beyond certification
- Recurring / first-class fleet bulk booking
- Cross-region active-active booking writes
- Redis/distributed lock as correctness boundary (optional UX only)

---

## Quick reference — LLD requirement coverage

| Req | Covered by |
| --- | --- |
| F1 Availability | T7.1, T7.2 |
| F2 Book + idempotency | T6.1, T3.3 |
| F3 Hold | T5.1–T5.3 |
| F4 Cancel | T6.2 |
| F5 Reschedule | T6.3 |
| F6 Per-tenant rules | T4.2–T4.4 |
| F7 Multi-tenant isolation | T1.5, T4.1, T8.3 |
| F8 Staff booking | T8.1, T8.2 |
| F9 Idempotent cancel/reschedule | T3.4, T6.2, T6.3 |
| N1–N3 Strong book / no double-book | T3.1, T6.1, T10.1 |
| N4 Idempotent writes | T3.3, T10.2 |
| N5 Tenant isolation | T4.1 |
