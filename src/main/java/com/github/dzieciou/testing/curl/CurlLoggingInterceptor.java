package com.github.dzieciou.testing.curl;

import java.util.function.Consumer;
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

    private boolean logStacktrace;
    private boolean printMultiliner;
    private final Http2Curl http2Curl;
    private boolean useShortForm;
    private Consumer<CurlCommand> curlUpdater;

    private CurlLoggingInterceptor() {
        http2Curl = new Http2Curl();
    }

    public static Builder builder() {
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
            String curl = http2Curl.generateCurl(request, printMultiliner, useShortForm, curlUpdater);
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

        public Builder useShortForm() {
            interceptor.useShortForm = true;
            return this;
        }

        public Builder useLongForm() {
            interceptor.useShortForm = false;
            return this;
        }

        public Builder updateCurl(Consumer<CurlCommand> curlUpdater) {
            interceptor.curlUpdater = curlUpdater;
            return this;
        }

        public CurlLoggingInterceptor build() {
            return interceptor;
        }

    }
}
