package com.onthisday.ingestion.media;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OwnedImageAssetValidatorTest {

  private final OwnedImageAssetValidator validator = new OwnedImageAssetValidator();

  @Test
  void producesAnImmutablePlanForAValidatedStagedRendition(@TempDir Path tempDir)
      throws IOException {
    var staged = writeJpeg(tempDir, "quiz-images/identify-colosseum.jpg", 640, 480);
    var entry = entry(tempDir, staged, "quiz-images/identify-colosseum");

    var plan =
        validator.validateAndPlan(
            new OwnedImageManifest(1, java.util.List.of(entry)),
            tempDir,
            java.net.URI.create("https://images.example.test"));

    assertEquals(1, plan.entries().size());
    var planned = plan.entries().getFirst();
    assertEquals(entry.objectKey(), planned.objectKey());
    assertEquals(OwnedImageAssetValidator.IMMUTABLE_CACHE_CONTROL, planned.cacheControl());
    assertEquals("https://images.example.test/" + entry.objectKey(), planned.proposedUrl());
    assertEquals(640, planned.width());
    assertEquals(480, planned.height());
  }

  @Test
  void allowsAPlanWithoutAnOwnedOriginWhileDeliveryIsUnapproved(@TempDir Path tempDir)
      throws IOException {
    var staged = writeJpeg(tempDir, "quiz-images/identify-colosseum.jpg", 640, 480);
    var entry = entry(tempDir, staged, "quiz-images/identify-colosseum");

    var plan = validator.validateAndPlan(new OwnedImageManifest(1, java.util.List.of(entry)), tempDir, null);

    assertNull(plan.entries().getFirst().proposedUrl());
  }

  @Test
  void rejectsCorruptBytesAndDoesNotProduceAPlan(@TempDir Path tempDir) throws IOException {
    var staged = writeJpeg(tempDir, "quiz-images/not-an-image.jpg", 640, 480);
    var entry = entry(tempDir, staged, "quiz-images/identify-colosseum");
    Files.writeString(staged, "not a JPEG");

    var exception =
        assertThrows(
            OwnedImageValidationException.class,
            () -> validator.validateAndPlan(new OwnedImageManifest(1, java.util.List.of(entry)), tempDir, null));

    assertTrue(
        exception.validationErrors().stream()
            .anyMatch(error -> error.message().equals("could not read or decode staged rendition")));
  }

  @Test
  void rejectsChecksumMismatchAndUnsafeObjectKey(@TempDir Path tempDir) throws IOException {
    var staged = writeJpeg(tempDir, "quiz-images/identify-colosseum.jpg", 640, 480);
    var valid = entry(tempDir, staged, "quiz-images/identify-colosseum");
    var invalid =
        new OwnedImageManifestEntry(
            valid.id(),
            valid.questionId(),
            valid.relativePath(),
            valid.sourceRenditionUrl(),
            valid.source(),
            valid.sourceUrl(),
            valid.altText(),
            valid.attribution(),
            valid.creator(),
            valid.license(),
            valid.licenseUrl(),
            valid.contentType(),
            valid.width(),
            valid.height(),
            valid.byteSize(),
            "0".repeat(64),
            "quiz-images/identify-colosseum/not-versioned.jpg");

    var exception =
        assertThrows(
            OwnedImageValidationException.class,
            () -> validator.validateAndPlan(new OwnedImageManifest(1, java.util.List.of(invalid)), tempDir, null));

    assertTrue(
        exception.validationErrors().stream()
            .anyMatch(error -> error.path().endsWith(".objectKey")));
    assertTrue(exception.validationErrors().stream().anyMatch(error -> error.path().endsWith(".sha256")));
  }

  @Test
  void rejectsOversizedDimensionsBeforeDecoding(@TempDir Path tempDir) throws IOException {
    var staged = writeJpeg(tempDir, "quiz-images/identify-colosseum.jpg", 1025, 1);
    var entry = entry(tempDir, staged, "quiz-images/identify-colosseum");

    var exception =
        assertThrows(
            OwnedImageValidationException.class,
            () -> validator.validateAndPlan(new OwnedImageManifest(1, java.util.List.of(entry)), tempDir, null));

    assertTrue(
        exception.validationErrors().stream()
            .anyMatch(error -> error.message().contains("1024 pixel longest-edge limit")));
  }

  @Test
  void rejectsMissingLicenseMetadataAndDuplicateQuestionAssets(@TempDir Path tempDir) throws IOException {
    var staged = writeJpeg(tempDir, "quiz-images/identify-colosseum.jpg", 640, 480);
    var valid = entry(tempDir, staged, "quiz-images/identify-colosseum");
    var incomplete =
        new OwnedImageManifestEntry(
            "colosseum-second",
            valid.questionId(),
            valid.relativePath(),
            valid.sourceRenditionUrl(),
            valid.source(),
            valid.sourceUrl(),
            valid.altText(),
            valid.attribution(),
            valid.creator(),
            "",
            valid.licenseUrl(),
            valid.contentType(),
            valid.width(),
            valid.height(),
            valid.byteSize(),
            valid.sha256(),
            valid.objectKey());

    var exception =
        assertThrows(
            OwnedImageValidationException.class,
            () ->
                validator.validateAndPlan(
                    new OwnedImageManifest(1, java.util.List.of(valid, incomplete)), tempDir, null));

    assertTrue(
        exception.validationErrors().stream()
            .anyMatch(error -> error.message().contains("duplicate question id")));
    assertTrue(exception.validationErrors().stream().anyMatch(error -> error.path().endsWith(".license")));
  }

  @Test
  void rejectsMalformedPathsAndMissingContentTypesAsValidationErrors(@TempDir Path tempDir)
      throws IOException {
    var staged = writeJpeg(tempDir, "quiz-images/identify-colosseum.jpg", 640, 480);
    var valid = entry(tempDir, staged, "quiz-images/identify-colosseum");
    var invalid =
        new OwnedImageManifestEntry(
            valid.id(),
            valid.questionId(),
            "\u0000not-a-path",
            valid.sourceRenditionUrl(),
            valid.source(),
            valid.sourceUrl(),
            valid.altText(),
            valid.attribution(),
            valid.creator(),
            valid.license(),
            valid.licenseUrl(),
            null,
            valid.width(),
            valid.height(),
            valid.byteSize(),
            valid.sha256(),
            valid.objectKey());

    var exception =
        assertThrows(
            OwnedImageValidationException.class,
            () -> validator.validateAndPlan(new OwnedImageManifest(1, java.util.List.of(invalid)), tempDir, null));

    assertTrue(exception.validationErrors().stream().anyMatch(error -> error.path().endsWith(".relativePath")));
    assertTrue(exception.validationErrors().stream().anyMatch(error -> error.path().endsWith(".contentType")));
  }

  @Test
  void readerRejectsUnknownManifestProperties(@TempDir Path tempDir) throws IOException {
    var manifest = tempDir.resolve("manifest.json");
    Files.writeString(manifest, "{\"schemaVersion\":1,\"assets\":[],\"unexpected\":true}");

    assertThrows(
        OwnedImageValidationException.class,
        () -> new OwnedImageManifestReader(new ObjectMapper()).read(manifest));
  }

  private static OwnedImageManifestEntry entry(Path root, Path staged, String keyPrefix)
      throws IOException {
    var bytes = Files.size(staged);
    var checksum = sha256(staged);
    var image = ImageIO.read(staged.toFile());
    var contentType = "image/jpeg";
    return new OwnedImageManifestEntry(
        "identify-colosseum",
        "identify-colosseum",
        root.relativize(staged).toString(),
        "https://upload.wikimedia.org/wikipedia/commons/example.jpg",
        "Wikimedia Commons",
        "https://commons.wikimedia.org/wiki/File:Example.jpg",
        "Exterior view of an elliptical stone amphitheatre at dusk",
        "Photo by Example Photographer",
        "Example Photographer",
        "CC BY-SA 4.0",
        "https://creativecommons.org/licenses/by-sa/4.0/",
        contentType,
        image.getWidth(),
        image.getHeight(),
        bytes,
        checksum,
        keyPrefix + "/" + checksum + ".jpg");
  }

  private static Path writeJpeg(Path root, String relativePath, int width, int height) throws IOException {
    var path = root.resolve(relativePath);
    Files.createDirectories(path.getParent());
    var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    assertTrue(ImageIO.write(image, "jpeg", path.toFile()));
    return path;
  }

  private static String sha256(Path path) throws IOException {
    try {
      var digest = MessageDigest.getInstance("SHA-256");
      digest.update(Files.readAllBytes(path));
      return java.util.HexFormat.of().formatHex(digest.digest());
    } catch (java.security.NoSuchAlgorithmException exception) {
      throw new AssertionError(exception);
    }
  }
}
