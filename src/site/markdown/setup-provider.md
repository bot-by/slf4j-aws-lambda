# Setup a provider by a system property

[SLF4J 2.0.9][slf4j.provider] allows a provider to be explicitly specified.
Since AWS Lambda Environment does not provide any configuration options to set what options
should be used on startup, the [trick is to use the JAVA_TOOL_OPTIONS][java-tool-options-trick]
environment variable.

If your AWS Java Lambda has some SLF4J providers and you want to point one of them,
that needs to setup a System property named `slf4j.provider`, as in the following:

![Using JAVA_TOOL_OPTIONS to setup a provider](environment-variable-screenshot.png)

Further reading [AWS Lambda, Developer Guide: Configuring environment variables][configuration-envvars]

[slf4j.provider]: https://jira.qos.ch/browse/SLF4J-450 "[SLF4J-450]: Allow binding to be explicitly specified"

[java-tool-options-trick]: https://zenidas.wordpress.com/recipes/system-properties-for-a-java-lambda-function/ "System properties for a Java Lambda function"

[configuration-envvars]: https://docs.aws.amazon.com/lambda/latest/dg/configuration-envvars.html "Using Lambda environment variables"