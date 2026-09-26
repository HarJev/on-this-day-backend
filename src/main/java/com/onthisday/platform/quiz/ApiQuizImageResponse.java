package com.onthisday.platform.quiz;

import java.net.URI;

public record ApiQuizImageResponse(
    URI url,
    String altText,
    String source,
    URI sourceUrl,
    String attribution,
    String creator,
    String license,
    URI licenseUrl) {}
