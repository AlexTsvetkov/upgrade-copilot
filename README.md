# upgrade-copilot

**AI-assisted version-upgrade and deprecation engine for SAP Commerce — turn a multi-month upgrade project into a tool-driven workflow.**

**🌐 Live site: https://alextsvetkov.github.io/upgrade-copilot/**

> ✅ **Status:** working core. A real, tested implementation of the core capability runs offline (no live SAP Commerce instance needed); unit tests pass in CI. Not yet a production product — see [Roadmap](#roadmap) for what would make it one.

**Stack:** Java 21 + Gradle.

---

## The problem

SAP Commerce version upgrades (2205→2211→…) are multi-month, high-risk and largely manual. Deprecations are discovered at compile/run time; custom-code compatibility is guesswork; SCA scanners drown teams in noise with no SAP-aware triage.

## The solution

Ingest a codebase + target version and produce a **deprecation/impact report**, **automated codemods** for mechanical migrations, and an **AI agent** that proposes and explains the rest — with a test-backed verification loop. Extend to SAP-aware CVE triage.

See the [project site](https://alextsvetkov.github.io/upgrade-copilot/) for the full benefits narrative.

## Design principles

1. **Report before change** — Start from a complete, prioritized impact report — no surprises mid-upgrade.
2. **Codemods for the mechanical** — Deterministic transforms handle the bulk; humans/agents handle the judgment calls.
3. **Verify every change** — A harness runs the project's own tests after each transform.
4. **Compounding knowledge** — A curated deprecation/codemod knowledge base per version delta grows with every upgrade.

## Core abstraction

`DeprecationScanner` — Analyzes a codebase against a target-version knowledge base and emits a prioritized, evidence-backed impact report.

## Features

| Capability | Description |
|------------|-------------|
| ``scan`` | Produce a deprecation & impact report for a target version. |
| ``codemod`` | Apply deterministic migrations to Java / items.xml / spring XML / ImpEx. |
| `Agent long-tail` | LLM-proposed changes with rationale + verification. |
| `CVE triage` | SAP-aware reachability filtering of transitive advisories. |

## Quick start

```bash
gradle build
gradle test
```

## Usage

Everything below is distilled from the runnable, heavily-commented tutorial at
`src/main/java/com/sapcommercetools/upgrade/examples/Example.java`. The
`Output:` blocks are the **real stdout** captured from running it.

### 1. Build the default scanner and scan source files

`DeprecationRules.defaultScanner()` returns a `DeprecationScanner` pre-loaded
with the illustrative SAP Commerce ruleset. The scanner is line-oriented and
language-agnostic: it applies every rule's regex to every line of each file
(supplied here as in-memory `path -> content` strings).

```java
DeprecationScanner scanner = DeprecationRules.defaultScanner();

Map<String, String> files = new LinkedHashMap<>();
files.put("LegacyTest.java", "import junit.framework.TestCase;");           // BLOCKER
files.put("CartService.java", "@Autowired\n... sessionService.getAttribute(\"cart\");"); // INFO + BLOCKER
files.put("ProductModel.java", "@Deprecated\npublic String getOldCode() { return null; }"); // WARNING

List<Finding> raw = scanner.scanAll(files);
List<Finding> prioritized = scanner.prioritize(raw); // BLOCKER > WARNING > INFO, then file/line/col
prioritized.forEach(f -> System.out.println(f.display()));
```

```text
Output:
Loaded rules:
  - platform-deprecated-marker [WARNING]
  - sessionservice-getattribute-legacy [BLOCKER]
  - junit-3-4-import [BLOCKER]
  - autowired-field-injection [INFO]

Total findings: 4 (shown highest-priority first)
  1. [BLOCKER] CartService.java:9:31 sessionservice-getattribute-legacy - Single-argument getAttribute(String) is a legacy accessor signature and no longer compiles against the target API. -> Migrate to the typed getAttribute(String, T defaultValue) overload or a dedicated typed accessor.
  2. [BLOCKER] LegacyTest.java:3:1 junit-3-4-import - Imports the JUnit 3/4 'junit.framework' package, which is unavailable under JUnit 5 (Jupiter). -> Replace with 'org.junit.jupiter.api.*' imports and migrate assertions to org.junit.jupiter.api.Assertions.
  3. [WARNING] ProductModel.java:4:5 platform-deprecated-marker - Usage annotated as @Deprecated; the referenced API may be removed in a future platform release. -> Locate the replacement API in the target-version migration notes and update the call site.
  4. [INFO] CartService.java:5:5 autowired-field-injection - Field injection via @Autowired hinders testability and immutability. -> Prefer constructor injection: inject dependencies as final constructor parameters.
```

### 2. Print the grouped, human-readable report

`report(...)` is what you'd surface to a developer: a summary line, per-severity
totals, and findings grouped under BLOCKER / WARNING / INFO.

```java
System.out.println(scanner.report(raw));
```

```text
Output:
Upgrade impact report: 4 finding(s)
Totals: BLOCKER=2, WARNING=1, INFO=1

BLOCKER (2)
  [BLOCKER] CartService.java:9:31 sessionservice-getattribute-legacy - Single-argument getAttribute(String) is a legacy accessor signature and no longer compiles against the target API. -> Migrate to the typed getAttribute(String, T defaultValue) overload or a dedicated typed accessor.
  [BLOCKER] LegacyTest.java:3:1 junit-3-4-import - Imports the JUnit 3/4 'junit.framework' package, which is unavailable under JUnit 5 (Jupiter). -> Replace with 'org.junit.jupiter.api.*' imports and migrate assertions to org.junit.jupiter.api.Assertions.

WARNING (1)
  [WARNING] ProductModel.java:4:5 platform-deprecated-marker - Usage annotated as @Deprecated; the referenced API may be removed in a future platform release. -> Locate the replacement API in the target-version migration notes and update the call site.

INFO (1)
  [INFO] CartService.java:5:5 autowired-field-injection - Field injection via @Autowired hinders testability and immutability. -> Prefer constructor injection: inject dependencies as final constructor parameters.
```

Gradle is not required — compile and run with the JDK (Java 21):

```bash
find src/main/java -name '*.java' | xargs javac -d /tmp/ex-upgrade
java -cp /tmp/ex-upgrade com.sapcommercetools.upgrade.examples.Example
```

## Roadmap

- [x] Implement the core capability with real logic + unit tests.
- [ ] Broaden coverage (more rules/edge cases) beyond the first working version.
- [ ] Wire against a live SAP Commerce / BTP environment.
- [ ] Publish artifacts and usage docs.

## Contributing

See [CONTRIBUTING.md](./CONTRIBUTING.md). Conventional commits; generated code stays out of version control.

## License

[MIT](./LICENSE) © 2026 Aliaksandr Tsviatkou

## Honest assessment

> From the v2 self-critical analysis. Scores use **Gap · Value · Moat · Time-to-revenue · Risk** (for Risk, **higher = safer**). Prior art is named deliberately — "no competitor" is almost never true.

**Scores:** Gap 3 · Value 5 · Moat 3 · TTR 2 · Risk 2

- **Prior art / competition.** OpenRewrite does JVM codemods (free); SAP has its own code-upgrade / AI-assisted tooling; SIs sell upgrades fixed-bid. You'd fight a free framework *and* the vendor.
- **True differentiator.** A curated SAP-Commerce-specific deprecation/codemod knowledge base (compounding data moat), built *on* OpenRewrite rather than reinventing it.
- **Kill criterion.** If OpenRewrite recipes + SAP's own tool cover the common version deltas, the paid slice is too thin.
- **Verdict.** **Explore, don't commit.** Biggest budget line, but validate the paid slice before building.

This assessment is part of a broader, self-critical analysis of the whole tool suite (problem landscape, go-to-market, and an IP / conflict-of-interest review) maintained privately by the author.

---

*Part of a backend tooling suite for SAP Commerce Cloud. See [`commerce-mcp`](https://github.com/AlexTsvetkov/commerce-mcp) for the AI-native flagship.*
