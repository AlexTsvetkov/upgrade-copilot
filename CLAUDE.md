# CLAUDE.md

Guidance for Claude Code (and other AI agents) working in this repository.

## What this project is

**upgrade-copilot** — AI-assisted version-upgrade and deprecation engine for SAP Commerce — turn a multi-month upgrade project into a tool-driven workflow.

SAP Commerce version upgrades (2205→2211→…) are multi-month, high-risk and largely manual. Deprecations are discovered at compile/run time; custom-code compatibility is guesswork; SCA scanners drown teams in noise with no SAP-aware triage.

**Solution:** Ingest a codebase + target version and produce a **deprecation/impact report**, **automated codemods** for mechanical migrations, and an **AI agent** that proposes and explains the rest — with a test-backed verification loop. Extend to SAP-aware CVE triage.

> Status: early scaffold. The core abstraction, a starter implementation and tests are real; most capabilities are documented intent, not yet built. Do not claim features exist that aren't in the code.

## Stack

Java 21 + Gradle (`java-library` plugin), JUnit 5.

## Project layout

- `src/main/java/**` — production code (core abstraction: `DeprecationScanner`).
- `src/test/java/**` — JUnit 5 tests.
- `build.gradle`, `settings.gradle` — build config.
- `docs/` — GitHub Pages site (`index.html`, `.nojekyll`). Served at https://alextsvetkov.github.io/upgrade-copilot/.
- `.github/workflows/ci.yml` — CI (build + test on push/PR).

## Common commands

```bash
gradle build      # compile
gradle test       # run tests
```

## Conventions

- Prefer **constructor injection**; interface + `Default*` impl per service.
- No inline literals — use constants classes for log/config/exception strings.
- Keep the core abstraction (`DeprecationScanner`) honest so implementations stay swappable.
- **Conventional commits** (`feat:`, `fix:`, `docs:`, `refactor:`, `test:`).
- Generated code (if any) stays out of version control.
- Keep `README.md`, `docs/index.html` and this file in sync when the scope changes.

## Working agreements for agents

- This is part of a **suite of SAP Commerce backend tools**; keep terminology consistent with the sibling repos (e.g. `commerce-mcp`, `flow-context`).
- When adding real behaviour, update the Roadmap in `README.md` and add tests in the same PR.
- Don't introduce a live-backend dependency into the default build — keep the scaffold green on a clean checkout.
- If you change the public contract, reflect it in the docs site and the README capability table.
