package uk.bot_by.aws_lambda.slf4j;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.mockito.Mockito.doReturn;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith({MockitoExtension.class, SystemStubsExtension.class})
@Tag("slow")
class PropertiesTest {

  @SystemStub
  private EnvironmentVariables environment;

  @Spy
  private LambdaLoggerFactory loggerFactory;

  @AfterEach
  void tearDown() {
    MDC.clear();
  }

  @DisplayName("Read logger properties from the file, get logger then print out debug message")
  @Test
  void useLoggerProperties() {
    // given
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream(100);
    PrintStream printStream = new PrintStream(outputStream);

    doReturn(printStream).when(loggerFactory).getPrintStream();

    MDC.put("AWS_REQUEST_ID", "properties-request-id");

    // when
    loggerFactory.getLogger("lambda.logger.test").debug("debug message");

    // then
    printStream.flush();
    printStream.close();
    outputStream.toString(StandardCharsets.UTF_8);
    assertThat(outputStream.toString(StandardCharsets.UTF_8), matchesPattern(
        "properties-request-id \\d{2}:\\d{2}:\\d{2}\\.\\d{3} \\[main\\] thread=1 \\[DEBUG\\] test - debug message[\\n\\r]+"));
  }

}
