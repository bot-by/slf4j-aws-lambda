package uk.bot_by.aws_lambda.slf4j;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.slf4j.event.Level;

@ExtendWith(MockitoExtension.class)
@Tag("fast")
class AWSLambdaLoggerUtilTest {

  @Captor
  private ArgumentCaptor<String> stringCaptor;
  @Mock
  private LambdaLogger lambdaLogger;

  @AfterEach
  void tearDown() {
    MDC.clear();
  }

  @DisplayName("Default log message")
  @Test
  void logMessage() {
    // given
    var configuration = AWSLambdaLoggerConfiguration.builder().name("error test logger")
        .loggerLevel(Level.ERROR).requestId("request#").build();

    // when
    AWSLambdaLoggerUtil.log(configuration, lambdaLogger, Level.ERROR, "test error message", null);

    // then
    verify(lambdaLogger).log(stringCaptor.capture());

    assertEquals("ERROR test error message", stringCaptor.getValue());
  }

  @DisplayName("Show relative time")
  @Test
  void relativeTime() {
    // given
    var configuration = AWSLambdaLoggerConfiguration.builder().name("trace test logger")
        .loggerLevel(Level.ERROR).showDateTime(true).requestId("request#").build();

    // when
    AWSLambdaLoggerUtil.log(configuration, lambdaLogger, Level.ERROR, "test error message", null);

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
    var configuration = AWSLambdaLoggerConfiguration.builder().name("trace test logger")
        .loggerLevel(Level.TRACE).showDateTime(true).dateTimeFormat(dateTimeFormat)
        .requestId("request#").build();

    // when
    AWSLambdaLoggerUtil.log(configuration, lambdaLogger, Level.ERROR, "test error message", null);

    // then
    verify(lambdaLogger).log(stringCaptor.capture());

    assertEquals("1/1/1970 0:00 ERROR test error message", stringCaptor.getValue());
  }

  @DisplayName("Show AWS request ID")
  @Test
  void requestId() {
    // given
    MDC.put("request#", "123-456-789-abc-0");
    var configuration = AWSLambdaLoggerConfiguration.builder().name("error test logger")
        .loggerLevel(Level.ERROR).requestId("request#").build();

    // when
    AWSLambdaLoggerUtil.log(configuration, lambdaLogger, Level.ERROR, "test error message", null);

    // then
    verify(lambdaLogger).log(stringCaptor.capture());

    assertEquals("123-456-789-abc-0 ERROR test error message", stringCaptor.getValue());
  }

  @DisplayName("Show a thread name")
  @Test
  void showThreadName() {
    // given
    Thread.currentThread().setName("test thread");
    var configuration = AWSLambdaLoggerConfiguration.builder().name("error test logger")
        .loggerLevel(Level.ERROR).showThreadName(true).requestId("request#").build();

    // when
    AWSLambdaLoggerUtil.log(configuration, lambdaLogger, Level.ERROR, "test error message", null);

    // then
    verify(lambdaLogger).log(stringCaptor.capture());

    assertEquals("[test thread] ERROR test error message", stringCaptor.getValue());
  }

  @DisplayName("Show a thread id")
  @Test
  void showThreadId() {
    // given
    var configuration = AWSLambdaLoggerConfiguration.builder().name("error test logger")
        .loggerLevel(Level.ERROR).showThreadId(true).requestId("request#").build();

    // when
    AWSLambdaLoggerUtil.log(configuration, lambdaLogger, Level.ERROR, "test error message", null);

    // then
    verify(lambdaLogger).log(stringCaptor.capture());

    assertThat(stringCaptor.getValue(), matchesPattern("thread=\\d+ ERROR test error message"));
  }

  @DisplayName("Show a level in brackets")
  @Test
  void showLevelInBrackets() {
    // given
    var configuration = AWSLambdaLoggerConfiguration.builder().name("error test logger")
        .loggerLevel(Level.ERROR).levelInBrackets(true).requestId("request#").build();

    // when
    AWSLambdaLoggerUtil.log(configuration, lambdaLogger, Level.ERROR, "test error message", null);

    // then
    verify(lambdaLogger).log(stringCaptor.capture());

    assertEquals("[ERROR] test error message", stringCaptor.getValue());
  }

  @DisplayName("Show a log name")
  @Test
  void showLogName() {
    // given
    var configuration = AWSLambdaLoggerConfiguration.builder().name("com.example.TestLogger")
        .loggerLevel(Level.ERROR).showLogName(true).requestId("request#").build();

    // when
    AWSLambdaLoggerUtil.log(configuration, lambdaLogger, Level.ERROR, "test error message", null);

    // then
    verify(lambdaLogger).log(stringCaptor.capture());

    assertEquals("ERROR com.example.TestLogger - test error message", stringCaptor.getValue());
  }

  @DisplayName("Show a short log name")
  @Test
  void showShortLogName() {
    // given
    var configuration = AWSLambdaLoggerConfiguration.builder().name("com.example.TestLogger")
        .loggerLevel(Level.ERROR).showShortLogName(true).requestId("request#").build();

    // when
    AWSLambdaLoggerUtil.log(configuration, lambdaLogger, Level.ERROR, "test error message", null);

    // then
    verify(lambdaLogger).log(stringCaptor.capture());

    assertEquals("ERROR TestLogger - test error message", stringCaptor.getValue());
  }

  @DisplayName("Show a short log name instead of full one")
  @Test
  void showShortLogNameInsteadOfFullOne() {
    // given
    var configuration = AWSLambdaLoggerConfiguration.builder().name("com.example.TestLogger")
        .loggerLevel(Level.ERROR).showShortLogName(true).showLogName(true).requestId("request#")
        .build();

    // when
    AWSLambdaLoggerUtil.log(configuration, lambdaLogger, Level.ERROR, "test error message", null);

    // then
    verify(lambdaLogger).log(stringCaptor.capture());

    assertEquals("ERROR TestLogger - test error message", stringCaptor.getValue());
  }

  @DisplayName("Print a stack trace")
  @Test
  void stackTrace() {
    // given
    var configuration = AWSLambdaLoggerConfiguration.builder().name("error test logger")
        .loggerLevel(Level.ERROR).requestId("request#").build();
    var throwable = mock(Throwable.class);
    doAnswer(invocationOnMock -> {
      invocationOnMock.getArgument(0, PrintStream.class).println("*");
      return null;
    }).when(throwable).printStackTrace(isA(PrintStream.class));

    // when
    AWSLambdaLoggerUtil.log(configuration, lambdaLogger, Level.ERROR, "test error message",
        throwable);

    // then
    verify(lambdaLogger).log(stringCaptor.capture());

    assertThat(stringCaptor.getValue(), startsWith("ERROR test error message\n*"));
  }

}
