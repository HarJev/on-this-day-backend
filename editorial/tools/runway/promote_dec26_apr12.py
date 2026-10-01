"""Promote the December 26 - April 12 drafts after the delegated owner review.

Run once from the repository root: python3 editorial/tools/runway/promote_dec26_apr12.py

The owner delegated the review on 2026-10-01 and asked for the drafts from PRs #29, #30
and #31 to be reviewed and published. Rules applied, as in the October 9 - December 25
review: keep only events with an exact or widely accepted date, drop anything weak rather
than rework it, and accept a quiz answer that appears in an event summary. Dropped records
stay in their ledgers as `rejected`.
"""
import glob
import hashlib
import json
import os

REVIEWER = "Claude (owner review delegated by Jevaun Harris)"
REVIEWED_ON = "2026-10-01"
APPROVED_NOTE = (" Approved in the delegated owner review on 2026-10-01 and promoted to canonical "
                 "content; see docs/OWNER_REVIEW_DEC26_APR12_PROGRESS.md.")

# Event id -> reason. Each is dropped instead of reworked.
DROPPED = {
    "constance-markievicz-elected-1918":
        "Wrong key fact for the date: the general election was held on December 14, 1918; "
        "December 28 is when results were declared.",
    "ford-five-dollar-day-1914":
        "Wrong key fact for the date: Ford announced the $5 day on January 5, 1914; the new pay took effect on January 12.",
    "georges-simenon-born-1903":
        "Birth date disputed: his birth certificate gives February 12, 1903; Simenon said he was born just after midnight on the 13th.",
    "hey-detects-solar-radio-waves-1942":
        "No single date: the radar interference Hey traced to the Sun was reported on February 27 and 28, 1942.",
}

# Quiz drafts that exist only for a dropped event.
DROPPED_QUESTIONS = {
    "markievicz-took-seat": "Its only related event, constance-markievicz-elected-1918, was dropped.",
    "ford-five-dollar-day": "Its only related event, ford-five-dollar-day-1914, was dropped.",
    "simenon-detective": "Its only related event, georges-simenon-born-1903, was dropped.",
    "hey-radio-source": "Its only related event, hey-detects-solar-radio-waves-1942, was dropped.",
}

# A day whose featured event was dropped gets a new featured event with notification copy.
REFEATURE = {
    (2, 27): ("reichstag-fire-1933", "The Reichstag fire",
              "In 1933, fire gutted the Reichstag, home of Germany's parliament, in Berlin."),
}

# Narrow corrections to a secondary detail; the event's date and key fact stand.
CORRECTIONS = {
    "eris-discovered-2005": {
        "summary": ("Astronomers spot it in images taken two years earlier.",
                    "Astronomers spot it in images taken in 2003."),
        "description": ("in images taken two years earlier at Palomar Observatory.",
                        "in images taken in 2003 at Palomar Observatory."),
    },
    "haiti-earthquake-2010": {
        "description": ("devastated Haiti, killing more than 300,000 people.",
                        "devastated Haiti, one of the deadliest earthquakes on record."),
    },
}

EXCEPTION = ("Three events after the delegated owner review on 2026-10-01 dropped {id} ({reason}). "
             "Kept below the four-event floor rather than padded with a weaker claim.")

EVENT_BATCHES = sorted(glob.glob("editorial/batches/2026-12-26-01-01-historical-events")
                       + glob.glob("editorial/batches/2027-0[1-4]-*-historical-events"))
QUIZ_BATCHES = {
    "editorial/batches/2026-12-26-01-31-connected-quiz": "170-connected-december-26-january-31.json",
    "editorial/batches/2027-02-01-03-07-connected-quiz": "180-connected-february-01-march-07.json",
    "editorial/batches/2027-03-08-04-12-connected-quiz": "190-connected-march-08-april-12.json",
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


def review_ledger(batch_dir, rejected):
    path = os.path.join(batch_dir, "review-ledger.json")
    ledger = load(path)
    for entry in ledger["entries"]:
        assert entry["reviewStatus"] == "source_verified", (path, entry["canonicalId"])
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


def correct(event):
    for field, (old, new) in CORRECTIONS.get(event["id"], {}).items():
        assert event[field].count(old) == 1, (event["id"], field)
        event[field] = event[field].replace(old, new)


def promote_events():
    known = {e["id"] for e in load("content/events.json")["events"]}
    known_days = {(d["month"], d["day"]) for d in load("content/daily-events.json")["days"]}
    new_events, new_days, corrected = [], [], set()

    for batch in EVENT_BATCHES:
        events = {e["id"]: e for e in load(os.path.join(batch, "draft-events.json"))["events"]}
        days = load(os.path.join(batch, "draft-daily-events.json"))["days"]

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
                if event_id in CORRECTIONS:
                    correct(events[event_id])
                    corrected.add(event_id)
                new_events.append(events[event_id])
                known.add(event_id)
            new_days.append(d)
            known_days.add(key)

        review_ledger(batch, DROPPED)
        os.remove(os.path.join(batch, "draft-events.json"))
        os.remove(os.path.join(batch, "draft-daily-events.json"))

    assert corrected == set(CORRECTIONS), corrected
    assert not DROPPED.keys() & known
    append_records("content/events.json", "events", new_events)
    append_records("content/daily-events.json", "days", new_days)


def promote_quizzes():
    # Several batches link the same canonical question, and each snapshot was taken before any
    # link was applied, so check every snapshot first and then apply the links.
    links = [link for batch in QUIZ_BATCHES
             for link in load(os.path.join(batch, "proposed-links.json"))["existingQuestionLinks"]]
    for link in links:
        q = next(x for x in load(link["canonicalPack"])["questions"] if x["id"] == link["questionId"])
        snapshot = hashlib.sha256(json.dumps(q, separators=(",", ":")).encode()).hexdigest()
        assert snapshot == link["canonicalSnapshotSha256"], f"{q['id']} changed since review"
        assert not set(link["addRelatedEventIds"]) & DROPPED.keys(), link["questionId"]

    removed = set()
    for batch, pack in QUIZ_BATCHES.items():
        target = os.path.join("content/quizzes/questions", pack)
        assert not os.path.exists(target), target
        drafts = load(os.path.join(batch, "draft-questions.json"))
        assert all(q["publicationState"] == "draft" for q in drafts["questions"])
        kept = []
        for q in drafts["questions"]:
            if q["id"] in DROPPED_QUESTIONS:
                removed.add(q["id"])
                continue
            assert not set(q.get("relatedEventIds", [])) & DROPPED.keys(), q["id"]
            kept.append(dict(q, publicationState="published"))
        dump(target, dict(drafts, questions=kept))

        for link in load(os.path.join(batch, "proposed-links.json"))["existingQuestionLinks"]:
            add_related_event_ids(link["canonicalPack"], link["questionId"], link["addRelatedEventIds"])

        review_ledger(batch, DROPPED_QUESTIONS)
        os.remove(os.path.join(batch, "draft-questions.json"))
    assert removed == set(DROPPED_QUESTIONS), removed


if __name__ == "__main__":
    promote_events()
    promote_quizzes()
