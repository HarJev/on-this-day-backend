package com.onthisday.platform.notifications.scheduled;

import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;

/** Reads a SecureString from SSM Parameter Store using the function's own IAM role. */
final class SsmParameterReader implements ParameterReader {

  @Override
  public String readDecrypted(String name) {
    try (var client = SsmClient.builder().httpClient(UrlConnectionHttpClient.create()).build()) {
      return client
          .getParameter(GetParameterRequest.builder().name(name).withDecryption(true).build())
          .parameter()
          .value();
    }
  }
}
