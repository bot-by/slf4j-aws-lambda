package org.example;

import uk.bot_by.aws_lambda.slf4j.AWSLambdaLoggerOutput;

public interface ServiceProvider extends AWSLambdaLoggerOutput {

  String hello();

}
