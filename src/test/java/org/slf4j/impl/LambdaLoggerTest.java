package org.slf4j.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.slf4j.impl.LambdaLogger.AWS_REQUEST_ID;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.event.Level;

@ExtendWith(MockitoExtension.class)
@Tag("fast")
class LambdaLoggerTest {

  @Mock
  private Marker marker;
  @Mock
  private PrintStream printStream;
  @Mock
  private Throwable throwable;

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
        .loggerLevel(level).build();
    Logger logger = new LambdaLogger(configuration, printStream);

    // when and then
    assertEquals(enabled, logger.isTraceEnabled());
  }

  @DisplayName("Trace message")
  @Test
  void trace() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("trace test logger")
        .loggerLevel(Level.TRACE).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger).log(isA(Level.class), isNull(), anyString(), isNull());

    // when
    logger.trace("test trace message");

    // then
    verify(logger).log(Level.TRACE, null, "test trace message", null);
  }

  @DisplayName("Trace formatted message with an argument")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, false", "INFO, false", "WARN, false", "ERROR, false"})
  void trace1(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("trace test logger")
        .loggerLevel(level).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isNull(), anyString(), isNull());
    }

    // when
    logger.trace("test trace message {}", "with an argument");

    // then
    if (enabled) {
      verify(logger).log(Level.TRACE, null, "test trace message with an argument", null);
    } else {
      verify(logger, never()).log(isA(Level.class), any(), anyString(), any());
    }
  }

  @DisplayName("Trace formatted message with two arguments")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, false", "INFO, false", "WARN, false", "ERROR, false"})
  void trace2(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("trace test logger")
        .loggerLevel(level).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isNull(), anyString(), isNull());
    }

    // when
    logger.trace("test trace message {} {}", "with", "arguments");

    // then
    if (enabled) {
      verify(logger).log(Level.TRACE, null, "test trace message with arguments", null);
    } else {
      verify(logger, never()).log(isA(Level.class), any(), anyString(), any());
    }
  }

  @DisplayName("Trace formatted message with varargs")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, false", "INFO, false", "WARN, false", "ERROR, false"})
  void traceVarargs(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("trace test logger")
        .loggerLevel(level).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isNull(), anyString(), isNull());
    }

    // when
    logger.trace("test trace message {} {} {}", "with", "some", "arguments");

    // then
    if (enabled) {
      verify(logger).log(Level.TRACE, null, "test trace message with some arguments", null);
    } else {
      verify(logger, never()).log(isA(Level.class), any(), anyString(), any());
    }
  }

  @DisplayName("Trace message with a throwable")
  @Test
  void traceThrowable() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("trace test logger")
        .loggerLevel(Level.ERROR).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger).log(isA(Level.class), isNull(), anyString(), isA(Throwable.class));

    // when
    logger.trace("test trace message", throwable);

    // then
    verify(logger).log(Level.TRACE, null, "test trace message", throwable);
  }

  @DisplayName("Trace marker is enabled")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, false", "INFO, false", "WARN, false", "ERROR, false"})
  void isTraceMarkerEnabled(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("trace test logger")
        .loggerLevel(level).build();
    Logger logger = new LambdaLogger(configuration, printStream);

    // when and then
    assertEquals(enabled, logger.isTraceEnabled(marker));
  }

  @DisplayName("Trace message with a marker")
  @Test
  void traceMarker() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("trace test logger")
        .loggerLevel(Level.ERROR).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());

    // when
    logger.trace(marker, "test trace message");

    // then
    verify(logger).log(Level.TRACE, marker, "test trace message", null);
  }

  @DisplayName("Trace formatted message with argument and marker")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, false", "INFO, false", "WARN, false", "ERROR, false"})
  void trace1Marker(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("trace test logger")
        .loggerLevel(level).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());
    }

    // when
    logger.trace(marker, "test trace message {}", "with argument and marker");

    // then
    if (enabled) {
      verify(logger).log(Level.TRACE, marker, "test trace message with argument and marker", null);
    } else {
      verify(logger, never()).log(isA(Level.class), any(), anyString(), any());
    }
  }

  @DisplayName("Trace formatted message with two arguments and marker")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, false", "INFO, false", "WARN, false", "ERROR, false"})
  void trace2Marker(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("trace test logger")
        .loggerLevel(level).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());
    }

    // when
    logger.trace(marker, "test trace message with {} {}", "arguments", "and marker");

    // then
    if (enabled) {
      verify(logger).log(Level.TRACE, marker, "test trace message with arguments and marker", null);
    } else {
      verify(logger, never()).log(isA(Level.class), any(), anyString(), any());
    }
  }

  @DisplayName("Trace formatted message with varargs and marker")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, false", "INFO, false", "WARN, false", "ERROR, false"})
  void traceVarargsAndMarker(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("trace test logger")
        .loggerLevel(level).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());
    }

    // when
    logger.trace(marker, "test trace message {} {} {}", "with", "arguments", "and marker");

    // then
    if (enabled) {
      verify(logger).log(Level.TRACE, marker, "test trace message with arguments and marker", null);
    } else {
      verify(logger, never()).log(isA(Level.class), any(), anyString(), any());
    }
  }

  @DisplayName("Trace message with throwable and marker")
  @Test
  void traceThrowableAndMarker() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("trace test logger")
        .loggerLevel(Level.ERROR).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger)
        .log(isA(Level.class), isA(Marker.class), anyString(), isA(Throwable.class));

    // when
    logger.trace(marker, "test trace message", throwable);

    // then
    verify(logger).log(Level.TRACE, marker, "test trace message", throwable);
  }

  @DisplayName("Debug is enabled")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO, false", "WARN, false", "ERROR, false"})
  void isDebugEnabled(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("debug test logger")
        .loggerLevel(level).build();
    Logger logger = new LambdaLogger(configuration, printStream);

    // when and then
    assertEquals(enabled, logger.isDebugEnabled());
  }

  @DisplayName("Debug message")
  @Test
  void debug() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("debug test logger")
        .loggerLevel(Level.DEBUG).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger).log(isA(Level.class), isNull(), anyString(), isNull());

    // when
    logger.debug("test debug message");

    // then
    verify(logger).log(Level.DEBUG, null, "test debug message", null);
  }

  @DisplayName("Debug formatted message with an argument")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO, false", "WARN, false", "ERROR, false"})
  void debug1(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("debug test logger")
        .loggerLevel(level).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isNull(), anyString(), isNull());
    }

    // when
    logger.debug("test debug message {}", "with an argument");

    // then
    if (enabled) {
      verify(logger).log(Level.DEBUG, null, "test debug message with an argument", null);
    } else {
      verify(logger, never()).log(isA(Level.class), any(), anyString(), any());
    }
  }

  @DisplayName("Debug formatted message with two arguments")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO, false", "WARN, false", "ERROR, false"})
  void debug2(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("debug test logger")
        .loggerLevel(level).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isNull(), anyString(), isNull());
    }

    // when
    logger.debug("test debug message {} {}", "with", "arguments");

    // then
    if (enabled) {
      verify(logger).log(Level.DEBUG, null, "test debug message with arguments", null);
    } else {
      verify(logger, never()).log(isA(Level.class), any(), anyString(), any());
    }
  }

  @DisplayName("Debug formatted message with varargs")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO, false", "WARN, false", "ERROR, false"})
  void debugVarargs(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("debug test logger")
        .loggerLevel(level).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isNull(), anyString(), isNull());
    }

    // when
    logger.debug("test debug message {} {} {}", "with", "some", "arguments");

    // then
    if (enabled) {
      verify(logger).log(Level.DEBUG, null, "test debug message with some arguments", null);
    } else {
      verify(logger, never()).log(isA(Level.class), any(), anyString(), any());
    }
  }

  @DisplayName("Debug message with a throwable")
  @Test
  void debugThrowable() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("debug test logger")
        .loggerLevel(Level.DEBUG).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger).log(isA(Level.class), isNull(), anyString(), isA(Throwable.class));

    // when
    logger.debug("test debug message", throwable);

    // then
    verify(logger).log(Level.DEBUG, null, "test debug message", throwable);
  }

  @DisplayName("Debug marker is enabled")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO, false", "WARN, false", "ERROR, false"})
  void isDEbugMarkerEnabled(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("debug test logger")
        .loggerLevel(level).build();
    Logger logger = new LambdaLogger(configuration, printStream);

    // when and then
    assertEquals(enabled, logger.isDebugEnabled(marker));
  }

  @DisplayName("Debug message with a marker")
  @Test
  void debugMarker() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("debug test logger")
        .loggerLevel(Level.ERROR).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());

    // when
    logger.debug(marker, "test debug message");

    // then
    verify(logger).log(Level.DEBUG, marker, "test debug message", null);
  }

  @DisplayName("Debug formatted message with argument and marker")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO, false", "WARN, false", "ERROR, false"})
  void debug1Marker(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("debug test logger")
        .loggerLevel(level).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());
    }

    // when
    logger.debug(marker, "test debug message {}", "with argument and marker");

    // then
    if (enabled) {
      verify(logger).log(Level.DEBUG, marker, "test debug message with argument and marker", null);
    } else {
      verify(logger, never()).log(isA(Level.class), any(), anyString(), any());
    }
  }

  @DisplayName("Debug formatted message with two arguments and marker")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO, false", "WARN, false", "ERROR, false"})
  void debug2Marker(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("debug test logger")
        .loggerLevel(level).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());
    }

    // when
    logger.debug(marker, "test debug message with {} {}", "arguments", "and marker");

    // then
    if (enabled) {
      verify(logger).log(Level.DEBUG, marker, "test debug message with arguments and marker", null);
    } else {
      verify(logger, never()).log(isA(Level.class), any(), anyString(), any());
    }
  }

  @DisplayName("Debug formatted message with varargs and marker")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO, false", "WARN, false", "ERROR, false"})
  void debugVarargsAndMarker(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("debug test logger")
        .loggerLevel(level).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());
    }

    // when
    logger.debug(marker, "test debug message {} {} {}", "with", "arguments", "and marker");

    // then
    if (enabled) {
      verify(logger).log(Level.DEBUG, marker, "test debug message with arguments and marker", null);
    } else {
      verify(logger, never()).log(isA(Level.class), any(), anyString(), any());
    }
  }

  @DisplayName("Debug message with throwable and marker")
  @Test
  void debugThrowableAndMarker() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("debug test logger")
        .loggerLevel(Level.ERROR).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger)
        .log(isA(Level.class), isA(Marker.class), anyString(), isA(Throwable.class));

    // when
    logger.debug(marker, "test debug message", throwable);

    // then
    verify(logger).log(Level.DEBUG, marker, "test debug message", throwable);
  }

  @DisplayName("Info is enabled")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO,  true", "WARN, false", "ERROR, false"})
  void isInfoEnabled(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("info test logger")
        .loggerLevel(level).build();
    Logger logger = new LambdaLogger(configuration, printStream);

    // when and then
    assertEquals(enabled, logger.isInfoEnabled());
  }

  @DisplayName("Info message")
  @Test
  void info() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("info test logger")
        .loggerLevel(Level.INFO).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger).log(isA(Level.class), isNull(), anyString(), isNull());

    // when
    logger.info("test info message");

    // then
    verify(logger).log(Level.INFO, null, "test info message", null);
  }

  @DisplayName("Info formatted message with an argument")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO, true", "WARN, false", "ERROR, false"})
  void info1(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("info test logger")
        .loggerLevel(level).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isNull(), anyString(), isNull());
    }

    // when
    logger.info("test info message {}", "with an argument");

    // then
    if (enabled) {
      verify(logger).log(Level.INFO, null, "test info message with an argument", null);
    } else {
      verify(logger, never()).log(isA(Level.class), any(), anyString(), any());
    }
  }

  @DisplayName("Info formatted message with two arguments")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO, true", "WARN, false", "ERROR, false"})
  void info2(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("info test logger")
        .loggerLevel(level).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isNull(), anyString(), isNull());
    }

    // when
    logger.info("test info message {} {}", "with", "arguments");

    // then
    if (enabled) {
      verify(logger).log(Level.INFO, null, "test info message with arguments", null);
    } else {
      verify(logger, never()).log(isA(Level.class), any(), anyString(), any());
    }
  }

  @DisplayName("Info formatted message with varargs")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO, true", "WARN, false", "ERROR, false"})
  void infoVarargs(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("info test logger")
        .loggerLevel(level).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isNull(), anyString(), isNull());
    }

    // when
    logger.info("test info message {} {} {}", "with", "some", "arguments");

    // then
    if (enabled) {
      verify(logger).log(Level.INFO, null, "test info message with some arguments", null);
    } else {
      verify(logger, never()).log(isA(Level.class), any(), anyString(), any());
    }
  }

  @DisplayName("Info message with a throwable")
  @Test
  void infoThrowable() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("info test logger")
        .loggerLevel(Level.ERROR).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger).log(isA(Level.class), isNull(), anyString(), isA(Throwable.class));

    // when
    logger.info("test info message", throwable);

    // then
    verify(logger).log(Level.INFO, null, "test info message", throwable);
  }

  @DisplayName("Info marker is enabled")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO, true", "WARN, false", "ERROR, false"})
  void isInfoMarkerEnabled(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("info test logger")
        .loggerLevel(level).build();
    Logger logger = new LambdaLogger(configuration, printStream);

    // when and then
    assertEquals(enabled, logger.isInfoEnabled(marker), "isInfoEnabled");
  }

  @DisplayName("Info message with a marker")
  @Test
  void infoMarker() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("info test logger")
        .loggerLevel(Level.ERROR).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());

    // when
    logger.info(marker, "test info message");

    // then
    verify(logger).log(Level.INFO, marker, "test info message", null);
  }

  @DisplayName("Info formatted message with argument and marker")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO, true", "WARN, false", "ERROR, false"})
  void info1Marker(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("info test logger")
        .loggerLevel(level).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());
    }

    // when
    logger.info(marker, "test info message {}", "with argument and marker");

    // then
    if (enabled) {
      verify(logger).log(Level.INFO, marker, "test info message with argument and marker", null);
    } else {
      verify(logger, never()).log(isA(Level.class), any(), anyString(), any());
    }
  }

  @DisplayName("Info formatted message with two arguments and marker")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO, true", "WARN, false", "ERROR, false"})
  void infoe2Marker(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("info test logger")
        .loggerLevel(level).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());
    }

    // when
    logger.info(marker, "test info message with {} {}", "arguments", "and marker");

    // then
    if (enabled) {
      verify(logger).log(Level.INFO, marker, "test info message with arguments and marker", null);
    } else {
      verify(logger, never()).log(isA(Level.class), any(), anyString(), any());
    }
  }

  @DisplayName("Info formatted message with varargs and marker")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO, true", "WARN, false", "ERROR, false"})
  void infoVarargsAndMarker(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("info test logger")
        .loggerLevel(level).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());
    }

    // when
    logger.info(marker, "test info message {} {} {}", "with", "arguments", "and marker");

    // then
    if (enabled) {
      verify(logger).log(Level.INFO, marker, "test info message with arguments and marker", null);
    } else {
      verify(logger, never()).log(isA(Level.class), any(), anyString(), any());
    }
  }

  @DisplayName("Info message with throwable and marker")
  @Test
  void infoThrowableAndMarker() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("info test logger")
        .loggerLevel(Level.INFO).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger)
        .log(isA(Level.class), isA(Marker.class), anyString(), isA(Throwable.class));

    // when
    logger.info(marker, "test info message", throwable);

    // then
    verify(logger).log(Level.INFO, marker, "test info message", throwable);
  }

  @DisplayName("Warn is enabled")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO,  true", "WARN,  true", "ERROR, false"})
  void isWarnEnabled(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("warning test logger")
        .loggerLevel(level).build();
    Logger logger = new LambdaLogger(configuration, printStream);

    // when and then
    assertEquals(enabled, logger.isWarnEnabled());
  }

  @DisplayName("Warning message")
  @Test
  void warn() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("warning test logger")
        .loggerLevel(Level.WARN).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger).log(isA(Level.class), isNull(), anyString(), isNull());

    // when
    logger.warn("test warning message");

    // then
    verify(logger).log(Level.WARN, null, "test warning message", null);
  }


  @DisplayName("Warning formatted message with an argument")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO, true", "WARN, true", "ERROR, false"})
  void warning1(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("warning test logger")
        .loggerLevel(level).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isNull(), anyString(), isNull());
    }

    // when
    logger.warn("test warning message {}", "with an argument");

    // then
    if (enabled) {
      verify(logger).log(Level.WARN, null, "test warning message with an argument", null);
    } else {
      verify(logger, never()).log(isA(Level.class), any(), anyString(), any());
    }
  }

  @DisplayName("Warning formatted message with two arguments")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO, true", "WARN, true", "ERROR, false"})
  void warning2(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("warning test logger")
        .loggerLevel(level).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isNull(), anyString(), isNull());
    }

    // when
    logger.warn("test warning message {} {}", "with", "arguments");

    // then
    if (enabled) {
      verify(logger).log(Level.WARN, null, "test warning message with arguments", null);
    } else {
      verify(logger, never()).log(isA(Level.class), any(), anyString(), any());
    }
  }

  @DisplayName("Warning formatted message with varargs")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO, true", "WARN, true", "ERROR, false"})
  void warnVarargs(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("warning test logger")
        .loggerLevel(level).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isNull(), anyString(), isNull());
    }

    // when
    logger.warn("test warning message {} {} {}", "with", "some", "arguments");

    // then
    if (enabled) {
      verify(logger).log(Level.WARN, null, "test warning message with some arguments", null);
    } else {
      verify(logger, never()).log(isA(Level.class), any(), anyString(), any());
    }
  }

  @DisplayName("Warning message with a throwable")
  @Test
  void warnThrowable() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("warning test logger")
        .loggerLevel(Level.WARN).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger).log(isA(Level.class), isNull(), anyString(), isA(Throwable.class));

    // when
    logger.warn("test warning message", throwable);

    // then
    verify(logger).log(Level.WARN, null, "test warning message", throwable);
  }

  @DisplayName("Warning marker is enabled")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO, true", "WARN, true", "ERROR, false"})
  void isWarnMarkerEnabled(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("warning test logger")
        .loggerLevel(level).build();
    Logger logger = new LambdaLogger(configuration, printStream);

    // when and then
    assertEquals(enabled, logger.isWarnEnabled(marker));
  }

  @DisplayName("Warning message with a marker")
  @Test
  void warnMarker() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("warning test logger")
        .loggerLevel(Level.ERROR).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());

    // when
    logger.warn(marker, "test warning message");

    // then
    verify(logger).log(Level.WARN, marker, "test warning message", null);
  }

  @DisplayName("Warning formatted message with argument and marker")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO, true", "WARN, true", "ERROR, false"})
  void warn1Marker(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("warning test logger")
        .loggerLevel(level).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());
    }

    // when
    logger.warn(marker, "test warning message {}", "with argument and marker");

    // then
    if (enabled) {
      verify(logger).log(Level.WARN, marker, "test warning message with argument and marker", null);
    } else {
      verify(logger, never()).log(isA(Level.class), any(), anyString(), any());
    }
  }

  @DisplayName("Warning formatted message with two arguments and marker")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO, true", "WARN, true", "ERROR, false"})
  void warn2Marker(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("warning test logger")
        .loggerLevel(level).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());
    }

    // when
    logger.warn(marker, "test warning message with {} {}", "arguments", "and marker");

    // then
    if (enabled) {
      verify(logger).log(Level.WARN, marker, "test warning message with arguments and marker",
          null);
    } else {
      verify(logger, never()).log(isA(Level.class), any(), anyString(), any());
    }
  }

  @DisplayName("Warning formatted message with varargs and marker")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO, true", "WARN, true", "ERROR, false"})
  void warnVarargsAndMarker(Level level, boolean enabled) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("warning test logger")
        .loggerLevel(level).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    if (enabled) {
      doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());
    }

    // when
    logger.warn(marker, "test warning message {} {} {}", "with", "arguments", "and marker");

    // then
    if (enabled) {
      verify(logger).log(Level.WARN, marker, "test warning message with arguments and marker",
          null);
    } else {
      verify(logger, never()).log(isA(Level.class), any(), anyString(), any());
    }
  }

  @DisplayName("Warning message with throwable and marker")
  @Test
  void warnThrowableAndMarker() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("warning test logger")
        .loggerLevel(Level.ERROR).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger)
        .log(isA(Level.class), isA(Marker.class), anyString(), isA(Throwable.class));

    // when
    logger.warn(marker, "test warning message", throwable);

    // then
    verify(logger).log(Level.WARN, marker, "test warning message", throwable);
  }

  @DisplayName("Error is enabled")
  @ParameterizedTest
  @CsvSource({"TRACE", "DEBUG", "INFO", "WARN", "ERROR"})
  void isErrorEnabled(Level level) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("error test logger")
        .loggerLevel(level).build();
    Logger logger = new LambdaLogger(configuration, printStream);

    // when and then
    assertTrue(logger.isErrorEnabled());
  }

  @DisplayName("Error message")
  @Test
  void error() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("error test logger")
        .loggerLevel(Level.TRACE).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger).log(isA(Level.class), isNull(), anyString(), isNull());

    // when
    logger.error("test error message");

    // then
    verify(logger).log(Level.ERROR, null, "test error message", null);
  }

  @DisplayName("Error formatted message with an argument")
  @ParameterizedTest
  @CsvSource({"TRACE", "DEBUG", "INFO", "WARN", "ERROR"})
  void error1(Level level) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("error test logger")
        .loggerLevel(level).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger).log(isA(Level.class), isNull(), anyString(), isNull());

    // when
    logger.error("test error message {}", "with an argument");

    // then
    verify(logger).log(Level.ERROR, null, "test error message with an argument", null);
  }

  @DisplayName("Error formatted message with two arguments")
  @ParameterizedTest
  @CsvSource({"TRACE", "DEBUG", "INFO", "WARN", "ERROR"})
  void error2(Level level) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("error test logger")
        .loggerLevel(level).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger).log(isA(Level.class), isNull(), anyString(), isNull());

    // when
    logger.error("test error message {} {}", "with", "arguments");

    // then
    verify(logger).log(Level.ERROR, null, "test error message with arguments", null);
  }

  @DisplayName("Error formatted message with varargs")
  @ParameterizedTest
  @CsvSource({"TRACE", "DEBUG", "INFO", "WARN", "ERROR"})
  void errorVarargs(Level level) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("error test logger")
        .loggerLevel(level).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger).log(isA(Level.class), isNull(), anyString(), isNull());

    // when
    logger.error("test error message {} {} {}", "with", "some", "arguments");

    // then
    verify(logger).log(Level.ERROR, null, "test error message with some arguments", null);
  }

  @DisplayName("Error message with a throwable")
  @Test
  void errorThrowable() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("error test logger")
        .loggerLevel(Level.ERROR).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger).log(isA(Level.class), isNull(), anyString(), isA(Throwable.class));

    // when
    logger.error("test error message", throwable);

    // then
    verify(logger).log(Level.ERROR, null, "test error message", throwable);
  }

  @DisplayName("Error marker is enabled")
  @ParameterizedTest
  @CsvSource({"TRACE", "DEBUG", "INFO", "WARN", "ERROR"})
  void isErrorMarkerEnabled(Level level) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("error test logger")
        .loggerLevel(level).build();
    Logger logger = new LambdaLogger(configuration, printStream);

    // when and then
    assertTrue(logger.isErrorEnabled(marker));
  }

  @DisplayName("Error message with a marker")
  @Test
  void errorMarker() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("error test logger")
        .loggerLevel(Level.ERROR).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());

    // when
    logger.error(marker, "test error message");

    // then
    verify(logger).log(Level.ERROR, marker, "test error message", null);
  }

  @DisplayName("Error formatted message with argument and marker")
  @ParameterizedTest
  @CsvSource({"TRACE", "DEBUG", "INFO", "WARN", "ERROR"})
  void error1Marker(Level level) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("error test logger")
        .loggerLevel(level).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());

    // when
    logger.error(marker, "test error message {}", "with argument and marker");

    // then
    verify(logger).log(Level.ERROR, marker, "test error message with argument and marker", null);
  }

  @DisplayName("Error formatted message with two arguments and marker")
  @ParameterizedTest
  @CsvSource({"TRACE", "DEBUG", "INFO", "WARN", "ERROR"})
  void error2Marker(Level level) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("error test logger")
        .loggerLevel(level).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());

    // when
    logger.error(marker, "test error message with {} {}", "arguments", "and marker");

    // then
    verify(logger).log(Level.ERROR, marker, "test error message with arguments and marker", null);
  }

  @DisplayName("Error formatted message with varargs and marker")
  @ParameterizedTest
  @CsvSource({"TRACE", "DEBUG", "INFO", "WARN", "ERROR"})
  void errorVarargsAndMarker(Level level) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("error test logger")
        .loggerLevel(level).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger).log(isA(Level.class), isA(Marker.class), anyString(), isNull());

    // when
    logger.error(marker, "test error message {} {} {}", "with", "arguments", "and marker");

    // then
    verify(logger).log(Level.ERROR, marker, "test error message with arguments and marker", null);
  }

  @DisplayName("Error message with throwable and marker")
  @Test
  void errorThrowableAndMarker() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("error test logger")
        .loggerLevel(Level.ERROR).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    doNothing().when(logger)
        .log(isA(Level.class), isA(Marker.class), anyString(), isA(Throwable.class));

    // when
    logger.error(marker, "test error message", throwable);

    // then
    verify(logger).log(Level.ERROR, marker, "test error message", throwable);
  }

  @DisplayName("Default log message")
  @Test
  void logMessage() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("error test logger")
        .loggerLevel(Level.ERROR).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    // when
    logger.error("test error message");

    // then
    verify(printStream).println("ERROR test error message");
  }

  @DisplayName("Silent")
  @Test
  void silent() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("error test logger")
        .loggerLevel(Level.ERROR).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    // when
    logger.trace("test trace message");

    // then
    verify(printStream, never()).println(anyString());
  }

  @DisplayName("Show relative time")
  @Test
  void relativeTime() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("trace test logger")
        .loggerLevel(Level.ERROR).showDateTime(true).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    // when
    logger.error("test error message");

    // then
    verify(printStream).println(matches("\\d+ ERROR test error message"));
  }

  @DisplayName("Show timestamp")
  @Test
  void timestamp() {
    // given
    DateFormat dateTimeFormat = new DateFormat() {

      @Override
      public StringBuffer format(java.util.Date date, StringBuffer toAppendTo,
          FieldPosition fieldPosition) {
        return toAppendTo.append("1/1/1970 0:00");
      }

      @Override
      public java.util.Date parse(String source, ParsePosition pos) {
        return null;
      }

    };
    var configuration = LambdaLoggerConfiguration.builder().name("trace test logger")
        .loggerLevel(Level.TRACE).showDateTime(true).dateTimeFormat(dateTimeFormat).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    // when
    logger.error("test error message");

    // then
    verify(printStream).println("1/1/1970 0:00 ERROR test error message");
  }

  @DisplayName("Show AWS Request Id")
  @Test
  void requestId() {
    // given
    MDC.put(AWS_REQUEST_ID, "123-456-789-abc-0");
    var configuration = LambdaLoggerConfiguration.builder().name("error test logger")
        .loggerLevel(Level.ERROR).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    // when
    logger.error("test error message");

    // then
    verify(printStream).println("123-456-789-abc-0 ERROR test error message");
  }

  @DisplayName("Show a thread name")
  @Test
  void showThreadName() {
    // given
    Thread.currentThread().setName("test thread");
    var configuration = LambdaLoggerConfiguration.builder().name("error test logger")
        .loggerLevel(Level.ERROR).showThreadName(true).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    // when
    logger.error("test error message");

    // then
    verify(printStream).println("[test thread] ERROR test error message");
  }

  @DisplayName("Show a thread id")
  @Test
  void showThreadId() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("error test logger")
        .loggerLevel(Level.ERROR).showThreadId(true).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    // when
    logger.error("test error message");

    // then
    verify(printStream).println(matches("thread=\\d+ ERROR test error message"));
  }

  @DisplayName("Show a level in brackets")
  @Test
  void showLevelInBrackets() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("error test logger")
        .loggerLevel(Level.ERROR).levelInBrackets(true).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    // when
    logger.error("test error message");

    // then
    verify(printStream).println("[ERROR] test error message");
  }

  @DisplayName("Show a log name")
  @Test
  void showLogName() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("com.example.TestLogger")
        .loggerLevel(Level.ERROR).showLogName(true).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    // when
    logger.error("test error message");

    // then
    verify(printStream).println("ERROR com.example.TestLogger - test error message");
  }

  @DisplayName("Show a short log name")
  @Test
  void showShortLogName() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("com.example.TestLogger")
        .loggerLevel(Level.ERROR).showShortLogName(true).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    // when
    logger.error("test error message");

    // then
    verify(printStream).println("ERROR TestLogger - test error message");
  }

  @DisplayName("Show a short log name instead of full one")
  @Test
  void showShortLogNameInsteadOfFullOne() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("com.example.TestLogger")
        .loggerLevel(Level.ERROR).showShortLogName(true).showLogName(true).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    // when
    logger.error("test error message");

    // then
    verify(printStream).println("ERROR TestLogger - test error message");
  }

  @DisplayName("Print out markers")
  @Test
  void printOutMarkers() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("error test logger")
        .loggerLevel(Level.ERROR).build();
    var logger = spy(new LambdaLogger(configuration, printStream));
    var main = StaticMarkerBinder.getSingleton().getMarkerFactory().getMarker("Main");

    main.add(StaticMarkerBinder.getSingleton().getMarkerFactory().getMarker("Child"));

    // when
    logger.error(main, "test error message");

    // then
    verify(printStream).println("ERROR Main,Child test error message");
  }

  @DisplayName("Print a stack trace")
  @Test
  void stackTrace() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("error test logger")
        .loggerLevel(Level.ERROR).build();
    var logger = spy(new LambdaLogger(configuration, printStream));
    doAnswer(invocationOnMock -> {
      invocationOnMock.getArgument(0, PrintStream.class).println("*");
      return null;
    }).when(throwable).printStackTrace(isA(PrintStream.class));

    // when
    logger.error("test error message", throwable);

    // then
    verify(printStream).println("ERROR test error message");
    verify(printStream, times(2)).write(notNull(), anyInt(), anyInt());
  }

}
