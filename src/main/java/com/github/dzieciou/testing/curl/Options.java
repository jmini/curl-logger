package com.github.dzieciou.testing.curl;


import org.slf4j.event.Level;

import java.util.Optional;
import java.util.function.Consumer;

public class Options {

  private boolean logStacktrace;
  private Level logLevel = Level.DEBUG;
  private boolean printMultiliner;
  private boolean useShortForm;
  private Consumer<CurlCommand> curlUpdater;
  private Platform targetPlatform = Platform.RECOGNIZE_AUTOMATICALLY;
  private boolean escapeNonAscii;

  private Options() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public boolean canLogStacktrace() {
    return logStacktrace;
  }

  public Level getLogLevel() {
    return logLevel;
  }

  public boolean printMultiliner() {
    return printMultiliner;
  }

  public boolean escapeNonAscii() {
    return escapeNonAscii;
  }

  public boolean useShortForm() {
    return useShortForm;
  }

  public Optional<Consumer<CurlCommand>> getCurlUpdater() {
    return Optional.ofNullable(curlUpdater);
  }

  public Platform getTargetPlatform() {
    return targetPlatform;
  }

  public static class Builder {

    private final Options options = new Options();

    /**
     * Configures the library to print a stacktrace where curl command has been generated.
     */
    public Builder logStacktrace() {
      options.logStacktrace = true;
      return this;
    }

    /**
     * Configures the library to not print a stacktrace where curl command has been generated.
     */
    public Builder dontLogStacktrace() {
      options.logStacktrace = false;
      return this;
    }

    /**
     * Configure the level at which the generated curl statement should be logged. <code>null</code> means no logging.
     */
    public Builder logLevel(Level level) {
      options.logLevel = level;
      return this;
    }

    /**
     * Configures the library to print a curl command in multiple lines.
     */
    public Builder printMultiliner() {
      options.printMultiliner = true;
      return this;
    }

    /**
     * Configures the library to print a curl command in a single line.
     */
    public Builder printSingleliner() {
      options.printMultiliner = false;
      return this;
    }

    /**
     * Configures the library to print short form of curl parameters.
     */
    public Builder useShortForm() {
      options.useShortForm = true;
      return this;
    }

    /**
     * Configures the library to print long form of curl parameters.
     */
    public Builder useLongForm() {
      options.useShortForm = false;
      return this;
    }

    /**
     * Configures the library to update curl command with a given {@code curlUpdater} before
     * printing.
     */
    public Builder updateCurl(Consumer<CurlCommand> curlUpdater) {
      options.curlUpdater = curlUpdater;
      return this;
    }

    /**
     * Configure the library to print curl command that will be executable on a given {@code
     * targetPlatform}.
     */
    public Builder targetPlatform(Platform targetPlatform) {
      options.targetPlatform = targetPlatform;
      return this;
    }

    /**
     * Enable escaping non ASCII characters for POSIX platforms.
     */
    public Builder escapeNonAscii() {
      options.escapeNonAscii = true;
      return this;
    }

    /**
     * Disable escaping non ASCII characters for POSIX platforms.
     */
    public Builder dontEscapeNonAscii() {
      options.escapeNonAscii = false;
      return this;
    }

    public Options build() {
      return options;
    }

  }
}
