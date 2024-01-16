/*
 * Copyright 2022-2024 Vitalij Berdinskih
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.bot_by.aws_lambda.slf4j.json_output;

import static java.util.Objects.nonNull;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.LambdaRuntime;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.json.JSONObject;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import uk.bot_by.aws_lambda.slf4j.AWSLambdaLoggerConfiguration;
import uk.bot_by.aws_lambda.slf4j.AWSLambdaLoggerOutput;

/**
 * An SLF4J Logger implementation for AWS Lambda with JSON output.
 */
public class JSONLoggerOutput implements AWSLambdaLoggerOutput {

  private static final String AWS_REQUEST_ID = "aws-request-id";
  private static final String RELATIVE_TIMESTAMP = "relative-timestamp";
  private static final String LEVEL = "level";
  private static final String LOGNAME = "logname";
  private static final String MARKERS = "markers";
  private static final String MESSAGE = "message";
  private static final String STACK_TRACE = "stack-trace";
  private static final Long START_TIME = System.currentTimeMillis();
  private static final String THREAD_ID = "thread-id";
  private static final String THREAD_NAME = "thread-name";
  private static final String THROWABLE_CLASS = "throwable-class";
  private static final String THROWABLE_MESSAGE = "throwable-message";
  private static final String TIMESTAMP = "timestamp";

  private static void addLevel(Level level, JSONObject jsonObject) {
    jsonObject.put(LEVEL, level);
  }

  private static void addLogName(AWSLambdaLoggerConfiguration configuration,
      JSONObject jsonObject) {
    if (nonNull(configuration.logName())) {
      jsonObject.put(LOGNAME, configuration.logName());
    }
  }

  private static void addMarkerAndReferences(Marker marker, JSONObject jsonObject) {
    if (nonNull(marker)) {
      var markers = new ArrayList<String>();

      marker.iterator().forEachRemaining(referenceMarker -> markers.add(referenceMarker.getName()));
      markers.add(0, marker.getName());
      jsonObject.put(MARKERS, markers);
    }
  }

  private static void addRequestId(AWSLambdaLoggerConfiguration configuration,
      JSONObject jsonObject) {
    if (nonNull(MDC.get(configuration.requestId()))) {
      jsonObject.put(AWS_REQUEST_ID, MDC.get(configuration.requestId()));
    }
  }

  private static void addThread(AWSLambdaLoggerConfiguration configuration, JSONObject jsonObject) {
    if (configuration.showThreadName()) {
      jsonObject.put(THREAD_NAME, Thread.currentThread().getName());
    }
    if (configuration.showThreadId()) {
      jsonObject.put(THREAD_ID, Thread.currentThread().getId());
    }
  }

  private static void addThrowable(Throwable throwable, JSONObject jsonObject) {
    if (nonNull(throwable)) {
      jsonObject.put(THROWABLE_CLASS, throwable.getClass().getName());
      if (nonNull(throwable.getMessage())) {
        jsonObject.put(THROWABLE_MESSAGE, throwable.getMessage());
      }

      var stackTraceOutputStream = new ByteArrayOutputStream();

      throwable.printStackTrace(new PrintStream(stackTraceOutputStream));
      jsonObject.put(STACK_TRACE, stackTraceOutputStream);
    }
  }

  @SuppressWarnings("ConstantConditions")
  private static void addTimestamp(AWSLambdaLoggerConfiguration configuration,
      JSONObject jsonObject) {
    if (configuration.showDateTime()) {
      if (nonNull(configuration.dateTimeFormat())) {
        jsonObject.put(TIMESTAMP, getFormattedDate(configuration.dateTimeFormat()));
      } else {
        jsonObject.put(RELATIVE_TIMESTAMP, System.currentTimeMillis() - START_TIME);
      }
    }
  }

  private static String getFormattedDate(DateFormat dateFormat) {
    String dateText;

    synchronized (START_TIME) {
      dateText = dateFormat.format(new Date());
    }

    return dateText;
  }

  /**
   * Write a message to the AWS lambda log in JSON.
   *
   * @param configuration logging configuration
   * @param marker        logging marker
   * @param level         logging level
   * @param message       logging message
   * @param throwable     exception
   */
  @Override
  public void log(@NotNull AWSLambdaLoggerConfiguration configuration, @Nullable Marker marker,
      @NotNull Level level, @NotNull String message, @Nullable Throwable throwable) {
    log(configuration, getLambdaLogger(), marker, level, message, throwable);
  }

  @VisibleForTesting
  LambdaLogger getLambdaLogger() {
    return LambdaRuntime.getLogger();
  }

  @VisibleForTesting
  void log(@NotNull AWSLambdaLoggerConfiguration configuration, @NotNull LambdaLogger lambdaLogger,
      @Nullable Marker marker, @NotNull Level level, @NotNull String message,
      @Nullable Throwable throwable) {
    JSONObject jsonObject = new JSONObject();

    addRequestId(configuration, jsonObject);
    addTimestamp(configuration, jsonObject);
    addThread(configuration, jsonObject);
    addMarkerAndReferences(marker, jsonObject);
    addLevel(level, jsonObject);
    addLogName(configuration, jsonObject);
    jsonObject.put(MESSAGE, message);
    addThrowable(throwable, jsonObject);

    synchronized (START_TIME) {
      lambdaLogger.log(jsonObject.toString());
    }
  }

}
