package com.github.dzieciou.testing.curl;


import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import io.restassured.RestAssured;
import org.mockserver.client.server.MockServerClient;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CurlLoggingRestAssuredConfigFactoryTest {

  public static final int MOCK_PORT = 9999;
  public static final String MOCK_HOST = "localhost";
  public static final String MOCK_BASE_URI = "http://" + MOCK_HOST;
  private MockServerClient mockServer;

  @BeforeClass
  public void setupMock() {
    mockServer = startClientAndServer(MOCK_PORT);
    mockServer.when(request()).respond(response().withStatusCode(200));
  }

  @Test
  public void shouldSentRequestWhenUsingConfigurationBuilder() {
    RestAssured.given()
        .config(CurlLoggingRestAssuredConfigFactory
            .createConfig(Options.builder().logStacktrace().build()))
        .baseUri(MOCK_BASE_URI)
        .port(MOCK_PORT)
        .when()
        .get("/anypath")
        .then()
        .statusCode(200);
  }

  @AfterClass
  public void closeMock() {
    mockServer.stop();
  }


}
