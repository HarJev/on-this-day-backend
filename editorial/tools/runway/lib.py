import json, os
C = "2026-09-27"
MONTHS = {1:"jan",2:"feb",3:"mar",4:"apr",5:"may",6:"jun",7:"jul",8:"aug",9:"sep",10:"oct",11:"nov",12:"dec"}

class Batch:
    def __init__(self, batch_id):
        self.id = batch_id
        self.dir = "editorial/batches/" + batch_id
        self.ev = []

    def add(self, day, short, id, title, year, hd, summary, desc, sources, regions, eras,
            nt=None, nb=None, note=None):
        """sources: list of (name, url, check_note). First event added for a day is featured."""
        e = {"id": id, "title": title, "year": year, "historicalDate": hd, "dateNote": note,
             "summary": summary, "description": desc, "notificationTitle": nt, "notificationBody": nb,
             "sources": [{"name": n, "url": u} for n, u, _ in sources], "images": []}
        self.ev.append((day, short, e, [c for _, _, c in sources], regions, eras))

    def write(self):
        os.makedirs(self.dir, exist_ok=True)
        month_days = sorted({x[0] for x in self.ev}, key=lambda d: (int(d[:2]), int(d[3:])))
        days = []
        for d in month_days:
            ids = [x[2]["id"] for x in self.ev if x[0] == d]
            days.append({"month": int(d[:2]), "day": int(d[3:]), "featuredEventId": ids[0],
                         "additionalEventIds": ids[1:]})
        cand, led = [], []
        for day, short, e, checks, regions, eras in self.ev:
            cid = f"{MONTHS[int(day[:2])]}-{day[3:]}-{short}"
            urls = [s["url"] for s in e["sources"]]
            cand.append({"candidateId": cid, "kind": "event", "proposedCanonicalId": e["id"],
                         "workingClaim": f"{e['historicalDate']}: {e['description']}", "sourceUrls": urls})
            led.append({"candidateId": cid, "canonicalId": e["id"], "reviewStatus": "source_verified",
                        "sourceChecks": [{"url": u, "status": "verified_supporting", "checkedOn": C, "note": n}
                                         for u, n in zip(urls, checks)],
                        "imageRightsStatus": "not_applicable", "regions": regions, "eras": eras,
                        "calendarDays": [day],
                        "notes": "Drafted and source-checked by Claude; awaiting owner editorial review."})
        def w(n, o):
            with open(os.path.join(self.dir, n), "w") as f:
                json.dump(o, f, indent=2, ensure_ascii=False); f.write("\n")
        w("draft-events.json", {"events": [x[2] for x in self.ev]})
        w("draft-daily-events.json", {"days": days})
        w("batch.json", {"schemaVersion": 1, "batchId": self.id, "candidateFile": "candidates.json",
                         "reviewLedgerFile": "review-ledger.json", "eventIds": [], "monthDays": month_days,
                         "quizPackFilenames": []})
        w("candidates.json", {"schemaVersion": 1, "batchId": self.id, "candidates": cand})
        w("review-ledger.json", {"schemaVersion": 1, "batchId": self.id, "entries": led})
        print(self.id, len(self.ev), "events", {d: sum(1 for x in self.ev if x[0] == d) for d in month_days})

    def load_existing(self):
        """Load already-written draft events (with their ledger checks) so a batch can be extended."""
        ev = json.load(open(os.path.join(self.dir, "draft-events.json")))["events"]
        led = {x["canonicalId"]: x for x in json.load(open(os.path.join(self.dir, "review-ledger.json")))["entries"]}
        days = json.load(open(os.path.join(self.dir, "draft-daily-events.json")))["days"]
        by_id = {e["id"]: e for e in ev}
        for d in days:
            key = f"{d['month']:02d}-{d['day']:02d}"
            for eid in [d["featuredEventId"]] + d["additionalEventIds"]:
                l = led[eid]
                short = l["candidateId"].split("-", 2)[2]
                self.ev.append((key, short, by_id[eid], [c["note"] for c in l["sourceChecks"]], l["regions"], l["eras"]))
