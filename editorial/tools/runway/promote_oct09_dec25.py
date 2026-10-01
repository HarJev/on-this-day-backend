"""Promote the October 9 - December 25 runway drafts after the delegated owner review.

Run once from the repository root: python3 editorial/tools/runway/promote_oct09_dec25.py

The owner delegated the review on 2026-10-01 ("run an owner's review for me of all the
unpublished stuff ... if all good"). Rules applied: keep only events with an exact or
widely accepted date, drop anything weak rather than rework it, and accept a quiz answer
that appears in an event summary. Dropped records stay in their ledgers as `rejected`.
"""
import glob
import hashlib
import json
import os

REVIEWER = "Claude (owner review delegated by Jevaun Harris)"
REVIEWED_ON = "2026-10-01"
APPROVED_NOTE = (" Approved in the delegated owner review on 2026-10-01 and promoted to canonical "
                 "content; see docs/OWNER_REVIEW_OCT09_DEC25_PROGRESS.md.")

# Event id -> reason. Each is dropped instead of reworked.
DROPPED = {
    "stanley-meets-livingstone-1871":
        "Date disputed by its own source: Stanley gave November 10, Livingstone's journal suggests October 24-28.",
    "diocletian-acclaimed-emperor-284":
        "Accession date disputed: most accounts give November 20, 284, not November 17.",
    "turkey-legal-equality-reform-2001":
        "Date uncertain: the new Turkish Civil Code was adopted on November 22, 2001, not November 24.",
    "lady-astor-takes-seat-1919":
        "Wrong key fact for the date: Astor was elected on November 28, 1919 but took her seat on December 1.",
    "mary-celeste-found-abandoned-1872":
        "Discovery date disputed between December 4 and December 5, 1872.",
    "avatar-released-2009":
        "Weak release date: London premiere December 10, staggered international release, US release December 18.",
    "us-cuba-relations-restored-2014":
        "Wrong key fact: December 17, 2014 was the announcement; relations were restored on July 20, 2015.",
    "curies-discover-radium-1898":
        "Date disputed: the discovery was announced to the Academy of Sciences on December 26, 1898.",
    "king-john-born-1167":
        "Birth year disputed between 1166 and 1167.",
}

# A day whose featured event was dropped gets a new featured event with notification copy.
REFEATURE = {
    (12, 5): ("nelson-mandela-dies-2013", "Remembering Nelson Mandela",
              "In 2013, Nelson Mandela, South Africa's first Black president, died at 95."),
}

EXCEPTION = ("Three events after the delegated owner review on 2026-10-01 dropped {id} ({reason}). "
             "Kept below the four-event floor rather than padded with a weaker claim.")

EVENT_BATCHES = sorted(glob.glob("editorial/batches/2026-1[0-2]-*-historical-events"))
SUPPLEMENT = "editorial/batches/2026-10-09-15-supplementary-events"
QUIZ_BATCHES = {
    "editorial/batches/2026-10-09-15-connected-quiz": "150-connected-october-09-15.json",
    "editorial/batches/2026-10-01-08-connected-quiz-supplement": "160-connected-october-01-08-supplement.json",
}


def load(path):
    with open(path) as f:
        return json.load(f)


def dump(path, data):
    with open(path, "w") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")


def append_records(path, key, records):
    """Append records to a hand-formatted canonical file without reformatting what is there."""
    with open(path) as f:
        text = f.read()
    before = json.loads(text)[key]
    tail = "\n  ]\n}\n"
    assert text.endswith(tail), path
    body = ",\n".join("\n".join("    " + line for line in json.dumps(r, indent=2, ensure_ascii=False).splitlines())
                      for r in records)
    text = text[: -len(tail)] + ",\n" + body + tail
    assert json.loads(text)[key] == before + records, path
    with open(path, "w") as f:
        f.write(text)


def add_related_event_ids(path, question_id, event_ids):
    """Add relatedEventIds to one question in a hand-formatted pack, leaving every other byte alone."""
    with open(path) as f:
        text = f.read()
    before = json.loads(text)
    start = text.index(f'"id": "{question_id}"')
    end = text.index("\n    }", start)
    block = text[start:end]
    if '"relatedEventIds"' in block:
        close = block.index("]", block.index('"relatedEventIds"'))
        block = block[:close].rstrip() + ", " + ", ".join(json.dumps(i) for i in event_ids) + block[close:]
    else:
        block = block.rstrip() + ',\n      "relatedEventIds": [' + ", ".join(json.dumps(i) for i in event_ids) + "]"
    text = text[:start] + block + text[end:]
    after = json.loads(text)
    for old, new in zip(before["questions"], after["questions"]):
        if old["id"] == question_id:
            old["relatedEventIds"] = old.get("relatedEventIds", []) + event_ids
        assert old == new, (path, old["id"])
    with open(path, "w") as f:
        f.write(text)


def review_ledger(batch_dir, rejected=None):
    rejected = rejected or {}
    path = os.path.join(batch_dir, "review-ledger.json")
    ledger = load(path)
    for entry in ledger["entries"]:
        if entry["reviewStatus"] != "source_verified":
            continue
        entry["reviewer"] = REVIEWER
        entry["reviewedOn"] = REVIEWED_ON
        reason = rejected.get(entry["canonicalId"])
        if reason:
            entry["reviewStatus"] = "rejected"
            entry["notes"] = entry.get("notes", "") + " Rejected in the delegated owner review on 2026-10-01: " + reason
        else:
            entry["reviewStatus"] = "approved"
            entry["notes"] = entry.get("notes", "") + APPROVED_NOTE
    dump(path, ledger)


def promote_events():
    known = {e["id"] for e in load("content/events.json")["events"]}
    known_days = {(d["month"], d["day"]) for d in load("content/daily-events.json")["days"]}
    new_events, new_days = [], []
    supplement_events = {e["id"]: e for e in load(os.path.join(SUPPLEMENT, "draft-events.json"))["events"]}
    day_additions = load(os.path.join(SUPPLEMENT, "proposed-day-additions.json"))

    for batch in EVENT_BATCHES:
        draft_events = os.path.join(batch, "draft-events.json")
        if not os.path.exists(draft_events):
            continue
        events = {e["id"]: e for e in load(draft_events)["events"]}
        days = load(os.path.join(batch, "draft-daily-events.json"))["days"]
        if os.path.basename(batch) == day_additions["targetBatch"]:
            for addition in day_additions["additions"]:
                event_id = addition["addAdditionalEventId"]
                month, day = (int(x) for x in addition["monthDay"].split("-"))
                next(d for d in days if (d["month"], d["day"]) == (month, day))["additionalEventIds"].append(event_id)
                events[event_id] = supplement_events[event_id]

        for d in days:
            key = (d["month"], d["day"])
            assert key not in known_days, f"{key} already canonical"
            dropped = [i for i in [d["featuredEventId"], *d["additionalEventIds"]] if i in DROPPED]
            if d["featuredEventId"] in DROPPED:
                new_id, title, body = REFEATURE[key]
                d["additionalEventIds"].remove(new_id)
                d["featuredEventId"] = new_id
                events[new_id]["notificationTitle"] = title
                events[new_id]["notificationBody"] = body
            d["additionalEventIds"] = [i for i in d["additionalEventIds"] if i not in DROPPED]
            if 1 + len(d["additionalEventIds"]) < 4:
                assert len(dropped) == 1, key
                d["editorialException"] = EXCEPTION.format(id=dropped[0], reason=DROPPED[dropped[0]].rstrip("."))
            for event_id in [d["featuredEventId"], *d["additionalEventIds"]]:
                assert event_id not in known, event_id
                new_events.append(events[event_id])
                known.add(event_id)
            new_days.append(d)
            known_days.add(key)

        review_ledger(batch, DROPPED)
        os.remove(draft_events)
        os.remove(os.path.join(batch, "draft-daily-events.json"))

    review_ledger(SUPPLEMENT)
    os.remove(os.path.join(SUPPLEMENT, "draft-events.json"))
    append_records("content/events.json", "events", new_events)
    append_records("content/daily-events.json", "days", new_days)


def promote_quizzes():
    for batch, pack in QUIZ_BATCHES.items():
        target = os.path.join("content/quizzes/questions", pack)
        assert not os.path.exists(target), target
        with open(os.path.join(batch, "draft-questions.json")) as f:
            text = f.read()
        drafts = json.loads(text)["questions"]
        assert all(q["publicationState"] == "draft" for q in drafts)
        text = text.replace('"publicationState": "draft"', '"publicationState": "published"')
        assert [dict(q, publicationState="published") for q in drafts] == json.loads(text)["questions"]
        with open(target, "w") as f:
            f.write(text)

        for link in load(os.path.join(batch, "proposed-links.json"))["existingQuestionLinks"]:
            q = next(x for x in load(link["canonicalPack"])["questions"] if x["id"] == link["questionId"])
            snapshot = hashlib.sha256(json.dumps(q, separators=(",", ":")).encode()).hexdigest()
            assert snapshot == link["canonicalSnapshotSha256"], f"{q['id']} changed since review"
            add_related_event_ids(link["canonicalPack"], link["questionId"], link["addRelatedEventIds"])

        review_ledger(batch)
        os.remove(os.path.join(batch, "draft-questions.json"))


if __name__ == "__main__":
    promote_events()
    promote_quizzes()
