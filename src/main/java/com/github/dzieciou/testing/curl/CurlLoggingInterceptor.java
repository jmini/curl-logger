package com.github.dzieciou.testing.curl;

import java.io.IOException;
import java.util.function.Consumer;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Logs each HTTP request as CURL command in "curl" log.
 */
public class CurlLoggingInterceptor implements HttpRequestInterceptor {

  private static final Logger log = LoggerFactory.getLogger("curl");

  private final Options options;

  private final Http2Curl http2Curl;

  public CurlLoggingInterceptor(Options options) {
    this.options = options;
    http2Curl = new Http2Curl(options);
  }

  private static void printStacktrace(StringBuffer sb) {
    StackTraceElement[] trace = Thread.currentThread().getStackTrace();
    for (StackTraceElement traceElement : trace) {
      sb.append("\tat ").append(traceElement).append(System.lineSeparator());
    }
  }

  @Override
  public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
    try {
      String curl = http2Curl.generateCurl(request);
      StringBuffer message = new StringBuffer(curl);
      if (options.canLogStacktrace()) {
        message.append(String.format("%n\tgenerated%n"));
        printStacktrace(message);
      }
      String m = message.toString();
      for (Consumer<String> consumer : options.getConsumers()) {
        consumer.accept(m);
      }
      if (options.getLogLevel() != null) {
        switch (options.getLogLevel()) {
          case DEBUG:
            log.debug(m);
            break;
          case ERROR:
            log.error(m);
            break;
          case INFO:
            log.info(m);
            break;
          case TRACE:
            log.trace(m);
            break;
          case WARN:
            log.warn(m);
            break;
          default:
            throw new IllegalStateException("Unknown log level: " + options.getLogLevel());
        }
      }
    } catch (Exception e) {
      log.warn("Failed to generate CURL command for HTTP request", e);
    }
  }


}
