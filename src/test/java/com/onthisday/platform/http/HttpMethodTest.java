package com.onthisday.platform.http;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HttpMethodTest {

  @Test
  void parsesKnownMethodsCaseInsensitively() {
    assertEquals(HttpMethod.GET, HttpMethod.from("get"));
    assertEquals(HttpMethod.POST, HttpMethod.from("POST"));
    assertEquals(HttpMethod.DELETE, HttpMethod.from("Delete"));
  }

  @Test
  void returnsUnknownForBlankOrUnsupportedValues() {
    assertEquals(HttpMethod.UNKNOWN, HttpMethod.from(null));
    assertEquals(HttpMethod.UNKNOWN, HttpMethod.from(""));
    assertEquals(HttpMethod.UNKNOWN, HttpMethod.from("PATCH"));
  }
}
