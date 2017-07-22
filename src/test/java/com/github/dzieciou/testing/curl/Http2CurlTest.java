package com.github.dzieciou.testing.curl;


import java.util.HashSet;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Test(groups = "unit")
public class Http2CurlTest {

    @Test
    public void shouldPrintGetRequestProperly() throws Exception {
        HttpGet getRequest = new HttpGet("http://test.com:8080/items/query?x=y#z");
        assertThat(Http2Curl.generateCurl(getRequest),
                equalTo("curl 'http://test.com:8080/items/query?x=y#z' --compressed --insecure --verbose"));
    }

    @Test
    public void shouldPrintBasicAuthnUserCredentials() throws Exception {
        HttpGet getRequest = new HttpGet("http://test.com:8080/items/query?x=y#z");
        String encodedCredentials = Base64.getEncoder().encodeToString("xx:yy".getBytes());
        getRequest.addHeader("Authorization", "Basic " + encodedCredentials);
        assertThat(Http2Curl.generateCurl(getRequest),
                equalTo("curl 'http://test.com:8080/items/query?x=y#z' --user 'xx:yy' --compressed --insecure --verbose"));
    }

    @Test
    public void shouldPrintPostRequestProperly() throws Exception {
        HttpPost postRequest = new HttpPost("http://google.pl/");
        List<NameValuePair> postParameters = new ArrayList<>();
        postParameters.add(new BasicNameValuePair("param1", "param1_value"));
        postParameters.add(new BasicNameValuePair("param2", "param2_value"));

        postRequest.setEntity(new UrlEncodedFormEntity(postParameters));
        postRequest.setHeader("Content-Type", "application/x-www-form-urlencoded");
        assertThat(Http2Curl.generateCurl(postRequest),
                equalTo("curl 'http://google.pl/' -H 'Content-Type: application/x-www-form-urlencoded' --data 'param1=param1_value&param2=param2_value' --compressed --insecure --verbose"));
    }

    @Test
    public void shouldPrintDeleteRequestProperly() throws Exception {
        HttpDelete deleteRequest = new HttpDelete("http://test.com/items/12345");
        assertThat(Http2Curl.generateCurl(deleteRequest),
                equalTo("curl 'http://test.com/items/12345' -X DELETE --compressed --insecure --verbose"));
    }

    @Test
    public void shouldPrintHeadRequestProperly() throws Exception {
        HttpHead headRequest = new HttpHead("http://test.com/items/12345");
        assertThat(Http2Curl.generateCurl(headRequest),
                equalTo("curl 'http://test.com/items/12345' -X HEAD --compressed --insecure --verbose"));
    }

    @Test
    public void shouldPrintMultipleCookiesInOneParameter() throws Exception {
        HttpHead headRequest = new HttpHead("http://test.com/items/12345");
        headRequest.setHeader("Cookie", "X=Y; A=B");
        assertThat(Http2Curl.generateCurl(headRequest),
                equalTo("curl 'http://test.com/items/12345' -X HEAD -b 'X=Y; A=B' --compressed --insecure --verbose"));
    }

    @Test
    public void shouldPrintPutRequestProperly() throws Exception {
        HttpPut putRequest = new HttpPut("http://test.com/items/12345");
        putRequest.setEntity(new StringEntity("details={\"name\":\"myname\",\"age\":\"20\"}"));
        putRequest.setHeader("Content-Type", "application/json");
        assertThat(Http2Curl.generateCurl(putRequest),
                equalTo("curl 'http://test.com/items/12345' -X PUT -H 'Content-Type: application/json' --data 'details={\"name\":\"myname\",\"age\":\"20\"}' --compressed --insecure --verbose"));
    }

    @Test
    public void shouldPrintMultilineRequestProperly() throws Exception {
        HttpPost postRequest = new HttpPost("http://google.pl/");
        List<NameValuePair> postParameters = new ArrayList<>();
        postParameters.add(new BasicNameValuePair("param1", "param1_value"));
        postParameters.add(new BasicNameValuePair("param2", "param2_value"));

        postRequest.setEntity(new UrlEncodedFormEntity(postParameters));
        postRequest.setHeader("Content-Type", "application/x-www-form-urlencoded");

        assertThat(Http2Curl.generateCurl(postRequest, true, new HashSet<>()),
                equalTo(osIndepenent("curl 'http://google.pl/' \\\n  -H 'Content-Type: application/x-www-form-urlencoded' \\\n  --data 'param1=param1_value&param2=param2_value' \\\n  --compressed \\\n  --insecure \\\n  --verbose")));
    }

    private static String osIndepenent(String s) {
        return s.replace("\n", System.lineSeparator());
    }
}
