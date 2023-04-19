package uk.bot_by.aws_lambda.slf4j.lambda_logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.BasicMarkerFactory;
import uk.bot_by.aws_lambda.slf4j.AWSLambdaLoggerConfiguration;

@ExtendWith(MockitoExtension.class)
@Tag("fast")
class LambdaLoggerOutputTest {

  @Mock
  private AWSLambdaLoggerConfiguration configuration;
  @Mock
  private LambdaLogger lambdaLogger;
  @Spy
  private LambdaLoggerOutput loggerOutput;
  @Captor
  private ArgumentCaptor<String> stringCaptor;

  @AfterEach
  void tearDown() {
    MDC.clear();
  }

  @DisplayName("Happy path")
  @Test
  void happyPath() {
    // given
    doNothing().when(loggerOutput).log(any(), any(), any(), any(), anyString(), any());

    // when and then
    assertDoesNotThrow(
        () -> loggerOutput.log(configuration, null, Level.ERROR, "test error message", null));

    verify(loggerOutput).log(isA(AWSLambdaLoggerConfiguration.class), isA(LambdaLogger.class),
        isNull(), isA(Level.class), anyString(), isNull());
  }

  @DisplayName("Happy path with a marker")
  @Test
  void happyPathWithMarker() {
    // given
    var marker = new BasicMarkerFactory().getMarker("aMarker");

    doNothing().when(loggerOutput).log(any(), any(), any(), any(), anyString(), any());

    // when and then
    assertDoesNotThrow(
        () -> loggerOutput.log(configuration, marker, Level.ERROR, "test error message", null));

    verify(loggerOutput).log(isA(AWSLambdaLoggerConfiguration.class), isA(LambdaLogger.class),
        isA(Marker.class), isA(Level.class), anyString(), isNull());
  }

  @DisplayName("Happy path with a throwable")
  @Test
  void happyPathWithThrowable() {
    // given
    var throwable = new Throwable("test throwable");

    doNothing().when(loggerOutput).log(any(), any(), any(), any(), anyString(), any());

    // when and then
    assertDoesNotThrow(
        () -> loggerOutput.log(configuration, null, Level.ERROR, "test error message", throwable));

    verify(loggerOutput).log(isA(AWSLambdaLoggerConfiguration.class), isA(LambdaLogger.class),
        isNull(), isA(Level.class), anyString(), isA(Throwable.class));
  }

  @DisplayName("Default log message")
  @Test
  void logMessage() {
    // given
    when(configuration.requestId()).thenReturn("request#");

    // when
    loggerOutput.log(configuration, lambdaLogger, null, Level.ERROR, "test error message", null);

    // then
    verify(lambdaLogger).log(stringCaptor.capture());

    assertEquals("ERROR test error message", stringCaptor.getValue());
  }

  @DisplayName("Show relative time")
  @Test
  void relativeTime() {
    // given
    when(configuration.requestId()).thenReturn("request#");
    when(configuration.showDateTime()).thenReturn(true);

    // when
    loggerOutput.log(configuration, lambdaLogger, null, Level.ERROR, "test error message", null);

    // then
    verify(lambdaLogger).log(stringCaptor.capture());

    assertThat(stringCaptor.getValue(), matchesPattern("\\d+ ERROR test error message"));
  }

  @DisplayName("Show timestamp")
  @Test
  void timestamp() {
    // given
    var dateTimeFormat = new DateFormat() {

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

    when(configuration.dateTimeFormat()).thenReturn(dateTimeFormat);
    when(configuration.requestId()).thenReturn("request#");
    when(configuration.showDateTime()).thenReturn(true);

    // when
    loggerOutput.log(configuration, lambdaLogger, null, Level.ERROR, "test error message", null);

    // then
    verify(lambdaLogger).log(stringCaptor.capture());

    assertEquals("1/1/1970 0:00 ERROR test error message", stringCaptor.getValue());
  }

  @DisplayName("Show AWS request ID")
  @Test
  void requestId() {
    // given
    MDC.put("request#", "123-456-789-abc-0");

    when(configuration.requestId()).thenReturn("request#");

    // when
    loggerOutput.log(configuration, lambdaLogger, null, Level.ERROR, "test error message", null);

    // then
    verify(lambdaLogger).log(stringCaptor.capture());

    assertEquals("123-456-789-abc-0 ERROR test error message", stringCaptor.getValue());
  }

  @DisplayName("Show a thread name")
  @Test
  void showThreadName() {
    // given
    Thread.currentThread().setName("test thread");

    when(configuration.requestId()).thenReturn("request#");
    when(configuration.showThreadName()).thenReturn(true);

    // when
    loggerOutput.log(configuration, lambdaLogger, null, Level.ERROR, "test error message", null);

    // then
    verify(lambdaLogger).log(stringCaptor.capture());

    assertEquals("[test thread] ERROR test error message", stringCaptor.getValue());
  }

  @DisplayName("Show a thread id")
  @Test
  void showThreadId() {
    // given
    when(configuration.requestId()).thenReturn("request#");
    when(configuration.showThreadId()).thenReturn(true);

    // when
    loggerOutput.log(configuration, lambdaLogger, null, Level.ERROR, "test error message", null);

    // then
    verify(lambdaLogger).log(stringCaptor.capture());

    assertThat(stringCaptor.getValue(), matchesPattern("thread=\\d+ ERROR test error message"));
  }

  @DisplayName("Show a level in brackets")
  @Test
  void showLevelInBrackets() {
    // given
    when(configuration.levelInBrackets()).thenReturn(true);
    when(configuration.requestId()).thenReturn("request#");

    // when
    loggerOutput.log(configuration, lambdaLogger, null, Level.ERROR, "test error message", null);

    // then
    verify(lambdaLogger).log(stringCaptor.capture());

    assertEquals("[ERROR] test error message", stringCaptor.getValue());
  }

  @DisplayName("Show a log name")
  @Test
  void showLogName() {
    // given
    when(configuration.logName()).thenReturn("com.example.TestLogger");
    when(configuration.requestId()).thenReturn("request#");

    // when
    loggerOutput.log(configuration, lambdaLogger, null, Level.ERROR, "test error message", null);

    // then
    verify(lambdaLogger).log(stringCaptor.capture());

    assertEquals("ERROR com.example.TestLogger - test error message", stringCaptor.getValue());
  }

  @DisplayName("Print a stack trace")
  @Test
  void stackTrace() {
    // given
    var throwable = mock(Throwable.class);

    doAnswer(invocationOnMock -> {
      invocationOnMock.getArgument(0, PrintStream.class).println("*");
      return null;
    }).when(throwable).printStackTrace(isA(PrintStream.class));
    when(configuration.requestId()).thenReturn("request#");

    // when
    loggerOutput.log(configuration, lambdaLogger, null, Level.ERROR, "test error message",
        throwable);

    // then
    verify(lambdaLogger).log(stringCaptor.capture());

    assertThat(stringCaptor.getValue(), startsWith("ERROR test error message\n*"));
  }

}