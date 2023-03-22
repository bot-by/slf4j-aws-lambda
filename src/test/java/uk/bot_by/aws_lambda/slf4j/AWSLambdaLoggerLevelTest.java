package uk.bot_by.aws_lambda.slf4j;

import static java.util.Objects.isNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsArrayWithSize.arrayWithSize;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.converter.TypedArgumentConverter;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.event.Level;
import uk.bot_by.aws_lambda.slf4j.AWSLambdaLoggerLevel.Builder;

@Tag("fast")
class AWSLambdaLoggerLevelTest {

  @Test
  void builder() {
    // when and the
    assertNotNull(AWSLambdaLoggerLevel.builder());
  }

  @Test
  void level() {
    // given
    Builder builder = AWSLambdaLoggerLevel.builder();

    // when
    Exception exception = assertThrows(NullPointerException.class, builder::build);

    // then
    assertEquals("Logger level is null", exception.getMessage());
  }

  @ParameterizedTest
  @CsvSource(value = {"N/A, 0", "first, 1", "first|Second|THIRD, 3"}, nullValues = "N/A")
  void getMarkers(@ConvertWith(PipeSeparatedValues.class) String[] markerNames, int count) {
    // given
    Builder builder = AWSLambdaLoggerLevel.builder().level(Level.DEBUG);

    for (String markerName : markerNames) {
      builder.marker(markerName);
    }

    // when
    AWSLambdaLoggerLevel loggerLevel = assertDoesNotThrow(builder::build);

    // then
    assertThat(loggerLevel.getMarkers(), arrayWithSize(count));
  }

  static class PipeSeparatedValues extends TypedArgumentConverter<String, String[]> {

    /**
     * Create a new {@code PipeSeparatedValues}.
     */
    public PipeSeparatedValues() {
      super(String.class, String[].class);
    }

    /**
     * Convert a pipe-separated string to a string array.
     *
     * @param source the source object to convert; may be {@code null}
     * @return string array
     */
    @Override
    protected String[] convert(String source) {
      if (isNull(source)) {
        return new String[0];
      }

      return source.split("\\|");
    }

  }

}
