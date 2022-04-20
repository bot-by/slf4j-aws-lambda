package org.slf4j.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.mockito.Mockito.doReturn;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Tag("slow")
class LambdaLoggerFactoryTest {

  @Spy
  private LambdaLoggerFactory loggerFactory;

  @Test
  void getLogger() {
    // given
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream(100);
    PrintStream printStream = new PrintStream(outputStream);

    doReturn(printStream).when(loggerFactory).getPrintStream();

    // when
    loggerFactory.getLogger("lambda.logger.test").debug("debug message");

    // then
    printStream.flush();
    printStream.close();
    outputStream.toString(StandardCharsets.UTF_8);
    assertThat(outputStream.toString(StandardCharsets.UTF_8), matchesPattern(
        "\\d{2}:\\d{2}:\\d{2}\\.\\d{3} \\[main\\] thread=1 \\[DEBUG\\] test - debug message[\\n\\r]+"));
  }

}
