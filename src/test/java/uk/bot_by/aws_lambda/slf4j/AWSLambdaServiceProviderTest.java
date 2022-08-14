package uk.bot_by.aws_lambda.slf4j;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("fast")
class AWSLambdaServiceProviderTest {

  private AWSLambdaServiceProvider provider;

  @BeforeEach
  void setUp() {
    provider = new AWSLambdaServiceProvider();
  }

  @Test
  void initialize() {
    // given
    assertAll("Before initialization",
        () -> assertNull(provider.getLoggerFactory(), "logger factory"),
        () -> assertNull(provider.getMarkerFactory(), "marker factory"),
        () -> assertNull(provider.getMDCAdapter(), "context map adapter"));

    // when
    provider.initialize();

    // then
    assertAll("After initialization",
        () -> assertNotNull(provider.getLoggerFactory(), "logger factory"),
        () -> assertNotNull(provider.getMarkerFactory(), "marker factory"),
        () -> assertNotNull(provider.getMDCAdapter(), "context map adapter"));
  }

}
