package com.onthisday.ingestion;

import java.util.List;

public record CuratedDailyEventsFile(List<CuratedDayJson> days) {}
