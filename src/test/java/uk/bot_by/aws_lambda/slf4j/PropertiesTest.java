package uk.bot_by.aws_lambda.slf4j;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.helpers.BasicMarkerFactory;

@ExtendWith(MockitoExtension.class)
@Tag("slow")
class PropertiesTest {

  @Mock
  private LambdaLogger lambdaLogger;
  @Captor
  private ArgumentCaptor<String> stringCaptor;

  private Marker markerA;
  private Marker markerB;
  private Marker markerC;

  @BeforeEach
  void setUp() {
    markerA = new BasicMarkerFactory().getMarker("iAmMarker");
    markerB = new BasicMarkerFactory().getMarker("important");
    markerC = new BasicMarkerFactory().getMarker("important");

  }

  @AfterEach
  void tearDown() {
    MDC.clear();
  }

  @DisplayName("Parent log level")
  @ParameterizedTest(name = "[{index}] {0}")
  @CsvSource({"trace,true,true,true,true,true", "debug,false,true,true,true,true",
      "info,false,false,true,true,true", "warn,false,false,false,true,true",
      "error,false,false,false,false,true", "none,false,false,false,false,false"})
  void parentLogLevel(String levelName, boolean traceEnabled, boolean debugEnabled,
      boolean infoEnabled, boolean warnEnabled, boolean errorEnabled) {
    // given
    var loggerFactory = spy(
        new AWSLambdaLoggerFactory("parent-log-level-" + levelName + ".properties"));

    doReturn(lambdaLogger).when(loggerFactory).getLambdaLogger();

    // when
    var logger = loggerFactory.getLogger("org.test.Class");

    // then
    logger.isTraceEnabled();
    assertAll("Check parent log level",
        () -> assertThat("trace", logger.isTraceEnabled(), is(traceEnabled)),
        () -> assertThat("debug", logger.isDebugEnabled(), is(debugEnabled)),
        () -> assertThat("info", logger.isInfoEnabled(), is(infoEnabled)),
        () -> assertThat("warn", logger.isWarnEnabled(), is(warnEnabled)),
        () -> assertThat("error", logger.isErrorEnabled(), is(errorEnabled)));
  }

  @DisplayName("Parent log level with a marker")
  @Test
  void parentLogLevelWithMarker() {
    // given
    var loggerFactory = spy(new AWSLambdaLoggerFactory("parent-log-level-marker.properties"));
    var marker = new BasicMarkerFactory().getMarker("iAmMarker");

    doReturn(lambdaLogger).when(loggerFactory).getLambdaLogger();

    // when
    var logger = loggerFactory.getLogger("org.test.Class");

    // then
    logger.isTraceEnabled();
    assertAll("Check parent log level", () -> assertFalse(logger.isTraceEnabled(), "trace"),
        () -> assertFalse(logger.isDebugEnabled(), "debug"),
        () -> assertFalse(logger.isInfoEnabled(), "info"),
        () -> assertFalse(logger.isWarnEnabled(), "warn"),
        () -> assertFalse(logger.isErrorEnabled(), "error"),
        () -> assertTrue(logger.isWarnEnabled(marker), "debug with a marker"));
  }

  @DisplayName("Parent log multi-levels with multi-markers")
  @Test
  void parentLogLevelMulti() {
    // given
    var loggerFactory = spy(new AWSLambdaLoggerFactory("parent-log-level-multi.properties"));

    doReturn(lambdaLogger).when(loggerFactory).getLambdaLogger();

    // when
    var logger = loggerFactory.getLogger("org.test.Class");

    // then
    logger.isTraceEnabled();
    assertAll("Check parent log level", () -> assertFalse(logger.isTraceEnabled(), "trace"),
        () -> assertFalse(logger.isDebugEnabled(), "debug"),
        () -> assertFalse(logger.isInfoEnabled(), "info"),
        () -> assertTrue(logger.isWarnEnabled(), "warn"),
        () -> assertTrue(logger.isErrorEnabled(), "error"),
        () -> assertTrue(logger.isInfoEnabled(markerA), "info with a marker"),
        () -> assertTrue(logger.isTraceEnabled(markerB), "trace with the marker B"),
        () -> assertTrue(logger.isTraceEnabled(markerC), "trace with the marker C"));
  }

  @DisplayName("Class log level")
  @ParameterizedTest(name = "[{index}] {0}")
  @CsvSource({"trace,true,true,true,true,true", "debug,false,true,true,true,true",
      "info,false,false,true,true,true", "warn,false,false,false,true,true",
      "error,false,false,false,false,true", "none,false,false,false,false,false"})
  void classLogLevel(String levelName, boolean traceEnabled, boolean debugEnabled,
      boolean infoEnabled, boolean warnEnabled, boolean errorEnabled) {
    // given
    var loggerFactory = spy(
        new AWSLambdaLoggerFactory("class-log-level-" + levelName + ".properties"));

    doReturn(lambdaLogger).when(loggerFactory).getLambdaLogger();

    // when
    var logger = loggerFactory.getLogger("org.test.Class");

    // then
    logger.isTraceEnabled();
    assertAll("Check class log level",
        () -> assertThat("trace", logger.isTraceEnabled(), is(traceEnabled)),
        () -> assertThat("debug", logger.isDebugEnabled(), is(debugEnabled)),
        () -> assertThat("info", logger.isInfoEnabled(), is(infoEnabled)),
        () -> assertThat("warn", logger.isWarnEnabled(), is(warnEnabled)),
        () -> assertThat("error", logger.isErrorEnabled(), is(errorEnabled)));
  }

  @DisplayName("Class log level with a marker")
  @Test
  void classLogLevelWithMarker() {
    // given
    var loggerFactory = spy(new AWSLambdaLoggerFactory("class-log-level-marker.properties"));
    var marker = new BasicMarkerFactory().getMarker("iAmMarker");

    doReturn(lambdaLogger).when(loggerFactory).getLambdaLogger();

    // when
    var logger = loggerFactory.getLogger("org.test.Class");

    // then
    logger.isTraceEnabled();
    assertAll("Check class log level", () -> assertFalse(logger.isTraceEnabled(), "trace"),
        () -> assertFalse(logger.isDebugEnabled(), "debug"),
        () -> assertFalse(logger.isInfoEnabled(), "info"),
        () -> assertFalse(logger.isWarnEnabled(), "warn"),
        () -> assertFalse(logger.isErrorEnabled(), "error"),
        () -> assertTrue(logger.isInfoEnabled(marker), "debug with a marker"));
  }

  @DisplayName("Class log multi-levels with multi-markers")
  @Test
  void classLogLevelMulti() {
    // given
    var loggerFactory = spy(new AWSLambdaLoggerFactory("class-log-level-multi.properties"));

    doReturn(lambdaLogger).when(loggerFactory).getLambdaLogger();

    // when
    var logger = loggerFactory.getLogger("org.test.Class");

    // then
    logger.isTraceEnabled();
    assertAll("Check class log level", () -> assertFalse(logger.isTraceEnabled(), "trace"),
        () -> assertFalse(logger.isDebugEnabled(), "debug"),
        () -> assertFalse(logger.isInfoEnabled(), "info"),
        () -> assertTrue(logger.isWarnEnabled(), "warn"),
        () -> assertTrue(logger.isErrorEnabled(), "error"),
        () -> assertTrue(logger.isInfoEnabled(markerA), "info with a marker"),
        () -> assertTrue(logger.isTraceEnabled(markerB), "trace with the marker B"),
        () -> assertTrue(logger.isTraceEnabled(markerC), "trace with the marker C"));
  }

  @DisplayName("Default log level")
  @ParameterizedTest(name = "[{index}] {0}")
  @CsvSource({"trace,true,true,true,true,true", "debug,false,true,true,true,true",
      "info,false,false,true,true,true", "warn,false,false,false,true,true",
      "error,false,false,false,false,true", "none,false,false,false,false,false"})
  void defaultLogLevel(String levelName, boolean traceEnabled, boolean debugEnabled,
      boolean infoEnabled, boolean warnEnabled, boolean errorEnabled) {
    // given
    var loggerFactory = spy(
        new AWSLambdaLoggerFactory("default-log-level-" + levelName + ".properties"));

    doReturn(lambdaLogger).when(loggerFactory).getLambdaLogger();

    // when
    var logger = loggerFactory.getLogger(levelName + " test");

    // then
    logger.isTraceEnabled();
    assertAll("Check default log level",
        () -> assertThat("trace", logger.isTraceEnabled(), is(traceEnabled)),
        () -> assertThat("debug", logger.isDebugEnabled(), is(debugEnabled)),
        () -> assertThat("info", logger.isInfoEnabled(), is(infoEnabled)),
        () -> assertThat("warn", logger.isWarnEnabled(), is(warnEnabled)),
        () -> assertThat("error", logger.isErrorEnabled(), is(errorEnabled)));
  }

  @DisplayName("Default log level with a marker")
  @Test
  void defaultLogLevelWithMarker() {
    // given
    var loggerFactory = spy(new AWSLambdaLoggerFactory("default-log-level-marker.properties"));
    var marker = new BasicMarkerFactory().getMarker("iAmMarker");

    doReturn(lambdaLogger).when(loggerFactory).getLambdaLogger();

    // when
    var logger = loggerFactory.getLogger("marker test");

    // then
    logger.isTraceEnabled();
    assertAll("Check default log level", () -> assertFalse(logger.isTraceEnabled(), "trace"),
        () -> assertFalse(logger.isDebugEnabled(), "debug"),
        () -> assertFalse(logger.isInfoEnabled(), "info"),
        () -> assertFalse(logger.isWarnEnabled(), "warn"),
        () -> assertFalse(logger.isErrorEnabled(), "error"),
        () -> assertTrue(logger.isDebugEnabled(marker), "debug with a marker"));
  }

  @DisplayName("Default log multi-levels with multi-markers")
  @Test
  void defaultLogLevelMulti() {
    // given
    var loggerFactory = spy(new AWSLambdaLoggerFactory("default-log-level-multi.properties"));

    doReturn(lambdaLogger).when(loggerFactory).getLambdaLogger();

    // when
    var logger = loggerFactory.getLogger("org.test.Class");

    // then
    logger.isTraceEnabled();
    assertAll("Check default log level", () -> assertFalse(logger.isTraceEnabled(), "trace"),
        () -> assertFalse(logger.isDebugEnabled(), "debug"),
        () -> assertFalse(logger.isInfoEnabled(), "info"),
        () -> assertTrue(logger.isWarnEnabled(), "warn"),
        () -> assertTrue(logger.isErrorEnabled(), "error"),
        () -> assertTrue(logger.isInfoEnabled(markerA), "info with a marker"),
        () -> assertTrue(logger.isTraceEnabled(markerB), "trace with the marker B"),
        () -> assertTrue(logger.isTraceEnabled(markerC), "trace with the marker C"));
  }

  @DisplayName("Custom level and marker separators")
  @Test
  void customLevelAndMarkerSeparators() {
    // given
    var loggerFactory = spy(new AWSLambdaLoggerFactory("custom-separators.properties"));

    doReturn(lambdaLogger).when(loggerFactory).getLambdaLogger();

    // when
    var logger = loggerFactory.getLogger("org.test.Class");

    // then
    logger.isTraceEnabled();
    assertAll("Check custom level and marker separators",
        () -> assertFalse(logger.isTraceEnabled(), "trace"),
        () -> assertFalse(logger.isDebugEnabled(), "debug"),
        () -> assertFalse(logger.isInfoEnabled(), "info"),
        () -> assertTrue(logger.isWarnEnabled(), "warn"),
        () -> assertTrue(logger.isErrorEnabled(), "error"),
        () -> assertTrue(logger.isInfoEnabled(markerA), "info with a marker"),
        () -> assertTrue(logger.isTraceEnabled(markerB), "trace with the marker B"),
        () -> assertTrue(logger.isTraceEnabled(markerC), "trace with the marker C"));
  }

  @DisplayName("Read logger properties from the file, get logger then print out debug message")
  @Test
  void useLoggerProperties() {
    // given
    var loggerFactory = spy(AWSLambdaLoggerFactory.class);

    doReturn(lambdaLogger).when(loggerFactory).getLambdaLogger();

    MDC.put("request#", "properties-request-id");

    // when
    loggerFactory.getLogger("lambda.logger.test").debug("debug message");

    // then
    verify(lambdaLogger).log(stringCaptor.capture());

    assertThat(stringCaptor.getValue(), matchesPattern(
        "properties-request-id \\d{2}:\\d{2}:\\d{2}\\.\\d{3} \\[main\\] thread=1 \\[DEBUG\\] test - debug message"));
  }

  @DisplayName("Try to read missed logger properties file, use default values")
  @Test
  void missedProperties() {
    // given
    var loggerFactory = spy(new AWSLambdaLoggerFactory("missed.properties"));

    doReturn(lambdaLogger).when(loggerFactory).getLambdaLogger();

    MDC.put("request#", "properties-request-id");

    var logger = loggerFactory.getLogger("lambda.logger.test");

    // when
    logger.debug("debug message");
    logger.info("info message");

    // then
    verify(lambdaLogger).log(stringCaptor.capture());

    assertEquals("INFO lambda.logger.test - info message", stringCaptor.getValue());
  }

  @DisplayName("Wrong a date-time format")
  @Test
  void wrongDateTimeFormat() {
    // given
    var loggerFactory = spy(new AWSLambdaLoggerFactory("wrong-date-time-format.properties"));

    doReturn(lambdaLogger).when(loggerFactory).getLambdaLogger();

    MDC.put("request#", "properties-request-id");

    // when
    loggerFactory.getLogger("lambda.logger.test").info("info message");

    // then
    verify(lambdaLogger).log(stringCaptor.capture());

    assertThat(stringCaptor.getValue(), matchesPattern(
        "properties-request-id \\d+ \\[main\\] thread=1 \\[INFO\\] test - info message"));
  }

  @DisplayName("Wrong the default logger level")
  @Test
  void wrongDefaultLoggerLevel() {
    // given
    var loggerFactory = spy(new AWSLambdaLoggerFactory("wrong-default-logger-level.properties"));

    doReturn(lambdaLogger).when(loggerFactory).getLambdaLogger();

    MDC.put("request#", "properties-request-id");

    var logger = loggerFactory.getLogger("lambda.logger.test");

    // when
    logger.debug("debug message");
    logger.info("info message");

    // then
    verify(lambdaLogger).log(stringCaptor.capture());

    assertEquals("properties-request-id [main] thread=1 [INFO] test - info message",
        stringCaptor.getValue());
  }

  @DisplayName("Wrong a logger level")
  @Test
  void wrongLoggerLevel() {
    // given
    var loggerFactory = spy(new AWSLambdaLoggerFactory("wrong-logger-level.properties"));

    doReturn(lambdaLogger).when(loggerFactory).getLambdaLogger();

    MDC.put("request#", "properties-request-id");

    var logger = loggerFactory.getLogger("org.test.Class");

    // when
    logger.trace("trace message");
    logger.debug("debug message");

    // then
    verify(lambdaLogger).log(stringCaptor.capture());

    assertEquals("properties-request-id [main] thread=1 [DEBUG] Class - debug message",
        stringCaptor.getValue());
  }

}
