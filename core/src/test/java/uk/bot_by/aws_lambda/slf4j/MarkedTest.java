package uk.bot_by.aws_lambda.slf4j;

import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.BasicMarkerFactory;

@ExtendWith(MockitoExtension.class)
@Tag("fast")
class MarkedTest {

  @Mock
  private AWSLambdaLoggerOutput output;
  @Mock
  private Marker marker;
  @Mock
  private Throwable throwable;

  private Marker knownMarker;
  private Marker markerWithReference;
  private Marker unknownMarker;

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
  @CsvSource(value = {"TRACE, i-am-a-marker, true", "TRACE, i-am-an-another-marker, false",
      "DEBUG, N/A, false", "INFO, N/A, false", "WARN, N/A, false",
      "ERROR, N/A, false"}, nullValues = "N/A")
  void isTraceEnabled(Level level, String markerName, boolean enabled) {
    // given
    var logger = getLambdaLogger(level, markerName);

    // when and then
    assertAll("Trace is enabled",
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
    var logger = getSpiedLambdaLogger(Level.TRACE, null);

    doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());

    // when
    logger.trace(knownMarker, "test trace message");

    // then
    verify(logger).log(eq(Level.TRACE), isA(Marker.class), eq("test trace message"), isNull());
  }

  @DisplayName("Trace formatted message with an argument")
  @ParameterizedTest
  @CsvSource(value = {"TRACE, i-am-a-marker, true", "TRACE, i-am-an-another-marker, false",
      "DEBUG, N/A, false", "INFO, N/A, false", "WARN, N/A, false",
      "ERROR, N/A, false"}, nullValues = "N/A")
  void trace1(Level level, String markerName, boolean enabled) {
    // given
    var logger = getSpiedLambdaLogger(level, markerName);

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());
    }

    // when
    logger.trace(knownMarker, "test trace message {}", "with an argument");

    // then
    if (enabled) {
      verify(logger).log(Level.TRACE, knownMarker, "test trace message with an argument", null);
    } else {
      verify(logger, never()).log(isA(Level.class), isA(Marker.class), anyString(), any());
    }
  }

  @DisplayName("Trace formatted message with two arguments")
  @ParameterizedTest
  @CsvSource(value = {"TRACE, i-am-a-marker, true", "TRACE, i-am-an-another-marker, false",
      "DEBUG, N/A, false", "INFO, N/A, false", "WARN, N/A, false",
      "ERROR, N/A, false"}, nullValues = "N/A")
  void trace2(Level level, String markerName, boolean enabled) {
    // given
    var logger = getSpiedLambdaLogger(level, markerName);

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());
    }

    // when
    logger.trace(knownMarker, "test trace message {} {}", "with", "arguments");

    // then
    if (enabled) {
      verify(logger).log(Level.TRACE, knownMarker, "test trace message with arguments", null);
    } else {
      verify(logger, never()).log(isA(Level.class), isA(Marker.class), anyString(), any());
    }
  }

  @DisplayName("Trace formatted message with varargs")
  @ParameterizedTest
  @CsvSource(value = {"TRACE, i-am-a-marker, true", "TRACE, i-am-an-another-marker, false",
      "DEBUG, N/A, false", "INFO, N/A, false", "WARN, N/A, false",
      "ERROR, N/A, false"}, nullValues = "N/A")
  void traceVarargs(Level level, String markerName, boolean enabled) {
    // given
    var logger = getSpiedLambdaLogger(level, markerName);

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());
    }

    // when
    logger.trace(knownMarker, "test trace message {} {} {}", "with", "some", "arguments");

    // then
    if (enabled) {
      verify(logger).log(Level.TRACE, knownMarker, "test trace message with some arguments", null);
    } else {
      verify(logger, never()).log(isA(Level.class), isA(Marker.class), anyString(), any());
    }
  }

  @DisplayName("Trace message with a throwable")
  @Test
  void traceThrowable() {
    // given
    var logger = getSpiedLambdaLogger(Level.TRACE, null);

    doNothing().when(logger)
        .log(isA(Level.class), isA(Marker.class), anyString(), isA(Throwable.class));

    // when
    logger.trace(knownMarker, "test trace message", throwable);

    // then
    verify(logger).log(eq(Level.TRACE), isA(Marker.class), eq("test trace message"), eq(throwable));
  }

  @DisplayName("Debug is enabled")
  @ParameterizedTest
  @CsvSource(value = {"TRACE, i-am-a-marker, true", "DEBUG, i-am-an-another-marker, false",
      "DEBUG, i-am-a-marker, true", "INFO, N/A, false", "WARN, N/A, false",
      "ERROR, N/A, false"}, nullValues = "N/A")
  void isDebugEnabled(Level level, String markerName, boolean enabled) {
    // given
    var logger = getLambdaLogger(level, markerName);

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
    var logger = getSpiedLambdaLogger(Level.DEBUG, null);

    doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());

    // when
    logger.debug(knownMarker, "test debug message");

    // then
    verify(logger).log(eq(Level.DEBUG), isA(Marker.class), eq("test debug message"), isNull());
  }

  @DisplayName("Debug formatted message with an argument")
  @ParameterizedTest
  @CsvSource(value = {"TRACE, i-am-a-marker, true", "DEBUG, i-am-an-another-marker, false",
      "DEBUG, i-am-a-marker, true", "INFO, N/A, false", "WARN, N/A, false",
      "ERROR, N/A, false"}, nullValues = "N/A")
  void debug1(Level level, String markerName, boolean enabled) {
    // given
    var logger = getSpiedLambdaLogger(level, markerName);

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());
    }

    // when
    logger.debug(knownMarker, "test debug message {}", "with an argument");

    // then
    if (enabled) {
      verify(logger).log(Level.DEBUG, knownMarker, "test debug message with an argument", null);
    } else {
      verify(logger, never()).log(isA(Level.class), isA(Marker.class), anyString(), any());
    }
  }

  @DisplayName("Debug formatted message with two arguments")
  @ParameterizedTest
  @CsvSource(value = {"TRACE, i-am-a-marker, true", "DEBUG, i-am-an-another-marker, false",
      "DEBUG, i-am-a-marker, true", "INFO, N/A, false", "WARN, N/A, false",
      "ERROR, N/A, false"}, nullValues = "N/A")
  void debug2(Level level, String markerName, boolean enabled) {
    // given
    var logger = getSpiedLambdaLogger(level, markerName);

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());
    }

    // when
    logger.debug(knownMarker, "test debug message {} {}", "with", "arguments");

    // then
    if (enabled) {
      verify(logger).log(Level.DEBUG, knownMarker, "test debug message with arguments", null);
    } else {
      verify(logger, never()).log(isA(Level.class), isA(Marker.class), anyString(), any());
    }
  }

  @DisplayName("Debug formatted message with varargs")
  @ParameterizedTest
  @CsvSource(value = {"TRACE, i-am-a-marker, true", "DEBUG, i-am-an-another-marker, false",
      "DEBUG, i-am-a-marker, true", "INFO, N/A, false", "WARN, N/A, false",
      "ERROR, N/A, false"}, nullValues = "N/A")
  void debugVarargs(Level level, String markerName, boolean enabled) {
    // given
    var logger = getSpiedLambdaLogger(level, markerName);

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());
    }

    // when
    logger.debug(knownMarker, "test debug message {} {} {}", "with", "some", "arguments");

    // then
    if (enabled) {
      verify(logger).log(Level.DEBUG, knownMarker, "test debug message with some arguments", null);
    } else {
      verify(logger, never()).log(isA(Level.class), isA(Marker.class), anyString(), any());
    }
  }

  @DisplayName("Debug message with a throwable")
  @Test
  void debugThrowable() {
    // given
    var logger = getSpiedLambdaLogger(Level.DEBUG, null);

    doNothing().when(logger)
        .log(isA(Level.class), isA(Marker.class), anyString(), isA(Throwable.class));

    // when
    logger.debug(knownMarker, "test debug message", throwable);

    // then
    verify(logger).log(eq(Level.DEBUG), isA(Marker.class), eq("test debug message"), eq(throwable));
  }

  @DisplayName("Info is enabled")
  @ParameterizedTest
  @CsvSource(value = {"TRACE, i-am-a-marker, true", "DEBUG, i-am-a-marker, true",
      "INFO, i-am-an-another-marker, false", "INFO, i-am-a-marker, true", "WARN, N/A, false",
      "ERROR, N/A, false"}, nullValues = "N/A")
  void isInfoEnabled(Level level, String markerName, boolean enabled) {
    // given
    var logger = getLambdaLogger(level, markerName);

    // when and then
    assertAll("Marked info is enabled",
        () -> assertEquals(enabled, logger.isInfoEnabled(knownMarker), "known marker"),
        () -> assertEquals(enabled, logger.isInfoEnabled(markerWithReference),
            "marker with reference"),
        () -> assertFalse(logger.isInfoEnabled(), "without any markers"),
        () -> assertFalse(logger.isInfoEnabled(unknownMarker), "unknown marker"));
  }

  @DisplayName("Info message")
  @Test
  void info() {
    // given
    var logger = getSpiedLambdaLogger(Level.INFO, null);

    doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());

    // when
    logger.info(knownMarker, "test info message");

    // then
    verify(logger).log(eq(Level.INFO), isA(Marker.class), eq("test info message"), isNull());
  }

  @DisplayName("Info formatted message with an argument")
  @ParameterizedTest
  @CsvSource(value = {"TRACE, i-am-a-marker, true", "DEBUG, i-am-a-marker, true",
      "INFO, i-am-an-another-marker, false", "INFO, i-am-a-marker, true", "WARN, N/A, false",
      "ERROR, N/A, false"}, nullValues = "N/A")
  void info1(Level level, String markerName, boolean enabled) {
    // given
    var logger = getSpiedLambdaLogger(level, markerName);

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());
    }

    // when
    logger.info(knownMarker, "test info message {}", "with an argument");

    // then
    if (enabled) {
      verify(logger).log(Level.INFO, knownMarker, "test info message with an argument", null);
    } else {
      verify(logger, never()).log(isA(Level.class), isA(Marker.class), anyString(), any());
    }
  }

  @DisplayName("Info formatted message with two arguments")
  @ParameterizedTest
  @CsvSource(value = {"TRACE, i-am-a-marker, true", "DEBUG, i-am-a-marker, true",
      "INFO, i-am-an-another-marker, false", "INFO, i-am-a-marker, true", "WARN, N/A, false",
      "ERROR, N/A, false"}, nullValues = "N/A")
  void info2(Level level, String markerName, boolean enabled) {
    // given
    var logger = getSpiedLambdaLogger(level, markerName);

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());
    }

    // when
    logger.info(knownMarker, "test info message {} {}", "with", "arguments");

    // then
    if (enabled) {
      verify(logger).log(Level.INFO, knownMarker, "test info message with arguments", null);
    } else {
      verify(logger, never()).log(isA(Level.class), isA(Marker.class), anyString(), any());
    }
  }

  @DisplayName("Info formatted message with varargs")
  @ParameterizedTest
  @CsvSource(value = {"TRACE, i-am-a-marker, true", "DEBUG, i-am-a-marker, true",
      "INFO, i-am-a-marker, true", "WARN, N/A, false", "ERROR, N/A, false"}, nullValues = "N/A")
  void infoVarargs(Level level, String markerName, boolean enabled) {
    // given
    var logger = getSpiedLambdaLogger(level, markerName);

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());
    }

    // when
    logger.info(knownMarker, "test info message {} {} {}", "with", "some", "arguments");

    // then
    if (enabled) {
      verify(logger).log(Level.INFO, knownMarker, "test info message with some arguments", null);
    } else {
      verify(logger, never()).log(isA(Level.class), isA(Marker.class), anyString(), any());
    }
  }

  @DisplayName("Info message with a throwable")
  @Test
  void infoThrowable() {
    // given
    var logger = getSpiedLambdaLogger(Level.INFO, null);

    doNothing().when(logger)
        .log(isA(Level.class), isA(Marker.class), anyString(), isA(Throwable.class));

    // when
    logger.info(knownMarker, "test info message", throwable);

    // then
    verify(logger).log(eq(Level.INFO), isA(Marker.class), eq("test info message"), eq(throwable));
  }

  @DisplayName("Warning is enabled")
  @ParameterizedTest
  @CsvSource(value = {"TRACE, i-am-a-marker, true", "DEBUG, i-am-a-marker, true",
      "INFO, i-am-a-marker, true", "WARN, i-am-an-another-marker, false",
      "WARN, i-am-a-marker, true", "ERROR, N/A, false"}, nullValues = "N/A")
  void isWarnEnabled(Level level, String markerName, boolean enabled) {
    // given
    var logger = getLambdaLogger(level, markerName);

    // when and then
    assertAll("Marked warning is enabled",
        () -> assertEquals(enabled, logger.isWarnEnabled(knownMarker), "known marker"),
        () -> assertEquals(enabled, logger.isWarnEnabled(markerWithReference),
            "marker with reference"),
        () -> assertFalse(logger.isWarnEnabled(), "without any markers"),
        () -> assertFalse(logger.isWarnEnabled(unknownMarker), "unknown marker"));
  }

  @DisplayName("Warning message")
  @Test
  void warn() {
    // given
    var logger = getSpiedLambdaLogger(Level.WARN, null);

    doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());

    // when
    logger.warn(knownMarker, "test warning message");

    // then
    verify(logger).log(eq(Level.WARN), isA(Marker.class), eq("test warning message"), isNull());
  }

  @DisplayName("Warning formatted message with an argument")
  @ParameterizedTest
  @CsvSource(value = {"TRACE, i-am-a-marker, true", "DEBUG, i-am-a-marker, true",
      "INFO, i-am-a-marker, true", "WARN, i-am-an-another-marker, false",
      "WARN, i-am-a-marker, true", "ERROR, N/A, false"}, nullValues = "N/A")
  void warning1(Level level, String markerName, boolean enabled) {
    // given
    var logger = getSpiedLambdaLogger(level, markerName);

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());
    }

    // when
    logger.warn(knownMarker, "test warning message {}", "with an argument");

    // then
    if (enabled) {
      verify(logger).log(Level.WARN, knownMarker, "test warning message with an argument", null);
    } else {
      verify(logger, never()).log(isA(Level.class), isA(Marker.class), anyString(), any());
    }
  }

  @DisplayName("Warning formatted message with two arguments")
  @ParameterizedTest
  @CsvSource(value = {"TRACE, i-am-a-marker, true", "DEBUG, i-am-a-marker, true",
      "INFO, i-am-a-marker, true", "WARN, i-am-an-another-marker, false",
      "WARN, i-am-a-marker, true", "ERROR, N/A, false"}, nullValues = "N/A")
  void warning2(Level level, String markerName, boolean enabled) {
    // given
    var logger = getSpiedLambdaLogger(level, markerName);

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());
    }

    // when
    logger.warn(knownMarker, "test warning message {} {}", "with", "arguments");

    // then
    if (enabled) {
      verify(logger).log(Level.WARN, knownMarker, "test warning message with arguments", null);
    } else {
      verify(logger, never()).log(isA(Level.class), isA(Marker.class), anyString(), any());
    }
  }

  @DisplayName("Warning formatted message with varargs")
  @ParameterizedTest
  @CsvSource(value = {"TRACE, i-am-a-marker, true", "DEBUG, i-am-a-marker, true",
      "INFO, i-am-a-marker, true", "WARN, i-am-an-another-marker, false",
      "WARN, i-am-a-marker, true", "ERROR, N/A, false"}, nullValues = "N/A")
  void warnVarargs(Level level, String markerName, boolean enabled) {
    // given
    var logger = getSpiedLambdaLogger(level, markerName);

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());
    }

    // when
    logger.warn(knownMarker, "test warning message {} {} {}", "with", "some", "arguments");

    // then
    if (enabled) {
      verify(logger).log(Level.WARN, knownMarker, "test warning message with some arguments", null);
    } else {
      verify(logger, never()).log(isA(Level.class), isA(Marker.class), anyString(), any());
    }
  }

  @DisplayName("Warning message with a throwable")
  @Test
  void warnThrowable() {
    // given
    var logger = getSpiedLambdaLogger(Level.WARN, null);

    doNothing().when(logger)
        .log(isA(Level.class), isA(Marker.class), anyString(), isA(Throwable.class));

    // when
    logger.warn(knownMarker, "test warning message", throwable);

    // then
    verify(logger).log(eq(Level.WARN), isA(Marker.class), eq("test warning message"),
        eq(throwable));
  }

  @DisplayName("Error is enabled")
  @ParameterizedTest
  @CsvSource({"TRACE, i-am-a-marker, true", "DEBUG, i-am-a-marker, true",
      "INFO, i-am-a-marker, true", "WARN, i-am-a-marker, true",
      "ERROR, i-am-an-another-marker, false", "ERROR, i-am-a-marker, true"})
  void isErrorEnabled(Level level, String markerName, boolean enabled) {
    // given
    var logger = getLambdaLogger(level, markerName);

    // when and then
    assertAll("Marked trace is enabled",
        () -> assertEquals(enabled, logger.isErrorEnabled(knownMarker), "known marker"),
        () -> assertEquals(enabled, logger.isErrorEnabled(markerWithReference),
            "marker with reference"),
        () -> assertFalse(logger.isErrorEnabled(), "without any markers"),
        () -> assertFalse(logger.isErrorEnabled(unknownMarker), "unknown marker"));
  }

  @DisplayName("Error message")
  @Test
  void error() {
    // given
    var logger = getSpiedLambdaLogger(Level.ERROR, null);

    doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());

    // when
    logger.error(knownMarker, "test error message");

    // then
    verify(logger).log(eq(Level.ERROR), isA(Marker.class), eq("test error message"), isNull());
  }

  @DisplayName("Error formatted message with an argument")
  @ParameterizedTest
  @CsvSource(value = {"TRACE, i-am-a-marker, true", "DEBUG, i-am-a-marker, true",
      "INFO, i-am-a-marker, true", "WARN, i-am-a-marker, true",
      "ERROR, i-am-an-another-marker, false", "ERROR, i-am-a-marker, true"}, nullValues = "N/A")
  void error1(Level level, String markerName, boolean enabled) {
    // given
    var logger = getSpiedLambdaLogger(level, markerName);

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());
    }

    // when
    logger.error(knownMarker, "test error message {}", "with an argument");

    // then
    if (enabled) {
      verify(logger).log(Level.ERROR, knownMarker, "test error message with an argument", null);
    } else {
      verify(logger, never()).log(isA(Level.class), isA(Marker.class), anyString(), any());
    }
  }

  @DisplayName("Error formatted message with two arguments")
  @ParameterizedTest
  @CsvSource(value = {"TRACE, i-am-a-marker, true", "DEBUG, i-am-a-marker, true",
      "INFO, i-am-a-marker, true", "WARN, i-am-a-marker, true",
      "ERROR, i-am-an-another-marker, false", "ERROR, i-am-a-marker, true"}, nullValues = "N/A")
  void error2(Level level, String markerName, boolean enabled) {
    // given
    var logger = getSpiedLambdaLogger(level, markerName);

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());
    }

    // when
    logger.error(knownMarker, "test error message {} {}", "with", "arguments");

    // then
    if (enabled) {
      verify(logger).log(Level.ERROR, knownMarker, "test error message with arguments", null);
    } else {
      verify(logger, never()).log(isA(Level.class), isA(Marker.class), anyString(), any());
    }
  }

  @DisplayName("Error formatted message with varargs")
  @ParameterizedTest
  @CsvSource(value = {"TRACE, i-am-a-marker, true", "DEBUG, i-am-a-marker, true",
      "INFO, i-am-a-marker, true", "WARN, i-am-a-marker, true",
      "ERROR, i-am-an-another-marker, false", "ERROR, i-am-a-marker, true"}, nullValues = "N/A")
  void errorVarargs(Level level, String markerName, boolean enabled) {
    // given
    var logger = getSpiedLambdaLogger(level, markerName);

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());
    }

    // when
    logger.error(knownMarker, "test error message {} {} {}", "with", "some", "arguments");

    // then
    if (enabled) {
      verify(logger).log(Level.ERROR, knownMarker, "test error message with some arguments", null);
    } else {
      verify(logger, never()).log(isA(Level.class), isA(Marker.class), anyString(), any());
    }
  }

  @DisplayName("Error message with a throwable")
  @Test
  void errorThrowable() {
    // given
    var logger = getSpiedLambdaLogger(Level.ERROR, null);

    doNothing().when(logger)
        .log(isA(Level.class), isA(Marker.class), anyString(), isA(Throwable.class));

    // when
    logger.error(knownMarker, "test error message", throwable);

    // then
    verify(logger).log(eq(Level.ERROR), isA(Marker.class), eq("test error message"), eq(throwable));
  }

  @NotNull
  private AWSLambdaLogger getLambdaLogger(Level level, String markerName) {
    var configuration = AWSLambdaLoggerConfiguration.builder().name("test logger")
        .loggerLevel(level, marker).requestId("request#").build();

    if (nonNull(markerName)) {
      when(marker.getName()).thenReturn(markerName);
    }

    return new AWSLambdaLogger(configuration, output);
  }

  @NotNull
  private AWSLambdaLogger getSpiedLambdaLogger(Level level, String markerName) {
    return spy(getLambdaLogger(level, markerName));
  }

}
