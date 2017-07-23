package com.github.dzieciou.testing.curl;


import static io.restassured.RestAssured.config;
import static io.restassured.RestAssured.given;
import static io.restassured.config.HttpClientConfig.httpClientConfig;
import static io.restassured.config.MultiPartConfig.multiPartConfig;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.Cookie;
import io.restassured.http.Cookies;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.mockserver.client.server.MockServerClient;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class UsingWithRestAssuredTest extends AbstractTest {

  private static final int MOCK_PORT = 9999;
  private static final String MOCK_HOST = "localhost";
  private static final String MOCK_BASE_URI = "http://" + MOCK_HOST;
  private static final String[] HEADERS_TO_IGNORE = new String[]{"User-Agent", "Connection"};

  private MockServerClient mockServer;

  private RestAssuredConfig getRestAssuredConfig(Consumer<String> curlConsumer) {
    return config()
        .httpClient(httpClientConfig()
            .reuseHttpClientInstance().httpClientFactory(new MyHttpClientFactory(curlConsumer)));
  }

  @BeforeClass
  public void setupMock() {
    mockServer = startClientAndServer(MOCK_PORT);
    mockServer.when(request()).respond(response());
  }

  @Test(groups = "end-to-end-samples")
  public void cookiesTest() {

    Consumer<String> curlConsumer = mock(Consumer.class);

    //@formatter:off
    given()
        .redirects().follow(false)
        .baseUri(MOCK_BASE_URI)
        .port(MOCK_PORT)
        .cookie("token", "tokenValue")
        .cookie("context", "contextValue")
        .config(config()
            .httpClient(httpClientConfig()
                .reuseHttpClientInstance()
                .httpClientFactory(new MyHttpClientFactory(curlConsumer))))
        .when()
        .get("/access")
        .then()
        .statusCode(200);
    //@formatter:on

    verify(curlConsumer).accept("curl 'http://localhost:" + MOCK_PORT
        + "/access' -b 'token=tokenValue; context=contextValue' -H 'Accept: */*' -H 'Content-Length: 0' -H 'Host: localhost:"
        + MOCK_PORT + "' --compressed --insecure --verbose");
  }

  @Test(groups = "end-to-end-samples")
  public void cookieWithSpecialCharactersTest() {

    Consumer<String> curlConsumer = mock(Consumer.class);

    //@formatter:off
    given()
        .redirects().follow(false)
        .baseUri(MOCK_BASE_URI)
        .port(MOCK_PORT)
        .cookie("token1",
            "1-XQLTiKxwRNyUpJYkr+IV2g==-+nLy/6GiMDj7SW/jN107UGmpf4hsM7IXsXdN9z/+7dyljV5N+0Pqpg/da0XIGOgSt2mMIIStakcjGyPlEq30Wx2gvYmVadkmH7gmcSGcaBupjlcKM2Fio96AbzJVjxUUsE5jvjBI8YlyX8fMiesQ8Gbt8XhEGbJKJe4/ogMDn7Qv687DQraxGewISOu5VIQuhgztTDqa2OUCgObG94wtAo3lSo+7HSbxcbM0LNKbbqA=-5GVOIPO4SZ7m8E0DtLS1E76h0LOmzWN00iiIeWZz360=")
        .cookie("token2",
            "2-XQLTiKxwRNyUpJYkr+IV2g==-+nLy/6GiMDj7SW/jN107UGmpf4hsM7IXsXdN9z/+7dyljV5N+0Pqpg/da0XIGOgSt2mMIIStakcjGyPlEq30Wx2gvYmVadkmH7gmcSGcaBupjlcKM2Fio96AbzJVjxUUsE5jvjBI8YlyX8fMiesQ8Gbt8XhEGbJKJe4/ogMDn7Qv687DQraxGewISOu5VIQuhgztTDqa2OUCgObG94wtAo3lSo+7HSbxcbM0LNKbbqA=-5GVOIPO4SZ7m8E0DtLS1E76h0LOmzWN00iiIeWZz360=")
        .config(config()
            .httpClient(httpClientConfig()
                .reuseHttpClientInstance()
                .httpClientFactory(new MyHttpClientFactory(curlConsumer))))
        .when()
        .get("/access")
        .then()
        .statusCode(200);
    //@formatter:on

    verify(curlConsumer).accept("curl 'http://localhost:" + MOCK_PORT + "/access' " +
        "-b '"
        + "token1=1-XQLTiKxwRNyUpJYkr+IV2g==-+nLy/6GiMDj7SW/jN107UGmpf4hsM7IXsXdN9z/+7dyljV5N+0Pqpg/da0XIGOgSt2mMIIStakcjGyPlEq30Wx2gvYmVadkmH7gmcSGcaBupjlcKM2Fio96AbzJVjxUUsE5jvjBI8YlyX8fMiesQ8Gbt8XhEGbJKJe4/ogMDn7Qv687DQraxGewISOu5VIQuhgztTDqa2OUCgObG94wtAo3lSo+7HSbxcbM0LNKbbqA=-5GVOIPO4SZ7m8E0DtLS1E76h0LOmzWN00iiIeWZz360=; "
        + "token2=2-XQLTiKxwRNyUpJYkr+IV2g==-+nLy/6GiMDj7SW/jN107UGmpf4hsM7IXsXdN9z/+7dyljV5N+0Pqpg/da0XIGOgSt2mMIIStakcjGyPlEq30Wx2gvYmVadkmH7gmcSGcaBupjlcKM2Fio96AbzJVjxUUsE5jvjBI8YlyX8fMiesQ8Gbt8XhEGbJKJe4/ogMDn7Qv687DQraxGewISOu5VIQuhgztTDqa2OUCgObG94wtAo3lSo+7HSbxcbM0LNKbbqA=-5GVOIPO4SZ7m8E0DtLS1E76h0LOmzWN00iiIeWZz360="
        + "' " +
        "-H 'Accept: */*' -H 'Content-Length: 0' -H 'Host: localhost:" + MOCK_PORT
        + "' --compressed --insecure --verbose");
  }

  @Test(groups = "end-to-end-samples")
  public void customizedCookie() {

    Consumer<String> curlConsumer = mock(Consumer.class);

    List<Cookie> cookies = new ArrayList<>();
    cookies.add(
        new Cookie.Builder("token", "tokenValue").setDomain("testing.com").setPath("/access")
            .build());

    //@formatter:off
    given()

        .redirects().follow(false)
        .baseUri(MOCK_BASE_URI)
        .port(MOCK_PORT)
        .cookies(new Cookies(cookies))
        .config(config()
            .httpClient(httpClientConfig()
                .reuseHttpClientInstance()
                .httpClientFactory(new MyHttpClientFactory(curlConsumer))))
        .when()
        .get("/access")
        .then()
        .statusCode(200);
    //@formatter:on

    verify(curlConsumer).accept("curl 'http://localhost:" + MOCK_PORT
        + "/access' -b 'token=tokenValue' -H 'Accept: */*' -H 'Content-Length: 0' -H 'Host: localhost:"
        + MOCK_PORT + "' --compressed --insecure --verbose");
  }

  @Test(groups = "end-to-end-samples")
  public void basicIntegrationTest() {

    Consumer<String> curlConsumer = mock(Consumer.class);

    //@formatter:off
    given()
        .redirects().follow(false)
        .baseUri(MOCK_BASE_URI)
        .port(MOCK_PORT)
        .config(getRestAssuredConfig(curlConsumer))
        .when()
        .get("/")
        .then()
        .statusCode(200);
    //@formatter:on

    verify(curlConsumer).accept("curl 'http://localhost:" + MOCK_PORT
        + "/' -H 'Accept: */*' -H 'Content-Length: 0' -H 'Host: localhost:" + MOCK_PORT
        + "' --compressed --insecure --verbose");
  }

  @Test(groups = "end-to-end-samples")
  public void shouldPrintPostRequestWithMultipartDataProperly() {

    Consumer<String> curlConsumer = mock(Consumer.class);

    //@formatter:off
    given()
        .baseUri(MOCK_BASE_URI)
        .port(MOCK_PORT)
        .config(getRestAssuredConfig(curlConsumer))
        .multiPart(new File("README.md"))
        .formParam("parameterX", "parameterXValue")
        .when().post("/");
    //@formatter:on

    verify(curlConsumer).accept(
        "curl 'http://localhost:9999/' -F 'file=@README.md;type=application/octet-stream' -F 'parameterX=parameterXValue;type=text/plain' -X POST -H 'Accept: */*' -H 'Host: localhost:9999' --compressed --insecure --verbose");

  }

  @Test(groups = "end-to-end-samples")
  public void shouldPrintMultipartWithContentTypesForTypes() {

    Consumer<String> curlConsumer = mock(Consumer.class);

    //@formatter:off
    given()
        .baseUri(MOCK_BASE_URI)
        .port(MOCK_PORT)
        .config(getRestAssuredConfig(curlConsumer))
        .multiPart("message", "{content:\"interesting\"}", "application/json")
        .when().post("/");
    //@formatter:on

    verify(curlConsumer).accept(
        "curl 'http://localhost:9999/' -F 'message={content:\"interesting\"};type=application/json' -X POST -H 'Accept: */*' -H 'Host: localhost:9999' --compressed --insecure --verbose");

  }

  @Test
  public void shouldPrintMultipartWithMixedType() {

    Consumer<String> curlConsumer = mock(Consumer.class);

    //@formatter:off
    given()
        .baseUri(MOCK_BASE_URI)
        .port(MOCK_PORT)
        .config(getRestAssuredConfig(curlConsumer)
            .multiPartConfig(multiPartConfig().defaultSubtype("mixed")))
        .multiPart("myfile", new File("README.md"), "application/json")
        .when().post("/");
    //@formatter:on

    verify(curlConsumer).accept(
        "curl 'http://localhost:9999/' -F 'myfile=@README.md;type=application/json' -X POST -H 'Accept: */*' -H 'Host: localhost:9999' -H 'Content-Type: multipart/mixed' --compressed --insecure --verbose");
  }

  @AfterClass
  public void closeMock() {
    mockServer.stop();
  }

  private class MyHttpClientFactory implements HttpClientConfig.HttpClientFactory {

    public final Consumer<String> curlConsumer;

    private MyHttpClientFactory(Consumer<String> curlConsumer) {
      this.curlConsumer = curlConsumer;
    }

    @Override
    public HttpClient createHttpClient() {
      AbstractHttpClient client = new DefaultHttpClient();
      client.addRequestInterceptor(new CurlTestingInterceptor(curlConsumer));
      return client;
    }
  }

  private class CurlTestingInterceptor implements HttpRequestInterceptor {

    public final Consumer<String> curlConsumer;

    public CurlTestingInterceptor(Consumer<String> curlConsumer) {
      this.curlConsumer = curlConsumer;
    }

    @Override
    public void process(HttpRequest request, HttpContext context)
        throws HttpException, IOException {
      try {
        curlConsumer.accept(getNonWindowsHttp2Curl().generateCurl(request, false, new HashSet<>(
            Arrays.asList(HEADERS_TO_IGNORE))));
      } catch (Exception e) {
        new RuntimeException(e);
      }
    }
  }

}
