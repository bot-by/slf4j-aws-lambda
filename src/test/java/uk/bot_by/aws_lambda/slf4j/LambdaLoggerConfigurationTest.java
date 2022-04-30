package uk.bot_by.aws_lambda.slf4j;

import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.text.SimpleDateFormat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.event.Level;

@Tag("fast")
class LambdaLoggerConfigurationTest {

  @DisplayName("Logger level is required")
  @Test
  void loggerLevelIsRequired() {
    // given
    var builder = LambdaLoggerConfiguration.builder().name("test");

    // when
    Exception exception = assertThrows(NullPointerException.class, builder::build);

    // then
    assertEquals("Logger level is null", exception.getMessage());
  }

  @DisplayName("Logger name is required")
  @Test
  void nameIsRequired() {
    // given
    var builder = LambdaLoggerConfiguration.builder().loggerLevel(Level.TRACE);

    // when
    Exception exception = assertThrows(NullPointerException.class, builder::build);

    // then
    assertEquals("Logger name is null", exception.getMessage());
  }

  @DisplayName("Date and time format")
  @ParameterizedTest(name = "[{index}] Date format: {arguments}")
  @CsvSource(value = {"null", "MM/dd/yy HH:mm"}, nullValues = "null")
  void dateTimeFormat(String dateTimeFormat) {
    // given
    var builder = LambdaLoggerConfiguration.builder().name("test").loggerLevel(Level.TRACE);
    if (nonNull(dateTimeFormat)) {
      builder.dateTimeFormat(new SimpleDateFormat(dateTimeFormat));
    }

    // when
    var configuration = builder.build();

    // then
    if (nonNull(dateTimeFormat)) {
      assertNotNull(configuration.dateTimeFormat());
    } else {
      assertNull(configuration.dateTimeFormat());
    }
  }

  @DisplayName("Level in brackets")
  @ParameterizedTest(name = "[{index}] Level in brackets: {arguments}")
  @ValueSource(booleans = {true, false})
  void levelInBrackets(boolean levelInBrackets) {
    // given
    var builder = LambdaLoggerConfiguration.builder().name("test").loggerLevel(Level.TRACE);
    builder.levelInBrackets(levelInBrackets);

    // when
    var configuration = builder.build();

    // then
    assertEquals(levelInBrackets, configuration.levelInBrackets());
  }

  @DisplayName("Logger level")
  @ParameterizedTest(name = "[{index}] Logger level: {arguments}")
  @CsvSource({"TRACE", "DEBUG", "INFO", "WARN", "ERROR"})
  void loggerLevel(Level level) {
    // given
    var builder = LambdaLoggerConfiguration.builder().name("test").loggerLevel(Level.TRACE);
    builder.loggerLevel(level);

    // when
    var configuration = builder.build();

    // then
    assertEquals(level, configuration.loggerLevel());
  }

  @DisplayName("Name")
  @Test
  void name() {
    // given
    var builder = LambdaLoggerConfiguration.builder().name("test").loggerLevel(Level.TRACE);

    // when
    var configuration = builder.build();

    // then
    assertEquals("test", configuration.name());
  }

  @DisplayName("Show a short log name")
  @ParameterizedTest(name = "[{index}] Short log name: {arguments}")
  @ValueSource(booleans = {true, false})
  void showShortLogName(boolean showShortLogName) {
    // given
    var builder = LambdaLoggerConfiguration.builder().loggerLevel(Level.TRACE);
    builder.name("abc.xyz.TestLog").showShortLogName(showShortLogName);

    // when
    var configuration = builder.build();

    // then
    if (showShortLogName) {
      assertEquals("TestLog", configuration.logName());
    } else {
      assertNull(configuration.logName());
    }
  }

  @DisplayName("Show a log name")
  @ParameterizedTest(name = "[{index}] Log name: {arguments}")
  @ValueSource(booleans = {true, false})
  void showLogName(boolean showLogName) {
    // given
    var builder = LambdaLoggerConfiguration.builder().loggerLevel(Level.TRACE);
    builder.name("abc.xyz.TestLog").showLogName(showLogName);

    // when
    var configuration = builder.build();

    // then
    if (showLogName) {
      assertEquals("abc.xyz.TestLog", configuration.logName());
    } else {
      assertNull(configuration.logName());
    }
  }

  @DisplayName("Show a short log name instead of full one")
  @Test
  void showShortLogNameInsteadOfFullOne() {
    // given
    var builder = LambdaLoggerConfiguration.builder().loggerLevel(Level.TRACE);
    builder.name("abc.xyz.TestLog").showShortLogName(true).showLogName(true);

    // when
    var configuration = builder.build();

    // then
    assertEquals("TestLog", configuration.logName());
  }

  @DisplayName("Show date and time")
  @ParameterizedTest(name = "[{index}] Date and time: {arguments}")
  @ValueSource(booleans = {true, false})
  void showDateTime(boolean showDateTime) {
    // given
    var builder = LambdaLoggerConfiguration.builder().name("test").loggerLevel(Level.TRACE);
    builder.showDateTime(showDateTime);

    // when
    var configuration = builder.build();

    // then
    assertEquals(showDateTime, configuration.showDateTime());
  }

  @DisplayName("Show a thread id")
  @ParameterizedTest(name = "[{index}] Thread Id: {arguments}")
  @ValueSource(booleans = {true, false})
  void showThreadId(boolean showThreadId) {
    // given
    var builder = LambdaLoggerConfiguration.builder().name("test").loggerLevel(Level.TRACE);
    builder.showThreadId(showThreadId);

    // when
    var configuration = builder.build();

    // then
    assertEquals(showThreadId, configuration.showThreadId());
  }

  @DisplayName("Show a thread name")
  @ParameterizedTest(name = "[{index}] Thread name: {arguments}")
  @ValueSource(booleans = {true, false})
  void showThreadName(boolean showThreadName) {
    // given
    var builder = LambdaLoggerConfiguration.builder().name("test").loggerLevel(Level.TRACE);
    builder.showThreadName(showThreadName);

    // when
    var configuration = builder.build();

    // then
    assertEquals(showThreadName, configuration.showThreadName());
  }

}
