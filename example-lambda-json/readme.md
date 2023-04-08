# Demo AWS Lambda with SLF4J, JSON Output

## Usage

1. Build and deploy it to AWS. See the section [Deploy to Lambda][deploy-lambda] or do it manually.
2. Run a test event.

The expected log lines

```log
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

The screenshot of CloudWatch log
![Maven Central](cloudwatch-screenshot.png)

[deploy-lambda]: https://github.com/davidmoten/aws-maven-plugin#deploy-to-lambda
