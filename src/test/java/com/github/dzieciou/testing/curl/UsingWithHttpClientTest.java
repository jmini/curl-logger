package com.github.dzieciou.testing.curl;


import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.testng.annotations.Test;

import java.io.IOException;

public class UsingWithHttpClientTest {

    private static HttpClient createHttpClient() {
        return HttpClientBuilder.create()
                .addInterceptorFirst(CurlLoggingInterceptor.builder().build())
                .build();
    }

    @Test(groups = "end-to-end-samples")
    public void test() throws IOException {
        HttpGet getRequest = new HttpGet("http://google.com");
        createHttpClient().execute(getRequest);
    }
}
