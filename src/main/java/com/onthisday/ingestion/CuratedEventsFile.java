package com.onthisday.ingestion;

import java.util.List;

public record CuratedEventsFile(List<CuratedEventJson> events) {}
