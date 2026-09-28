# L7 Image Identification Pack

15 image-identification questions added 2026-09-28 in
`content/quizzes/questions/130-l7-image-identification.json`, taking the
published image questions from 21 to 36.

Each image is a public-domain or CC BY-SA file on Wikimedia Commons. The
licence, creator and source page were read from the Commons file page; the
original rendition was downscaled to at most 1024 px on its longest edge with
no crop or edit. Question facts were checked in a real browser against
Britannica, UNESCO, the British Museum, the National Gallery of Art, the
Smithsonian and NASA. Alt text describes the picture without naming the
answer.

`image.url` already points at the owned CDN
(`https://d2v6di8uk52rif.cloudfront.net/quiz-images/<questionId>/<sha256>.jpg`).
The objects are uploaded by the owner from the untracked staging directory
`review/media-staging/2026-09-28-quiz-images/` in the workspace, following
`docs/OWNED_IMAGE_DELIVERY.md`. Upload and verify the checksums before
importing this pack, or the images will 404.

`owned-image-manifest.json` holds just these 15 assets; the same entries are
appended to `content/media/quiz-images.manifest.json`.

Dry run:

```sh
mvn -q exec:java \
  -Dexec.mainClass=com.onthisday.ingestion.media.OwnedImagePublishDryRunCommand \
  -Dexec.args="editorial/batches/2026-09-l7-image-identification/owned-image-manifest.json ../review/media-staging/2026-09-28-quiz-images https://d2v6di8uk52rif.cloudfront.net"
```

Subjects overlap with some existing multiple-choice questions (Taj Mahal,
Great Zimbabwe, Angkor Wat, Mansa Musa); the question types differ.
