package uk.bot_by.aws_lambda.slf4j;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import org.apache.commons.text.StringEscapeUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.slf4j.event.Level;
import uk.bot_by.aws_lambda.slf4j.LambdaLoggerUtil.WrappedPrintStream;

@ExtendWith(MockitoExtension.class)
@Tag("fast")
class LambdaLoggerUtilTest {

  @Captor
  private ArgumentCaptor<StringBuilder> messageBuilder;
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
        .loggerLevel(Level.ERROR).requestId("request#").build();

    // when
    LambdaLoggerUtil.log(configuration, printStream, Level.ERROR, "test error message", null);

    // then
    verify(printStream).println(messageBuilder.capture());
    verify(printStream).flush();
    assertEquals("ERROR test error message", messageBuilder.getValue().toString());
  }

  @DisplayName("CRLF injection")
  @ParameterizedTest
  @ValueSource(strings = {"\\r", "\\n", "\\r\\n"})
  void newLineInjection(String newLine) {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("error test logger")
        .loggerLevel(Level.ERROR).requestId("request#").build();
    var message = StringEscapeUtils.unescapeJava("test" + newLine + "error" + newLine + "message");

    // when
    LambdaLoggerUtil.log(configuration, printStream, Level.ERROR, message, null);

    // then
    verify(printStream).println(messageBuilder.capture());
    verify(printStream).flush();
    assertEquals("ERROR test\rerror\rmessage", messageBuilder.getValue().toString());
  }

  @DisplayName("Show relative time")
  @Test
  void relativeTime() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("trace test logger")
        .loggerLevel(Level.ERROR).showDateTime(true).requestId("request#").build();

    // when
    LambdaLoggerUtil.log(configuration, printStream, Level.ERROR, "test error message", null);

    // then
    verify(printStream).println(messageBuilder.capture());
    verify(printStream).flush();
    assertThat(messageBuilder.getValue().toString(),
        matchesPattern("\\d+ ERROR test error message"));
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
        .loggerLevel(Level.TRACE).showDateTime(true).dateTimeFormat(dateTimeFormat)
        .requestId("request#").build();

    // when
    LambdaLoggerUtil.log(configuration, printStream, Level.ERROR, "test error message", null);

    // then
    verify(printStream).println(messageBuilder.capture());
    verify(printStream).flush();
    assertEquals("1/1/1970 0:00 ERROR test error message", messageBuilder.getValue().toString());
  }

  @DisplayName("Show AWS request ID")
  @Test
  void requestId() {
    // given
    MDC.put("request#", "123-456-789-abc-0");
    var configuration = LambdaLoggerConfiguration.builder().name("error test logger")
        .loggerLevel(Level.ERROR).requestId("request#").build();

    // when
    LambdaLoggerUtil.log(configuration, printStream, Level.ERROR, "test error message", null);

    // then
    verify(printStream).println(messageBuilder.capture());
    verify(printStream).flush();
    assertEquals("123-456-789-abc-0 ERROR test error message",
        messageBuilder.getValue().toString());
  }

  @DisplayName("Show a thread name")
  @Test
  void showThreadName() {
    // given
    Thread.currentThread().setName("test thread");
    var configuration = LambdaLoggerConfiguration.builder().name("error test logger")
        .loggerLevel(Level.ERROR).showThreadName(true).requestId("request#").build();

    // when
    LambdaLoggerUtil.log(configuration, printStream, Level.ERROR, "test error message", null);

    // then
    verify(printStream).println(messageBuilder.capture());
    verify(printStream).flush();
    assertEquals("[test thread] ERROR test error message", messageBuilder.getValue().toString());
  }

  @DisplayName("Show a thread id")
  @Test
  void showThreadId() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("error test logger")
        .loggerLevel(Level.ERROR).showThreadId(true).requestId("request#").build();

    // when
    LambdaLoggerUtil.log(configuration, printStream, Level.ERROR, "test error message", null);

    // then
    verify(printStream).println(messageBuilder.capture());
    verify(printStream).flush();
    assertThat(messageBuilder.getValue().toString(),
        matchesPattern("thread=\\d+ ERROR test error message"));
  }

  @DisplayName("Show a level in brackets")
  @Test
  void showLevelInBrackets() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("error test logger")
        .loggerLevel(Level.ERROR).levelInBrackets(true).requestId("request#").build();

    // when
    LambdaLoggerUtil.log(configuration, printStream, Level.ERROR, "test error message", null);

    // then
    verify(printStream).println(messageBuilder.capture());
    verify(printStream).flush();
    assertEquals("[ERROR] test error message", messageBuilder.getValue().toString());
  }

  @DisplayName("Show a log name")
  @Test
  void showLogName() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("com.example.TestLogger")
        .loggerLevel(Level.ERROR).showLogName(true).requestId("request#").build();

    // when
    LambdaLoggerUtil.log(configuration, printStream, Level.ERROR, "test error message", null);

    // then
    verify(printStream).println(messageBuilder.capture());
    verify(printStream).flush();
    assertEquals("ERROR com.example.TestLogger - test error message",
        messageBuilder.getValue().toString());
  }

  @DisplayName("Show a short log name")
  @Test
  void showShortLogName() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("com.example.TestLogger")
        .loggerLevel(Level.ERROR).showShortLogName(true).requestId("request#").build();

    // when
    LambdaLoggerUtil.log(configuration, printStream, Level.ERROR, "test error message", null);

    // then
    verify(printStream).println(messageBuilder.capture());
    verify(printStream).flush();
    assertEquals("ERROR TestLogger - test error message", messageBuilder.getValue().toString());
  }

  @DisplayName("Show a short log name instead of full one")
  @Test
  void showShortLogNameInsteadOfFullOne() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("com.example.TestLogger")
        .loggerLevel(Level.ERROR).showShortLogName(true).showLogName(true).requestId("request#")
        .build();

    // when
    LambdaLoggerUtil.log(configuration, printStream, Level.ERROR, "test error message", null);

    // then
    verify(printStream).println(messageBuilder.capture());
    verify(printStream).flush();
    assertEquals("ERROR TestLogger - test error message", messageBuilder.getValue().toString());
  }

  @DisplayName("Print a stack trace")
  @Test
  void stackTrace() {
    // given
    var configuration = LambdaLoggerConfiguration.builder().name("error test logger")
        .loggerLevel(Level.ERROR).requestId("request#").build();
    var throwable = mock(Throwable.class);
    doAnswer(invocationOnMock -> {
      invocationOnMock.getArgument(0, PrintStream.class).println("*");
      return null;
    }).when(throwable).printStackTrace(isA(PrintStream.class));

    // when
    LambdaLoggerUtil.log(configuration, printStream, Level.ERROR, "test error message", throwable);

    // then
    verify(printStream).println(messageBuilder.capture());
    verify(printStream).flush();
    assertThat(messageBuilder.getValue().toString(), startsWith("ERROR test error message\r*"));
  }

  @DisplayName("Wrapped print stream")
  @Test
  void wrappedPrintStream() {
    // given
    var exception = new Exception("Wrapped Print Stream Test");
    var outputPrintStream = new ByteArrayOutputStream();

    // when
    exception.printStackTrace(new WrappedPrintStream(new PrintStream(outputPrintStream)));

    // then
    assertThat(outputPrintStream.toString(), startsWith(
        "java.lang.Exception: Wrapped Print Stream Test\r"
            + "\tat uk.bot_by.aws_lambda.slf4j.LambdaLoggerUtilTest.wrappedPrintStream"
            + "(LambdaLoggerUtilTest.java:"));
  }

}
