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

import java.io.ObjectStreamException;
import java.io.PrintStream;
import java.io.Serializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.impl.StaticLoggerBinder;

/**
 * An SLF4J {@link org.slf4j.Logger} implementation for <a href="https://aws.amazon.com/lambda/">AWS
 * Lambda</a>.
 * <p>
 * This is common with SLF4J Simple but supports MDC. You could put AWS request ID to MDC then it is
 * printed out in start every log line:
 * <pre><code class="language-java">
 *   {@literal @}Override
 *   public String handleRequest({@literal Map<String, Object>} input, Context context) {
 *     MDC.put("AWS_REQUEST_ID", context.getAwsRequestId());
 *     ...
 *     logger.info("info message");
 *     ...
 *     return "done";
 *   }
 * </code></pre>
 * The log:
 * <pre><code class="language-log">
 * START RequestId: cc4eb5aa-66b4-42fc-b27a-138bd672b38a Version: $LATEST
 * cc4eb5aa-66b4-42fc-b27a-138bd672b38a INFO uk.bot_by.bot.slf4j_demo.BotHandler - info message
 * END RequestId: cc4eb5aa-66b4-42fc-b27a-138bd672b38a
 * </code></pre>
 *
 * @see LambdaLoggerConfiguration LambdaLogger's configuration
 */
public class LambdaLogger implements Logger, Serializable {

  private static final long serialVersionUID = 7893093825483346807L;

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
  public boolean isTraceEnabled(Marker marker) {
    return isTraceEnabled();
  }

  @Override
  public void trace(String message) {
    log(Level.TRACE, message, null);
  }

  @Override
  public void trace(Marker marker, String msg) {
    trace(msg);
  }

  @Override
  public void trace(String format, Object arg) {
    trace(format, arg, null);
  }

  @Override
  public void trace(Marker marker, String format, Object arg) {
    trace(format, arg);
  }

  @Override
  public void trace(String format, Object arg1, Object arg2) {
    formatAndLog(Level.TRACE, format, arg1, arg2);
  }

  @Override
  public void trace(Marker marker, String format, Object arg1, Object arg2) {
    trace(format, arg1, arg2);
  }

  @Override
  public void trace(String format, Object... arguments) {
    formatAndLog(Level.TRACE, format, arguments);
  }

  @Override
  public void trace(Marker marker, String format, Object... arguments) {
    trace(format, arguments);
  }

  @Override
  public void trace(String message, Throwable throwable) {
    log(Level.TRACE, message, throwable);
  }

  @Override
  public void trace(Marker marker, String msg, Throwable t) {
    trace(msg, t);
  }

  @Override
  public boolean isDebugEnabled() {
    return isLevelEnabled(Level.DEBUG);
  }

  @Override
  public boolean isDebugEnabled(Marker marker) {
    return isDebugEnabled();
  }

  @Override
  public void debug(String message) {
    log(Level.DEBUG, message, null);
  }

  @Override
  public void debug(Marker marker, String msg) {
    debug(msg);
  }

  @Override
  public void debug(String format, Object arg) {
    debug(format, arg, null);
  }

  @Override
  public void debug(Marker marker, String format, Object arg) {
    debug(format, arg);
  }

  @Override
  public void debug(String format, Object arg1, Object arg2) {
    formatAndLog(Level.DEBUG, format, arg1, arg2);
  }

  @Override
  public void debug(Marker marker, String format, Object arg1, Object arg2) {
    debug(format, arg1, arg2);
  }

  @Override
  public void debug(String format, Object... arguments) {
    formatAndLog(Level.DEBUG, format, arguments);
  }

  @Override
  public void debug(Marker marker, String format, Object... arguments) {
    debug(format, arguments);
  }

  @Override
  public void debug(String message, Throwable throwable) {
    log(Level.DEBUG, message, throwable);
  }

  @Override
  public void debug(Marker marker, String msg, Throwable t) {
    debug(msg, t);
  }

  @Override
  public boolean isInfoEnabled() {
    return isLevelEnabled(Level.INFO);
  }

  @Override
  public boolean isInfoEnabled(Marker marker) {
    return isInfoEnabled();
  }

  @Override
  public void info(String message) {
    log(Level.INFO, message, null);
  }

  @Override
  public void info(Marker marker, String msg) {
    info(msg);
  }

  @Override
  public void info(String format, Object arg) {
    info(format, arg, null);
  }

  @Override
  public void info(Marker marker, String format, Object arg) {
    info(format, arg);
  }

  @Override
  public void info(String format, Object arg1, Object arg2) {
    formatAndLog(Level.INFO, format, arg1, arg2);
  }

  @Override
  public void info(Marker marker, String format, Object arg1, Object arg2) {
    info(format, arg1, arg2);
  }

  @Override
  public void info(String format, Object... arguments) {
    formatAndLog(Level.INFO, format, arguments);
  }

  @Override
  public void info(Marker marker, String format, Object... arguments) {
    info(format, arguments);
  }

  @Override
  public void info(String message, Throwable throwable) {
    log(Level.INFO, message, throwable);
  }

  @Override
  public void info(Marker marker, String msg, Throwable t) {
    info(msg, t);
  }

  @Override
  public boolean isWarnEnabled() {
    return isLevelEnabled(Level.WARN);
  }

  @Override
  public boolean isWarnEnabled(Marker marker) {
    return isWarnEnabled();
  }

  @Override
  public void warn(String message) {
    log(Level.WARN, message, null);
  }

  @Override
  public void warn(Marker marker, String msg) {
    warn(msg);
  }

  @Override
  public void warn(String format, Object arg) {
    warn(format, arg, null);
  }

  @Override
  public void warn(Marker marker, String format, Object arg) {
    warn(format, arg);
  }

  @Override
  public void warn(String format, Object arg1, Object arg2) {
    formatAndLog(Level.WARN, format, arg1, arg2);
  }

  @Override
  public void warn(Marker marker, String format, Object arg1, Object arg2) {
    warn(format, arg1, arg2);
  }

  @Override
  public void warn(String format, Object... arguments) {
    formatAndLog(Level.WARN, format, arguments);
  }

  @Override
  public void warn(Marker marker, String format, Object... arguments) {
    warn(format, arguments);
  }

  @Override
  public void warn(String message, Throwable throwable) {
    log(Level.WARN, message, throwable);
  }

  @Override
  public void warn(Marker marker, String msg, Throwable t) {
    warn(msg, t);
  }

  @Override
  public boolean isErrorEnabled() {
    return isLevelEnabled(Level.ERROR);
  }

  @Override
  public boolean isErrorEnabled(Marker marker) {
    return isErrorEnabled();
  }

  @Override
  public void error(String message) {
    log(Level.ERROR, message, null);
  }

  @Override
  public void error(Marker marker, String msg) {
    error(msg);
  }

  @Override
  public void error(String format, Object arg) {
    error(format, arg, null);
  }

  @Override
  public void error(Marker marker, String format, Object arg) {
    error(format, arg);
  }

  @Override
  public void error(String format, Object arg1, Object arg2) {
    formatAndLog(Level.ERROR, format, arg1, arg2);
  }

  @Override
  public void error(Marker marker, String format, Object arg1, Object arg2) {
    error(format, arg1, arg2);
  }

  @Override
  public void error(String format, Object... arguments) {
    formatAndLog(Level.ERROR, format, arguments);
  }

  @Override
  public void error(Marker marker, String format, Object... arguments) {
    error(format, arguments);
  }

  @Override
  public void error(String message, Throwable throwable) {
    log(Level.ERROR, message, throwable);
  }

  @Override
  public void error(Marker marker, String msg, Throwable t) {
    error(msg, t);
  }

  @VisibleForTesting
  void log(Level level, String message, Throwable throwable) {
    if (!isLevelEnabled(level)) {
      return;
    }
    LambdaLoggerUtil.log(configuration, printStream, level, message, throwable);
  }

  private void formatAndLog(Level level, String format, Object... arguments) {
    if (!isLevelEnabled(level)) {
      return;
    }
    FormattingTuple formattingTuple = MessageFormatter.arrayFormat(format, arguments);
    log(level, formattingTuple.getMessage(), formattingTuple.getThrowable());
  }

  private boolean isLevelEnabled(Level level) {
    return level.toInt() >= configuration.loggerLevel().toInt();
  }

  private Object readResolve() throws ObjectStreamException {
    return StaticLoggerBinder.getSingleton().getLoggerFactory().getLogger(getName());
  }

}
