<!--
Sync Impact Report
==================
Version change: 1.0.0 → 1.1.0

Updated Principles:
- P6: Framework Isolation → Framework Isolation & Hexagonal Architecture
  - Added architecture layers diagram
  - Added dependency direction rules (Infrastructure → Application → Domain)
  - Added port interface location rules
  - Expanded rationale to include hexagonal architecture benefits

Previous Version (1.0.0):
- P1: Code Quality Excellence
- P2: Testing Standards
- P3: Behavior Driven Development
- P4: Domain Driven Design
- P5: SOLID Principles
- P6: Framework Isolation
- P7: User Experience Consistency
- P8: Performance Requirements

Templates requiring updates:
- .specify/templates/plan-template.md (pending creation)
- .specify/templates/spec-template.md (pending creation)
- .specify/templates/tasks-template.md (pending creation)

Follow-up TODOs: None
-->

# Circuit-Breaker Project Constitution

**Version:** 1.1.0
**Ratification Date:** 2025-12-03
**Last Amended:** 2025-12-04

## Preamble

This constitution establishes the foundational principles and non-negotiable standards
that govern all development activities within the Circuit-Breaker project. These
principles ensure maintainability, reliability, and consistency across the codebase
while enabling sustainable growth and evolution of the system.

All contributors MUST adhere to these principles. Violations require remediation
before code integration.

## Core Principles

### Principle 1: Code Quality Excellence

**Statement:** All code MUST meet established quality standards before integration.

**Non-Negotiable Rules:**
- Code MUST pass static analysis with zero errors and zero warnings
- Code MUST follow established naming conventions and formatting standards
- Functions MUST have a single, clear responsibility
- Cyclomatic complexity MUST NOT exceed 10 per function
- Code duplication MUST be eliminated through appropriate abstractions
- All public APIs MUST be documented with clear contracts
- Dead code MUST be removed, not commented out

**Rationale:** High code quality reduces technical debt, improves maintainability,
and enables faster iteration. Quality gates prevent degradation over time.

### Principle 2: Testing Standards

**Statement:** All code MUST be covered by appropriate automated tests.

**Non-Negotiable Rules:**
- Unit test coverage MUST be at minimum 80% for all modules
- Critical paths MUST have 100% test coverage
- Tests MUST be deterministic and isolated
- Tests MUST run without external dependencies (mocked/stubbed)
- Integration tests MUST verify component interactions
- Tests MUST be maintainable and readable as documentation
- Flaky tests MUST be fixed or removed immediately
- Test execution time MUST remain within acceptable bounds (unit tests < 5s each)

**Rationale:** Comprehensive testing provides confidence in code correctness,
enables refactoring, and serves as living documentation of expected behavior.

### Principle 3: Behavior Driven Development

**Statement:** Features MUST be specified and validated using BDD practices.

**Non-Negotiable Rules:**
- Features MUST be described using Given-When-Then scenarios
- Acceptance criteria MUST be defined before implementation begins
- Scenarios MUST be written in ubiquitous language understandable by stakeholders
- Automated acceptance tests MUST verify all defined scenarios
- Scenarios MUST focus on behavior outcomes, not implementation details
- Edge cases and error conditions MUST be explicitly specified
- Living documentation MUST be generated from executable specifications

**Rationale:** BDD ensures alignment between business requirements and technical
implementation while creating executable documentation that remains current.

### Principle 4: Domain Driven Design

**Statement:** The codebase MUST reflect domain concepts and boundaries.

**Non-Negotiable Rules:**
- Code MUST use ubiquitous language consistent with the domain model
- Bounded contexts MUST be clearly defined and respected
- Aggregates MUST enforce invariants within their boundaries
- Domain logic MUST reside in the domain layer, not in infrastructure
- Entities MUST be identified by unique identifiers, not attribute equality
- Value objects MUST be immutable and equality-comparable by value
- Domain events MUST capture significant state changes
- Anti-corruption layers MUST protect domain boundaries from external systems
- Repository interfaces MUST be defined in the domain, implementations in infrastructure

**Rationale:** DDD creates a shared understanding between technical and domain
experts, resulting in software that accurately models business processes.

### Principle 5: SOLID Principles

**Statement:** All object-oriented code MUST adhere to SOLID principles.

**Non-Negotiable Rules:**
- **Single Responsibility:** Each class/module MUST have exactly one reason to change
- **Open/Closed:** Code MUST be open for extension, closed for modification
- **Liskov Substitution:** Derived types MUST be substitutable for their base types
- **Interface Segregation:** Clients MUST NOT depend on interfaces they do not use
- **Dependency Inversion:** High-level modules MUST NOT depend on low-level modules;
  both MUST depend on abstractions

**Rationale:** SOLID principles produce loosely coupled, highly cohesive code that
is easier to maintain, test, and extend.

### Principle 6: Framework Isolation & Hexagonal Architecture

**Statement:** Frameworks and external dependencies MUST be confined to the
infrastructure layer. Dependencies MUST flow inward (Infrastructure → Application → Domain).

**Architecture Layers:**

```
┌─────────────────────────────────────────────────────────────────┐
│                    Infrastructure Layer (外層)                   │
│  Controllers, Repositories Impl, HTTP Clients, Schedulers       │
│                           │                                      │
│                           │ depends on / implements              │
│                           ▼                                      │
├─────────────────────────────────────────────────────────────────┤
│                    Application Layer (中層)                      │
│  Use Cases, Input Ports, Output Port Interfaces                 │
│                           │                                      │
│                           │ depends on                           │
│                           ▼                                      │
├─────────────────────────────────────────────────────────────────┤
│                      Domain Layer (核心)                         │
│  Entities, Value Objects, Domain Services, Domain Events        │
│                                                                  │
│  ⚠️ Domain MUST NOT depend on any outer layer                    │
└─────────────────────────────────────────────────────────────────┘
```

**Non-Negotiable Rules:**
- Domain and application layers MUST NOT import framework-specific code
- Framework dependencies MUST be wrapped behind abstraction interfaces
- Infrastructure implementations MUST be swappable without domain changes
- Configuration MUST be injected, not hardcoded or framework-coupled
- Database access, HTTP handling, and messaging MUST reside in infrastructure only
- Core business logic MUST remain framework-agnostic and portable
- Dependency injection MUST be configured at composition root only

**Dependency Direction Rules (Hexagonal Architecture):**
- ✅ Infrastructure MUST depend on Application and Domain (outer depends on inner)
- ✅ Application MUST depend on Domain only
- ❌ Domain MUST NOT depend on Application (prohibited)
- ❌ Domain MUST NOT depend on Infrastructure (prohibited)
- ❌ Application MUST NOT depend on Infrastructure (prohibited)

**Port Interface Location Rules:**
- Input Ports (Use Case interfaces): MUST be defined in Application layer
- Output Ports (Repository, Gateway interfaces): MUST be defined in Domain layer
- Infrastructure layer MUST implement Output Port interfaces defined in Domain

**Rationale:** Framework isolation protects core business logic from framework
churn, enables technology upgrades, and improves testability. The hexagonal
architecture ensures that domain logic remains pure and independent of technical
concerns, making the system more maintainable and adaptable to change.

### Principle 7: User Experience Consistency

**Statement:** All user-facing interfaces MUST provide consistent, predictable
experiences.

**Non-Negotiable Rules:**
- UI components MUST follow established design system guidelines
- Error messages MUST be user-friendly and actionable
- Loading states MUST provide appropriate feedback
- Interactions MUST be responsive (< 100ms perceived latency for user input)
- Accessibility standards (WCAG 2.1 AA) MUST be met
- Terminology MUST be consistent across all interfaces
- Navigation patterns MUST be predictable and documented
- Undo/recovery options MUST be provided for destructive actions

**Rationale:** Consistent UX reduces user cognitive load, improves adoption, and
builds trust in the system.

### Principle 8: Performance Requirements

**Statement:** The system MUST meet defined performance benchmarks.

**Non-Negotiable Rules:**
- API response time MUST be < 200ms at p95 under normal load
- Page load time MUST be < 2 seconds for initial load
- Memory usage MUST NOT grow unboundedly (no memory leaks)
- Database queries MUST be optimized (no N+1 queries, proper indexing)
- Batch operations MUST implement pagination or streaming
- Performance regression tests MUST be part of CI pipeline
- Resource utilization MUST be monitored and alerted
- Scalability limits MUST be documented and tested

**Rationale:** Performance directly impacts user experience and system reliability.
Proactive performance management prevents degradation.

## Governance

### Amendment Procedure

1. Propose amendment via documented change request
2. Gather feedback from all affected stakeholders
3. Achieve consensus or majority approval from maintainers
4. Update constitution with new version number
5. Propagate changes to all dependent templates and documentation
6. Communicate changes to all contributors

### Versioning Policy

This constitution follows semantic versioning:
- **MAJOR:** Backward-incompatible principle removals or fundamental redefinitions
- **MINOR:** New principles added or existing principles materially expanded
- **PATCH:** Clarifications, wording improvements, typo fixes

### Compliance Review

- Code reviews MUST verify adherence to constitutional principles
- Automated checks MUST enforce measurable requirements
- Quarterly audits MUST assess overall compliance
- Non-compliance MUST be tracked and remediated with documented plans
- Exceptions require explicit approval and documented justification

---

*This constitution is a living document. All contributors are expected to uphold
these principles and participate in their evolution.*
