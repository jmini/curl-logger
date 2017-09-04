package com.github.dzieciou.testing.curl;

import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.mockserver.client.server.MockServerClient;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uk.org.lidalia.slf4jext.Level;
import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import static io.restassured.RestAssured.config;
import static io.restassured.RestAssured.given;
import static io.restassured.config.HttpClientConfig.httpClientConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class CurlLoggingInterceptorTest {

    public static final int MOCK_PORT = 9999;
    public static final String MOCK_HOST = "localhost";
    public static final String MOCK_BASE_URI = "http://" + MOCK_HOST;
    private MockServerClient mockServer;
    private TestLogger log = TestLoggerFactory.getTestLogger("curl");

    private static RestAssuredConfig getRestAssuredConfig(CurlLoggingInterceptor curlLoggingInterceptor) {
        return config()
                .httpClient(httpClientConfig()
                        .reuseHttpClientInstance().httpClientFactory(new MyHttpClientFactory(curlLoggingInterceptor)));
    }

    @BeforeClass
    public void setupMock() {
        mockServer = startClientAndServer(MOCK_PORT);
        mockServer.when(request()).respond(response());
    }

    @Test
    public void shouldLogDebugMessageWithCurlCommand() {

        // given
        RestAssuredConfig restAssuredConfig = getRestAssuredConfig(CurlLoggingInterceptor.builder().build());

        // when
        //@formatter:off
        given()
                .redirects().follow(false)
                .baseUri( MOCK_BASE_URI)
                .port(MOCK_PORT)
                .config( restAssuredConfig)
        .when()
                .get("/")
        .then()
                .statusCode(200);
        //@formatter:on

        // then
        assertThat(log.getAllLoggingEvents().size(), is(1));
        LoggingEvent firstEvent = log.getLoggingEvents().get(0);
        assertThat(firstEvent.getLevel(), is(Level.DEBUG));
        assertThat(firstEvent.getMessage(), startsWith("curl"));
    }

    @Test
    public void shouldLogStacktraceWhenEnabled() {

        // given
        RestAssuredConfig restAssuredConfig = getRestAssuredConfig(CurlLoggingInterceptor.builder().logStacktrace().build());

        // when
        //@formatter:off
        given()
                .redirects().follow(false)
                .baseUri( MOCK_BASE_URI)
                .port(MOCK_PORT)
                .config( restAssuredConfig)
        .when()
                .get("/")
        .then()
                .statusCode(200);
        //@formatter:on

        // then
        assertThat(log.getAllLoggingEvents().size(), is(1));
        LoggingEvent firstEvent = log.getLoggingEvents().get(0);
        assertThat(firstEvent.getLevel(), is(Level.DEBUG));
        assertThat(firstEvent.getMessage(), both(startsWith("curl")).and(containsString("generated")).and(containsString(("java.lang.Thread.getStackTrace"))));
    }

    @AfterMethod
    public void clearLoggers() {
        log.clearAll();
        TestLoggerFactory.clear();
    }

    @AfterClass
    public void stopMockServer() {
        mockServer.stop();
    }

    private static class MyHttpClientFactory implements HttpClientConfig.HttpClientFactory {

        private CurlLoggingInterceptor curlLoggingInterceptor;

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
