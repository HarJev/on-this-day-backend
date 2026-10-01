#!/usr/bin/env python3
"""Publish the featured-story connected-quiz cycle written by build_featured_links_cycle.py.

The authoring session does not approve its own drafts. After reviewing the batch, the
reviewer runs this once from the repository root:

    python3 editorial/tools/connected-quiz/promote_featured_links.py --check
    python3 editorial/tools/connected-quiz/promote_featured_links.py --reviewer "NAME" [--reject ID ...]

--check verifies the link snapshots and that every link applies cleanly, writing nothing.
Without --check, every draft not rejected is published as pack 200, every proposed relation
is added to its unchanged canonical question (leaving every other byte of the pack alone),
ledger entries move from source_verified to approved (or rejected), and the draft file is
removed. A rejected link entry is named link-<questionId> and drops all of that question's
proposed relations.
"""
import argparse
import hashlib
import json
import os

BATCH = "editorial/batches/2026-10-01-12-25-connected-quiz"
PACK = "content/quizzes/questions/200-connected-featured-stories.json"
APPROVED_NOTE = (" Approved for publication on {date} by {reviewer} and promoted to canonical content; "
                 "see docs/FEATURED_LINKS_PROGRESS.md.")
REJECTED_NOTE = " Rejected on {date} by {reviewer}; not published."


def load(path):
    with open(path) as f:
        return json.load(f)


def dump(path, data):
    with open(path, "w") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")


def with_related_event_ids(text, question_id, event_ids):
    """Return pack text with event_ids appended to one question's relatedEventIds."""
    before = json.loads(text)
    start = text.index(f'"id": "{question_id}"')
    end = text.index("\n    }", start)
    block = text[start:end]
    if '"relatedEventIds"' in block:
        open_ = block.index("[", block.index('"relatedEventIds"'))
        close = block.index("]", open_)
        inner = block[open_ + 1:close]
        if "\n" in inner:
            indent = inner.rstrip().rsplit("\n", 1)[-1]
            indent = indent[: len(indent) - len(indent.lstrip())]
            body = inner.rstrip()
            addition = "".join(f",\n{indent}{json.dumps(i)}" for i in event_ids)
            block = block[:open_ + 1] + body + addition + inner[len(body):] + block[close:]
        else:
            block = block[:close].rstrip() + ", " + ", ".join(json.dumps(i) for i in event_ids) + block[close:]
    else:
        block = block.rstrip() + ',\n      "relatedEventIds": [' + ", ".join(json.dumps(i) for i in event_ids) + "]"
    text = text[:start] + block + text[end:]
    after = json.loads(text)
    for old, new in zip(before["questions"], after["questions"]):
        if old["id"] == question_id:
            old["relatedEventIds"] = old.get("relatedEventIds", []) + event_ids
        assert old == new, old["id"]
    return text


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--check", action="store_true", help="verify only; write nothing")
    ap.add_argument("--reviewer", help="name recorded on every ledger entry")
    ap.add_argument("--reviewed-on", default="2026-10-01")
    ap.add_argument("--reject", nargs="*", default=[], help="ledger candidateIds to reject")
    args = ap.parse_args()
    if not args.check and not args.reviewer:
        ap.error("--reviewer is required unless --check")

    ledger_path = os.path.join(BATCH, "review-ledger.json")
    ledger = load(ledger_path)
    known = {e["candidateId"] for e in ledger["entries"]}
    assert set(args.reject) <= known, set(args.reject) - known
    links = [l for l in load(os.path.join(BATCH, "proposed-links.json"))["existingQuestionLinks"]
             if f"link-{l['questionId']}" not in args.reject]
    drafts = load(os.path.join(BATCH, "draft-questions.json"))
    assert all(q["publicationState"] == "draft" for q in drafts["questions"])
    kept = [dict(q, publicationState="published") for q in drafts["questions"] if q["id"] not in args.reject]

    texts = {}
    for link in links:
        path = link["canonicalPack"]
        texts.setdefault(path, open(path).read())
        q = next(x for x in json.loads(texts[path])["questions"] if x["id"] == link["questionId"])
        snapshot = hashlib.sha256(json.dumps(q, separators=(",", ":")).encode()).hexdigest()
        assert snapshot == link["canonicalSnapshotSha256"], f"{q['id']} changed since review"
        assert q["publicationState"] == "published", q["id"]
        texts[path] = with_related_event_ids(texts[path], link["questionId"], link["addRelatedEventIds"])
    assert not os.path.exists(PACK), PACK

    if args.check:
        print(f"check ok: {len(kept)} questions to publish, {len(links)} links apply cleanly to {len(texts)} packs")
        return

    dump(PACK, dict(drafts, questions=kept))
    for path, text in texts.items():
        with open(path, "w") as f:
            f.write(text)
    for entry in ledger["entries"]:
        assert entry["reviewStatus"] == "source_verified", entry["canonicalId"]
        rejected = entry["candidateId"] in args.reject
        note = REJECTED_NOTE if rejected else APPROVED_NOTE
        entry.update(reviewStatus="rejected" if rejected else "approved", reviewer=args.reviewer,
                     reviewedOn=args.reviewed_on,
                     notes=entry["notes"] + note.format(date=args.reviewed_on, reviewer=args.reviewer))
    dump(ledger_path, ledger)
    os.remove(os.path.join(BATCH, "draft-questions.json"))
    print(f"published {len(kept)} questions to {PACK}; linked {len(links)} existing questions")


if __name__ == "__main__":
    main()
