# SLF4J for AWS Lambda

An [SLF4J][] Logger implementation for [AWS Lambda][lambda].

[![Codacy Grade](https://app.codacy.com/project/badge/Grade/dda626a02daf464c94aa10955a6b8f6b)](https://www.codacy.com/gl/bot-by/slf4j-aws-lambda/dashboard?utm_source=gitlab.com&utm_medium=referral&utm_content=bot-by/slf4j-aws-lambda&utm_campaign=Badge_Grade)
[![Codacy Coverage](https://app.codacy.com/project/badge/Coverage/dda626a02daf464c94aa10955a6b8f6b)](https://www.codacy.com/gl/bot-by/slf4j-aws-lambda/dashboard?utm_source=gitlab.com&utm_medium=referral&utm_content=bot-by/slf4j-aws-lambda&utm_campaign=Badge_Coverage)

## Getting started

Yet another SLF4J Simple, isn't it?

No, it isn't. This implementation supports MDC to print out **AWS request ID** in start of every
logging record. This implementation supports Markers too.

And last point: it resolves the [CRLF issue](https://twitter.com/ben11kehoe/status/1264597451010433025),
you don't have to prepare logging messages and stacktraces to log them on CloudWatch Logs.

The sample code, see the folder **[example][]** :

```language-java
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

The log:

```language-log
START RequestId: 59e01d60-cdb2-4a5b-8cd7-4e9df7870b74 Version: $LATEST
59e01d60-cdb2-4a5b-8cd7-4e9df7870b74 WARN uk.bot_by.bot.slf4j_demo.BotHandler - warning message
59e01d60-cdb2-4a5b-8cd7-4e9df7870b74 ERROR uk.bot_by.bot.slf4j_demo.BotHandler - error message
59e01d60-cdb2-4a5b-8cd7-4e9df7870b74 TRACE uk.bot_by.bot.slf4j_demo.BotHandler - CRLF
injection
59e01d60-cdb2-4a5b-8cd7-4e9df7870b74 TRACE uk.bot_by.bot.slf4j_demo.BotHandler - CRLF
injection
59e01d60-cdb2-4a5b-8cd7-4e9df7870b74 TRACE uk.bot_by.bot.slf4j_demo.BotHandler - CRLF
injection
59e01d60-cdb2-4a5b-8cd7-4e9df7870b74 WARN uk.bot_by.bot.slf4j_demo.BotHandler - printable stacktrace
```
![CloudWatch logs](cloudwatch-screenshot.png)

The footprint of **slf4j-aws-lambda** (68K) is same size as **slf4j-simple** (64K) and much smaller
than **logback** (716K). Other AWS centric loggers are [jlib-framework/jlib-awslambda-logback][awslambda-logback],
Logback's [CloudWatch appender][cloudwatch-appender] and [CloudWatchLogs Java appender][cloudwatchlogs-appender].

[SLF4J]: https://www.slf4j.org/

[lambda]: https://aws.amazon.com/lambda/

[example]: https://gitlab.com/bot-by/slf4j-aws-lambda/-/tree/main/example "SLF4J for AWS Lambda Demo"

[awslambda-logback]: https://github.com/jlib-framework/jlib-awslambda-logback

[cloudwatch-appender]: https://github.com/sndyuk/logback-more-appenders

[cloudwatchlogs-java-appender]: https://github.com/boxfuse/cloudwatchlogs-java-appender
