# Phase 0 — Foundation

**Date:** 2026-09-03

Phase 0 sets up the repository, the build, and the safety net. No application
code yet, on purpose. The goal is that a clean clone builds, and that broken
code cannot reach `main`.

## What was built

- Public repository with MIT licence, description and topics.
- `.gitattributes` and `.editorconfig` — committed before any code, so every
  file added afterwards follows the same rules.
- Spring Boot 4.1.1 skeleton on Java 25, Maven, JAR packaging.
- Two dependencies only: `spring-boot-starter-webmvc` and
  `spring-boot-starter-actuator`. More will be added when they are needed, so
  each one's effect is visible on its own.
- Maven wrapper pinned to Maven 3.9.16 and committed, so a clean clone builds
  without Maven installed.
- GitHub Actions workflow that builds and tests on every pull request and every
  push to `main`.
- Branch protection on `main`: pull request required, 0 approvals, linear
  history, force pushes blocked, deletions restricted, empty bypass list, and
  `Build and test` as a required status check.
- Squash merge only. Branches are deleted after merge.

## Evidence

| Check | Result |
| --- | --- |
| `./mvnw clean verify` | `BUILD SUCCESS`, produced `target/vetan-0.0.1-SNAPSHOT.jar` |
| Surefire report | `Tests run: 1, Failures: 0, Errors: 0` in `in.vetan.VetanApplicationTests` |
| `GET /actuator/health` | `{"status":"UP"}` |
| `git ls-files --eol` | every file `i/lf`; `mvnw` has `eol=lf`, `mvnw.cmd` has `eol=crlf` |
| `file mvnw` | `POSIX shell script, ASCII text executable` — no CRLF |
| `git ls-files -s mvnw` | `100755` |
| PR #1, PR #2 | merged with CI green |
| PR #3 | deliberately failing test — merge button disabled, check shown as `Required`, no owner override. Closed without merging. |

PR #3 was not an accident. The gate depends on four things being correct at the
same time: the workflow runs on pull requests, the check name matches the job
name exactly, the ruleset is active on `main`, and the bypass list is empty. If
any one of them is wrong there is no protection, and nothing looks different
while the build is passing. Pushing a failing test on purpose was the only way
to confirm all four.

## Problems hit

**1. Wrong base package.**
Spring Initializr sets the package to group plus artifact, which gave
`in.vetan.vetan`. `@SpringBootApplication` includes `@ComponentScan`, and that
only scans downwards from the package of the class it is on. With the main class
one level too deep, every planned module — `in.vetan.payroll`,
`in.vetan.identity` and the rest — would have been invisible to Spring. The
failure would have appeared later as "no qualifying bean" errors that point at
the injection site instead of the real cause. Moved the main class and the test
class to `in.vetan`. The surefire report now names
`in.vetan.VetanApplicationTests`, which confirms it.

**2. GitHub's stock Java `.gitignore` is not usable for Maven.**
It has no `target/` entry and no IDE ignores, so the first build would have
staged thousands of compiled files, and `.idea/` would have been committed.
Replaced it with the one Spring Initializr generates. This is the reason
`.gitignore` was committed in step one, before any build output could exist —
removing build artifacts from history afterwards is much harder than preventing
them.

**3. The Maven wrapper was committed without the executable bit.**
CI failed on its first run with `./mvnw: Permission denied` and exit code 126.
Windows has no Unix execute bit — NTFS uses ACLs — so Git for Windows sets
`core.fileMode=false` and the file was stored as mode `100644`. Locally nothing
broke, because `mvnw.cmd` runs through `cmd.exe`, which does not check
permissions. Fixed with `git update-index --chmod=+x mvnw`, which writes the
mode into the index directly.

This one is worth recording properly: **it could not have been found on the
development machine.** Windows cannot express the permission that was missing.
The Linux runner found it in six seconds. That is the clearest argument for CI
in this whole phase.

**4. `pom.xml` did not follow the project's own `.editorconfig`.**
Initializr generates it with tabs; the config specifies 2 spaces for XML.
Reformatted it. A rule that generated files are exempt from is not a rule.

## Still open

Nothing below exists yet.

- No Docker or Docker Compose. The project cannot be started with one command.
- No database. No JPA, no MySQL connection, no migrations.
- No Redis.
- No static analysis — Checkstyle, SpotBugs, gitleaks are all absent.
- No coverage measurement and no coverage gate.
- No Testcontainers, so no tests run against a real database.
- No OpenAPI specification.
- No deployment of any kind.
- **No domain code at all.** There is no company, no employee, no payroll run.

Phase 0 only proves the build and the gate work. The next step is Docker
Compose, so MySQL and Redis can be started from the repository itself.
