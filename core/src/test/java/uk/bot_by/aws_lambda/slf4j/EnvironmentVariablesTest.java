package uk.bot_by.aws_lambda.slf4j;

import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.BasicMarkerFactory;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith({MockitoExtension.class, SystemStubsExtension.class})
@Tag("slow")
class EnvironmentVariablesTest {

  @SystemStub
  private EnvironmentVariables environment;
  @Captor
  private ArgumentCaptor<AWSLambdaLoggerConfiguration> configurationCaptor;
  @Captor
  private ArgumentCaptor<Level> levelCaptor;
  @Captor
  private ArgumentCaptor<Marker> markerCaptor;
  @Mock
  private AWSLambdaLoggerOutput output;
  @Captor
  private ArgumentCaptor<String> stringCaptor;

  @BeforeEach
  void setUp() {
    // override all properties
    environment.set("LOG_AWS_REQUEST_ID", "request-id");
    environment.set("LOG_SHOW_DATE_TIME", "false");
    environment.set("LOG_LEVEL_IN_BRACKETS", "false");
    environment.set("LOG_SHOW_NAME", "false");
    environment.set("LOG_SHOW_SHORT_NAME", "false");
    environment.set("LOG_SHOW_THREAD_ID", "false");
    environment.set("LOG_SHOW_THREAD_NAME", "false");
    MDC.put("request-id", "variables-request-id");
  }

  @AfterEach
  void tearDown() {
    MDC.clear();
  }

  @DisplayName("Read logger properties from the environment, get logger then print out trace message")
  @Test
  void useEnvironmentVariables() {
    // given
    environment.set("LOG_DEFAULT_LEVEL", "Trace");

    var loggerFactory = spy(AWSLambdaLoggerFactory.class);

    doReturn(output).when(loggerFactory).getOutput();

    var logger = loggerFactory.getLogger("lambda.logger.test");

    // when
    logger.trace("trace message");

    // then
    verify(output).log(configurationCaptor.capture(), isNull(), levelCaptor.capture(),
        stringCaptor.capture(), isNull());

    var configuration = configurationCaptor.getValue();

    assertAll("Environment variable",
        () -> assertTrue(configuration.isLevelEnabled(Level.TRACE), "trace is enabled"),
        () -> assertEquals(Level.TRACE, levelCaptor.getValue(), "level"),
        () -> assertEquals("trace message", stringCaptor.getValue(), "message"));
  }

  @DisplayName("Default log level with a marker")
  @Test
  void defaultLogLevelWithMarker() {
    // given
    environment.set("LOG_DEFAULT_LEVEL", "Trace@aMarker");

    var loggerFactory = spy(AWSLambdaLoggerFactory.class);
    var marker = new BasicMarkerFactory().getMarker("aMarker");

    doReturn(output).when(loggerFactory).getOutput();

    var logger = loggerFactory.getLogger("lambda.logger.test");

    // when
    logger.trace(marker, "trace message");

    // then
    verify(output).log(configurationCaptor.capture(), markerCaptor.capture(), levelCaptor.capture(),
        stringCaptor.capture(), isNull());

    var configuration = configurationCaptor.getValue();

    assertAll("Default level with a marker",
        () -> assertNotNull(markerCaptor.getValue(), "marker not null"),
        () -> assertEquals("aMarker", markerCaptor.getValue().getName(), "marker"),
        () -> assertFalse(configuration.isLevelEnabled(Level.TRACE), "level without a marker"),
        () -> assertTrue(configuration.isLevelEnabled(Level.TRACE, marker), "level with a marker"),
        () -> assertEquals(Level.TRACE, levelCaptor.getValue(), "level"),
        () -> assertEquals("trace message", stringCaptor.getValue(), "message"));
  }

  @DisplayName("Implement NONE/OFF level")
  @ParameterizedTest
  @NullSource
  @ValueSource(strings = "aMarker")
  void implementNoneLevel(String markerName) {
    // given
    environment.set("LOG_DEFAULT_LEVEL", "Trace@none");

    var loggerFactory = spy(AWSLambdaLoggerFactory.class);
    var marker = (Marker) null;

    if (nonNull(markerName)) {
      marker = new BasicMarkerFactory().getMarker("aMarker");
    }

    doReturn(output).when(loggerFactory).getOutput();

    var logger = loggerFactory.getLogger("lambda.logger.test");

    // when
    if (nonNull(marker)) {
      logger.trace(marker, "trace message");
    } else {
      logger.trace("trace message");
    }

    // then
    verify(output, never()).log(any(), any(), any(), anyString(), any());
  }

  @DisplayName("Wrong a date-time format")
  @Test
  void wrongDateTimeFormat() {
    // given
    environment.set("LOG_DATE_TIME_FORMAT", "qwerty");
    environment.set("LOG_SHOW_DATE_TIME", "true");

    var loggerFactory = spy(AWSLambdaLoggerFactory.class);

    doReturn(output).when(loggerFactory).getOutput();

    var logger = loggerFactory.getLogger("lambda.logger.test");

    // when
    logger.warn("warn message");

    // then
    verify(output).log(configurationCaptor.capture(), isNull(), levelCaptor.capture(),
        stringCaptor.capture(), isNull());

    var configuration = configurationCaptor.getValue();

    assertAll("Timestamp", () -> assertTrue(configuration.showDateTime(), "timestamp"),
        () -> assertNull(configuration.dateTimeFormat(), "format"),
        () -> assertEquals(Level.WARN, levelCaptor.getValue(), "level"),
        () -> assertEquals("warn message", stringCaptor.getValue(), "message"));
  }

  @DisplayName("Wrong the default logger level")
  @Test
  void wrongDefaultLoggerLever() {
    // given
    environment.set("LOG_DEFAULT_LEVEL", "qwerty");

    var loggerFactory = spy(AWSLambdaLoggerFactory.class);

    doReturn(output).when(loggerFactory).getOutput();

    var logger = loggerFactory.getLogger("lambda.logger.test");

    // when
    logger.trace("trace message");
    logger.debug("debug message");

    // then
    verify(output).log(configurationCaptor.capture(), isNull(), levelCaptor.capture(),
        stringCaptor.capture(), isNull());

    var configuration = configurationCaptor.getValue();

    assertAll("Default level",
        () -> assertFalse(configuration.isLevelEnabled(Level.TRACE), "trace is disabled"),
        () -> assertTrue(configuration.isLevelEnabled(Level.DEBUG), "debug is enabled"),
        () -> assertEquals(Level.DEBUG, levelCaptor.getValue(), "level"),
        () -> assertEquals("debug message", stringCaptor.getValue(), "message"));
  }

}
