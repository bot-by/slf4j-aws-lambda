# Usage

There is a great original [manual][].

The sample code, see the folders **[example-lambda][]** or **[example-lambda-json][]**:

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

The log with **slf4j-aws-lambda-logger**:

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

There is a JSON option with **slf4j-aws-lambda-json-logger**:
```language-json
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

![CloudWatch logs](cloudwatch-screenshot.png)

[manual]: https://www.slf4j.org/manual.html "SLF4J user manual"

[example-lambda]: https://gitlab.com/bot-by/slf4j-aws-lambda/-/tree/main/example-lambda "Demo AWS Lambda with SLF4J"

[example-lambda-json]: https://gitlab.com/bot-by/slf4j-aws-lambda/-/tree/main/example-lambda-json "Demo AWS Lambda with SLF4J, JSON Output"
