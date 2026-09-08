package com.onthisday.platform.quiz;

import java.util.List;

public record ApiQuickPlayQuizResponse(
    String mode,
    int questionCount,
    ApiQuizSelectionResponse selection,
    ApiQuickPlayTimerResponse timer,
    List<ApiQuizQuestionResponse> questions) {}
