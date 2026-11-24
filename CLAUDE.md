# PcLogin: notes for agents

- Never run `m`/`lunch`/soong here to test: `core/run_tests.sh`, `python3 -m unittest discover -s tests` and
  `tools/typecheck.sh` (README, "Tests"). Keep JVMs at -Xmx1.5g or less.
- TDD: test first in `core/tests/*Test.kt`, see it fail, then code. Logic goes in `core/` (no Android);
  `src/` only wires Android (`AndroidUserDirectory` implements the `UserDirectory` port) and draws.
- Tables over if/else: `AdditionRules` rule lists, `PodSize.forCount`, `KIND_RULES`, `POD_METRICS`,
  `MESSAGES`. Names start with an English verb or prefix.
- UI text: pt-BR in `res/values-pt`, English in `res/values`; what happened and what to do, no em dash, no
  design explanations (`tests/test_strings.py`).
- Googlebook's Login app is a layout reference only (sizes, structure); never copy its code or assets.
- The headless system user setup (props, overlays, flags, sysconfig) lives in `vendor/pc/ui` (`pc_login.mk`).
- Commits: no AI attribution lines.
