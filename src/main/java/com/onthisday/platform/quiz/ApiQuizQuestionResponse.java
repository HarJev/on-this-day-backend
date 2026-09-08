package com.onthisday.platform.quiz;

/** Marker interface for the four documented quiz question response shapes. */
public sealed interface ApiQuizQuestionResponse
    permits ApiChoiceQuizQuestionResponse,
        ApiImageIdentificationQuizQuestionResponse,
        ApiChronologicalOrderingQuizQuestionResponse {}
