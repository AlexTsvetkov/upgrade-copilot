# upgrade-copilot

**AI-assisted version-upgrade and deprecation engine for SAP Commerce — turn a multi-month upgrade project into a tool-driven workflow.**

**🌐 Live site: https://alextsvetkov.github.io/upgrade-copilot/**

> ⚠️ **Status:** early scaffold. The core abstraction, a starter implementation and tests are real; this is a foundation to build on, not a finished product. See [Roadmap](#roadmap).

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

## Roadmap

- [ ] Flesh out the core beyond the starter implementation.
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

See the full landscape, go-to-market and the **IP / conflict-of-interest** discussion in [sap-commerce-general-ideas-for-startup.md](https://github.com/AlexTsvetkov/sap-commerce-ideas-for-projects/blob/main/ideas-for-startup/sap-commerce-general-ideas-for-startup.md).

---

*Part of a backend tooling suite for SAP Commerce Cloud. See [`commerce-mcp`](https://github.com/AlexTsvetkov/commerce-mcp) for the AI-native flagship.*
