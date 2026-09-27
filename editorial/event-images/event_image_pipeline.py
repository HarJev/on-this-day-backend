#!/usr/bin/env python3
"""Reviewable preparation, publishing, and attachment of event-image renditions."""

from __future__ import annotations

import argparse
import hashlib
import html
import json
import subprocess
import sys
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path

try:
    from PIL import Image, ImageOps, UnidentifiedImageError
except ImportError:
    print("Install Pillow: python3 -m pip install -r editorial/event-images/requirements.txt", file=sys.stderr)
    raise SystemExit(2)

Image.MAX_IMAGE_PIXELS = 40_000_000
SOURCE_LIMIT = 20 * 1024 * 1024
TARGET_LIMIT = 750 * 1024
HARD_LIMIT = 1536 * 1024
MAX_EDGE = 960
CACHE_CONTROL = "public, max-age=31536000, immutable"
USER_AGENT = "OnThisDayEventImagePipeline/1.0 (+https://github.com/HarJev/on-this-day-backend/issues)"


def require(value, name):
    if not isinstance(value, str) or not value.strip():
        raise ValueError(f"{name} must be a nonblank string")
    return value.strip()


def https(value, name):
    value = require(value, name)
    parsed = urllib.parse.urlparse(value)
    if parsed.scheme != "https" or not parsed.netloc:
        raise ValueError(f"{name} must be an absolute HTTPS URL")
    return value


def slug(value, name):
    value = require(value, name)
    if value.startswith("-") or value.endswith("-") or "--" in value or any(c not in "abcdefghijklmnopqrstuvwxyz0123456789-" for c in value):
        raise ValueError(f"{name} must be a lowercase slug")
    return value


def candidates(path):
    raw = json.loads(path.read_text(encoding="utf-8"))
    if not isinstance(raw, dict) or raw.get("schemaVersion") != 1 or not isinstance(raw.get("assets"), list) or not raw["assets"]:
        raise ValueError("candidate manifest requires schemaVersion 1 and a nonempty assets array")
    seen_ids, seen_events, result = set(), set(), []
    for index, item in enumerate(raw["assets"]):
        prefix = f"$.assets[{index}]"
        if not isinstance(item, dict):
            raise ValueError(f"{prefix} must be an object")
        asset_id, event_id = slug(item.get("id"), prefix + ".id"), slug(item.get("eventId"), prefix + ".eventId")
        if asset_id in seen_ids or event_id in seen_events:
            raise ValueError(f"{prefix} duplicates an asset or event ID")
        if not isinstance(item.get("primary"), bool):
            raise ValueError(f"{prefix}.primary must be boolean")
        creator = item.get("creator")
        if creator is not None:
            creator = require(creator, prefix + ".creator")
        result.append({
            "id": asset_id, "eventId": event_id, "primary": item["primary"],
            "sourceRenditionUrl": https(item.get("sourceRenditionUrl"), prefix + ".sourceRenditionUrl"),
            "source": require(item.get("source"), prefix + ".source"),
            "sourceUrl": https(item.get("sourceUrl"), prefix + ".sourceUrl"),
            "altText": require(item.get("altText"), prefix + ".altText"),
            "attribution": require(item.get("attribution"), prefix + ".attribution"),
            "creator": creator, "license": require(item.get("license"), prefix + ".license"),
            "licenseUrl": https(item.get("licenseUrl"), prefix + ".licenseUrl"),
        })
        seen_ids.add(asset_id)
        seen_events.add(event_id)
    return result


class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, request, fp, code, msg, headers, new_url):
        raise urllib.error.HTTPError(request.full_url, code, "redirects are not allowed", headers, fp)


def download(asset):
    request = urllib.request.Request(asset["sourceRenditionUrl"], headers={"User-Agent": USER_AGENT, "Accept": "image/*"})
    try:
        with urllib.request.build_opener(NoRedirect()).open(request, timeout=20) as response:
            if response.status != 200 or not response.headers.get_content_type().startswith("image/"):
                raise ValueError(f"{asset['id']}: source did not return a 200 image response")
            declared = response.headers.get("Content-Length")
            if declared and int(declared) > SOURCE_LIMIT:
                raise ValueError(f"{asset['id']}: source exceeds 20 MiB")
            chunks, total = [], 0
            while chunk := response.read(65536):
                total += len(chunk)
                if total > SOURCE_LIMIT:
                    raise ValueError(f"{asset['id']}: source exceeds 20 MiB")
                chunks.append(chunk)
            return b"".join(chunks)
    except urllib.error.HTTPError as error:
        raise ValueError(f"{asset['id']}: source HTTP {error.code}") from error
    except urllib.error.URLError as error:
        raise ValueError(f"{asset['id']}: source unavailable ({error.reason})") from error


def encode(raw):
    from io import BytesIO
    try:
        with Image.open(BytesIO(raw)) as original:
            image = ImageOps.exif_transpose(original).convert("RGB")
            for edge in (MAX_EDGE, 896, 832, 768, 704, 640, 576, 512):
                scale = min(1.0, edge / max(image.width, image.height))
                width, height = max(1, round(image.width * scale)), max(1, round(image.height * scale))
                resized = image.resize((width, height), Image.Resampling.LANCZOS)
                for quality in (82, 78, 74, 70):
                    output = BytesIO()
                    resized.save(output, "JPEG", quality=quality, optimize=True, progressive=True)
                    data = output.getvalue()
                    if len(data) <= TARGET_LIMIT:
                        return data, width, height
                    if edge == 512 and quality == 70 and len(data) <= HARD_LIMIT:
                        return data, width, height
    except (UnidentifiedImageError, Image.DecompressionBombError, OSError) as error:
        raise ValueError(f"could not decode image: {error}") from error
    raise ValueError("could not meet the 1.5 MiB event-image limit")


def write_review(assets, root, path):
    path.parent.mkdir(parents=True, exist_ok=True)
    cards = []
    for asset in assets:
        uri = (root / asset["relativePath"]).resolve().as_uri()
        cards.append(f"<article><img src='{html.escape(uri)}' alt='{html.escape(asset['altText'])}'><h2>{html.escape(asset['eventId'])}</h2><p>{html.escape(asset['attribution'])}</p><p>{asset['width']} x {asset['height']}; {asset['byteSize'] // 1024} KiB</p><p><a href='{html.escape(asset['sourceUrl'])}'>Source</a> | {html.escape(asset['license'])}</p></article>")
    path.write_text("<!doctype html><meta charset='utf-8'><title>Event image review</title><style>body{font:16px system-ui;margin:24px;background:#f6f2e9}main{display:grid;grid-template-columns:repeat(auto-fit,minmax(280px,1fr));gap:20px}article{background:#fff;padding:14px;border:1px solid #ddd}img{width:100%;height:250px;object-fit:contain;background:#eee}</style><main>" + "".join(cards) + "</main>", encoding="utf-8")


def prepare(args):
    root = args.asset_root.resolve()
    output_assets = []
    for asset in candidates(args.candidates):
        data, width, height = encode(download(asset))
        relative = Path("prepared") / f"{asset['id']}.jpg"
        destination = root / relative
        destination.parent.mkdir(parents=True, exist_ok=True)
        destination.write_bytes(data)
        digest = hashlib.sha256(data).hexdigest()
        output_assets.append({**asset, "relativePath": relative.as_posix(), "contentType": "image/jpeg", "width": width, "height": height, "byteSize": len(data), "sha256": digest, "objectKey": f"event-images/{asset['eventId']}/{digest}.jpg"})
    manifest = {"schemaVersion": 1, "assets": output_assets}
    args.output_manifest.parent.mkdir(parents=True, exist_ok=True)
    args.output_manifest.write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")
    write_review(output_assets, root, args.review_html)
    print(f"Prepared {len(output_assets)} image(s). Review {args.review_html} before publishing.")


def manifest(path):
    raw = json.loads(path.read_text(encoding="utf-8"))
    if not isinstance(raw, dict) or raw.get("schemaVersion") != 1 or not isinstance(raw.get("assets"), list) or not raw["assets"]:
        raise ValueError("prepared manifest requires schemaVersion 1 and a nonempty assets array")
    return raw


def validate(raw, root):
    root = root.resolve()
    keys = set()
    for asset in raw["assets"]:
        for key in ("id", "eventId", "relativePath", "sha256", "objectKey", "byteSize", "width", "height", "contentType", "source", "sourceUrl", "altText", "attribution", "license", "licenseUrl"):
            if key not in asset:
                raise ValueError(f"prepared asset lacks {key}")
        relative = Path(asset["relativePath"])
        path = (root / relative).resolve()
        if relative.is_absolute() or ".." in relative.parts or not path.is_file() or root not in path.parents:
            raise ValueError(f"{asset['id']}: unsafe or missing staged asset")
        data = path.read_bytes()
        expected_key = f"event-images/{asset['eventId']}/{asset['sha256']}.jpg"
        if len(data) != asset["byteSize"] or len(data) > HARD_LIMIT or hashlib.sha256(data).hexdigest() != asset["sha256"] or asset["objectKey"] != expected_key or expected_key in keys:
            raise ValueError(f"{asset['id']}: invalid immutable asset metadata")
        with Image.open(path) as image:
            if image.format != "JPEG" or max(image.width, image.height) > MAX_EDGE or (image.width, image.height) != (asset["width"], asset["height"]):
                raise ValueError(f"{asset['id']}: invalid rendered JPEG")
        keys.add(expected_key)


def publish(args):
    raw = manifest(args.manifest)
    validate(raw, args.asset_root)
    origin = https(args.origin, "origin").rstrip("/")
    for asset in raw["assets"]:
        command = ["aws", "s3", "cp", str(args.asset_root / asset["relativePath"]), f"s3://{args.bucket}/{asset['objectKey']}", "--content-type", "image/jpeg", "--cache-control", CACHE_CONTROL, "--sse", "AES256"]
        print(f"{asset['eventId']}: {origin}/{asset['objectKey']}")
        if args.execute:
            subprocess.run(command, check=True)
        else:
            print("DRY RUN: " + " ".join(command))


def attach(args):
    raw = manifest(args.manifest)
    validate(raw, args.asset_root)
    origin = https(args.origin, "origin").rstrip("/")
    events_document = json.loads(args.events.read_text(encoding="utf-8"))
    events = events_document.get("events") if isinstance(events_document, dict) else None
    if not isinstance(events, list):
        raise ValueError("events file must contain an events array")
    by_id = {event.get("id"): event for event in events if isinstance(event, dict)}
    for asset in raw["assets"]:
        event = by_id.get(asset["eventId"])
        if event is None:
            raise ValueError(f"{asset['id']}: event is absent from events JSON")
        images = event.setdefault("images", [])
        if not isinstance(images, list):
            raise ValueError(f"{asset['eventId']}: event images must be an array")
        if asset["primary"] and any(image.get("primary") is True for image in images if isinstance(image, dict)):
            if not args.replace_primary:
                raise ValueError(f"{asset['eventId']}: primary image exists; use --replace-primary after review")
            images[:] = [image for image in images if not isinstance(image, dict) or image.get("primary") is not True]
        images.append({"url": f"{origin}/{asset['objectKey']}", "altText": asset["altText"], "source": asset["source"], "sourceUrl": asset["sourceUrl"], "creator": asset.get("creator"), "attribution": asset["attribution"], "license": asset["license"], "licenseUrl": asset["licenseUrl"], "primary": asset["primary"]})
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(events_document, indent=2) + "\n", encoding="utf-8")
    print(f"Wrote image attachment proposal to {args.output}")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    commands = parser.add_subparsers(dest="command", required=True)
    prepare_parser = commands.add_parser("prepare")
    prepare_parser.add_argument("--candidates", type=Path, required=True)
    prepare_parser.add_argument("--asset-root", type=Path, required=True)
    prepare_parser.add_argument("--output-manifest", type=Path, required=True)
    prepare_parser.add_argument("--review-html", type=Path, required=True)
    validate_parser = commands.add_parser("validate")
    validate_parser.add_argument("--manifest", type=Path, required=True)
    validate_parser.add_argument("--asset-root", type=Path, required=True)
    publish_parser = commands.add_parser("publish")
    publish_parser.add_argument("--manifest", type=Path, required=True)
    publish_parser.add_argument("--asset-root", type=Path, required=True)
    publish_parser.add_argument("--bucket", required=True)
    publish_parser.add_argument("--origin", required=True)
    publish_parser.add_argument("--execute", action="store_true")
    attach_parser = commands.add_parser("attach")
    attach_parser.add_argument("--manifest", type=Path, required=True)
    attach_parser.add_argument("--asset-root", type=Path, required=True)
    attach_parser.add_argument("--events", type=Path, required=True)
    attach_parser.add_argument("--output", type=Path, required=True)
    attach_parser.add_argument("--origin", required=True)
    attach_parser.add_argument("--replace-primary", action="store_true")
    args = parser.parse_args()
    try:
        if args.command == "prepare":
            prepare(args)
        elif args.command == "validate":
            validate(manifest(args.manifest), args.asset_root)
            print("Prepared event-image manifest is valid.")
        elif args.command == "publish":
            publish(args)
        else:
            attach(args)
        return 0
    except (OSError, ValueError, subprocess.CalledProcessError) as error:
        print(f"ERROR: {error}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
