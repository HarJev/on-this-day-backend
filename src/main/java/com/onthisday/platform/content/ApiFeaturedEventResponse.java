package com.onthisday.platform.content;

public record ApiFeaturedEventResponse(
    String id,
    String title,
    String year,
    String historicalDate,
    String summary,
    String notificationTitle,
    String notificationBody,
    ApiEventImageResponse image,
    String dateNote) {}
