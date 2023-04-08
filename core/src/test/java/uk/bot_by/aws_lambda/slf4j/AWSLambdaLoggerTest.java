package uk.bot_by.aws_lambda.slf4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.slf4j.event.Level;

@ExtendWith(MockitoExtension.class)
@Tag("fast")
class AWSLambdaLoggerTest {

  @Mock
  private AWSLambdaLoggerOutput output;
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
    var logger = getLogger(level);

    // when and then
    assertEquals(enabled, logger.isTraceEnabled());
  }

  @DisplayName("Trace message")
  @Test
  void trace() {
    // given
    var logger = getSpiedLogger(Level.TRACE);

    doNothing().when(logger).log(isA(Level.class), anyString(), isNull());

    // when
    logger.trace("test trace message");

    // then
    verify(logger).log(Level.TRACE, "test trace message", null);
  }

  @DisplayName("Trace formatted message with an argument")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, false", "INFO, false", "WARN, false", "ERROR, false"})
  void trace1(Level level, boolean enabled) {
    // given
    var logger = getSpiedLogger(level);

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

  @DisplayName("Trace formatted message with two arguments")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, false", "INFO, false", "WARN, false", "ERROR, false"})
  void trace2(Level level, boolean enabled) {
    // given
    var logger = getSpiedLogger(level);

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

  @DisplayName("Trace formatted message with varargs")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, false", "INFO, false", "WARN, false", "ERROR, false"})
  void traceVarargs(Level level, boolean enabled) {
    // given
    var logger = getSpiedLogger(level);

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

  @DisplayName("Trace message with a throwable")
  @Test
  void traceThrowable() {
    // given
    var logger = getSpiedLogger(Level.ERROR);

    doNothing().when(logger).log(isA(Level.class), anyString(), isA(Throwable.class));

    // when
    logger.trace("test trace message", throwable);

    // then
    verify(logger).log(Level.TRACE, "test trace message", throwable);
  }

  @DisplayName("Debug is enabled")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO, false", "WARN, false", "ERROR, false"})
  void isDebugEnabled(Level level, boolean enabled) {
    // given
    var logger = getLogger(level);

    // when and then
    assertEquals(enabled, logger.isDebugEnabled());
  }

  @DisplayName("Debug message")
  @Test
  void debug() {
    // given
    var logger = getSpiedLogger(Level.DEBUG);

    doNothing().when(logger).log(isA(Level.class), anyString(), isNull());

    // when
    logger.debug("test debug message");

    // then
    verify(logger).log(Level.DEBUG, "test debug message", null);
  }

  @DisplayName("Debug formatted message with an argument")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO, false", "WARN, false", "ERROR, false"})
  void debug1(Level level, boolean enabled) {
    // given
    var logger = getSpiedLogger(level);

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

  @DisplayName("Debug formatted message with two arguments")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO, false", "WARN, false", "ERROR, false"})
  void debug2(Level level, boolean enabled) {
    // given
    var logger = getSpiedLogger(level);

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

  @DisplayName("Debug formatted message with varargs")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO, false", "WARN, false", "ERROR, false"})
  void debugVarargs(Level level, boolean enabled) {
    // given
    var logger = getSpiedLogger(level);

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

  @DisplayName("Debug message with a throwable")
  @Test
  void debugThrowable() {
    // given
    var logger = getSpiedLogger(Level.DEBUG);

    doNothing().when(logger).log(isA(Level.class), anyString(), isA(Throwable.class));

    // when
    logger.debug("test debug message", throwable);

    // then
    verify(logger).log(Level.DEBUG, "test debug message", throwable);
  }

  @DisplayName("Info is enabled")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO, true", "WARN, false", "ERROR, false"})
  void isInfoEnabled(Level level, boolean enabled) {
    // given
    var logger = getLogger(level);

    // when and then
    assertEquals(enabled, logger.isInfoEnabled());
  }

  @DisplayName("Info message")
  @Test
  void info() {
    // given
    var logger = getSpiedLogger(Level.INFO);

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
    var logger = getSpiedLogger(level);

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
    var logger = getSpiedLogger(level);

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
    var logger = getSpiedLogger(level);

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
    var logger = getSpiedLogger(Level.ERROR);

    doNothing().when(logger).log(isA(Level.class), anyString(), isA(Throwable.class));

    // when
    logger.info("test info message", throwable);

    // then
    verify(logger).log(Level.INFO, "test info message", throwable);
  }

  @DisplayName("Warn is enabled")
  @ParameterizedTest
  @CsvSource({"TRACE, true", "DEBUG, true", "INFO, true", "WARN, true", "ERROR, false"})
  void isWarnEnabled(Level level, boolean enabled) {
    // given
    var logger = getLogger(level);

    // when and then
    assertEquals(enabled, logger.isWarnEnabled());
  }

  @DisplayName("Warning message")
  @Test
  void warn() {
    // given
    var logger = getSpiedLogger(Level.WARN);

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
    var logger = getSpiedLogger(level);

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
    var logger = getSpiedLogger(level);

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
    var logger = getSpiedLogger(level);

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
    var logger = getSpiedLogger(Level.WARN);

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
    var logger = getLogger(level);

    // when and then
    assertTrue(logger.isErrorEnabled());
  }

  @DisplayName("Error message")
  @Test
  void error() {
    // given
    var logger = getSpiedLogger(Level.ERROR);

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
    var logger = getSpiedLogger(level);

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
    var logger = getSpiedLogger(level);

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
    var logger = getSpiedLogger(level);

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
    var logger = getSpiedLogger(Level.ERROR);

    doNothing().when(logger).log(isA(Level.class), anyString(), isA(Throwable.class));

    // when
    logger.error("test error message", throwable);

    // then
    verify(logger).log(Level.ERROR, "test error message", throwable);
  }

  @NotNull
  private AWSLambdaLogger getLogger(Level level) {
    var configuration = AWSLambdaLoggerConfiguration.builder().name("test logger")
        .loggerLevel(level).requestId("request#").build();

    return new AWSLambdaLogger(configuration, output);
  }

  @NotNull
  private AWSLambdaLogger getSpiedLogger(Level level) {
    return spy(getLogger(level));
  }

}
