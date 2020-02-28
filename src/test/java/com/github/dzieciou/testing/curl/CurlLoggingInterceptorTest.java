package com.github.dzieciou.testing.curl;

import static io.restassured.RestAssured.config;
import static io.restassured.RestAssured.given;
import static io.restassured.config.HttpClientConfig.httpClientConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.mockserver.client.MockServerClient;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uk.org.lidalia.slf4jext.Level;


public class CurlLoggingInterceptorTest {

  private static final int MOCK_PORT = 9999;
  private static final String MOCK_HOST = "localhost";
  private static final String MOCK_BASE_URI = "http://" + MOCK_HOST;
  private MockServerClient mockServer;
  private TestLogger log;

  private static RestAssuredConfig getRestAssuredConfig(
      CurlLoggingInterceptor curlLoggingInterceptor) {
    return config()
        .httpClient(httpClientConfig()
            .reuseHttpClientInstance()
            .httpClientFactory(new MyHttpClientFactory(curlLoggingInterceptor)));
  }

  @BeforeClass
  public void setupMock() {
    mockServer = startClientAndServer(MOCK_PORT);
    mockServer.when(request()).respond(response());
  }

  @Test
  public void shouldLogDebugMessageWithCurlCommand() {

    // given
    log = TestLoggerFactory.getTestLogger("curl");
    log.clearAll();
    Options OPTIONS = Options.builder().dontLogStacktrace().build();
    RestAssuredConfig restAssuredConfig = getRestAssuredConfig(new CurlLoggingInterceptor(OPTIONS));

    // when
    //@formatter:off
    given()
        .redirects().follow(false)
        .baseUri(MOCK_BASE_URI)
        .port(MOCK_PORT)
        .config(restAssuredConfig)
        .when()
        .get("/")
        .then()
        .statusCode(200);
    //@formatter:on

    // then
    assertThat(log.getLoggingEvents().size(), is(1));
    LoggingEvent firstEvent = log.getLoggingEvents().get(0);
    assertThat(firstEvent.getLevel(), is(Level.DEBUG));
    assertThat(firstEvent.getMessage(), startsWith("curl"));
  }

  @Test
  public void shouldLogStacktraceWhenEnabled() {

    // given
    log = TestLoggerFactory.getTestLogger("curl");
    log.clearAll();
    Options options = Options.builder().logStacktrace().build();
    RestAssuredConfig restAssuredConfig = getRestAssuredConfig(new CurlLoggingInterceptor(options));

    // when
    //@formatter:off
    given()
        .redirects().follow(false)
        .baseUri(MOCK_BASE_URI)
        .port(MOCK_PORT)
        .config(restAssuredConfig)
        .when()
        .get("/shouldLogStacktraceWhenEnabled")
        .then()
        .statusCode(200);
    //@formatter:on

    // then
    assertThat(log.getAllLoggingEvents().size(), is(1));
    LoggingEvent firstEvent = log.getLoggingEvents().get(0);
    assertThat(firstEvent.getLevel(), is(Level.DEBUG));
    assertThat(firstEvent.getMessage(), both(startsWith("curl")).and(containsString("generated"))
        .and(containsString(("java.lang.Thread.getStackTrace"))));
  }

  @Test
  public void shouldLogAtErrorLevel() {

      // given
      log = TestLoggerFactory.getTestLogger("curl");
      log.clearAll();
      Options options = Options.builder().logLevel(org.slf4j.event.Level.ERROR).build();
      RestAssuredConfig restAssuredConfig = getRestAssuredConfig(new CurlLoggingInterceptor(options));

      // when
      //@formatter:off
      given()
      .redirects().follow(false)
      .baseUri(MOCK_BASE_URI)
      .port(MOCK_PORT)
      .config(restAssuredConfig)
      .when()
      .get("/shouldLogStacktraceWhenEnabled")
      .then()
      .statusCode(200);
      //@formatter:on

      // then
      assertThat(log.getAllLoggingEvents().size(), is(1));
      LoggingEvent firstEvent = log.getLoggingEvents().get(0);
      assertThat(firstEvent.getLevel(), is(Level.ERROR));
      assertThat(firstEvent.getMessage(), startsWith("curl"));
  }

  @Test
  public void shouldLogAtInfoLevel() {

      // given
      log = TestLoggerFactory.getTestLogger("curl");
      log.clearAll();
      Options options = Options.builder().logLevel(org.slf4j.event.Level.INFO).build();
      RestAssuredConfig restAssuredConfig = getRestAssuredConfig(new CurlLoggingInterceptor(options));

      // when
      //@formatter:off
      given()
      .redirects().follow(false)
      .baseUri(MOCK_BASE_URI)
      .port(MOCK_PORT)
      .config(restAssuredConfig)
      .when()
      .get("/shouldLogStacktraceWhenEnabled")
      .then()
      .statusCode(200);
      //@formatter:on

      // then
      assertThat(log.getAllLoggingEvents().size(), is(1));
      LoggingEvent firstEvent = log.getLoggingEvents().get(0);
      assertThat(firstEvent.getLevel(), is(Level.INFO));
      assertThat(firstEvent.getMessage(), startsWith("curl"));
  }

  @Test
  public void shouldLogAtTraceLevel() {

      // given
      log = TestLoggerFactory.getTestLogger("curl");
      log.clearAll();
      Options options = Options.builder().logLevel(org.slf4j.event.Level.TRACE).build();
      RestAssuredConfig restAssuredConfig = getRestAssuredConfig(new CurlLoggingInterceptor(options));

      // when
      //@formatter:off
      given()
      .redirects().follow(false)
      .baseUri(MOCK_BASE_URI)
      .port(MOCK_PORT)
      .config(restAssuredConfig)
      .when()
      .get("/shouldLogStacktraceWhenEnabled")
      .then()
      .statusCode(200);
      //@formatter:on

      // then
      assertThat(log.getAllLoggingEvents().size(), is(1));
      LoggingEvent firstEvent = log.getLoggingEvents().get(0);
      assertThat(firstEvent.getLevel(), is(Level.TRACE));
      assertThat(firstEvent.getMessage(), startsWith("curl"));
  }

  @Test
  public void shouldLogAtWarnLevel() {

      // given
      log = TestLoggerFactory.getTestLogger("curl");
      log.clearAll();
      Options options = Options.builder().logLevel(org.slf4j.event.Level.WARN).build();
      RestAssuredConfig restAssuredConfig = getRestAssuredConfig(new CurlLoggingInterceptor(options));

      // when
      //@formatter:off
      given()
      .redirects().follow(false)
      .baseUri(MOCK_BASE_URI)
      .port(MOCK_PORT)
      .config(restAssuredConfig)
      .when()
      .get("/shouldLogStacktraceWhenEnabled")
      .then()
      .statusCode(200);
      //@formatter:on

      // then
      assertThat(log.getAllLoggingEvents().size(), is(1));
      LoggingEvent firstEvent = log.getLoggingEvents().get(0);
      assertThat(firstEvent.getLevel(), is(Level.WARN));
      assertThat(firstEvent.getMessage(), startsWith("curl"));
  }

  @Test
  public void shouldNotLog() {

      // given
      log = TestLoggerFactory.getTestLogger("curl");
      log.clearAll();
      Options options = Options.builder().logLevel(null).build();
      RestAssuredConfig restAssuredConfig = getRestAssuredConfig(new CurlLoggingInterceptor(options));

      // when
      //@formatter:off
      given()
      .redirects().follow(false)
      .baseUri(MOCK_BASE_URI)
      .port(MOCK_PORT)
      .config(restAssuredConfig)
      .when()
      .get("/shouldLogStacktraceWhenEnabled")
      .then()
      .statusCode(200);
      //@formatter:on

      // then
      assertThat(log.getAllLoggingEvents().size(), is(0));
  }


  @Test
  public void shouldAddToConsumer() {

      // given
      List<String> list = new ArrayList<>();
      Options options = Options.builder().addConsumer(list::add).build();
      RestAssuredConfig restAssuredConfig = getRestAssuredConfig(new CurlLoggingInterceptor(options));

      // when
      //@formatter:off
      given()
      .redirects().follow(false)
      .baseUri(MOCK_BASE_URI)
      .port(MOCK_PORT)
      .config(restAssuredConfig)
      .when()
      .get("/shouldLogStacktraceWhenEnabled")
      .then()
      .statusCode(200);
      //@formatter:on

      // then
      assertThat(list.size(), is(1));
      assertThat(list.get(0), startsWith("curl"));
  }

  @AfterMethod
  public void clearLoggers() {
    if (log != null) {
      log.clearAll();
    }
    TestLoggerFactory.clear();
  }

  @AfterClass
  public void stopMockServer() {
    mockServer.stop();
  }

  private static class MyHttpClientFactory implements HttpClientConfig.HttpClientFactory {

    private final CurlLoggingInterceptor curlLoggingInterceptor;

    public MyHttpClientFactory(CurlLoggingInterceptor curlLoggingInterceptor) {
      this.curlLoggingInterceptor = curlLoggingInterceptor;
    }

    @Override
    public HttpClient createHttpClient() {
      @SuppressWarnings("deprecation") AbstractHttpClient client = new DefaultHttpClient();
      client.addRequestInterceptor(curlLoggingInterceptor);
      return client;
    }
  }
}
