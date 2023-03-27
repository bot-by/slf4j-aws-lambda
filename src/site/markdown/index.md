# SLF4J for AWS Lambda

An [SLF4J][] Logger implementation for [AWS Lambda][lambda].

[![Codacy Grade](https://app.codacy.com/project/badge/Grade/dda626a02daf464c94aa10955a6b8f6b)](https://www.codacy.com/gl/bot-by/slf4j-aws-lambda/dashboard?utm_source=gitlab.com&utm_medium=referral&utm_content=bot-by/slf4j-aws-lambda&utm_campaign=Badge_Grade)
[![Codacy Coverage](https://app.codacy.com/project/badge/Coverage/dda626a02daf464c94aa10955a6b8f6b)](https://www.codacy.com/gl/bot-by/slf4j-aws-lambda/dashboard?utm_source=gitlab.com&utm_medium=referral&utm_content=bot-by/slf4j-aws-lambda&utm_campaign=Badge_Coverage)

## Getting started

Yet another SLF4J Simple, isn't it?

No, it isn't.

This implementation supports [MDC][mdc] to print out **AWS request ID** in start of every logging
record and supports [Markers][marker] too. And the killer feature: it solves
the [CRLF issue](https://twitter.com/ben11kehoe/status/1264597451010433025) described by
Frank Afriat in [Solving the Java Aws Lambda logging problem][aws-lambda-logging-problem] - you
don't have to prepare logging messages and stacktraces to log them on CloudWatch Logs.

The footprint of **slf4j-aws-lambda** (88K) is same size as **slf4j-simple** (79K) and much smaller
than **logback** (888K).

* [Acquire][]
* [Usage][]
* [Configuration][]

[SLF4J]: https://www.slf4j.org/

[lambda]: https://aws.amazon.com/lambda/

[mdc]: https://www.slf4j.org/manual.html#mdc "Mapped Diagnostic Context (MDC)"

[marker]: https://www.slf4j.org/apidocs/org/slf4j/Marker.html

[aws-lambda-logging-problem]: https://frank-afriat.medium.com/solving-the-java-aws-lambda-logging-problem-305b06df457f

[Acquire]: acquire.html

[Usage]: usage.html

[Configuration]: configuration.html
