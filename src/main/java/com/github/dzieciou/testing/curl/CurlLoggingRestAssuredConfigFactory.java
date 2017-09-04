package com.github.dzieciou.testing.curl;


import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Builds `RestAssuredConfig` that allows REST-assured to logs each HTTP request as CURL command.
 */
public class CurlLoggingRestAssuredConfigFactory {

  public static RestAssuredConfig createConfig() {
    return createConfig(getDefaultOptions());
  }

  public static RestAssuredConfig createConfig(Options options) {
    return updateConfig(RestAssuredConfig.config(), options);
  }

  public static RestAssuredConfig updateConfig(RestAssuredConfig config, Options options) {
    return config
        .httpClient(HttpClientConfig.httpClientConfig()
            .reuseHttpClientInstance()
            .httpClientFactory(new MyHttpClientFactory(new CurlLoggingInterceptor(options))));
  }

  private static Options getDefaultOptions() {
    return Options.builder()
        .dontLogStacktrace()
        .printSingleliner()
        .useShortForm()
        .build();
  }

  private static class MyHttpClientFactory implements HttpClientConfig.HttpClientFactory {

    private final CurlLoggingInterceptor curlLoggingInterceptor;

    public MyHttpClientFactory(CurlLoggingInterceptor curlLoggingInterceptor) {
      this.curlLoggingInterceptor = curlLoggingInterceptor;
    }

    @Override
    public HttpClient createHttpClient() {
      AbstractHttpClient client = new DefaultHttpClient();
      client.addRequestInterceptor(curlLoggingInterceptor);
      return client;
    }
  }

}
