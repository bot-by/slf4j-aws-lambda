# Usage

There is a great original [manual][].

The sample code, see the folder **[example-lambda][]** :

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
![CloudWatch logs](cloudwatch-screenshot.png)

[manual]: https://www.slf4j.org/manual.html "SLF4J user manual"

[example-lambda]: https://gitlab.com/bot-by/slf4j-aws-lambda/-/tree/main/example-lambda "SLF4J for AWS Lambda Demo"
