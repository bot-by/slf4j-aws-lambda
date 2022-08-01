The expected log lines

```log
START RequestId: d78368cd-de47-43d5-b690-02586951ec84 Version: $LATEST
d78368cd-de47-43d5-b690-02586951ec84 WARN uk.bot_by.bot.slf4j_demo.BotHandler - warning message
d78368cd-de47-43d5-b690-02586951ec84 ERROR uk.bot_by.bot.slf4j_demo.BotHandler - error message
d78368cd-de47-43d5-b690-02586951ec84 TRACE uk.bot_by.bot.slf4j_demo.BotHandler - CRLF injection
d78368cd-de47-43d5-b690-02586951ec84 TRACE uk.bot_by.bot.slf4j_demo.BotHandler - CRLF injection
d78368cd-de47-43d5-b690-02586951ec84 TRACE uk.bot_by.bot.slf4j_demo.BotHandler - CRLF injection
d78368cd-de47-43d5-b690-02586951ec84 WARN uk.bot_by.bot.slf4j_demo.BotHandler - printable stacktrace java.lang.Throwable: Printable Stacktrace Demo at uk.bot_by.bot.slf4j_demo.BotHandler.handleRequest(BotHandler.java:35) at uk.bot_by.bot.slf4j_demo.BotHandler.handleRequest(BotHandler.java:13) at lambdainternal.EventHandlerLoader$PojoHandlerAsStreamHandler.handleRequest(EventHandlerLoader.java:199) at lambdainternal.EventHandlerLoader$2.call(EventHandlerLoader.java:899) at lambdainternal.AWSLambda.startRuntime(AWSLambda.java:268) at lambdainternal.AWSLambda.startRuntime(AWSLambda.java:206) at lambdainternal.AWSLambda.main(AWSLambda.java:200)
END RequestId: d78368cd-de47-43d5-b690-02586951ec84
```

The screenshot of CloudWatch log
![Maven Central](cloudwatch-screenshot.png)
