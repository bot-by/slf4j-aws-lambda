package org.slf4j.impl;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.slf4j.impl.LambdaLogger.AWS_REQUEST_ID;
import static org.slf4j.impl.LambdaLoggerUtil.log;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.slf4j.event.Level;

@ExtendWith(MockitoExtension.class)
@Tag("fast")
class LambdaLoggerUtilTest {

  @Mock
  private PrintStream printStream;

  @AfterEach
  void tearDown() {
    MDC.clear();
  }

  @DisplayName("Default log message")
  @Test
  void logMessage() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("error test logger")
        .loggerLevel(Level.ERROR).build();

    // when
    log(configuration, printStream, Level.ERROR, null, "test error message", null);

    // then
    verify(printStream).println("ERROR test error message");
    verify(printStream).flush();
  }

  @DisplayName("Silent")
  @Test
  @Disabled("we do not check level in the log()")
  void silent() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("error test logger")
        .loggerLevel(Level.ERROR).build();
    var logger = spy(new LambdaLogger(configuration, printStream));

    // when
    log(configuration, printStream, Level.ERROR, null, "test trace message", null);

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
    log(configuration, printStream, Level.ERROR, null, "test error message", null);

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
    log(configuration, printStream, Level.ERROR, null, "test error message", null);

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
    log(configuration, printStream, Level.ERROR, null, "test error message", null);

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
    log(configuration, printStream, Level.ERROR, null, "test error message", null);

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
    log(configuration, printStream, Level.ERROR, null, "test error message", null);

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
    log(configuration, printStream, Level.ERROR, null, "test error message", null);

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
    log(configuration, printStream, Level.ERROR, null, "test error message", null);

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
    log(configuration, printStream, Level.ERROR, null, "test error message", null);

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
    log(configuration, printStream, Level.ERROR, null, "test error message", null);

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
    log(configuration, printStream, Level.ERROR, main, "test error message", null);

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
    var throwable = mock(Throwable.class);
    doAnswer(invocationOnMock -> {
      invocationOnMock.getArgument(0, PrintStream.class).println("*");
      return null;
    }).when(throwable).printStackTrace(isA(PrintStream.class));

    // when
    log(configuration, printStream, Level.ERROR, null, "test error message", throwable);

    // then
    verify(printStream).println("ERROR test error message");
    verify(printStream, times(2)).write(notNull(), anyInt(), anyInt());
  }

}
