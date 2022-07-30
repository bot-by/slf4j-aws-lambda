package uk.bot_by.aws_lambda.slf4j;

import static java.util.Objects.nonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEmptyString.emptyString;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.helpers.BasicMarkerFactory;
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
    environment.set("LOG_SHOW_THREAD_ID", "false");
    environment.set("LOG_SHOW_THREAD_NAME", "false");
    MDC.put("request-id", "variables-request-id");

    var outputStream = new ByteArrayOutputStream(100);
    var printStream = new PrintStream(outputStream);
    var loggerFactory = spy(LambdaLoggerFactory.class);

    doReturn(printStream).when(loggerFactory).getPrintStream();

    var logger = loggerFactory.getLogger("lambda.logger.test");

    // when
    logger.trace("trace message");

    // then
    printStream.flush();
    printStream.close();
    assertThat(outputStream.toString(StandardCharsets.UTF_8),
        matchesPattern("variables-request-id TRACE trace message[\\n\\r]+"));
  }

  @DisplayName("Default log level with a marker")
  @Test
  void defaultLogLevelWithMarker() {
    // given
    // override all properties
    environment.set("LOG_AWS_REQUEST_ID", "request-id");
    environment.set("LOG_SHOW_DATE_TIME", "false");
    environment.set("LOG_DEFAULT_LEVEL", "Trace@aMarker");
    environment.set("LOG_LEVEL_IN_BRACKETS", "false");
    environment.set("LOG_SHOW_NAME", "false");
    environment.set("LOG_SHOW_SHORT_NAME", "false");
    environment.set("LOG_SHOW_THREAD_ID", "false");
    environment.set("LOG_SHOW_THREAD_NAME", "false");
    MDC.put("request-id", "variables-request-id");

    var outputStream = new ByteArrayOutputStream(100);
    var printStream = new PrintStream(outputStream);
    var loggerFactory = spy(LambdaLoggerFactory.class);
    var marker = new BasicMarkerFactory().getMarker("aMarker");

    doReturn(printStream).when(loggerFactory).getPrintStream();

    var logger = loggerFactory.getLogger("lambda.logger.test");

    // when
    logger.trace(marker, "trace message");

    // then
    printStream.flush();
    printStream.close();
    assertThat(outputStream.toString(StandardCharsets.UTF_8),
        matchesPattern("variables-request-id TRACE trace message[\\n\\r]+"));
  }

  @DisplayName("Implement NONE/OFF level")
  @ParameterizedTest
  @NullSource
  @ValueSource(strings = "aMarker")
  void implementNoneLevel(String markerName) {
    // given
    // override all properties
    environment.set("LOG_AWS_REQUEST_ID", "request-id");
    environment.set("LOG_SHOW_DATE_TIME", "false");
    environment.set("LOG_DEFAULT_LEVEL", "Trace@none");
    environment.set("LOG_LEVEL_IN_BRACKETS", "false");
    environment.set("LOG_SHOW_NAME", "false");
    environment.set("LOG_SHOW_SHORT_NAME", "false");
    environment.set("LOG_SHOW_THREAD_ID", "false");
    environment.set("LOG_SHOW_THREAD_NAME", "false");
    MDC.put("request-id", "variables-request-id");

    var outputStream = new ByteArrayOutputStream(100);
    var printStream = new PrintStream(outputStream);
    var loggerFactory = spy(LambdaLoggerFactory.class);
    var marker = (Marker) null;

    if (nonNull(markerName)) {
      marker = new BasicMarkerFactory().getMarker("aMarker");
    }

    doReturn(printStream).when(loggerFactory).getPrintStream();

    var logger = loggerFactory.getLogger("lambda.logger.test");

    // when
    if (nonNull(marker)) {
      logger.trace(marker, "trace message");
    } else {
      logger.trace("trace message");
    }

    // then
    printStream.flush();
    printStream.close();
    outputStream.toString(StandardCharsets.UTF_8);
    assertThat(outputStream.toString(StandardCharsets.UTF_8), emptyString());
  }

}
