package com.onthisday.platform.content;

import java.net.URI;

public record ApiEventImageResponse(
    URI url,
    String altText,
    String source,
    URI sourceUrl,
    String attribution,
    String creator,
    String license,
    URI licenseUrl) {}
