package uk.bot_by.aws_lambda.slf4j.json_output;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import org.json.JSONObject;
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
class JSONLoggerOutputTest {

  @Mock
  private AWSLambdaLoggerConfiguration configuration;
  @Mock
  private LambdaLogger lambdaLogger;
  @Spy
  private JSONLoggerOutput loggerOutput;
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

    assertEquals("{\"level\":\"ERROR\",\"message\":\"test error message\"}",
        stringCaptor.getValue(), true);
  }

  @DisplayName("Marker")
  @Test
  void marker() {
    // given
    var marker = new BasicMarkerFactory().getMarker("aMarker");

    when(configuration.requestId()).thenReturn("request#");

    // when
    loggerOutput.log(configuration, lambdaLogger, marker, Level.ERROR, "test error message", null);

    // then
    verify(lambdaLogger).log(stringCaptor.capture());

    assertEquals(
        "{\"level\":\"ERROR\",\"markers\":[\"aMarker\"],\"message\":\"test error message\"}",
        stringCaptor.getValue(), true);
  }

  @DisplayName("Markers")
  @Test
  void markers() {
    // given
    var markerFactory = new BasicMarkerFactory();
    var marker = markerFactory.getMarker("aMarker");
    marker.add(markerFactory.getMarker("marker a"));
    marker.add(markerFactory.getMarker("marker 1"));
    marker.add(markerFactory.getMarker("marker *"));

    when(configuration.requestId()).thenReturn("request#");

    // when
    loggerOutput.log(configuration, lambdaLogger, marker, Level.ERROR, "test error message", null);

    // then
    verify(lambdaLogger).log(stringCaptor.capture());

    assertEquals(
        "{\"level\":\"ERROR\",\"markers\":[\"aMarker\",\"marker a\",\"marker 1\",\"marker *\"],\"message\":\"test error message\"}",
        stringCaptor.getValue(), true);
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

    var jsonObject = new JSONObject(stringCaptor.getValue());

    assertAll("relative time",
        () -> assertTrue(jsonObject.has("relative-timestamp"), "field exists"),
        () -> assertDoesNotThrow(() -> jsonObject.getLong("relative-timestamp"), "field value"));
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

    assertEquals(
        "{\"level\":\"ERROR\",\"message\":\"test error message\",\"timestamp\":\"1/1/1970 0:00\"}",
        stringCaptor.getValue(), true);
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

    assertEquals(
        "{\"level\":\"ERROR\",\"message\":\"test error message\",\"aws-request-id\":\"123-456-789-abc-0\"}",
        stringCaptor.getValue(), true);
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

    assertEquals(
        "{\"level\":\"ERROR\",\"message\":\"test error message\",\"thread-name\":\"test thread\"}",
        stringCaptor.getValue(), true);
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

    var jsonObject = new JSONObject(stringCaptor.getValue());

    assertAll("thread ID", () -> assertTrue(jsonObject.has("thread-id"), "field exists"),
        () -> assertDoesNotThrow(() -> jsonObject.getLong("thread-id"), "field value"));
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

    assertEquals(
        "{\"level\":\"ERROR\",\"message\":\"test error message\",\"logname\":\"com.example.TestLogger\"}",
        stringCaptor.getValue(), true);
  }

  @DisplayName("Print a throwable")
  @Test
  void throwableWithMessage() {
    // given
    var throwable = mock(Throwable.class);

    doAnswer(invocationOnMock -> {
      invocationOnMock.getArgument(0, PrintStream.class).println("*");
      return null;
    }).when(throwable).printStackTrace(isA(PrintStream.class));
    when(throwable.getMessage()).thenReturn("test message");
    when(configuration.requestId()).thenReturn("request#");

    // when
    loggerOutput.log(configuration, lambdaLogger, null, Level.ERROR, "test error message",
        throwable);

    // then
    verify(lambdaLogger).log(stringCaptor.capture());

    var jsonObject = new JSONObject(stringCaptor.getValue());

    assertAll("throwable", () -> assertTrue(jsonObject.has("throwable-class")),
        () -> assertThat("class", jsonObject.getString("throwable-class"),
            equalTo("java.lang.Throwable")), () -> assertTrue(jsonObject.has("throwable-message")),
        () -> assertThat("message", jsonObject.getString("throwable-message"),
            equalTo("test message")),
        () -> assertTrue(jsonObject.has("stack-trace"), "field exists"),
        () -> assertThat("field value", jsonObject.getString("stack-trace"), startsWith("*")));
  }

  @DisplayName("Print a throwable without message")
  @Test
  void throwableWithoutMessage() {
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

    var jsonObject = new JSONObject(stringCaptor.getValue());

    assertAll("throwable", () -> assertTrue(jsonObject.has("throwable-class")),
        () -> assertThat("class", jsonObject.getString("throwable-class"),
            equalTo("java.lang.Throwable")), () -> assertFalse(jsonObject.has("throwable-message")),
        () -> assertTrue(jsonObject.has("stack-trace"), "field exists"),
        () -> assertThat("field value", jsonObject.getString("stack-trace"), startsWith("*")));
  }

}