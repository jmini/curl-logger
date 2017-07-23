package com.github.dzieciou.testing.curl;


public class OsChecker {

  public boolean isOsWindows() {
    return System.getProperty("os.name") != null && System.getProperty("os.name")
        .startsWith("Windows");
  }

  public String lineSeparator() {
    return System.lineSeparator();
  }

}
