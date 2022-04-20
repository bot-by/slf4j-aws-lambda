# AWS Lambda Java SLF4J

An [SLF4J][] Logger implementation for [AWS Lambda][lambda].

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/dda626a02daf464c94aa10955a6b8f6b)](https://www.codacy.com/gl/bot-by/aws-lambda-java-slf4j/dashboard?utm_source=gitlab.com&amp;utm_medium=referral&amp;utm_content=bot-by/aws-lambda-java-slf4j&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://app.codacy.com/project/badge/Coverage/dda626a02daf464c94aa10955a6b8f6b)](https://www.codacy.com/gl/bot-by/aws-lambda-java-slf4j/dashboard?utm_source=gitlab.com&utm_medium=referral&utm_content=bot-by/aws-lambda-java-slf4j&utm_campaign=Badge_Coverage)

## Getting started

Another yet SLF4J Simple, isn't it?

No, it isn't. This implementation supports MDC to print out **AWS Request Id** in start of every logging record.

The sample code, see the folder **[example](example)** :

```java
  @Override
  public String handleRequest(Map<String, Object> input, Context context) {
    MDC.put(LambdaLogger.AWS_REQUEST_ID, context.getAwsRequestId());
    logger.trace("trace message");
    logger.debug("debug message");
    logger.info("info message");
    logger.warn("warning message");
    logger.error("error message");
    return "done";
  }
```

The log:

```log
START RequestId: cc4eb5aa-66b4-42fc-b27a-138bd672b38a Version: $LATEST
cc4eb5aa-66b4-42fc-b27a-138bd672b38a INFO uk.bot_by.bot.slf4j_demo.BotHandler - info message
cc4eb5aa-66b4-42fc-b27a-138bd672b38a WARN uk.bot_by.bot.slf4j_demo.BotHandler - warning message
cc4eb5aa-66b4-42fc-b27a-138bd672b38a ERROR uk.bot_by.bot.slf4j_demo.BotHandler - error message
END RequestId: cc4eb5aa-66b4-42fc-b27a-138bd672b38a
REPORT RequestId: cc4eb5aa-66b4-42fc-b27a-138bd672b38a	Duration: 40.87 ms	Billed Duration: 41 ms	Memory Size: 512 MB	Max Memory Used: 81 MB	Init Duration: 471.56 ms
```

## Acquire

The package is not published to Maven Central yet.
Use bot-by's GitLab repository instead, please:

```xml
<repositories>
  <repository>
    <id>bot-by-maven</id>
    <url>https://gitlab.com/api/v4/groups/7239110/-/packages/maven</url>
  </repository>
</repositories>
```

Please add dependency to your project:

```xml
<dependency>
  <groupId>uk.bot-by</groupId>
  <artifactId>aws-lambda-java-slf4j</artifactId>
  <version>1.0.0</version>
</dependency>
```

## Usage

There is a great original [manual][manual].

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
