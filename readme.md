# SLF4J for AWS Lambda

An [SLF4J][] Logger implementation for [AWS Lambda][lambda].

[![Codacy Grade](https://app.codacy.com/project/badge/Grade/dda626a02daf464c94aa10955a6b8f6b)](https://www.codacy.com/gl/bot-by/slf4j-aws-lambda/dashboard?utm_source=gitlab.com&utm_medium=referral&utm_content=bot-by/slf4j-aws-lambda&utm_campaign=Badge_Grade)
[![Codacy Coverage](https://app.codacy.com/project/badge/Coverage/dda626a02daf464c94aa10955a6b8f6b)](https://www.codacy.com/gl/bot-by/slf4j-aws-lambda/dashboard?utm_source=gitlab.com&utm_medium=referral&utm_content=bot-by/slf4j-aws-lambda&utm_campaign=Badge_Coverage)
[![Maven Central](https://img.shields.io/maven-central/v/uk.bot-by/slf4j-aws-lambda)](https://search.maven.org/artifact/uk.bot-by/slf4j-aws-lambda)
[![Javadoc](https://javadoc.io/badge2/uk.bot-by/slf4j-aws-lambda/javadoc.svg)](https://javadoc.io/doc/uk.bot-by/slf4j-aws-lambda)

## Getting started

Yet another SLF4J Simple, isn't it?

No, it isn't. This implementation supports MDC to print out **AWS request ID** in start of every
logging record. This implementation supports Markers too.

And last point: it resolves the [CRLF issue](https://twitter.com/ben11kehoe/status/1264597451010433025),
you don't have to prepare logging messages and stacktraces to log them on CloudWatch Logs.

The sample code, see the folder **[example](example)** :

```java
@Override
public String handleRequest(Map<String, Object> input,Context context) {
    MDC.put("AWS_REQUEST_ID", context.getAwsRequestId());
    logger.trace("trace message");
    logger.debug("debug message");
    logger.info("info message");
    logger.warn("warning message");
    logger.error("error message");
    return"done";
}
```

The log:

```log
START RequestId: cc4eb5aa-66b4-42fc-b27a-138bd672b38a Version: $LATEST
cc4eb5aa-66b4-42fc-b27a-138bd672b38a INFO uk.bot_by.bot.slf4j_demo.BotHandler - info message
cc4eb5aa-66b4-42fc-b27a-138bd672b38a WARN uk.bot_by.bot.slf4j_demo.BotHandler - warning message
cc4eb5aa-66b4-42fc-b27a-138bd672b38a ERROR uk.bot_by.bot.slf4j_demo.BotHandler - error message
END RequestId: cc4eb5aa-66b4-42fc-b27a-138bd672b38a
```

The footprint of **slf4j-aws-lambda** (68K) is same size as **slf4j-simple** (64K) and much smaller
than **logback** (716K). Other AWS centric loggers are [jlib-framework/jlib-awslambda-logback][awslambda-logback],
Logback's [CloudWatch appender][cloudwatch-appender] and [CloudWatchLogs Java appender][cloudwatchlogs-java-appender].

There is a great original [manual][manual].

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

## Contributing

Please read [Contributing](contributing.md).

## History

See [Changelog](changelog.md)

## Authors and acknowledgment

Show your appreciation to those who have contributed to the project.

## License

Copyright 2022 Witalij Berdinskich

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

[manual]: https://www.slf4j.org/manual.html

[slf4j-simple]: https://www.slf4j.org/api/org/slf4j/simple/SimpleLogger.html

[SimpleDateFormat]: https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/text/SimpleDateFormat.html

[awslambda-logback]: https://github.com/jlib-framework/jlib-awslambda-logback

[cloudwatch-appender]: https://github.com/sndyuk/logback-more-appenders

[cloudwatchlogs-java-appender]: https://github.com/boxfuse/cloudwatchlogs-java-appender
