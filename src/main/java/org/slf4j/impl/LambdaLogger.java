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
package org.slf4j.impl;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Date;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

/**
 * A SLF4J {@link org.slf4j.Logger} implementation for <a href="https://aws.amazon.com/lambda/">AWS
 * Lambda</a>. This is common with SLF4J Simple but supports MDC
 */
public class LambdaLogger implements Logger {

  public static final String AWS_REQUEST_ID = "AWSRequestId";

  private static final char COMMA = ',';
  private static final String DOT = ".";
  private static final char LEFT_BRACKET = '[';
  private static final String LOG_NAME_SEPARATOR = " - ";
  private static final char RIGHT_BRACKET = ']';
  private static final long START_TIME = System.currentTimeMillis();
  private static final char SPACE = ' ';

  private static final String THREAD = "thread=";
  private final DateFormat dateTimeFormat;
  private final boolean levelInBrackets;
  private final Level loggerLevel;
  private final String logName;
  private final String name;
  private final PrintStream printStream;
  private final boolean showDateTime;
  private final boolean showThreadId;
  private final boolean showThreadName;

  private LambdaLogger(LambdaLoggerBuilder builder) {
    dateTimeFormat = builder.dateTimeFormat;
    levelInBrackets = builder.levelInBrackets;
    loggerLevel = builder.loggerLevel;
    name = builder.name;
    printStream = builder.printStream;
    if (builder.showShortLogName) {
      logName = name.substring(name.lastIndexOf(DOT) + 1);
    } else if (builder.showLogName) {
      logName = name;
    } else {
      logName = null;
    }
    showDateTime = builder.showDateTime;
    showThreadId = builder.showThreadId;
    showThreadName = builder.showThreadName;
  }

  static LambdaLoggerBuilder builder() {
    return new LambdaLoggerBuilder();
  }

  public String getName() {
    return name;
  }

  @Override
  public boolean isTraceEnabled() {
    return isLevelEnabled(Level.TRACE);
  }

  @Override
  public void trace(String message) {
    log(Level.TRACE, null, message, null);
  }

  @Override
  public void trace(String format, Object arg) {
    trace(format, arg, null);
  }

  @Override
  public void trace(String format, Object arg1, Object arg2) {
    formatAndLog(Level.TRACE, null, format, arg1, arg2);
  }

  @Override
  public void trace(String format, Object... arguments) {
    formatAndLog(Level.TRACE, null, format, arguments);
  }

  @Override
  public void trace(String message, Throwable throwable) {
    log(Level.TRACE, null, message, throwable);
  }

  @Override
  public boolean isTraceEnabled(Marker marker) {
    return isTraceEnabled();
  }

  @Override
  public void trace(Marker marker, String message) {
    log(Level.TRACE, marker, message, null);
  }

  @Override
  public void trace(Marker marker, String format, Object arg) {
    trace(marker, format, arg, null);
  }

  @Override
  public void trace(Marker marker, String format, Object arg1, Object arg2) {
    formatAndLog(Level.TRACE, marker, format, arg1, arg2);
  }

  @Override
  public void trace(Marker marker, String format, Object... arguments) {
    formatAndLog(Level.TRACE, marker, format, arguments);
  }

  @Override
  public void trace(Marker marker, String message, Throwable throwable) {
    log(Level.TRACE, marker, message, throwable);
  }

  @Override
  public boolean isDebugEnabled() {
    return isLevelEnabled(Level.DEBUG);
  }

  @Override
  public void debug(String message) {
    log(Level.DEBUG, null, message, null);
  }

  @Override
  public void debug(String format, Object arg) {
    debug(format, arg, null);
  }

  @Override
  public void debug(String format, Object arg1, Object arg2) {
    formatAndLog(Level.DEBUG, null, format, arg1, arg2);
  }

  @Override
  public void debug(String format, Object... arguments) {
    formatAndLog(Level.DEBUG, null, format, arguments);
  }

  @Override
  public void debug(String message, Throwable throwable) {
    log(Level.DEBUG, null, message, throwable);
  }

  @Override
  public boolean isDebugEnabled(Marker marker) {
    return isDebugEnabled();
  }

  @Override
  public void debug(Marker marker, String message) {
    log(Level.DEBUG, marker, message, null);
  }

  @Override
  public void debug(Marker marker, String format, Object arg) {
    debug(marker, format, arg, null);
  }

  @Override
  public void debug(Marker marker, String format, Object arg1, Object arg2) {
    formatAndLog(Level.DEBUG, marker, format, arg1, arg2);
  }

  @Override
  public void debug(Marker marker, String format, Object... arguments) {
    formatAndLog(Level.DEBUG, marker, format, arguments);
  }

  @Override
  public void debug(Marker marker, String message, Throwable throwable) {
    log(Level.DEBUG, marker, message, throwable);
  }

  @Override
  public boolean isInfoEnabled() {
    return isLevelEnabled(Level.INFO);
  }

  @Override
  public void info(String message) {
    log(Level.INFO, null, message, null);
  }

  @Override
  public void info(String format, Object arg) {
    info(format, arg, null);
  }

  @Override
  public void info(String format, Object arg1, Object arg2) {
    formatAndLog(Level.INFO, null, format, arg1, arg2);
  }

  @Override
  public void info(String format, Object... arguments) {
    formatAndLog(Level.INFO, null, format, arguments);
  }

  @Override
  public void info(String message, Throwable throwable) {
    log(Level.INFO, null, message, throwable);
  }

  @Override
  public boolean isInfoEnabled(Marker marker) {
    return isInfoEnabled();
  }

  @Override
  public void info(Marker marker, String message) {
    log(Level.INFO, marker, message, null);
  }

  @Override
  public void info(Marker marker, String format, Object arg) {
    info(marker, format, arg, null);
  }

  @Override
  public void info(Marker marker, String format, Object arg1, Object arg2) {
    formatAndLog(Level.INFO, marker, format, arg1, arg2);
  }

  @Override
  public void info(Marker marker, String format, Object... arguments) {
    formatAndLog(Level.INFO, marker, format, arguments);
  }

  @Override
  public void info(Marker marker, String message, Throwable throwable) {
    log(Level.INFO, marker, message, throwable);
  }

  @Override
  public boolean isWarnEnabled() {
    return isLevelEnabled(Level.WARN);
  }

  @Override
  public void warn(String message) {
    log(Level.WARN, null, message, null);
  }

  @Override
  public void warn(String format, Object arg) {
    warn(format, arg, null);
  }

  @Override
  public void warn(String format, Object arg1, Object arg2) {
    formatAndLog(Level.WARN, null, format, arg1, arg2);
  }

  @Override
  public void warn(String format, Object... arguments) {
    formatAndLog(Level.WARN, null, format, arguments);
  }

  @Override
  public void warn(String message, Throwable throwable) {
    log(Level.WARN, null, message, throwable);
  }

  @Override
  public boolean isWarnEnabled(Marker marker) {
    return isWarnEnabled();
  }

  @Override
  public void warn(Marker marker, String message) {
    log(Level.WARN, marker, message, null);
  }

  @Override
  public void warn(Marker marker, String format, Object arg) {
    warn(marker, format, arg, null);
  }

  @Override
  public void warn(Marker marker, String format, Object arg1, Object arg2) {
    formatAndLog(Level.WARN, marker, format, arg1, arg2);
  }

  @Override
  public void warn(Marker marker, String format, Object... arguments) {
    formatAndLog(Level.WARN, marker, format, arguments);
  }

  @Override
  public void warn(Marker marker, String message, Throwable throwable) {
    log(Level.WARN, marker, message, throwable);
  }

  @Override
  public boolean isErrorEnabled() {
    return isLevelEnabled(Level.ERROR);
  }

  @Override
  public void error(String message) {
    log(Level.ERROR, null, message, null);
  }

  @Override
  public void error(String format, Object arg) {
    error(format, arg, null);
  }

  @Override
  public void error(String format, Object arg1, Object arg2) {
    formatAndLog(Level.ERROR, null, format, arg1, arg2);
  }

  @Override
  public void error(String format, Object... arguments) {
    formatAndLog(Level.ERROR, null, format, arguments);
  }

  @Override
  public void error(String message, Throwable throwable) {
    log(Level.ERROR, null, message, throwable);
  }

  @Override
  public boolean isErrorEnabled(Marker marker) {
    return isErrorEnabled();
  }

  @Override
  public void error(Marker marker, String message) {
    log(Level.ERROR, marker, message, null);
  }

  @Override
  public void error(Marker marker, String format, Object arg) {
    error(marker, format, arg, null);
  }

  @Override
  public void error(Marker marker, String format, Object arg1, Object arg2) {
    formatAndLog(Level.ERROR, marker, format, arg1, arg2);
  }

  @Override
  public void error(Marker marker, String format, Object... arguments) {
    formatAndLog(Level.ERROR, marker, format, arguments);
  }

  @Override
  public void error(Marker marker, String message, Throwable throwable) {
    log(Level.ERROR, marker, message, throwable);
  }

  private void formatAndLog(Level level, Marker marker, String format, Object arg1, Object arg2) {
    if (!isLevelEnabled(level)) {
      return;
    }
    FormattingTuple formattingTuple = MessageFormatter.format(format, arg1, arg2);
    log(level, marker, formattingTuple.getMessage(), formattingTuple.getThrowable());
  }

  private void formatAndLog(Level level, Marker marker, String format, Object... arguments) {
    if (!isLevelEnabled(level)) {
      return;
    }
    FormattingTuple formattingTuple = MessageFormatter.arrayFormat(format, arguments);
    log(level, marker, formattingTuple.getMessage(), formattingTuple.getThrowable());
  }

  private String getFormattedDate() {
    String dateText;

    synchronized (StaticLoggerBinder.getSingleton()) {
      dateText = dateTimeFormat.format(new Date());
    }

    return dateText;
  }

  private boolean isLevelEnabled(Level level) {
    return level.toInt() >= loggerLevel.toInt();
  }

  @VisibleForTesting
  void log(Level level, Marker marker, String message, Throwable throwable) {
    if (!isLevelEnabled(level)) {
      return;
    }

    StringBuilder builder = new StringBuilder();

    if (showDateTime) {
      if (nonNull(dateTimeFormat)) {
        builder.append(getFormattedDate());
      } else {
        builder.append(System.currentTimeMillis() - START_TIME);
      }
      builder.append(SPACE);
    } else if (nonNull(MDC.get(AWS_REQUEST_ID))) {
      builder.append(MDC.get(AWS_REQUEST_ID)).append(SPACE);
    }
    if (showThreadName) {
      builder.append(LEFT_BRACKET).append(Thread.currentThread().getName()).append(RIGHT_BRACKET)
          .append(SPACE);
    }
    if (showThreadId) {
      builder.append(THREAD).append(Thread.currentThread().getId()).append(SPACE);
    }
    if (levelInBrackets) {
      builder.append(LEFT_BRACKET);
    }
    builder.append(level);
    if (levelInBrackets) {
      builder.append(RIGHT_BRACKET);
    }
    builder.append(SPACE);
    if (nonNull(logName)) {
      builder.append(logName);
      builder.append(LOG_NAME_SEPARATOR);
    }
    if (nonNull(marker)) {
      builder.append(marker.getName());
      marker.iterator()
          .forEachRemaining(reference -> builder.append(COMMA).append(reference.getName()));
      builder.append(SPACE);
    }
    builder.append(message);

    write(builder.toString(), throwable);
  }

  private void write(String message, Throwable throwable) {
    synchronized (StaticLoggerBinder.getSingleton()) {
      printStream.println(message);
      if (nonNull(throwable)) {
        printStream.flush();
        throwable.printStackTrace(new WrappedPrintStream(printStream));
      }
      printStream.flush();
    }
  }

  /**
   * Package access allows only {@link LambdaLoggerFactory} to instantiate LambdaLogger instances.
   */
  static class LambdaLoggerBuilder {

    private DateFormat dateTimeFormat;
    private boolean levelInBrackets;
    private Level loggerLevel;
    private String name;
    private PrintStream printStream;
    private boolean showDateTime;
    private boolean showLogName;
    private boolean showShortLogName;
    private boolean showThreadId;
    private boolean showThreadName;

    private LambdaLoggerBuilder() {
    }

    LambdaLogger build() {
      requireNonNull(loggerLevel, "Log level is null");
      requireNonNull(name, "Logger name is null");
      requireNonNull(printStream, "Print stream is null");
      return new LambdaLogger(this);
    }

    LambdaLoggerBuilder dateTimeFormat(DateFormat dateTimeFormat) {
      this.dateTimeFormat = dateTimeFormat;
      return this;
    }

    LambdaLoggerBuilder levelInBrackets(boolean levelInBrackets) {
      this.levelInBrackets = levelInBrackets;
      return this;
    }

    LambdaLoggerBuilder loggerLevel(@NotNull Level level) {
      this.loggerLevel = level;
      return this;
    }

    LambdaLoggerBuilder name(@NotNull String name) {
      this.name = name;
      return this;
    }

    LambdaLoggerBuilder printStream(@NotNull PrintStream printStream) {
      this.printStream = printStream;
      return this;
    }

    LambdaLoggerBuilder showDateTime(boolean showDateTime) {
      this.showDateTime = showDateTime;
      return this;
    }

    LambdaLoggerBuilder showLogName(boolean showLogName) {
      this.showLogName = showLogName;
      return this;
    }

    LambdaLoggerBuilder showShortLogName(boolean showShortLogName) {
      this.showShortLogName = showShortLogName;
      return this;
    }

    LambdaLoggerBuilder showThreadId(boolean showThreadId) {
      this.showThreadId = showThreadId;
      return this;
    }

    LambdaLoggerBuilder showThreadName(boolean showThreadName) {
      this.showThreadName = showThreadName;
      return this;
    }

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
