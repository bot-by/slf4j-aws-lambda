# SLF4J for AWS Lambda

An [SLF4J][] Logger implementation for [AWS Lambda][lambda].

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/dda626a02daf464c94aa10955a6b8f6b)](https://www.codacy.com/gl/bot-by/slf4j-aws-lambda/dashboard?utm_source=gitlab.com&utm_medium=referral&utm_content=bot-by/slf4j-aws-lambda&utm_campaign=Badge_Grade)
[![Codacy Badge](https://app.codacy.com/project/badge/Coverage/dda626a02daf464c94aa10955a6b8f6b)](https://www.codacy.com/gl/bot-by/slf4j-aws-lambda/dashboard?utm_source=gitlab.com&utm_medium=referral&utm_content=bot-by/slf4j-aws-lambda&utm_campaign=Badge_Coverage)

## Getting started

Yet another SLF4J Simple, isn't it?

No, it isn't. This implementation supports MDC to print out **AWS request ID** in start of every
logging record.

The sample code, see the folder **[example](example)** :

```java
  @Override
  public String handleRequest(Map<String, Object> input,Context context){
    MDC.put(LambdaLogger.AWS_REQUEST_ID,context.getAwsRequestId());
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
  <artifactId>slf4j-aws-lambda</artifactId>
  <version>1.0.0</version>
</dependency>
```

## Usage

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
* **showDateTime** - Set to `true` if you want the current date and time to be included in output
  messages. Default is `false`.
* **showLogName** - Set to `true` if you want the Logger instance name to be included in output
  messages. Defaults to `true`.
* **showShortLogName** - Set to `true` if you want the last component of the name to be included in
  output messages. Defaults to `false`.
* **showThreadId** - If you would like to output the current thread id, then set to `true`.
  Defaults to `false`.
* **showThreadName** - Set to `true` if you want to output the current thread name.
  Defaults to `false`.

The environment variables overrides the properties: **LOG_DATE_TIME_FORMAT**, **LOG_DEFAULT_LEVEL**,
**LOG_LEVEL_IN_BRACKETS**, **LOG_SHOW_DATE_TIME**, **LOG_SHOW_NAME**, **LOG_SHOW_SHORT_NAME**,
**LOG_SHOW_THREAD_ID**, **LOG_SHOW_THREAD_NAME**.

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

[slf4j-simple]: https://www.slf4j.org/api/org/slf4j/simple/SimpleLogger.html

[manual]: https://www.slf4j.org/manual.html

[SimpleDateFormat]: https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/text/SimpleDateFormat.html
