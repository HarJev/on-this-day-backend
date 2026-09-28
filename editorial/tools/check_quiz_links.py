#!/usr/bin/env python3
"""Check every source and image URL cited by published quiz questions.

Usage (from the repository root):

    python3 editorial/tools/check_quiz_links.py            # human-readable report
    python3 editorial/tools/check_quiz_links.py --json out.json

The check is a paced HTTP GET that follows redirects. It is a triage step, not
a verdict: many publishers (Britannica, the Library of Congress, UNESCO, IWM,
parliament.uk, Smithsonian) answer scripted clients with 403/429 or a
bot-challenge page while serving real readers normally. Treat results as:

  ok        200 after redirects
  dead      404 / 410 - the page is gone; find a replacement source
  blocked   401 / 403 / 429 - open it in a real browser before judging
  error     no response (DNS, TLS, timeout) - retry, then open in a browser

Only replace a source after opening the replacement page and confirming it
directly states the question's answer and explanation. Record what changed.
"""
import argparse
import glob
import json
import subprocess
import sys
import time

UA = ("Mozilla/5.0 (Macintosh; Intel Mac OS X 14_0) AppleWebKit/537.36 "
      "(KHTML, like Gecko) Chrome/126 Safari/537.36")


def collect(content_dir):
    urls = {}
    for path in sorted(glob.glob(f"{content_dir}/quizzes/questions/*.json")):
        for q in json.load(open(path))["questions"]:
            if q["publicationState"] != "published":
                continue
            for s in q["sources"]:
                urls.setdefault(s["url"], []).append(q["id"])
            image = q.get("image") or {}
            for key in ("url", "sourceUrl"):
                if image.get(key):
                    urls.setdefault(image[key], []).append(f"{q['id']} (image {key})")
    return urls


def check(url, timeout):
    r = subprocess.run(
        ["curl", "-sL", "-o", "/dev/null", "--max-time", str(timeout), "-A", UA,
         "-w", "%{http_code} %{url_effective}", url],
        capture_output=True, text=True)
    code, _, final = r.stdout.partition(" ")
    if code in ("200",):
        verdict = "ok"
    elif code in ("404", "410"):
        verdict = "dead"
    elif code in ("401", "403", "429"):
        verdict = "blocked"
    else:
        verdict = "error"
    return {"code": code or "000", "final": final, "verdict": verdict}


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--content", default="content")
    ap.add_argument("--json", help="write full results to this file")
    ap.add_argument("--delay", type=float, default=0.7, help="seconds between requests")
    ap.add_argument("--timeout", type=int, default=25)
    args = ap.parse_args()

    urls = collect(args.content)
    results = {}
    for i, url in enumerate(urls, 1):
        results[url] = {**check(url, args.timeout), "questions": urls[url]}
        print(f"\r{i}/{len(urls)}", end="", file=sys.stderr)
        time.sleep(args.delay)
    print(file=sys.stderr)

    counts = {}
    for r in results.values():
        counts[r["verdict"]] = counts.get(r["verdict"], 0) + 1
    print(f"{len(results)} unique URLs: " + ", ".join(f"{k} {v}" for k, v in sorted(counts.items())))
    for verdict in ("dead", "error", "blocked"):
        rows = [(u, r) for u, r in results.items() if r["verdict"] == verdict]
        if not rows:
            continue
        print(f"\n{verdict.upper()} ({len(rows)})")
        for url, r in rows:
            print(f"  {r['code']} {url}\n      used by: {', '.join(r['questions'])}")
    if args.json:
        json.dump(results, open(args.json, "w"), indent=2)
    return 1 if counts.get("dead") else 0


if __name__ == "__main__":
    sys.exit(main())
