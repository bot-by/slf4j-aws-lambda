package uk.bot_by.aws_lambda.slf4j;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith({SystemStubsExtension.class})
@Tag("slow")
class EnvironmentVariablesTest {

  @SystemStub
  private EnvironmentVariables environment;

  @AfterEach
  void tearDown() {
    MDC.clear();
  }

  @DisplayName("Read logger properties from the environment, get logger then print out trace message")
  @Test
  void useEnvironmentVariables() {
    // given
    // override all properties
    environment.set("LOG_AWS_REQUEST_ID", "request-id");
    environment.set("LOG_SHOW_DATE_TIME", "false");
    environment.set("LOG_DEFAULT_LEVEL", "Trace");
    environment.set("LOG_LEVEL_IN_BRACKETS", "false");
    environment.set("LOG_SHOW_NAME", "false");
    environment.set("LOG_SHOW_SHORT_NAME", "false");
    environment.set("LOG_SHOW_THREAD_ID", "fasle");
    environment.set("LOG_SHOW_THREAD_NAME", "false");

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream(100);
    PrintStream printStream = new PrintStream(outputStream);
    LambdaLoggerFactory loggerFactory = spy(LambdaLoggerFactory.class);

    doReturn(printStream).when(loggerFactory).getPrintStream();

    MDC.put("request-id", "variables-request-id");

    // when
    loggerFactory.getLogger("lambda.logger.test").trace("trace message");

    // then
    printStream.flush();
    printStream.close();
    outputStream.toString(StandardCharsets.UTF_8);
    assertThat(outputStream.toString(StandardCharsets.UTF_8),
        matchesPattern("variables-request-id TRACE trace message[\\n\\r]+"));
  }

}
