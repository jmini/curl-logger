package com.github.dzieciou.testing.curl;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractTest {

  public Http2Curl getNonWindowsHttp2Curl() {
    OsChecker alwaysNonWindows = mock(OsChecker.class);
    when(alwaysNonWindows.isOsWindows()).thenReturn(false);
    when(alwaysNonWindows.lineSeparator()).thenReturn("\n");
    return new Http2Curl(alwaysNonWindows);
  }

}
