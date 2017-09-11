package com.github.dzieciou.testing.curl;


import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.mockserver.client.server.MockServerClient;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class CurlLoggingRestAssuredConfigFactoryTest {

  private static final int MOCK_PORT = 9999;
  private static final String MOCK_HOST = "localhost";
  private static final String MOCK_BASE_URI = "http://" + MOCK_HOST;
  private MockServerClient mockServer;

  @BeforeClass
  public void setupMock() {
    mockServer = startClientAndServer(MOCK_PORT);
    mockServer.when(request()).respond(response().withStatusCode(200));
  }


  @Test
  public void shouldIncludeCurlInterceptorWhenCreatingConfig() {
    RestAssuredConfig updatedConfig = CurlLoggingRestAssuredConfigFactory.createConfig();
    AbstractHttpClient updateClientConfig = (AbstractHttpClient) updatedConfig.getHttpClientConfig().httpClientInstance();
    assertThat(updateClientConfig, new ContainsRequestInterceptor(CurlLoggingInterceptor.class));
  }

  @Test
  public void shouldIncludeCurlInterceptorWhenUpdatingExistingConfig() {

    final RestAssuredConfig config = RestAssuredConfig.config()
        .httpClient(HttpClientConfig.httpClientConfig().httpClientFactory(
            new HttpClientConfig.HttpClientFactory() {
              @Override
              public HttpClient createHttpClient() {
                return new DefaultHttpClient();
              }
            }
        ));

    RestAssuredConfig updatedConfig = CurlLoggingRestAssuredConfigFactory.updateConfig(config, Options.builder().build());

    assertThat(updatedConfig, not(equalTo(config)));
    AbstractHttpClient clientConfig = (AbstractHttpClient) config.getHttpClientConfig().httpClientInstance();
    assertThat(clientConfig, not(new ContainsRequestInterceptor(CurlLoggingInterceptor.class)));
    AbstractHttpClient updateClientConfig = (AbstractHttpClient) updatedConfig.getHttpClientConfig().httpClientInstance();
    assertThat(updateClientConfig, new ContainsRequestInterceptor(CurlLoggingInterceptor.class));

  }


  @Test
  public void shouldSentRequestWhenUsingConfigurationFactory() {
    RestAssured.given()
        .config(CurlLoggingRestAssuredConfigFactory.createConfig(Options.builder().useShortForm().build()))
        .baseUri(MOCK_BASE_URI)
        .port(MOCK_PORT)
        .when()
        .get("/anypath2")
        .then()
        .statusCode(200);
  }

  @AfterClass
  public void closeMock() {
    mockServer.stop();
  }


  private static class ContainsRequestInterceptor extends TypeSafeDiagnosingMatcher<AbstractHttpClient> {

    private Class<? extends HttpRequestInterceptor> expectedRequestedInterceptor;

    public ContainsRequestInterceptor(Class<? extends HttpRequestInterceptor> expectedRequestedInterceptor) {
      this.expectedRequestedInterceptor = expectedRequestedInterceptor;
    }

    @Override
    protected boolean matchesSafely(AbstractHttpClient client, Description mismatchDescription) {
      for (int i = 0; i < client.getRequestInterceptorCount(); i++) {
        if (expectedRequestedInterceptor.isInstance(client.getRequestInterceptor(i))) {
          return true;
        }
      }
      return false;
    }

    @Override
    public void describeTo(Description description) {

    }
  }


}
