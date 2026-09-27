import argparse
import hashlib
import importlib.util
import json
import tempfile
import unittest
from pathlib import Path

from PIL import Image


MODULE_PATH = Path(__file__).parents[1] / "event_image_pipeline.py"
SPEC = importlib.util.spec_from_file_location("event_image_pipeline", MODULE_PATH)
PIPELINE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(PIPELINE)


class EventImagePipelineTest(unittest.TestCase):

    def test_candidate_manifest_rejects_non_https_source(self):
        with tempfile.TemporaryDirectory() as directory:
            manifest = Path(directory) / "candidates.json"
            manifest.write_text(json.dumps({
                "schemaVersion": 1,
                "assets": [{
                    "id": "test-image",
                    "eventId": "test-event",
                    "primary": True,
                    "sourceRenditionUrl": "http://example.com/image.jpg",
                    "source": "Example",
                    "sourceUrl": "https://example.com/source",
                    "altText": "A neutral image",
                    "attribution": "Example",
                    "creator": None,
                    "license": "Public domain",
                    "licenseUrl": "https://example.com/license"
                }]
            }), encoding="utf-8")

            with self.assertRaisesRegex(ValueError, "absolute HTTPS URL"):
                PIPELINE.candidates(manifest)

    def test_attach_requires_explicit_primary_replacement(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            prepared = root / "prepared"
            prepared.mkdir()
            image_path = prepared / "test.jpg"
            Image.new("RGB", (1600, 1000), (20, 40, 80)).save(image_path)
            raw = image_path.read_bytes()
            checksum = hashlib.sha256(raw).hexdigest()
            manifest = {
                "schemaVersion": 1,
                "assets": [{
                    "id": "test-image",
                    "eventId": "test-event",
                    "primary": True,
                    "relativePath": "prepared/test.jpg",
                    "sourceRenditionUrl": "https://example.com/image.jpg",
                    "source": "Example",
                    "sourceUrl": "https://example.com/source",
                    "altText": "A neutral image",
                    "attribution": "Example",
                    "creator": None,
                    "license": "Public domain",
                    "licenseUrl": "https://example.com/license",
                    "contentType": "image/jpeg",
                    "width": 1600,
                    "height": 1000,
                    "byteSize": len(raw),
                    "sha256": checksum,
                    "objectKey": f"event-images/test-event/{checksum}.jpg"
                }]
            }
            # The validator intentionally rejects an oversized staged source.
            image = Image.new("RGB", (960, 600), (20, 40, 80))
            image.save(image_path, quality=82)
            raw = image_path.read_bytes()
            checksum = hashlib.sha256(raw).hexdigest()
            manifest["assets"][0].update({
                "width": 960,
                "height": 600,
                "byteSize": len(raw),
                "sha256": checksum,
                "objectKey": f"event-images/test-event/{checksum}.jpg"
            })
            events = root / "events.json"
            events.write_text(json.dumps({
                "events": [{
                    "id": "test-event",
                    "images": [{"primary": True, "url": "https://old.example/image.jpg"}]
                }]
            }), encoding="utf-8")
            prepared_manifest = root / "manifest.json"
            prepared_manifest.write_text(json.dumps(manifest), encoding="utf-8")
            output = root / "output.json"
            args = argparse.Namespace(
                manifest=prepared_manifest,
                asset_root=root,
                events=events,
                output=output,
                origin="https://cdn.example.com",
                replace_primary=False
            )

            with self.assertRaisesRegex(ValueError, "primary image exists"):
                PIPELINE.attach(args)

            args.replace_primary = True
            PIPELINE.attach(args)
            attached = json.loads(output.read_text(encoding="utf-8"))
            images = attached["events"][0]["images"]
            self.assertEqual(1, len(images))
            self.assertTrue(images[0]["url"].startswith("https://cdn.example.com/event-images/test-event/"))


if __name__ == "__main__":
    unittest.main()
