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

import java.io.PrintStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.event.Level;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;

/**
 * A SLF4J {@link org.slf4j.Logger} implementation for <a href="https://aws.amazon.com/lambda/">AWS
 * Lambda</a>. This is common with SLF4J Simple but supports MDC
 */
public class LambdaLogger extends MarkerIgnoringBase {

  public static final String AWS_REQUEST_ID = "AWSRequestId";

  private final LambdaLoggerConfiguration configuration;
  private final PrintStream printStream;

  public LambdaLogger(@NotNull LambdaLoggerConfiguration configuration,
      @NotNull PrintStream printStream) {
    this.configuration = configuration;
    this.printStream = printStream;
  }

  public String getName() {
    return configuration.name();
  }

  @Override
  public boolean isTraceEnabled() {
    return isLevelEnabled(Level.TRACE);
  }

  @Override
  public void trace(String message) {
    log(Level.TRACE, message, null);
  }

  @Override
  public void trace(String format, Object arg) {
    trace(format, arg, null);
  }

  @Override
  public void trace(String format, Object arg1, Object arg2) {
    formatAndLog(Level.TRACE, format, arg1, arg2);
  }

  @Override
  public void trace(String format, Object... arguments) {
    formatAndLog(Level.TRACE, format, arguments);
  }

  @Override
  public void trace(String message, Throwable throwable) {
    log(Level.TRACE, message, throwable);
  }

  @Override
  public boolean isDebugEnabled() {
    return isLevelEnabled(Level.DEBUG);
  }

  @Override
  public void debug(String message) {
    log(Level.DEBUG, message, null);
  }

  @Override
  public void debug(String format, Object arg) {
    debug(format, arg, null);
  }

  @Override
  public void debug(String format, Object arg1, Object arg2) {
    formatAndLog(Level.DEBUG, format, arg1, arg2);
  }

  @Override
  public void debug(String format, Object... arguments) {
    formatAndLog(Level.DEBUG, format, arguments);
  }

  @Override
  public void debug(String message, Throwable throwable) {
    log(Level.DEBUG, message, throwable);
  }

  @Override
  public boolean isInfoEnabled() {
    return isLevelEnabled(Level.INFO);
  }

  @Override
  public void info(String message) {
    log(Level.INFO, message, null);
  }

  @Override
  public void info(String format, Object arg) {
    info(format, arg, null);
  }

  @Override
  public void info(String format, Object arg1, Object arg2) {
    formatAndLog(Level.INFO, format, arg1, arg2);
  }

  @Override
  public void info(String format, Object... arguments) {
    formatAndLog(Level.INFO, format, arguments);
  }

  @Override
  public void info(String message, Throwable throwable) {
    log(Level.INFO, message, throwable);
  }

  @Override
  public boolean isWarnEnabled() {
    return isLevelEnabled(Level.WARN);
  }

  @Override
  public void warn(String message) {
    log(Level.WARN, message, null);
  }

  @Override
  public void warn(String format, Object arg) {
    warn(format, arg, null);
  }

  @Override
  public void warn(String format, Object arg1, Object arg2) {
    formatAndLog(Level.WARN, format, arg1, arg2);
  }

  @Override
  public void warn(String format, Object... arguments) {
    formatAndLog(Level.WARN, format, arguments);
  }

  @Override
  public void warn(String message, Throwable throwable) {
    log(Level.WARN, message, throwable);
  }

  @Override
  public boolean isErrorEnabled() {
    return isLevelEnabled(Level.ERROR);
  }

  @Override
  public void error(String message) {
    log(Level.ERROR, message, null);
  }

  @Override
  public void error(String format, Object arg) {
    error(format, arg, null);
  }

  @Override
  public void error(String format, Object arg1, Object arg2) {
    formatAndLog(Level.ERROR, format, arg1, arg2);
  }

  @Override
  public void error(String format, Object... arguments) {
    formatAndLog(Level.ERROR, format, arguments);
  }

  @Override
  public void error(String message, Throwable throwable) {
    log(Level.ERROR, message, throwable);
  }

  private void formatAndLog(Level level, String format, Object... arguments) {
    if (!isLevelEnabled(level)) {
      return;
    }
    FormattingTuple formattingTuple = MessageFormatter.arrayFormat(format, arguments);
    log(level, formattingTuple.getMessage(), formattingTuple.getThrowable());
  }

  @VisibleForTesting
  void log(Level level, String message, Throwable throwable) {
    if (!isLevelEnabled(level)) {
      return;
    }
    LambdaLoggerUtil.log(configuration, printStream, level, message, throwable);
  }

  private boolean isLevelEnabled(Level level) {
    return level.toInt() >= configuration.loggerLevel().toInt();
  }

}
