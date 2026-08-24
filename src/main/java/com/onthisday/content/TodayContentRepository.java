package com.onthisday.content;

import java.time.MonthDay;

public interface TodayContentRepository {

  TodayContent getTodayContent(MonthDay date);
}
