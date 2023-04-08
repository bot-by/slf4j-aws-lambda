# Usage

There is a great original [manual][manual].

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

[example-lambda]: https://gitlab.com/bot-by/slf4j-aws-lambda/-/tree/main/example-lambda "SLF4J for AWS Lambda Demo"
