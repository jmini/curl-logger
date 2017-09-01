# CURL Logger

[![Build Status](https://travis-ci.org/dzieciou/curl-logger.svg?branch=master)](https://travis-ci.org/dzieciou/curl-logger/)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.dzieciou.testing/curl-logger/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.dzieciou.testing/curl-logger)

Logs each HTTP request sent by REST-assured as a [CURL][1] command.

The following request from REST-assured test
```java  
given()
  .config(config)
  .redirects().follow(false)
.when()
  .get("http://google.com")
.then()
  .statusCode(302); 
```
will be logged as:
```
curl 'http://google.com/' -H 'Accept: */*' -H 'Content-Length: 0' -H 'Host: google.com' -H 'Connection: Keep-Alive' -H 'User-Agent: Apache-HttpClient/4.5.1 (Java/1.8.0_45)' --compressed 
```

This way testers and developers can quickly reproduce an issue and isolate its root cause. 

## Usage

Latest release:

```xml
<dependency>
  <groupId>com.github.dzieciou.testing</groupId>
  <artifactId>curl-logger</artifactId>
  <version>0.6</version>
</dependency>
```
   
### Using with REST-assured client 
    
When sending HTTP Request with REST-assured, you must configure it first as follows:
        
```java
RestAssuredConfig config = new CurlLoggingRestAssuredConfigBuilder().build();  
```
  
and then use it:

```java
given()
  .config(config)
  ...
```

If you already have a `RestAssuredConfig` instance, you may reconfigure it as follows:

```java
RestAssuredConfig config = ...;
config = new CurlLoggingRestAssuredConfigBuilder(config).build();  
```

CURL commands are logged to a "curl" logger. The library requires only the logger to be [slf4j][4]-compliant, e.g.,
using [logback][5]. Sample logback configuration that logs all CURL commands to standard system output would be:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%-4relative [%thread] %-5level %logger{35} - %msg %n</pattern>
        </encoder>
    </appender>
    <logger name="curl" level="DEBUG">
        <appender-ref ref="STDOUT"/>
    </logger>
</configuration>
```

## Features

### Logging stacktrace

If your test is sending multiple requests it might be hard to understand which REST-assured request generated a given 
curl command. The library provides a way to log stacktrace where the curl generation was requested:

```java  
new CurlLoggingRestAssuredConfigBuilder()
  .logStacktrace()
  .build()
```

### Logging attached files

When you attach a file to your requests, e.g., sending content of "README.md" file:

```java
given()
  .config(new CurlLoggingRestAssuredConfigBuilder().build())
  .baseUri("http://someHost.com")
  .multiPart("myfile", new File("README.md"), "application/json")
.when()
  .post("/uploadFile");
```

the library will include reference to it instead of pasting its content:
```
curl 'http://somehost.com/uploadFile' -F 'myfile=@README.md;type=application/json' -X POST ...
```

### Printing command in multiple lines

For leggibility reasons you may want to print your command in multiple lines:
```
curl 'http://google.pl/' \ 
  -H 'Content-Type: application/x-www-form-urlencoded' \ 
  --data 'param1=param1_value&param2=param2_value' \ 
  --compressed \ 
  --insecure \ 
  --verbose
```
or in Windows:
```
curl 'http://google.pl/' ^ 
  -H 'Content-Type: application/x-www-form-urlencoded' ^ 
  --data 'param1=param1_value&param2=param2_value' ^ 
  --compressed ^ 
  --insecure ^ 
  --verbose
```
To achieve this configure your `CurlLoggingInterceptor` as follows:
```java
new CurlLoggingRestAssuredConfigBuilder()
  .printMultiliner()
  .build()
```

By default `CurlLoggingRestAssuredConfigBuilder` creates configuration that prints curl command in a single line.

## Prerequisities

* JDK 8
* Dependencies with which I tested the solution

```xml
<dependency>
    <groupId>org.apache.httpcomponents</groupId>
    <artifactId>httpclient</artifactId>
    <version>4.5.2</version>
</dependency>
<dependency>
    <groupId>io.restassured</groupId>
    <artifactId>rest-assured</artifactId>
    <version>3.0.2</version>
</dependency>
```

## Releases

0.7:


0.6:
* Fixed bug: For each cookie a separate "-b cookie=value" parameter was generated (https://github.com/dzieciou/curl-logger/issues/4)
* Upgraded to REST-assured 3.0.2
* Simplified curl-logger configuration with `CurlLoggingRestAssuredConfigBuilder`, based on suggestion from Tao Zhang (https://github.com/dzieciou/curl-logger/issues/4)

0.5:

* Upgraded to REST-assured 3.0.1 that contains important fix impacting curl-logger: Cookie attributes are no longer sent in request in accordance with RFC6265. 
* Fixed bug: cookie values can have = sign inside so we need to get around them somehow
* Cookie strings are now escaped
* `CurlLoggingInterceptor`'s constructor is now protected to make extending it possible 
* `CurlLoggingInterceptor` can now be configured to print a curl command in multiple lines
 

0.4:
 
* Upgraded to REST-assured 3.0

0.3:

 * Each cookie is now defined with "-b" option instead of -"H"
 * Removed heavy dependencies like Guava
 * Libraries like REST-assured and Apache must be now provided by the user (didn't want to constrain users to a specific version)
 * Can log stacktrace where curl generation was requested

0.2:

 * Support for multipart/mixed and multipart/form content types
 * Now all generated curl commands are "--insecure --verbose"
 
0.1:

 * Support for logging basic operations

## Bugs and features request

Report or request in [JIRA][2].

## Similar tools
  
* Chrome Web browser team has ["Copy as CURL"][7] in the network panel, similarly [Firebug add-on][8] for Firefox.
* OkHttp client provides similar request [interceptor][3] to log HTTP requests as curl command. 
* [Postman add-on][6] for Chrome provides a way to convert prepared requests as curl commands.


  [1]: https://curl.haxx.se/
  [2]: https://github.com/dzieciou/curl-logger/issues
  [3]: https://github.com/mrmike/Ok2Curl 
  [4]: http://www.slf4j.org/
  [5]: http://logback.qos.ch/
  [6]: https://www.getpostman.com/docs/creating_curl
  [7]: https://coderwall.com/p/-fdgoq/chrome-developer-tools-adds-copy-as-curl
  [8]: http://www.softwareishard.com/blog/planet-mozilla/firebug-tip-resend-http-request/
