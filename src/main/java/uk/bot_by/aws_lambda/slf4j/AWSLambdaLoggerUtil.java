/*
 * Copyright 2022-2023 Witalij Berdinskich
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
package uk.bot_by.aws_lambda.slf4j;

import static java.util.Objects.nonNull;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.LambdaRuntime;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.ServiceLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.MDC;
import org.slf4j.event.Level;

/**
 * The utility class.
 */
public class AWSLambdaLoggerUtil {

  private static final Long START_TIME = System.currentTimeMillis();

  private static final char LEFT_BRACKET = '[';
  private static final String LOG_NAME_SEPARATOR = " - ";
  private static final char RIGHT_BRACKET = ']';
  private static final char SPACE = ' ';
  private static final String THREAD = "thread=";

  private AWSLambdaLoggerUtil() {
  }

  /**
   * Write a message to the AWS lambda log.
   *
   * @param configuration logging configuration
   * @param level         logger level
   * @param message       logging message
   * @param throwable     exception
   */
  public static void log(@NotNull AWSLambdaLoggerConfiguration configuration, @NotNull Level level,
      @NotNull String message, @Nullable Throwable throwable) {
    log(configuration, LambdaRuntime.getLogger(), level, message, throwable);
  }

  static AWSLambdaLoggerOutput getOutputServiceProvider() {
    return getOutputServiceProvider(AWSLambdaLoggerOutput.class);
  }

  @SuppressWarnings("SameParameterValue")
  @VisibleForTesting
  static AWSLambdaLoggerOutput getOutputServiceProvider(
      Class<? extends AWSLambdaLoggerOutput> clazz) {
    var serviceProviders = ServiceLoader.load(clazz, AWSLambdaLoggerUtil.class.getClassLoader())
        .iterator();
    if (serviceProviders.hasNext()) {
      return serviceProviders.next();
    } else {
      return (AWSLambdaLoggerUtil::log);
    }
  }

  @VisibleForTesting
  static void log(@NotNull AWSLambdaLoggerConfiguration configuration,
      @NotNull LambdaLogger lambdaLogger, @NotNull Level level, @NotNull String message,
      @Nullable Throwable throwable) {
    StringBuilder builder = new StringBuilder();

    addRequestId(configuration, builder);
    addTimestamp(configuration, builder);
    addThread(configuration, builder);
    addLevel(configuration, level, builder);
    addLogName(configuration, builder);
    builder.append(message);
    if (nonNull(throwable)) {
      var stackTraceOutputStream = new ByteArrayOutputStream();

      throwable.printStackTrace(new PrintStream(stackTraceOutputStream));
      builder.append(System.lineSeparator()).append(stackTraceOutputStream);
    }

    synchronized (START_TIME) {
      lambdaLogger.log(builder.toString());
    }
  }

  private static void addLevel(AWSLambdaLoggerConfiguration configuration, Level level,
      StringBuilder builder) {
    if (configuration.levelInBrackets()) {
      builder.append(LEFT_BRACKET).append(level).append(RIGHT_BRACKET);
    } else {
      builder.append(level);
    }
    builder.append(SPACE);
  }

  private static void addLogName(AWSLambdaLoggerConfiguration configuration,
      StringBuilder builder) {
    if (nonNull(configuration.logName())) {
      builder.append(configuration.logName());
      builder.append(LOG_NAME_SEPARATOR);
    }
  }

  private static void addRequestId(AWSLambdaLoggerConfiguration configuration,
      StringBuilder builder) {
    if (nonNull(MDC.get(configuration.requestId()))) {
      builder.append(MDC.get(configuration.requestId())).append(SPACE);
    }
  }

  private static void addThread(AWSLambdaLoggerConfiguration configuration, StringBuilder builder) {
    if (configuration.showThreadName()) {
      builder.append(LEFT_BRACKET).append(Thread.currentThread().getName()).append(RIGHT_BRACKET)
          .append(SPACE);
    }
    if (configuration.showThreadId()) {
      builder.append(THREAD).append(Thread.currentThread().getId()).append(SPACE);
    }
  }

  private static void addTimestamp(AWSLambdaLoggerConfiguration configuration,
      StringBuilder builder) {
    if (configuration.showDateTime()) {
      if (nonNull(configuration.dateTimeFormat())) {
        builder.append(getFormattedDate(configuration.dateTimeFormat()));
      } else {
        builder.append(System.currentTimeMillis() - START_TIME);
      }
      builder.append(SPACE);
    }
  }

  private static String getFormattedDate(DateFormat dateFormat) {
    String dateText;

    synchronized (START_TIME) {
      dateText = dateFormat.format(new Date());
    }

    return dateText;
  }

}
