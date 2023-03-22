package uk.bot_by.aws_lambda.slf4j;

import static java.util.Objects.nonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
import org.slf4j.helpers.BasicMarkerFactory;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith({MockitoExtension.class, SystemStubsExtension.class})
@Tag("slow")
class EnvironmentVariablesTest {

  @SystemStub
  private EnvironmentVariables environment;
  @Mock
  private LambdaLogger lambdaLogger;
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

  @Disabled
  @DisplayName("Read logger properties from the environment, get logger then print out trace message")
  @Test
  void useEnvironmentVariables() {
    // given
    environment.set("LOG_DEFAULT_LEVEL", "Trace");

    var loggerFactory = spy(AWSLambdaLoggerFactory.class);

    doReturn(lambdaLogger).when(loggerFactory).getLambdaLogger();

    var logger = loggerFactory.getLogger("lambda.logger.test");

    // when
    logger.trace("trace message");

    // then
    verify(lambdaLogger).log(stringCaptor.capture());

    assertThat(stringCaptor.getValue(),
        matchesPattern("variables-request-id TRACE trace message[\\n\\r]+"));
  }

  @Disabled
  @DisplayName("Default log level with a marker")
  @Test
  void defaultLogLevelWithMarker() {
    // given
    environment.set("LOG_DEFAULT_LEVEL", "Trace@aMarker");

    var loggerFactory = spy(AWSLambdaLoggerFactory.class);
    var marker = new BasicMarkerFactory().getMarker("aMarker");

    doReturn(lambdaLogger).when(loggerFactory).getLambdaLogger();

    var logger = loggerFactory.getLogger("lambda.logger.test");

    // when
    logger.trace(marker, "trace message");

    // then
    verify(lambdaLogger).log(stringCaptor.capture());

    assertThat(stringCaptor.getValue(),
        matchesPattern("variables-request-id TRACE trace message[\\n\\r]+"));
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

    doReturn(lambdaLogger).when(loggerFactory).getLambdaLogger();

    var logger = loggerFactory.getLogger("lambda.logger.test");

    // when
    if (nonNull(marker)) {
      logger.trace(marker, "trace message");
    } else {
      logger.trace("trace message");
    }

    // then
    verify(lambdaLogger, never()).log(anyString());
  }

}
