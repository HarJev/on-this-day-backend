package com.onthisday.ingestion.media;

import com.onthisday.ingestion.ContentValidationError;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/**
 * Validates staged bytes before any publisher is allowed to address an object store.
 *
 * <p>This deliberately has no AWS dependency: it proves the rendition, immutable key, and
 * provenance are internally consistent while still offline.
 */
public final class OwnedImageAssetValidator {

  public static final long MAX_ENCODED_BYTES = 8L * 1024 * 1024;
  public static final int MAX_LONGEST_EDGE = 1024;
  public static final String IMMUTABLE_CACHE_CONTROL = "public, max-age=31536000, immutable";

  private static final int SUPPORTED_SCHEMA_VERSION = 1;
  private static final Pattern SLUG_PATTERN = Pattern.compile("[a-z0-9]+(?:-[a-z0-9]+)*");
  private static final Pattern SHA_256_PATTERN = Pattern.compile("[0-9a-f]{64}");
  private static final Set<String> SUPPORTED_CONTENT_TYPES = Set.of("image/jpeg", "image/png");

  public OwnedImagePublishPlan validateAndPlan(
      OwnedImageManifest manifest, Path assetRoot, URI ownedHttpsOrigin) {
    var errors = new ArrayList<ContentValidationError>();
    var planEntries = new ArrayList<OwnedImagePublishPlanEntry>();
    var normalizedRoot = normalizeAssetRoot(assetRoot, errors);
    var normalizedOrigin = validateOrigin(ownedHttpsOrigin, errors);

    if (manifest == null) {
      errors.add(error("$", "manifest must be present"));
    } else {
      if (manifest.schemaVersion() != SUPPORTED_SCHEMA_VERSION) {
        errors.add(error("$.schemaVersion", "schemaVersion must be 1"));
      }
      validateAssets(manifest.assets(), normalizedRoot, normalizedOrigin, errors, planEntries);
    }

    if (!errors.isEmpty()) {
      throw new OwnedImageValidationException(errors);
    }
    return new OwnedImagePublishPlan(planEntries);
  }

  private void validateAssets(
      List<OwnedImageManifestEntry> assets,
      Path assetRoot,
      URI ownedHttpsOrigin,
      List<ContentValidationError> errors,
      List<OwnedImagePublishPlanEntry> planEntries) {
    if (assets == null) {
      errors.add(error("$.assets", "assets must be present"));
      return;
    }
    if (assets.isEmpty()) {
      errors.add(error("$.assets", "assets must contain at least one entry"));
      return;
    }

    var entryIds = new HashSet<String>();
    var questionIds = new HashSet<String>();
    var relativePaths = new HashSet<String>();
    var objectKeys = new HashSet<String>();
    for (int index = 0; index < assets.size(); index++) {
      var entry = assets.get(index);
      var path = "$.assets[" + index + "]";
      if (entry == null) {
        errors.add(error(path, "asset must be present"));
        continue;
      }

      validateUniqueSlug(entry.id(), path + ".id", "asset id", entryIds, errors);
      validateUniqueSlug(entry.questionId(), path + ".questionId", "question id", questionIds, errors);
      validateProvenance(entry, path, errors);
      validateDeclaredAsset(entry, path, relativePaths, objectKeys, errors);
      validateStagedBytes(entry, path, assetRoot, ownedHttpsOrigin, errors, planEntries);
    }
  }

  private void validateProvenance(
      OwnedImageManifestEntry entry, String path, List<ContentValidationError> errors) {
    validateHttpsUrl(entry.sourceRenditionUrl(), path + ".sourceRenditionUrl", errors);
    requireNonBlank(entry.source(), path + ".source", errors);
    validateHttpsUrl(entry.sourceUrl(), path + ".sourceUrl", errors);
    requireNonBlank(entry.altText(), path + ".altText", errors);
    requireNonBlank(entry.attribution(), path + ".attribution", errors);
    if (entry.creator() != null && entry.creator().isBlank()) {
      errors.add(error(path + ".creator", "creator must not be blank when present"));
    }
    requireNonBlank(entry.license(), path + ".license", errors);
    validateHttpsUrl(entry.licenseUrl(), path + ".licenseUrl", errors);
  }

  private void validateDeclaredAsset(
      OwnedImageManifestEntry entry,
      String path,
      Set<String> relativePaths,
      Set<String> objectKeys,
      List<ContentValidationError> errors) {
    if (!isSafeRelativePath(entry.relativePath())) {
      errors.add(error(path + ".relativePath", "relativePath must stay below the asset root"));
    } else if (!relativePaths.add(entry.relativePath())) {
      errors.add(error(path + ".relativePath", "duplicate relativePath: " + entry.relativePath()));
    }

    if (entry.contentType() == null || !SUPPORTED_CONTENT_TYPES.contains(entry.contentType())) {
      errors.add(error(path + ".contentType", "contentType must be image/jpeg or image/png"));
    }
    if (entry.width() <= 0 || entry.height() <= 0) {
      errors.add(error(path, "width and height must be positive"));
    } else if (longestEdge(entry.width(), entry.height()) > MAX_LONGEST_EDGE) {
      errors.add(error(path, "dimensions exceed the 1024 pixel longest-edge limit"));
    }
    if (entry.byteSize() <= 0 || entry.byteSize() > MAX_ENCODED_BYTES) {
      errors.add(error(path + ".byteSize", "byteSize must be between 1 and " + MAX_ENCODED_BYTES));
    }

    var normalizedChecksum = normalizeChecksum(entry.sha256());
    if (normalizedChecksum == null) {
      errors.add(error(path + ".sha256", "sha256 must be a lowercase 64-character hexadecimal digest"));
    }
    var expectedObjectKey = expectedObjectKey(entry.questionId(), normalizedChecksum, entry.contentType());
    if (expectedObjectKey == null || !expectedObjectKey.equals(entry.objectKey())) {
      errors.add(error(path + ".objectKey", "objectKey must use the immutable questionId/checksum key"));
    } else if (!objectKeys.add(entry.objectKey())) {
      errors.add(error(path + ".objectKey", "duplicate objectKey: " + entry.objectKey()));
    }
  }

  private void validateStagedBytes(
      OwnedImageManifestEntry entry,
      String path,
      Path assetRoot,
      URI ownedHttpsOrigin,
      List<ContentValidationError> errors,
      List<OwnedImagePublishPlanEntry> planEntries) {
    if (assetRoot == null || !isSafeRelativePath(entry.relativePath())) {
      return;
    }
    var stagedPath = assetRoot.resolve(entry.relativePath()).normalize();
    if (!stagedPath.startsWith(assetRoot)) {
      errors.add(error(path + ".relativePath", "relativePath must stay below the asset root"));
      return;
    }
    if (!Files.isRegularFile(stagedPath)) {
      errors.add(error(path + ".relativePath", "staged rendition file is missing"));
      return;
    }

    try {
      var byteSize = Files.size(stagedPath);
      if (byteSize > MAX_ENCODED_BYTES) {
        errors.add(error(path + ".relativePath", "staged rendition exceeds the 8 MiB encoded-byte limit"));
        return;
      }
      if (byteSize != entry.byteSize()) {
        errors.add(error(path + ".byteSize", "declared byteSize does not match staged rendition"));
      }
      var digest = sha256(stagedPath);
      if (!digest.equals(entry.sha256())) {
        errors.add(error(path + ".sha256", "declared sha256 does not match staged rendition"));
      }
      var decoded = inspectAndDecode(stagedPath);
      if (entry.contentType() != null && !entry.contentType().equals(decoded.contentType())) {
        errors.add(error(path + ".contentType", "declared contentType does not match staged rendition"));
      }
      if (decoded.width() != entry.width() || decoded.height() != entry.height()) {
        errors.add(error(path, "declared dimensions do not match staged rendition"));
      }
      if (longestEdge(decoded.width(), decoded.height()) > MAX_LONGEST_EDGE) {
        errors.add(error(path, "staged rendition exceeds the 1024 pixel longest-edge limit"));
      }
      if (hasErrorsForPath(errors, path)) {
        return;
      }
      planEntries.add(
          new OwnedImagePublishPlanEntry(
              entry.id(),
              entry.questionId(),
              entry.objectKey(),
              entry.contentType(),
              byteSize,
              decoded.width(),
              decoded.height(),
              digest,
              IMMUTABLE_CACHE_CONTROL,
              ownedUrl(ownedHttpsOrigin, entry.objectKey())));
    } catch (IOException exception) {
      errors.add(error(path + ".relativePath", "could not read or decode staged rendition"));
    }
  }

  private static Path normalizeAssetRoot(Path assetRoot, List<ContentValidationError> errors) {
    if (assetRoot == null) {
      errors.add(error("$", "assetRoot must be present"));
      return null;
    }
    var normalizedRoot = assetRoot.toAbsolutePath().normalize();
    if (!Files.isDirectory(normalizedRoot)) {
      errors.add(error("$", "assetRoot must be an existing directory"));
      return null;
    }
    return normalizedRoot;
  }

  private static URI validateOrigin(URI origin, List<ContentValidationError> errors) {
    if (origin == null) {
      return null;
    }
    if (!"https".equals(origin.getScheme())
        || origin.getHost() == null
        || origin.getQuery() != null
        || origin.getFragment() != null) {
      errors.add(error("$", "owned HTTPS origin must be an absolute HTTPS URL without query or fragment"));
      return null;
    }
    return origin;
  }

  private static void validateUniqueSlug(
      String value,
      String path,
      String description,
      Set<String> values,
      List<ContentValidationError> errors) {
    if (value == null || value.isBlank()) {
      errors.add(error(path, description + " is required"));
      return;
    }
    if (!SLUG_PATTERN.matcher(value).matches()) {
      errors.add(error(path, description + " must use lowercase letters, numbers, and hyphens"));
      return;
    }
    if (!values.add(value)) {
      errors.add(error(path, "duplicate " + description + ": " + value));
    }
  }

  private static void validateHttpsUrl(String value, String path, List<ContentValidationError> errors) {
    if (value == null || value.isBlank()) {
      errors.add(error(path, "HTTPS URL is required"));
      return;
    }
    try {
      var uri = URI.create(value);
      if (!"https".equals(uri.getScheme()) || uri.getHost() == null) {
        errors.add(error(path, "must be an absolute HTTPS URL"));
      }
    } catch (IllegalArgumentException exception) {
      errors.add(error(path, "must be an absolute HTTPS URL"));
    }
  }

  private static void requireNonBlank(String value, String path, List<ContentValidationError> errors) {
    if (value == null || value.isBlank()) {
      errors.add(error(path, "value is required"));
    }
  }

  private static boolean isSafeRelativePath(String value) {
    if (value == null || value.isBlank()) {
      return false;
    }
    try {
      var path = Path.of(value).normalize();
      return !path.isAbsolute() && !path.startsWith("..") && !path.equals(Path.of("."));
    } catch (InvalidPathException exception) {
      return false;
    }
  }

  private static int longestEdge(int width, int height) {
    return Math.max(width, height);
  }

  private static String normalizeChecksum(String value) {
    if (value == null || !SHA_256_PATTERN.matcher(value).matches()) {
      return null;
    }
    return value;
  }

  private static String expectedObjectKey(String questionId, String checksum, String contentType) {
    if (questionId == null
        || !SLUG_PATTERN.matcher(questionId).matches()
        || checksum == null
        || contentType == null) {
      return null;
    }
    var extension = switch (contentType) {
      case "image/jpeg" -> ".jpg";
      case "image/png" -> ".png";
      default -> null;
    };
    return extension == null ? null : "quiz-images/" + questionId + "/" + checksum + extension;
  }

  private static DecodedImage inspectAndDecode(Path stagedPath) throws IOException {
    try (ImageInputStream input = ImageIO.createImageInputStream(stagedPath.toFile())) {
      Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
      if (!readers.hasNext()) {
        throw new IOException("No image reader is available");
      }
      var reader = readers.next();
      try {
        reader.setInput(input, true, true);
        var width = reader.getWidth(0);
        var height = reader.getHeight(0);
        if (longestEdge(width, height) > MAX_LONGEST_EDGE) {
          return new DecodedImage(contentType(reader.getFormatName()), width, height);
        }
        BufferedImage decoded = reader.read(0);
        if (decoded == null) {
          throw new IOException("Image reader returned no image");
        }
        return new DecodedImage(contentType(reader.getFormatName()), decoded.getWidth(), decoded.getHeight());
      } finally {
        reader.dispose();
      }
    }
  }

  private static String contentType(String imageFormat) throws IOException {
    return switch (imageFormat.toLowerCase(Locale.ROOT)) {
      case "jpeg", "jpg" -> "image/jpeg";
      case "png" -> "image/png";
      default -> throw new IOException("Unsupported image format");
    };
  }

  private static String sha256(Path path) throws IOException {
    try {
      var digest = MessageDigest.getInstance("SHA-256");
      try (var input = Files.newInputStream(path)) {
        var buffer = new byte[8192];
        for (int count; (count = input.read(buffer)) != -1; ) {
          digest.update(buffer, 0, count);
        }
      }
      return java.util.HexFormat.of().formatHex(digest.digest());
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is required by the Java runtime", exception);
    }
  }

  private static String ownedUrl(URI origin, String objectKey) {
    if (origin == null) {
      return null;
    }
    var originText = origin.toString();
    return (originText.endsWith("/") ? originText.substring(0, originText.length() - 1) : originText)
        + "/"
        + objectKey;
  }

  private static boolean hasErrorsForPath(List<ContentValidationError> errors, String path) {
    return errors.stream().anyMatch(error -> error.path().equals(path) || error.path().startsWith(path + "."));
  }

  private static ContentValidationError error(String path, String message) {
    return new ContentValidationError(path, message);
  }

  private record DecodedImage(String contentType, int width, int height) {}
}
