package com.onthisday.platform.http;

import com.fasterxml.jackson.databind.ObjectMapper;

/** Provides the JSON mapper shared by the API boundary. */
public final class JsonMapperFactory {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private JsonMapperFactory() {}

  public static ObjectMapper create() {
    return OBJECT_MAPPER;
  }
}
