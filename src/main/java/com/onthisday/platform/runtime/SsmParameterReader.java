package com.onthisday.platform.runtime;

import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;

/**
 * Reads a SecureString from SSM Parameter Store with the default AWS credentials: the function's
 * own IAM role when deployed, the operator's signed-in profile when a command runs on a laptop.
 */
public final class SsmParameterReader implements ParameterReader {

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
