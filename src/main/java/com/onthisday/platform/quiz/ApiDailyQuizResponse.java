package com.onthisday.platform.quiz;

import java.util.List;

public record ApiDailyQuizResponse(
    String mode,
    String challengeId,
    ApiDailyQuizDateResponse date,
    int questionCount,
    int assignmentQuestionCount,
    ApiDailyTimerResponse timer,
    List<ApiQuizQuestionResponse> questions) {}
