package com.github.dzieciou.testing.curl;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CurlCommand {

  private static Map<String, String> shortParameterNames = new HashMap<>(); {
    shortParameterNames.put("--user", "-u");
    shortParameterNames.put("--data", "-d");
    shortParameterNames.put("--insecure", "-k");
    shortParameterNames.put("--form", "-F");
    shortParameterNames.put("--cookie", "-b");
    shortParameterNames.put("--header", "-H");
    shortParameterNames.put("--request", "-X");
    shortParameterNames.put("--verbose", "-v");
  }
  private final OsChecker osChecker;
  private String url;
  private final List<Header> headers = new ArrayList<>();
  private final List<FormPart> formParts = new ArrayList<>();
  private final List<String> datas = new ArrayList<>();
  private final List<String> datasBinary = new ArrayList<>();
  private Optional<String> cookieHeader = Optional.empty();
  private boolean compressed;
  private boolean verbose;
  private boolean insecure;
  private Optional<String> method = Optional.empty();
  private Optional<ServerAuthentication> serverAuthentication = Optional.empty();

  public CurlCommand(OsChecker osChecker) {
    this.osChecker = osChecker;
  }


  public CurlCommand setUrl(String url) {
    this.url = url;
    return this;
  }

  public CurlCommand addHeader(String name, String value) {
    headers.add(new Header(name, value));
    return this;
  }

  public CurlCommand removeHeader(String name) {
    Iterator<Header> it = headers.iterator();
    while(it.hasNext()) {
      if (it.next().name.equals(name)) {
        it.remove();
      }
    }
    return this;
  }

  public CurlCommand addFormPart(String name, String content) {
    formParts.add(new FormPart(name, content));
    return this;
  }

  public CurlCommand addData(String data) {
    datas.add(data);
    return this;
  }

  public CurlCommand addDataBinary(String dataBinary) {
    datasBinary.add(dataBinary);
    return this;
  }


  public CurlCommand setCookieHeader(String cookieHeader) {
    this.cookieHeader = Optional.of(cookieHeader);
    return this;
  }

  public CurlCommand setCompressed(boolean compressed) {
    this.compressed = compressed;
    return this;
  }

  public CurlCommand setVerbose(boolean verbose) {
    this.verbose = verbose;
    return this;
  }

  public CurlCommand setInsecure(boolean insecure) {
    this.insecure = insecure;
    return this;
  }

  public CurlCommand setMethod(String method) {
    this.method = Optional.of(method);
    return this;
  }

  public CurlCommand setServerAuthentication(String user, String password) {
    serverAuthentication = Optional.of(new ServerAuthentication(user, password));
    return this;
  }

  public static class Header {

    private final String name;
    private final String value;

    public Header(String name, String value) {
      this.name = name;
      this.value = value;
    }

    public String getName() {
      return name;
    }

    public String getValue() {
      return value;
    }
  }

  public static class FormPart {

    private final String name;
    private final String content;

    public FormPart(String name, String content) {
      this.name = name;
      this.content = content;
    }

    public String getName() {
      return name;
    }

    public String getContent() {
      return content;
    }
  }

  public static class ServerAuthentication {

    private final String user;
    private final String password;

    public ServerAuthentication(String user, String password) {
      this.user = user;
      this.password = password;
    }

    public String getPassword() {
      return password;
    }

    public String getUser() {
      return user;
    }
  }

  @Override
  public String toString() {
    return asString(false, false);
  }

  public String asString(boolean printMultiliner, boolean useShortForm) {
    List<List<String>> command = new ArrayList<>();
    
    command.add(line(useShortForm, "curl", escapeString(url).replaceAll("[[{}\\\\]]", "\\$&")));

    method.ifPresent(method -> command.add(line(useShortForm, "--request", method)));

    cookieHeader.ifPresent(cookieHeader -> command.add(line(useShortForm, "--cookie", escapeString(cookieHeader))));

    headers.forEach(header
        -> command.add(line(useShortForm, "--header", escapeString(header.getName() + ": " + header.getValue()))));

    formParts.forEach(formPart
        -> command.add(line(useShortForm, "--form", escapeString(formPart.getName() + "=" + formPart.getContent()))));

    datas.forEach(data -> command.add(line(useShortForm, "--data", escapeString(data))));

    datasBinary.forEach(data -> command.add(line(useShortForm, "--data-binary", escapeString(data))));

    serverAuthentication.ifPresent(sa
        -> command.add(line(useShortForm, "--user", escapeString(sa.getUser() + ":" + sa.getPassword()))));

    if (compressed) {
      command.add(line(useShortForm, "--compressed"));
    }
    if (insecure) {
      command.add(line(useShortForm, "--insecure"));
    }
    if (verbose) {
      command.add(line(useShortForm, "--verbose"));
    }

    return command.stream()
        .map(line -> line.stream().collect(Collectors.joining(" ")))
        .collect(Collectors.joining(chooseJoiningString(printMultiliner)));
  }

  private static String parameterName(String longParameterName, boolean useShortForm) {
    return useShortForm
        ? (shortParameterNames.get(longParameterName) == null ? longParameterName : shortParameterNames.get(longParameterName))
        : longParameterName;
  }

  private CharSequence chooseJoiningString(boolean printMultiliner) {
    String commandLineSeparator = osChecker.isOsWindows() ? "^" : "\\";
    return printMultiliner
        ? String.format(" %s%s  ", commandLineSeparator, osChecker.lineSeparator())
        : " ";
  }

  private static List<String> line(boolean useShortForm, String longParameterName, String... arguments) {
    List<String> line = new ArrayList<>(Arrays.asList(arguments));
    line.add(0, parameterName(longParameterName, useShortForm));
    return line;
  }

  private String escapeString(String s) {
    // cURL command is expected to run on the same platform that test run
    return osChecker.isOsWindows() ? escapeStringWin(s) : escapeStringPosix(s);
  }

  /**
   * Replace quote by double quote (but not by \") because it is recognized by both cmd.exe and MS
   * Crt arguments parser.
   * <p>
   * Replace % by "%" because it could be expanded to an environment variable value. So %% becomes
   * "%""%". Even if an env variable "" (2 doublequotes) is declared, the cmd.exe will not
   * substitute it with its value.
   * <p>
   * Replace each backslash with double backslash to make sure MS Crt arguments parser won't
   * collapse them.
   * <p>
   * Replace new line outside of quotes since cmd.exe doesn't let to do it inside.
   */
  private static String escapeStringWin(String s) {
    return "\""
        + s
        .replaceAll("\"", "\"\"")
        .replaceAll("%", "\"%\"")
        .replaceAll("\\\\", "\\\\")
        .replaceAll("[\r\n]+", "\"^$&\"")
        + "\"";
  }

  private static String escapeStringPosix(String s) {

    if (s.matches("^.*([^\\x20-\\x7E]|\').*$")) {
      // Use ANSI-C quoting syntax.
      String escaped = s
          .replaceAll("\\\\", "\\\\")
          .replaceAll("'", "\\'")
          .replaceAll("\n", "\\n")
          .replaceAll("\r", "\\r");

      escaped = escaped.chars()
          .mapToObj(c -> escapeCharacter((char) c))
          .collect(Collectors.joining());

      return "$\'" + escaped + "'";
    } else {
      // Use single quote syntax.
      return "'" + s + "'";
    }

  }

  private static String escapeCharacter(char c) {
    int code = (int) c;
    String codeAsHex = Integer.toHexString(code);
    if (code < 256) {
      // Add leading zero when needed to not care about the next character.
      return code < 16 ? "\\x0" + codeAsHex : "\\x" + codeAsHex;
    }
    return "\\u" + ("" + codeAsHex).substring(codeAsHex.length(), 4);
  }
}
