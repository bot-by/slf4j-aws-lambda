# SLF4J for AWS Lambda

An [SLF4J][] Logger implementation for [AWS Lambda][lambda].

[![Codacy Grade](https://app.codacy.com/project/badge/Grade/dda626a02daf464c94aa10955a6b8f6b)](https://www.codacy.com/gl/bot-by/slf4j-aws-lambda/dashboard?utm_source=gitlab.com&utm_medium=referral&utm_content=bot-by/slf4j-aws-lambda&utm_campaign=Badge_Grade)
[![Codacy Coverage](https://app.codacy.com/project/badge/Coverage/dda626a02daf464c94aa10955a6b8f6b)](https://www.codacy.com/gl/bot-by/slf4j-aws-lambda/dashboard?utm_source=gitlab.com&utm_medium=referral&utm_content=bot-by/slf4j-aws-lambda&utm_campaign=Badge_Coverage)

## Getting started

Yet another SLF4J Simple, isn't it?

No, it isn't. This implementation supports MDC to print out **AWS request ID** in start of every
logging record. This implementation does not currently support Markers.

The sample code, see the folder **[example][]** :

```language-java
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

```language-log
START RequestId: cc4eb5aa-66b4-42fc-b27a-138bd672b38a Version: $LATEST
cc4eb5aa-66b4-42fc-b27a-138bd672b38a INFO uk.bot_by.bot.slf4j_demo.BotHandler - info message
cc4eb5aa-66b4-42fc-b27a-138bd672b38a WARN uk.bot_by.bot.slf4j_demo.BotHandler - warning message
cc4eb5aa-66b4-42fc-b27a-138bd672b38a ERROR uk.bot_by.bot.slf4j_demo.BotHandler - error message
END RequestId: cc4eb5aa-66b4-42fc-b27a-138bd672b38a
```

The footprint of **slf4j-aws-lambda** (68K) is same size as **slf4j-simple** (64K) and much smaller
than **logback** (716K).

[SLF4J]: https://www.slf4j.org/

[lambda]: https://aws.amazon.com/lambda/

[example]: https://gitlab.com/bot-by/slf4j-aws-lambda/-/tree/main/example "SLF4J for AWS Lambda Demo"
