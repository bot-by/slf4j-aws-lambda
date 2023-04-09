package uk.bot_by.aws_lambda.slf4j;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.example.ServiceProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("slow")
class AWSLambdaLoggerFactoryTest {

  @DisplayName("Custom output service provider")
  @Test
  void customOutputServiceProvider() {
    // when
    var serviceProvider = (ServiceProvider) assertDoesNotThrow(
        () -> AWSLambdaLoggerFactory.getOutputServiceProvider(ServiceProvider.class));
    var message = serviceProvider.hello();

    // then
    assertEquals("hello world", message);
  }

  @DisplayName("No output service providers")
  @Test
  void noOutputServiceProviders() {
    // when and then
    Exception exception = assertThrows(IllegalStateException.class,
        AWSLambdaLoggerFactory::getOutputServiceProvider);

    assertEquals("No AWS Lambda Logger providers were found", exception.getMessage());
  }

}