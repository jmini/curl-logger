package com.github.dzieciou.testing.curl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


/**
 * Logs each HTTP request as CURL command in "curl" log.
 */
public class CurlLoggingInterceptor implements HttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger("curl");

    private Set<String> headersToIgnore = new HashSet<>();
    private boolean logStacktrace = false;
    private boolean printMultiliner = false;
    private final Http2Curl http2Curl;

    private CurlLoggingInterceptor() {
        http2Curl = new Http2Curl();
    }

    public static Builder defaultBuilder() {
        return new Builder();
    }

    private static void printStacktrace(StringBuffer sb) {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        for (StackTraceElement traceElement : trace) {
            sb.append("\tat " + traceElement + System.lineSeparator());
        }
    }

    @Override
    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        try {
            String curl = http2Curl.generateCurl(request, printMultiliner, Collections.unmodifiableSet(headersToIgnore));
            StringBuffer message = new StringBuffer(curl);
            if (logStacktrace) {
                message.append(String.format("%n\tgenerated%n"));
                printStacktrace(message);
            }
            log.debug(message.toString());
        } catch (Exception e) {
            log.warn("Failed to generate CURL command for HTTP request", e);
        }
    }

    public static class Builder {

        private final CurlLoggingInterceptor interceptor = new CurlLoggingInterceptor();

        /**
         * Configures {@code CurlLoggingInterceptor} to print a stacktrace where curl command has been generated.
         */
        public Builder logStacktrace() {
            interceptor.logStacktrace = true;
            return this;
        }

        /**
         * Configures {@code CurlLoggingInterceptor} to not print a stacktrace where curl command has been generated.
         */
        public Builder dontLogStacktrace() {
            interceptor.logStacktrace = false;
            return this;
        }

        /**
         * Configures {@code CurlLoggingInterceptor} to print a curl command in multiple lines.
         */
        public Builder printMultiliner() {
            interceptor.printMultiliner = true;
            return this;
        }

        /**
         * Configures {@code CurlLoggingInterceptor} to print a curl command in a single line.
         */
        public Builder printSingleliner() {
            interceptor.printMultiliner = false;
            return this;
        }

        public Builder ignoreHeader(String headerName) {
            interceptor.headersToIgnore.add(headerName);
            return this;
        }

        public CurlLoggingInterceptor build() {
            return interceptor;
        }

    }
}
