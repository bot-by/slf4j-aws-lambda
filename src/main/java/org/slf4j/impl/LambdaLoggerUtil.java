package org.slf4j.impl;

import static java.util.Objects.nonNull;
import static org.slf4j.impl.LambdaLogger.AWS_REQUEST_ID;

import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Date;
import org.slf4j.MDC;
import org.slf4j.event.Level;

public class LambdaLoggerUtil {

  private static final long START_TIME = System.currentTimeMillis();

  private static final char LEFT_BRACKET = '[';
  private static final String LOG_NAME_SEPARATOR = " - ";
  private static final char RIGHT_BRACKET = ']';
  private static final char SPACE = ' ';
  private static final String THREAD = "thread=";

  private LambdaLoggerUtil() {
  }

  public static void log(LambdaLoggerConfiguration configuration, PrintStream printStream,
      Level level, String message, Throwable throwable) {
    StringBuilder builder = new StringBuilder();

    addTimestampOrRequestId(configuration, builder);
    addThread(configuration, builder);
    addLevel(configuration, level, builder);
    addLogName(configuration, builder);
    builder.append(message);

    synchronized (StaticLoggerBinder.getSingleton()) {
      printStream.println(builder);
      if (nonNull(throwable)) {
        printStream.flush();
        throwable.printStackTrace(new WrappedPrintStream(printStream));
      }
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
    } else if (nonNull(MDC.get(AWS_REQUEST_ID))) {
      builder.append(MDC.get(AWS_REQUEST_ID)).append(SPACE);
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

    private static final char CARRIAGE_RETURN = '\r';

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
