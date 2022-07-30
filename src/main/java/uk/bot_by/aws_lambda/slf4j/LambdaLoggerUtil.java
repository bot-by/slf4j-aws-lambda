/*
 * Copyright 2022 Witalij Berdinskich
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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Date;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.MDC;
import org.slf4j.event.Level;
import org.slf4j.impl.StaticLoggerBinder;

/**
 * Utility class.
 */
class LambdaLoggerUtil {

  private static final long START_TIME = System.currentTimeMillis();

  private static final String ANY_NEW_LINE = "[\r\n]+";
  private static final String CARRIAGE_RETURN = "\r";
  private static final char LEFT_BRACKET = '[';
  private static final String LOG_NAME_SEPARATOR = " - ";
  private static final char RIGHT_BRACKET = ']';
  private static final char SPACE = ' ';
  private static final String THREAD = "thread=";

  private LambdaLoggerUtil() {
  }

  /**
   * Log a message.
   *
   * @param configuration logger configuration
   * @param printStream   print stream
   * @param level         log level
   * @param message       formatted message
   * @param throwable     throwable
   */
  static void log(@NotNull LambdaLoggerConfiguration configuration,
      @NotNull PrintStream printStream, @NotNull Level level, @NotNull String message,
      @Nullable Throwable throwable) {
    StringBuilder builder = new StringBuilder();

    addRequestId(configuration, builder);
    addTimestampOrRequestId(configuration, builder);
    addThread(configuration, builder);
    addLevel(configuration, level, builder);
    addLogName(configuration, builder);
    builder.append(message.replaceAll(ANY_NEW_LINE, CARRIAGE_RETURN));
    if (nonNull(throwable)) {
      var stackTraceOutputStream = new ByteArrayOutputStream();

      throwable.printStackTrace(new WrappedPrintStream(new PrintStream(stackTraceOutputStream)));
      builder.append(CARRIAGE_RETURN).append(stackTraceOutputStream);
    }

    synchronized (StaticLoggerBinder.getSingleton()) {
      printStream.println(builder);
      printStream.flush();
    }
  }

  private static void addLevel(LambdaLoggerConfiguration configuration, Level level,
      StringBuilder builder) {
    if (configuration.levelInBrackets()) {
      builder.append(LEFT_BRACKET).append(level).append(RIGHT_BRACKET);
    } else {
      builder.append(level);
    }
    builder.append(SPACE);
  }

  private static void addLogName(LambdaLoggerConfiguration configuration, StringBuilder builder) {
    if (nonNull(configuration.logName())) {
      builder.append(configuration.logName());
      builder.append(LOG_NAME_SEPARATOR);
    }
  }

  private static void addRequestId(LambdaLoggerConfiguration configuration, StringBuilder builder) {
    if (nonNull(MDC.get(configuration.requestId()))) {
      builder.append(MDC.get(configuration.requestId())).append(SPACE);
    }
  }

  private static void addThread(LambdaLoggerConfiguration configuration, StringBuilder builder) {
    if (configuration.showThreadName()) {
      builder.append(LEFT_BRACKET).append(Thread.currentThread().getName()).append(RIGHT_BRACKET)
          .append(SPACE);
    }
    if (configuration.showThreadId()) {
      builder.append(THREAD).append(Thread.currentThread().getId()).append(SPACE);
    }
  }

  private static void addTimestampOrRequestId(LambdaLoggerConfiguration configuration,
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

    synchronized (StaticLoggerBinder.getSingleton()) {
      dateText = dateFormat.format(new Date());
    }

    return dateText;
  }

  static class WrappedPrintStream extends PrintStream {

    WrappedPrintStream(PrintStream printStream) {
      super(printStream);
    }

    @Override
    public void flush() {
      super.flush();
    }

    /**
     * This corrects how Cloud Watch handles the newline character.
     *
     * @param object The {@code Object} to be printed.
     */
    @Override
    public void println(Object object) {
      super.print(object);
      super.print(CARRIAGE_RETURN);
    }

  }

}
