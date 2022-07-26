package uk.bot_by.aws_lambda.slf4j;

import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintStream;
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
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.BasicMarkerFactory;

@ExtendWith(MockitoExtension.class)
@Tag("fast")
class LambdaLoggerTest {

  @Captor
  private ArgumentCaptor<Marker> markerCaptor;
  @Mock
  private Marker marker;
  @Mock
  private PrintStream printStream;
  @Mock
  private Throwable throwable;

  private Marker knownMarker, markerWithReference, unknownMarker;

  @BeforeEach
  void setUp() {
    var markerFactory = new BasicMarkerFactory();

    knownMarker = markerFactory.getMarker("i-am-a-marker");
    markerWithReference = markerFactory.getMarker("marker-with-referenct");
    markerWithReference.add(knownMarker);
    unknownMarker = markerFactory.getMarker("i-am-an-unknown-marker");
  }

  @AfterEach
  void tearDown() {
    MDC.clear();
  }

  @DisplayName("Trace is enabled")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, false", "INFO, false", "WARN, false", "ERROR, false"})
  void isTraceEnabled(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("trace test logger")
        .loggerLevel(level).requestId("request#").build();
    Logger logger = new LambdaLogger(configuration, printStream);

    // when and then
    assertEquals(enabled, logger.isTraceEnabled());
  }

  @DisplayName("Marked trace is enabled")
  @ParameterizedTest
  @CsvSource(value = {"TRACE, i-am-a-marker, true", "TRACE, i-am-an-another-marker, false",
      "DEBUG, N/A, false", "INFO, N/A, false", "WARN, N/A, false",
      "ERROR, N/A, false"}, nullValues = "N/A")
  void isMarkedTraceEnabled(Level level, String markerName, boolean enabled) {
    // given
    if (nonNull(markerName)) {
      when(marker.getName()).thenReturn(markerName);
    }

    var configuration = LambdaLoggerConfiguration.builder().name("trace test logger")
        .loggerLevel(level, marker).requestId("request#").build();
    var logger = new LambdaLogger(configuration, printStream);

    // when and then
    assertAll("Marked trace is enabled",
        () -> assertEquals(enabled, logger.isTraceEnabled(knownMarker), "known marker"),
        () -> assertEquals(enabled, logger.isTraceEnabled(markerWithReference),
            "marker with reference"),
        () -> assertFalse(logger.isTraceEnabled(), "without any markers"),
        () -> assertFalse(logger.isTraceEnabled(unknownMarker), "unknown marker"));
  }

  @DisplayName("Trace message")
  @Test
  void trace() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("trace test logger")
        .loggerLevel(Level.TRACE).requestId("request#").build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger).log(isA(Level.class), anyString(), isNull());

    // when
    logger.trace("test trace message");

    // then
    verify(logger).log(Level.TRACE, "test trace message", null);
  }

  @DisplayName("Marked trace message")
  @Test
  void markedTrace() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("trace test logger")
        .loggerLevel(Level.TRACE, marker).requestId("request#").build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());

    // when
    logger.trace(knownMarker, "test marked trace message");

    // then
    verify(logger).log(eq(Level.TRACE), isA(Marker.class), eq("test marked trace message"),
        isNull());
  }

  @DisplayName("Trace formatted message with an argument")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, false", "INFO, false", "WARN, false", "ERROR, false"})
  void trace1(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("trace test logger")
        .loggerLevel(level).requestId("request#").build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), anyString(), isNull());
    }

    // when
    logger.trace("test trace message {}", "with an argument");

    // then
    if (enabled) {
      verify(logger).log(Level.TRACE, "test trace message with an argument", null);
    } else {
      verify(logger, never()).log(isA(Level.class), anyString(), any());
    }
  }

  @DisplayName("Marked trace formatted message with an argument")
  @ParameterizedTest
  @CsvSource(value = {"TRACE, i-am-a-marker, true", "TRACE, i-am-an-another-marker, false",
      "DEBUG, N/A, false", "INFO, N/A, false", "WARN, N/A, false",
      "ERROR, N/A, false"}, nullValues = "N/A")
  void markedTrace1(Level level, String markerName, boolean enabled) {
    // given
    if (nonNull(markerName)) {
      when(marker.getName()).thenReturn(markerName);
    }

    var configuration = LambdaLoggerConfiguration.builder().name("trace test logger")
        .loggerLevel(level, marker).requestId("request#").build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());
    }

    // when
    logger.trace(knownMarker, "test marked trace message {}", "with an argument");

    // then
    if (enabled) {
      verify(logger).log(Level.TRACE, knownMarker, "test marked trace message with an argument",
          null);
    } else {
      verify(logger, never()).log(isA(Level.class), isA(Marker.class), anyString(), any());
    }
  }

  @DisplayName("Trace formatted message with two arguments")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, false", "INFO, false", "WARN, false", "ERROR, false"})
  void trace2(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("trace test logger")
        .loggerLevel(level).requestId("request#").build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), anyString(), isNull());
    }

    // when
    logger.trace("test trace message {} {}", "with", "arguments");

    // then
    if (enabled) {
      verify(logger).log(Level.TRACE, "test trace message with arguments", null);
    } else {
      verify(logger, never()).log(isA(Level.class), anyString(), any());
    }
  }

  @DisplayName("Marked trace formatted message with two arguments")
  @ParameterizedTest
  @CsvSource(value = {"TRACE, i-am-a-marker, true", "TRACE, i-am-an-another-marker, false",
      "DEBUG, N/A, false", "INFO, N/A, false", "WARN, N/A, false",
      "ERROR, N/A, false"}, nullValues = "N/A")
  void markedTrace2(Level level, String markerName, boolean enabled) {
    // given
    if (nonNull(markerName)) {
      when(marker.getName()).thenReturn(markerName);
    }

    var configuration = LambdaLoggerConfiguration.builder().name("trace test logger")
        .loggerLevel(level, marker).requestId("request#").build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());
    }

    // when
    logger.trace(knownMarker, "test marked trace message {} {}", "with", "arguments");

    // then
    if (enabled) {
      verify(logger).log(Level.TRACE, knownMarker, "test marked trace message with arguments",
          null);
    } else {
      verify(logger, never()).log(isA(Level.class), isA(Marker.class), anyString(), any());
    }
  }

  @DisplayName("Trace formatted message with varargs")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, false", "INFO, false", "WARN, false", "ERROR, false"})
  void traceVarargs(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("trace test logger")
        .loggerLevel(level).requestId("request#").build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), anyString(), isNull());
    }

    // when
    logger.trace("test trace message {} {} {}", "with", "some", "arguments");

    // then
    if (enabled) {
      verify(logger).log(Level.TRACE, "test trace message with some arguments", null);
    } else {
      verify(logger, never()).log(isA(Level.class), anyString(), any());
    }
  }

  @DisplayName("Marked trace formatted message with varargs")
  @ParameterizedTest
  @CsvSource(value = {"TRACE, i-am-a-marker, true", "TRACE, i-am-an-another-marker, false",
      "DEBUG, N/A, false", "INFO, N/A, false", "WARN, N/A, false",
      "ERROR, N/A, false"}, nullValues = "N/A")
  void markedTraceVarargs(Level level, String markerName, boolean enabled) {
    // given
    if (nonNull(markerName)) {
      when(marker.getName()).thenReturn(markerName);
    }

    var configuration = LambdaLoggerConfiguration.builder().name("trace test logger")
        .loggerLevel(level, marker).requestId("request#").build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());
    }

    // when
    logger.trace(knownMarker, "test marked trace message {} {} {}", "with", "some", "arguments");

    // then
    if (enabled) {
      verify(logger).log(Level.TRACE, knownMarker, "test marked trace message with some arguments",
          null);
    } else {
      verify(logger, never()).log(isA(Level.class), isA(Marker.class), anyString(), any());
    }
  }

  @DisplayName("Trace message with a throwable")
  @Test
  void traceThrowable() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("trace test logger")
        .loggerLevel(Level.ERROR).requestId("request#").build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger).log(isA(Level.class), anyString(), isA(Throwable.class));

    // when
    logger.trace("test trace message", throwable);

    // then
    verify(logger).log(Level.TRACE, "test trace message", throwable);
  }

  @DisplayName("Marked trace message with a throwable")
  @Test
  void markedTraceThrowable() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("trace test logger")
        .loggerLevel(Level.TRACE, marker).requestId("request#").build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger)
        .log(isA(Level.class), isA(Marker.class), anyString(), isA(Throwable.class));

    // when
    logger.trace(knownMarker, "test marked trace message", throwable);

    // then
    verify(logger).log(eq(Level.TRACE), isA(Marker.class), eq("test marked trace message"),
        eq(throwable));
  }

  @DisplayName("Debug is enabled")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO, false", "WARN, false", "ERROR, false"})
  void isDebugEnabled(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("debug test logger")
        .loggerLevel(level).requestId("request#").build();
    Logger logger = new LambdaLogger(configuration, printStream);

    // when and then
    assertEquals(enabled, logger.isDebugEnabled());
  }

  @DisplayName("Marked debug is enabled")
  @ParameterizedTest
  @CsvSource(value = {"TRACE, i-am-a-marker, true", "DEBUG, i-am-an-another-marker, false",
      "DEBUG, i-am-a-marker, true", "INFO, N/A, false", "WARN, N/A, false",
      "ERROR, N/A, false"}, nullValues = "N/A")
  void isMarkedDebugEnabled(Level level, String markerName, boolean enabled) {
    // given
    if (nonNull(markerName)) {
      when(marker.getName()).thenReturn(markerName);
    }

    var configuration = LambdaLoggerConfiguration.builder().name("debug test logger")
        .loggerLevel(level, marker).requestId("request#").build();
    Logger logger = new LambdaLogger(configuration, printStream);

    // when and then
    assertAll("Marked debug is enabled",
        () -> assertEquals(enabled, logger.isDebugEnabled(knownMarker), "known marker"),
        () -> assertEquals(enabled, logger.isDebugEnabled(markerWithReference),
            "marker with reference"),
        () -> assertFalse(logger.isDebugEnabled(), "without any markers"),
        () -> assertFalse(logger.isDebugEnabled(unknownMarker), "unknown marker"));
  }

  @DisplayName("Debug message")
  @Test
  void debug() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("debug test logger")
        .loggerLevel(Level.DEBUG).requestId("request#").build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger).log(isA(Level.class), anyString(), isNull());

    // when
    logger.debug("test debug message");

    // then
    verify(logger).log(Level.DEBUG, "test debug message", null);
  }

  @DisplayName("Marked debug message")
  @Test
  void markedDebug() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("debug test logger")
        .loggerLevel(Level.DEBUG, marker).requestId("request#").build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());

    // when
    logger.debug(knownMarker, "test marked debug message");

    // then
    verify(logger).log(eq(Level.DEBUG), isA(Marker.class), eq("test marked debug message"),
        isNull());
  }

  @DisplayName("Debug formatted message with an argument")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO, false", "WARN, false", "ERROR, false"})
  void debug1(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("debug test logger")
        .loggerLevel(level).requestId("request#").build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), anyString(), isNull());
    }

    // when
    logger.debug("test debug message {}", "with an argument");

    // then
    if (enabled) {
      verify(logger).log(Level.DEBUG, "test debug message with an argument", null);
    } else {
      verify(logger, never()).log(isA(Level.class), anyString(), any());
    }
  }

  @DisplayName("Marked debug formatted message with an argument")
  @ParameterizedTest
  @CsvSource(value = {"TRACE, i-am-a-marker, true", "DEBUG, i-am-an-another-marker, false",
      "DEBUG, i-am-a-marker, true", "INFO, N/A, false", "WARN, N/A, false",
      "ERROR, N/A, false"}, nullValues = "N/A")
  void markedDebug1(Level level, String markerName, boolean enabled) {
    // given
    if (nonNull(markerName)) {
      when(marker.getName()).thenReturn(markerName);
    }

    var configuration = LambdaLoggerConfiguration.builder().name("debug test logger")
        .loggerLevel(level, marker).requestId("request#").build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());
    }

    // when
    logger.debug(knownMarker, "test marked debug message {}", "with an argument");

    // then
    if (enabled) {
      verify(logger).log(Level.DEBUG, knownMarker, "test marked debug message with an argument",
          null);
    } else {
      verify(logger, never()).log(isA(Level.class), isA(Marker.class), anyString(), any());
    }
  }

  @DisplayName("Debug formatted message with two arguments")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO, false", "WARN, false", "ERROR, false"})
  void debug2(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("debug test logger")
        .loggerLevel(level).requestId("request#").build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), anyString(), isNull());
    }

    // when
    logger.debug("test debug message {} {}", "with", "arguments");

    // then
    if (enabled) {
      verify(logger).log(Level.DEBUG, "test debug message with arguments", null);
    } else {
      verify(logger, never()).log(isA(Level.class), anyString(), any());
    }
  }

  @DisplayName("Marked debug formatted message with two arguments")
  @ParameterizedTest
  @CsvSource(value = {"TRACE, i-am-a-marker, true", "DEBUG, i-am-an-another-marker, false",
      "DEBUG, i-am-a-marker, true", "INFO, N/A, false", "WARN, N/A, false",
      "ERROR, N/A, false"}, nullValues = "N/A")
  void markedDebug2(Level level, String markerName, boolean enabled) {
    // given
    if (nonNull(markerName)) {
      when(marker.getName()).thenReturn(markerName);
    }

    var configuration = LambdaLoggerConfiguration.builder().name("debug test logger")
        .loggerLevel(level, marker).requestId("request#").build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());
    }

    // when
    logger.debug(knownMarker, "test marked debug message {} {}", "with", "arguments");

    // then
    if (enabled) {
      verify(logger).log(Level.DEBUG, knownMarker, "test marked debug message with arguments",
          null);
    } else {
      verify(logger, never()).log(isA(Level.class), isA(Marker.class), anyString(), any());
    }
  }

  @DisplayName("Debug formatted message with varargs")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO, false", "WARN, false", "ERROR, false"})
  void debugVarargs(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("debug test logger")
        .loggerLevel(level).requestId("request#").build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), anyString(), isNull());
    }

    // when
    logger.debug("test debug message {} {} {}", "with", "some", "arguments");

    // then
    if (enabled) {
      verify(logger).log(Level.DEBUG, "test debug message with some arguments", null);
    } else {
      verify(logger, never()).log(isA(Level.class), anyString(), any());
    }
  }

  @DisplayName("Marked debug formatted message with varargs")
  @ParameterizedTest
  @CsvSource(value = {"TRACE, i-am-a-marker, true", "DEBUG, i-am-an-another-marker, false",
      "DEBUG, i-am-a-marker, true", "INFO, N/A, false", "WARN, N/A, false",
      "ERROR, N/A, false"}, nullValues = "N/A")
  void markedDebugVarargs(Level level, String markerName, boolean enabled) {
    // given
    if (nonNull(markerName)) {
      when(marker.getName()).thenReturn(markerName);
    }

    var configuration = LambdaLoggerConfiguration.builder().name("debug test logger")
        .loggerLevel(level, marker).requestId("request#").build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());
    }

    // when
    logger.debug(knownMarker, "test marked debug message {} {} {}", "with", "some", "arguments");

    // then
    if (enabled) {
      verify(logger).log(Level.DEBUG, knownMarker, "test marked debug message with some arguments",
          null);
    } else {
      verify(logger, never()).log(isA(Level.class), isA(Marker.class), anyString(), any());
    }
  }

  @DisplayName("Debug message with a throwable")
  @Test
  void debugThrowable() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("debug test logger")
        .loggerLevel(Level.DEBUG).requestId("request#").build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger).log(isA(Level.class), anyString(), isA(Throwable.class));

    // when
    logger.debug("test debug message", throwable);

    // then
    verify(logger).log(Level.DEBUG, "test debug message", throwable);
  }

  @DisplayName("Marked debug message with a throwable")
  @Test
  void markedDebugThrowable() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("debug test logger")
        .loggerLevel(Level.DEBUG, marker).requestId("request#").build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger)
        .log(isA(Level.class), isA(Marker.class), anyString(), isA(Throwable.class));

    // when
    logger.debug(knownMarker, "test debug message", throwable);

    // then
    verify(logger).log(eq(Level.DEBUG), isA(Marker.class), eq("test debug message"), eq(throwable));
  }

  @DisplayName("Info is enabled")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO,  true", "WARN, false", "ERROR, false"})
  void isInfoEnabled(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("info test logger")
        .loggerLevel(level).requestId("request#").build();
    Logger logger = new LambdaLogger(configuration, printStream);

    // when and then
    assertEquals(enabled, logger.isInfoEnabled());
  }

  @DisplayName("Info message")
  @Test
  void info() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("info test logger")
        .loggerLevel(Level.INFO).requestId("request#").build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger).log(isA(Level.class), anyString(), isNull());

    // when
    logger.info("test info message");

    // then
    verify(logger).log(Level.INFO, "test info message", null);
  }

  @DisplayName("Info formatted message with an argument")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO, true", "WARN, false", "ERROR, false"})
  void info1(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("info test logger")
        .loggerLevel(level).requestId("request#").build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), anyString(), isNull());
    }

    // when
    logger.info("test info message {}", "with an argument");

    // then
    if (enabled) {
      verify(logger).log(Level.INFO, "test info message with an argument", null);
    } else {
      verify(logger, never()).log(isA(Level.class), anyString(), any());
    }
  }

  @DisplayName("Info formatted message with two arguments")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO, true", "WARN, false", "ERROR, false"})
  void info2(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("info test logger")
        .loggerLevel(level).requestId("request#").build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), anyString(), isNull());
    }

    // when
    logger.info("test info message {} {}", "with", "arguments");

    // then
    if (enabled) {
      verify(logger).log(Level.INFO, "test info message with arguments", null);
    } else {
      verify(logger, never()).log(isA(Level.class), anyString(), any());
    }
  }

  @DisplayName("Info formatted message with varargs")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO, true", "WARN, false", "ERROR, false"})
  void infoVarargs(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("info test logger")
        .loggerLevel(level).requestId("request#").build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), anyString(), isNull());
    }

    // when
    logger.info("test info message {} {} {}", "with", "some", "arguments");

    // then
    if (enabled) {
      verify(logger).log(Level.INFO, "test info message with some arguments", null);
    } else {
      verify(logger, never()).log(isA(Level.class), anyString(), any());
    }
  }

  @DisplayName("Info message with a throwable")
  @Test
  void infoThrowable() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("info test logger")
        .loggerLevel(Level.ERROR).requestId("request#").build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger).log(isA(Level.class), anyString(), isA(Throwable.class));

    // when
    logger.info("test info message", throwable);

    // then
    verify(logger).log(Level.INFO, "test info message", throwable);
  }

  @DisplayName("Warn is enabled")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO,  true", "WARN,  true", "ERROR, false"})
  void isWarnEnabled(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("warning test logger")
        .loggerLevel(level).requestId("request#").build();
    Logger logger = new LambdaLogger(configuration, printStream);

    // when and then
    assertEquals(enabled, logger.isWarnEnabled());
  }

  @DisplayName("Warning message")
  @Test
  void warn() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("warning test logger")
        .loggerLevel(Level.WARN).requestId("request#").build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger).log(isA(Level.class), anyString(), isNull());

    // when
    logger.warn("test warning message");

    // then
    verify(logger).log(Level.WARN, "test warning message", null);
  }


  @DisplayName("Warning formatted message with an argument")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO, true", "WARN, true", "ERROR, false"})
  void warning1(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("warning test logger")
        .loggerLevel(level).requestId("request#").build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), anyString(), isNull());
    }

    // when
    logger.warn("test warning message {}", "with an argument");

    // then
    if (enabled) {
      verify(logger).log(Level.WARN, "test warning message with an argument", null);
    } else {
      verify(logger, never()).log(isA(Level.class), anyString(), any());
    }
  }

  @DisplayName("Warning formatted message with two arguments")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO, true", "WARN, true", "ERROR, false"})
  void warning2(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("warning test logger")
        .loggerLevel(level).requestId("request#").build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), anyString(), isNull());
    }

    // when
    logger.warn("test warning message {} {}", "with", "arguments");

    // then
    if (enabled) {
      verify(logger).log(Level.WARN, "test warning message with arguments", null);
    } else {
      verify(logger, never()).log(isA(Level.class), anyString(), any());
    }
  }

  @DisplayName("Warning formatted message with varargs")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO, true", "WARN, true", "ERROR, false"})
  void warnVarargs(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("warning test logger")
        .loggerLevel(level).requestId("request#").build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), anyString(), isNull());
    }

    // when
    logger.warn("test warning message {} {} {}", "with", "some", "arguments");

    // then
    if (enabled) {
      verify(logger).log(Level.WARN, "test warning message with some arguments", null);
    } else {
      verify(logger, never()).log(isA(Level.class), anyString(), any());
    }
  }

  @DisplayName("Warning message with a throwable")
  @Test
  void warnThrowable() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("warning test logger")
        .loggerLevel(Level.WARN).requestId("request#").build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger).log(isA(Level.class), anyString(), isA(Throwable.class));

    // when
    logger.warn("test warning message", throwable);

    // then
    verify(logger).log(Level.WARN, "test warning message", throwable);
  }

  @DisplayName("Error is enabled")
  @ParameterizedTest
  @CsvSource({"TRACE", "DEBUG", "INFO", "WARN", "ERROR"})
  void isErrorEnabled(Level level) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("error test logger")
        .loggerLevel(level).requestId("request#").build();
    Logger logger = new LambdaLogger(configuration, printStream);

    // when and then
    assertTrue(logger.isErrorEnabled());
  }

  @DisplayName("Error message")
  @Test
  void error() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("error test logger")
        .loggerLevel(Level.ERROR).requestId("request#").build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger).log(isA(Level.class), anyString(), isNull());

    // when
    logger.error("test error message");

    // then
    verify(logger).log(Level.ERROR, "test error message", null);
  }

  @DisplayName("Error formatted message with an argument")
  @ParameterizedTest
  @CsvSource({"TRACE", "DEBUG", "INFO", "WARN", "ERROR"})
  void error1(Level level) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("error test logger")
        .loggerLevel(level).requestId("request#").build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger).log(isA(Level.class), anyString(), isNull());

    // when
    logger.error("test error message {}", "with an argument");

    // then
    verify(logger).log(Level.ERROR, "test error message with an argument", null);
  }

  @DisplayName("Error formatted message with two arguments")
  @ParameterizedTest
  @CsvSource({"TRACE", "DEBUG", "INFO", "WARN", "ERROR"})
  void error2(Level level) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("error test logger")
        .loggerLevel(level).requestId("request#").build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger).log(isA(Level.class), anyString(), isNull());

    // when
    logger.error("test error message {} {}", "with", "arguments");

    // then
    verify(logger).log(Level.ERROR, "test error message with arguments", null);
  }

  @DisplayName("Error formatted message with varargs")
  @ParameterizedTest
  @CsvSource({"TRACE", "DEBUG", "INFO", "WARN", "ERROR"})
  void errorVarargs(Level level) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("error test logger")
        .loggerLevel(level).requestId("request#").build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger).log(isA(Level.class), anyString(), isNull());

    // when
    logger.error("test error message {} {} {}", "with", "some", "arguments");

    // then
    verify(logger).log(Level.ERROR, "test error message with some arguments", null);
  }

  @DisplayName("Error message with a throwable")
  @Test
  void errorThrowable() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("error test logger")
        .loggerLevel(Level.ERROR).requestId("request#").build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger).log(isA(Level.class), anyString(), isA(Throwable.class));

    // when
    logger.error("test error message", throwable);

    // then
    verify(logger).log(Level.ERROR, "test error message", throwable);
  }

}
