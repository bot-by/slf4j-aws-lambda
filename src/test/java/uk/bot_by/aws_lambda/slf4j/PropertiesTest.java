package uk.bot_by.aws_lambda.slf4j;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.helpers.BasicMarkerFactory;

@Tag("slow")
class PropertiesTest {

  private ByteArrayOutputStream outputStream = new ByteArrayOutputStream(100);
  private PrintStream printStream = new PrintStream(outputStream);
  private Marker markerA;
  private Marker markerB;
  private Marker markerC;

  @BeforeEach
  void setUp() {
    outputStream = new ByteArrayOutputStream(100);
    printStream = new PrintStream(outputStream);
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
        new LambdaLoggerFactory("parent-log-level-" + levelName + ".properties"));

    doReturn(printStream).when(loggerFactory).getPrintStream();

    // when
    var logger = loggerFactory.getLogger("org.test.Class");

    // then
    logger.isTraceEnabled();
    assertAll("Check parent log level",
        () -> assertThat("trace", logger.isTraceEnabled(), is(traceEnabled)),
        () -> assertThat("trace", logger.isDebugEnabled(), is(debugEnabled)),
        () -> assertThat("trace", logger.isInfoEnabled(), is(infoEnabled)),
        () -> assertThat("trace", logger.isWarnEnabled(), is(warnEnabled)),
        () -> assertThat("trace", logger.isErrorEnabled(), is(errorEnabled)));
  }

  @DisplayName("Parent log level with a marker")
  @Test
  void parentLogLevelWithMarker() {
    // given
    var loggerFactory = spy(new LambdaLoggerFactory("parent-log-level-marker.properties"));
    var marker = new BasicMarkerFactory().getMarker("iAmMarker");

    doReturn(printStream).when(loggerFactory).getPrintStream();

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
    var loggerFactory = spy(new LambdaLoggerFactory("parent-log-level-multi.properties"));

    doReturn(printStream).when(loggerFactory).getPrintStream();

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
        new LambdaLoggerFactory("class-log-level-" + levelName + ".properties"));

    doReturn(printStream).when(loggerFactory).getPrintStream();

    // when
    var logger = loggerFactory.getLogger("org.test.Class");

    // then
    logger.isTraceEnabled();
    assertAll("Check class log level",
        () -> assertThat("trace", logger.isTraceEnabled(), is(traceEnabled)),
        () -> assertThat("trace", logger.isDebugEnabled(), is(debugEnabled)),
        () -> assertThat("trace", logger.isInfoEnabled(), is(infoEnabled)),
        () -> assertThat("trace", logger.isWarnEnabled(), is(warnEnabled)),
        () -> assertThat("trace", logger.isErrorEnabled(), is(errorEnabled)));
  }

  @DisplayName("Class log level with a marker")
  @Test
  void classLogLevelWithMarker() {
    // given
    var loggerFactory = spy(new LambdaLoggerFactory("class-log-level-marker.properties"));
    var marker = new BasicMarkerFactory().getMarker("iAmMarker");

    doReturn(printStream).when(loggerFactory).getPrintStream();

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
    var loggerFactory = spy(new LambdaLoggerFactory("class-log-level-multi.properties"));

    doReturn(printStream).when(loggerFactory).getPrintStream();

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
        new LambdaLoggerFactory("default-log-level-" + levelName + ".properties"));

    doReturn(printStream).when(loggerFactory).getPrintStream();

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
    var loggerFactory = spy(new LambdaLoggerFactory("default-log-level-marker.properties"));
    var marker = new BasicMarkerFactory().getMarker("iAmMarker");

    doReturn(printStream).when(loggerFactory).getPrintStream();

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
    var loggerFactory = spy(new LambdaLoggerFactory("default-log-level-multi.properties"));

    doReturn(printStream).when(loggerFactory).getPrintStream();

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
    var loggerFactory = spy(new LambdaLoggerFactory("custom-separators.properties"));

    doReturn(printStream).when(loggerFactory).getPrintStream();

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
    var loggerFactory = spy(LambdaLoggerFactory.class);

    doReturn(printStream).when(loggerFactory).getPrintStream();

    MDC.put("request#", "properties-request-id");

    // when
    loggerFactory.getLogger("lambda.logger.test").debug("debug message");

    // then
    printStream.flush();
    printStream.close();
    outputStream.toString(StandardCharsets.UTF_8);
    assertThat(outputStream.toString(StandardCharsets.UTF_8), matchesPattern(
        "properties-request-id \\d{2}:\\d{2}:\\d{2}\\.\\d{3} \\[main\\] thread=1 \\[DEBUG\\] test - debug message[\\n\\r]+"));
  }

  @DisplayName("Try to read missed logger properties file, use default values")
  @Test
  void missedProperties() {
    // given
    var loggerFactory = spy(new LambdaLoggerFactory("missed.properties"));

    doReturn(printStream).when(loggerFactory).getPrintStream();

    MDC.put("request#", "properties-request-id");

    // when
    var logger = loggerFactory.getLogger("lambda.logger.test");

    logger.debug("debug message");
    logger.info("info message");

    // then
    printStream.flush();
    printStream.close();
    outputStream.toString(StandardCharsets.UTF_8);
    assertThat(outputStream.toString(StandardCharsets.UTF_8),
        matchesPattern("INFO lambda.logger.test - info message[\\n\\r]+"));
  }

}
