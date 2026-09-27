# Runway drafting helpers

Scratch tooling for the closed-beta runway (see `docs/CLOSED_BETA_RUNWAY_PLAN.md`).
Not used by the runtime or importers. Run from the repository root.

- `lib.py`: `Batch` writes a batch's draft files, candidates, and ledger.
  Re-adding an id that `load_existing()` loaded replaces it, so batch scripts can be rerun.
  `load_existing()` extends a batch that has already been written.
- `b1002.py` ... `b1218.py`: one script per weekly batch (named by start date).
  `b1009.py` extends an already-written batch via `load_existing()`; the
  Nov 13 and later scripts cite Britannica's dated On This Day pages.
  Run it with `PYTHONPATH=editorial/tools/runway python3 editorial/tools/runway/b1009.py`.
- `merge.py <outdir>`: merges canonical content with every draft batch into
  `<outdir>/merged`.
- `ValidateMerged.java`: runs the canonical event and quiz validators on the
  merged directory:
  `java -cp target/classes:$(mvn -q dependency:build-classpath -Dmdep.outputFile=/dev/stdout) editorial/tools/runway/ValidateMerged.java <outdir>/merged`
- `EVENT_BRIEF.md`: the editorial and sourcing rules for each event.
