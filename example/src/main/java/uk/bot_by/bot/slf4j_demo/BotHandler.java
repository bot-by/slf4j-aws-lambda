package uk.bot_by.bot.slf4j_demo;

import static org.slf4j.impl.LambdaLogger.AWS_REQUEST_ID;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class BotHandler implements RequestHandler<Map<String, Object>, String> {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public String handleRequest(Map<String, Object> input, Context context) {
    MDC.put(AWS_REQUEST_ID, context.getAwsRequestId());
    logger.trace("trace message");
    logger.debug("debug message");
    logger.info("info message");
    logger.warn("warning message");
    logger.error("error message");
    return "done";
  }

}
