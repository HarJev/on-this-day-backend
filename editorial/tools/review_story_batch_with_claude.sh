#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "Usage: $0 editorial/batches/<batch>/batch.json" >&2
  exit 2
fi

batch_manifest=$1
if [[ ! -f "$batch_manifest" ]]; then
  echo "Batch manifest not found: $batch_manifest" >&2
  exit 2
fi

# Set CLAUDE_REVIEW_MODEL to the exact approved model ID for reproducibility.
# Read-only tools keep the reviewer from editing or importing the batch.
model=${CLAUDE_REVIEW_MODEL:-sonnet}
claude --print \
  --model "$model" \
  --effort medium \
  --tools Read,Glob,Grep \
  --permission-mode dontAsk \
  --permission-prompts none \
  --no-session-persistence \
  "Review the event-story batch selected by $batch_manifest as the delegated owner reviewer. Read the manifest, candidate file, ledger, every selected canonical event, and docs/EVENT_STORY_QUALITY_GUIDE.md. Check factual support, clarity for readers without background, the two-part detail structure, preservation of teaser/IDs/assignments/images/quiz links/notifications, and whether source checks support new claims. Do not edit files or run imports. Return APPROVE only if no material issue remains; otherwise return CHANGES_REQUESTED with specific event IDs and evidence."
