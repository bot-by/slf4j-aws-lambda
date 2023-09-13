# SLF4J for AWS Lambda

An [SLF4J][] Logger implementation for [AWS Lambda][lambda] and [CloudWatch][cloudwatch].

[![Codacy Grade](https://app.codacy.com/project/badge/Grade/dda626a02daf464c94aa10955a6b8f6b)](https://www.codacy.com/gl/bot-by/slf4j-aws-lambda/dashboard?utm_source=gitlab.com&utm_medium=referral&utm_content=bot-by/slf4j-aws-lambda&utm_campaign=Badge_Grade)
[![Codacy Coverage](https://app.codacy.com/project/badge/Coverage/dda626a02daf464c94aa10955a6b8f6b)](https://www.codacy.com/gl/bot-by/slf4j-aws-lambda/dashboard?utm_source=gitlab.com&utm_medium=referral&utm_content=bot-by/slf4j-aws-lambda&utm_campaign=Badge_Coverage)
[![Java Version](https://img.shields.io/static/v1?label=java&message=11&color=blue&logo=java&logoColor=E23D28)](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)

| Logger | Release | Javadoc |
|---|---|---|
| **slf4j-aws-lambda-logger** | [![Maven Central](https://img.shields.io/maven-central/v/uk.bot-by/slf4j-aws-lambda-logger)](https://search.maven.org/artifact/uk.bot-by/slf4j-aws-lambda-logger) | [![Javadoc](https://javadoc.io/badge2/uk.bot-by/slf4j-aws-lambda-logger/javadoc.svg)](https://javadoc.io/doc/uk.bot-by/slf4j-aws-lambda-logger) |
| **slf4j-aws-lambda-json-logger** | [![Maven Central](https://img.shields.io/maven-central/v/uk.bot-by/slf4j-aws-lambda-json-logger)](https://search.maven.org/artifact/uk.bot-by/slf4j-aws-lambda-json-logger) | [![Javadoc](https://javadoc.io/badge2/uk.bot-by/slf4j-aws-lambda-json-logger/javadoc.svg)](https://javadoc.io/doc/uk.bot-by/slf4j-aws-lambda-json-logger) |


Table of Contents
=================

   * [Getting started](#getting-started)
      * [Usage](#usage)
      * [Configuration](#configuration)
   * [Other solutions](#other-solutions)
   * [Contributing](#contributing)
   * [History](#history)
   * [Authors and acknowledgment](#authors-and-acknowledgment)
   * [License](#license)

Created by [gh-md-toc](https://github.com/ekalinin/github-markdown-toc)

## Getting started

Yet another SLF4J Simple, isn't it?

No, it isn't.

This implementation supports [MDC][mdc] to print out **AWS request ID** in start of every logging
record and supports [Markers][marker] too. And the killer feature: it solves
the [CRLF issue](https://twitter.com/ben11kehoe/status/1264597451010433025) described by
Frank Afriat in [Solving the Java Aws Lambda logging problem][aws-lambda-logging-problem] - you
don't have to prepare logging messages and stacktraces to log them on CloudWatch Logs.

The footprint of **slf4j-aws-lambda** (88K) is same size as **slf4j-simple** (79K) and much smaller
than **logback** (888K).

### Usage

There is a great original [manual][manual].

The sample code, see the folder **[example-lambda](example-lambda)** or **[example-lambda-json](example-lambda-json)**:

```java
@Override
public String handleRequest(Map<String, Object> input, Context context) {
  MDC.put("@aws-request-id@", context.getAwsRequestId());

  logger.trace("trace message");
  logger.debug("debug message");
  logger.info("info message");
  logger.warn("warning message");
  logger.error("error message");

  var marker = new BasicMarkerFactory().getMarker("important");

  Stream.of("\n", "\r\n", "\r").forEach(injection -> {
    logger.trace(marker, "CRLF{}injection", injection);
  });

  logger.warn("printable stacktrace", new Throwable("Printable Stacktrace Demo"));
  return "done";
}
```

The log with **slf4j-aws-lambda-logger**:

```log
983f71e5-9091-443b-8c01-6668120c0e5d INFO uk.bot_by.slf4j_demo.BotHandler - info message
983f71e5-9091-443b-8c01-6668120c0e5d WARN uk.bot_by.slf4j_demo.BotHandler - warning message
983f71e5-9091-443b-8c01-6668120c0e5d ERROR uk.bot_by.slf4j_demo.BotHandler - error message
983f71e5-9091-443b-8c01-6668120c0e5d TRACE uk.bot_by.slf4j_demo.BotHandler - CRLF
injection
983f71e5-9091-443b-8c01-6668120c0e5d TRACE uk.bot_by.slf4j_demo.BotHandler - CRLF
injection
983f71e5-9091-443b-8c01-6668120c0e5d TRACE uk.bot_by.slf4j_demo.BotHandler - CRLF
injection
983f71e5-9091-443b-8c01-6668120c0e5d WARN uk.bot_by.slf4j_demo.BotHandler - printable stacktrace
```

There is a JSON option with **slf4j-aws-lambda-json-logger**:
```json
{
    "level": "INFO",
    "logname": "uk.bot_by.slf4j_demo.BotHandler",
    "message": "info message",
    "aws-request-id": "7b9af47e-d861-44b4-bde7-fa2e84ffb7cf"
}
{
    "level": "WARN",
    "logname": "uk.bot_by.slf4j_demo.BotHandler",
    "message": "warning message",
    "aws-request-id": "7b9af47e-d861-44b4-bde7-fa2e84ffb7cf"
}
{
    "level": "ERROR",
    "logname": "uk.bot_by.slf4j_demo.BotHandler",
    "message": "error message",
    "aws-request-id": "7b9af47e-d861-44b4-bde7-fa2e84ffb7cf"
}
{
    "level": "TRACE",
    "logname": "uk.bot_by.slf4j_demo.BotHandler",
    "message": "CRLF\ninjection",
    "aws-request-id": "7b9af47e-d861-44b4-bde7-fa2e84ffb7cf"
}
{
    "level": "TRACE",
    "logname": "uk.bot_by.slf4j_demo.BotHandler",
    "message": "CRLF\r\ninjection",
    "aws-request-id": "7b9af47e-d861-44b4-bde7-fa2e84ffb7cf"
}
{
    "level": "TRACE",
    "logname": "uk.bot_by.slf4j_demo.BotHandler",
    "message": "CRLF\rinjection",
    "aws-request-id": "7b9af47e-d861-44b4-bde7-fa2e84ffb7cf"
}
{
    "stack-trace": "java.lang.Throwable: Printable Stacktrace Demo\n\tat uk.bot_by.slf4j_demo.BotHandler.handleRequest(BotHandler.java:36)\n\tat uk.bot_by.slf4j_demo.BotHandler.handleRequest(BotHandler.java:12)\n\tat lambdainternal.EventHandlerLoader$PojoHandlerAsStreamHandler.handleRequest(EventHandlerLoader.java:205)\n\tat lambdainternal.EventHandlerLoader$2.call(EventHandlerLoader.java:905)\n\tat lambdainternal.AWSLambda.startRuntime(AWSLambda.java:261)\n\tat lambdainternal.AWSLambda.startRuntime(AWSLambda.java:200)\n\tat lambdainternal.AWSLambda.main(AWSLambda.java:194)\n",
    "level": "WARN",
    "logname": "uk.bot_by.slf4j_demo.BotHandler",
    "message": "printable stacktrace",
    "aws-request-id": "7b9af47e-d861-44b4-bde7-fa2e84ffb7cf"
}
```

### Configuration

The configuration is similar to [SLF4J Simple][slf4j-simple].

It looks for the `lambda-logger.properties` resource and read properties:

* **dateTimeFormat** - The date and time format to be used in the output messages. The pattern
  describing the date and time format is defined by [SimpleDateFormat][]. If the format is not
  specified or is invalid, the number of milliseconds since start up will be output.
* **defaultLogLevel** - Default log level for all instances of LambdaLogger.
  Must be one of (_trace_, _debug_, _info_, _warn_, _error_), a value is case-insensitive.
  If not specified, defaults to _info_.
* **levelInBrackets** - Should the level string be output in brackets? Defaults to `false`.
* **log.a.b.c** - Logging detail level for a LambdaLogger instance named _a.b.c_.
* **requestId** - Set the context name of AWS request ID. Defaults to `AWS_REQUEST_ID`.
* **showDateTime** - Set to `true` if you want the current date and time to be included in output
  messages. Defaults to `false`.
* **showLogName** - Set to `true` if you want the Logger instance name to be included in output
  messages. Defaults to `true`.
* **showShortLogName** - Set to `true` if you want the last component of the name to be included in
  output messages. Defaults to `false`.
* **showThreadId** - If you would like to output the current thread id, then set to `true`.
  Defaults to `false`.
* **showThreadName** - Set to `true` if you want to output the current thread name.
  Defaults to `false`.

The environment variables overrides the properties: **LOG_AWS_REQUEST_ID**,
**LOG_DATE_TIME_FORMAT**, **LOG_DEFAULT_LEVEL**, **LOG_LEVEL_IN_BRACKETS**, **LOG_SHOW_DATE_TIME**,
**LOG_SHOW_NAME**, **LOG_SHOW_SHORT_NAME**, **LOG_SHOW_THREAD_ID**, **LOG_SHOW_THREAD_NAME**.

More information you can find on site:

https://slf4j-aws-lambda.bot-by.uk/

## Other solutions

Other AWS centric loggers are [SLF4J/Logback Appender][awslambda-logback], [slf4j-simple-lambda][],
Logback's [CloudWatch appender][cloudwatch-appender] and [CloudWatchLogs Java appender][cloudwatchlogs-java-appender].

See also how to [setup a provider by a system property][setup-provider]

## Contributing

Please read [Contributing](contributing.md).

## History

See [Changelog](changelog.md)

## Authors and acknowledgment

- Replace the custom output to stdout with AWS LambdaLogger, the main idea from @igorakkerman's [SLF4J/Logback Appender][awslambda-logback].


## License

Copyright 2022-2023 Vitalij Berdinskih

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

[Apache License v2.0](LICENSE)  
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0.html)

[SLF4J]: https://www.slf4j.org/

[lambda]: https://aws.amazon.com/lambda/

[cloudwatch]: https://aws.amazon.com/cloudwatch/

[mdc]: https://www.slf4j.org/manual.html#mdc "Mapped Diagnostic Context (MDC)"

[marker]: https://www.slf4j.org/apidocs/org/slf4j/Marker.html

[aws-lambda-logging-problem]: https://frank-afriat.medium.com/solving-the-java-aws-lambda-logging-problem-305b06df457f

[manual]: https://www.slf4j.org/manual.html "SLF4J user manual"

[slf4j-simple]: https://www.slf4j.org/api/org/slf4j/simple/SimpleLogger.html

[SimpleDateFormat]: https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/text/SimpleDateFormat.html

[awslambda-logback]: https://github.com/jlib-framework/jlib-awslambda-logback

[slf4j-simple-lambda]: https://github.com/microlam-io/slf4j-simple-lambda

[cloudwatch-appender]: https://github.com/sndyuk/logback-more-appenders

[cloudwatchlogs-java-appender]: https://github.com/boxfuse/cloudwatchlogs-java-appender

[setup-provider]: https://slf4j-aws-lambda.bot-by.uk/setup-provider.html
