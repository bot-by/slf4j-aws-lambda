package uk.bot_by.slf4j_demo;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.util.Map;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.helpers.BasicMarkerFactory;

public class BotHandler implements RequestHandler<Map<String, Object>, String> {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  {
    logger.info("Load version: @project.version@");
  }

  @Override
  public String handleRequest(Map<String, Object> input, Context context) {
    MDC.put("@aws-request-id@", context.getAwsRequestId());

    logger.trace("trace message");
    logger.debug("debug message");
    logger.info("info message");
    logger.warn("warning message");
    logger.error("error message");

    var marker = new BasicMarkerFactory().getMarker("important");

    Stream.of("\n", "\r\n", "\r").forEach(injection -> {
      logger.trace(marker, "CRLF{}injection", injection);
    });

    logger.warn("printable stacktrace", new Throwable("Printable Stacktrace Demo"));

    MDC.clear();
    return "done";
  }

}
