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
package uk.bot_by.aws_lambda.slf4j;

import java.io.ObjectStreamException;
import java.io.Serializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

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
 * @see AWSLambdaLoggerConfiguration AWSLambdaLogger's configuration
 */
public class AWSLambdaLogger implements Logger, Serializable {

  private static final long serialVersionUID = 7893093825483346807L;

  private final AWSLambdaLoggerConfiguration configuration;
  private final AWSLambdaLoggerOutput output;

  public AWSLambdaLogger(@NotNull AWSLambdaLoggerConfiguration configuration,
      @NotNull AWSLambdaLoggerOutput output) {
    this.configuration = configuration;
    this.output = output;
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
    return isLevelEnabled(Level.TRACE, marker);
  }

  @Override
  public void trace(String message) {
    log(Level.TRACE, message, null);
  }

  @Override
  public void trace(Marker marker, String message) {
    log(Level.TRACE, marker, message, null);
  }

  @Override
  public void trace(String format, Object argument) {
    trace(format, argument, null);
  }

  @Override
  public void trace(Marker marker, String format, Object argument) {
    trace(marker, format, argument, null);
  }

  @Override
  public void trace(String format, Object argument1, Object argument2) {
    formatAndLog(Level.TRACE, format, argument1, argument2);
  }

  @Override
  public void trace(Marker marker, String format, Object argument1, Object argument2) {
    formatAndLog(Level.TRACE, marker, format, argument1, argument2);
  }

  @Override
  public void trace(String format, Object... arguments) {
    formatAndLog(Level.TRACE, format, arguments);
  }

  @Override
  public void trace(Marker marker, String format, Object... arguments) {
    formatAndLog(Level.TRACE, marker, format, arguments);
  }

  @Override
  public void trace(String message, Throwable throwable) {
    log(Level.TRACE, message, throwable);
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
  public boolean isDebugEnabled(Marker marker) {
    return isLevelEnabled(Level.DEBUG, marker);
  }

  @Override
  public void debug(String message) {
    log(Level.DEBUG, message, null);
  }

  @Override
  public void debug(Marker marker, String message) {
    log(Level.DEBUG, marker, message, null);
  }

  @Override
  public void debug(String format, Object argument) {
    debug(format, argument, null);
  }

  @Override
  public void debug(Marker marker, String format, Object argument) {
    debug(marker, format, argument, null);
  }

  @Override
  public void debug(String format, Object argument1, Object argument2) {
    formatAndLog(Level.DEBUG, format, argument1, argument2);
  }

  @Override
  public void debug(Marker marker, String format, Object argument1, Object argument2) {
    formatAndLog(Level.DEBUG, marker, format, argument1, argument2);
  }

  @Override
  public void debug(String format, Object... arguments) {
    formatAndLog(Level.DEBUG, format, arguments);
  }

  @Override
  public void debug(Marker marker, String format, Object... arguments) {
    formatAndLog(Level.DEBUG, marker, format, arguments);
  }

  @Override
  public void debug(String message, Throwable throwable) {
    log(Level.DEBUG, message, throwable);
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
  public boolean isInfoEnabled(Marker marker) {
    return isLevelEnabled(Level.INFO, marker);
  }

  @Override
  public void info(String message) {
    log(Level.INFO, message, null);
  }

  @Override
  public void info(Marker marker, String message) {
    log(Level.INFO, marker, message, null);
  }

  @Override
  public void info(String format, Object argument) {
    info(format, argument, null);
  }

  @Override
  public void info(Marker marker, String format, Object argument) {
    info(marker, format, argument, null);
  }

  @Override
  public void info(String format, Object argument1, Object argument2) {
    formatAndLog(Level.INFO, format, argument1, argument2);
  }

  @Override
  public void info(Marker marker, String format, Object argument1, Object argument2) {
    formatAndLog(Level.INFO, marker, format, argument1, argument2);
  }

  @Override
  public void info(String format, Object... arguments) {
    formatAndLog(Level.INFO, format, arguments);
  }

  @Override
  public void info(Marker marker, String format, Object... arguments) {
    formatAndLog(Level.INFO, marker, format, arguments);
  }

  @Override
  public void info(String message, Throwable throwable) {
    log(Level.INFO, message, throwable);
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
  public boolean isWarnEnabled(Marker marker) {
    return isLevelEnabled(Level.WARN, marker);
  }

  @Override
  public void warn(String message) {
    log(Level.WARN, message, null);
  }

  @Override
  public void warn(Marker marker, String message) {
    log(Level.WARN, marker, message, null);
  }

  @Override
  public void warn(String format, Object argument) {
    warn(format, argument, null);
  }

  @Override
  public void warn(Marker marker, String format, Object argument) {
    warn(marker, format, argument, null);
  }

  @Override
  public void warn(String format, Object argument1, Object argument2) {
    formatAndLog(Level.WARN, format, argument1, argument2);
  }

  @Override
  public void warn(Marker marker, String format, Object argument1, Object argument2) {
    formatAndLog(Level.WARN, marker, format, argument1, argument2);
  }

  @Override
  public void warn(String format, Object... arguments) {
    formatAndLog(Level.WARN, format, arguments);
  }

  @Override
  public void warn(Marker marker, String format, Object... arguments) {
    formatAndLog(Level.WARN, marker, format, arguments);
  }

  @Override
  public void warn(String message, Throwable throwable) {
    log(Level.WARN, message, throwable);
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
  public boolean isErrorEnabled(Marker marker) {
    return isLevelEnabled(Level.ERROR, marker);
  }

  @Override
  public void error(String message) {
    log(Level.ERROR, message, null);
  }

  @Override
  public void error(Marker marker, String message) {
    log(Level.ERROR, marker, message, null);
  }

  @Override
  public void error(String format, Object argument) {
    error(format, argument, null);
  }

  @Override
  public void error(Marker marker, String format, Object argument) {
    error(marker, format, argument, null);
  }

  @Override
  public void error(String format, Object argument1, Object argument2) {
    formatAndLog(Level.ERROR, format, argument1, argument2);
  }

  @Override
  public void error(Marker marker, String format, Object argument1, Object argument2) {
    formatAndLog(Level.ERROR, marker, format, argument1, argument2);
  }

  @Override
  public void error(String format, Object... arguments) {
    formatAndLog(Level.ERROR, format, arguments);
  }

  @Override
  public void error(Marker marker, String format, Object... arguments) {
    formatAndLog(Level.ERROR, marker, format, arguments);
  }

  @Override
  public void error(String message, Throwable throwable) {
    log(Level.ERROR, message, throwable);
  }

  @Override
  public void error(Marker marker, String message, Throwable throwable) {
    log(Level.ERROR, marker, message, throwable);
  }

  @VisibleForTesting
  void log(Level level, String message, Throwable throwable) {
    if (!isLevelEnabled(level)) {
      return;
    }
    output.log(configuration, null, level, message, throwable);
  }

  @VisibleForTesting
  void log(Level level, Marker marker, String message, Throwable throwable) {
    if (!isLevelEnabled(level, marker)) {
      return;
    }
    output.log(configuration, marker, level, message, throwable);
  }

  private void formatAndLog(Level level, String format, Object... arguments) {
    if (!isLevelEnabled(level)) {
      return;
    }
    FormattingTuple formattingTuple = MessageFormatter.arrayFormat(format, arguments);
    log(level, formattingTuple.getMessage(), formattingTuple.getThrowable());
  }

  private void formatAndLog(Level level, Marker marker, String format, Object... arguments) {
    if (!isLevelEnabled(level, marker)) {
      return;
    }
    FormattingTuple formattingTuple = MessageFormatter.arrayFormat(format, arguments);
    log(level, marker, formattingTuple.getMessage(), formattingTuple.getThrowable());
  }

  private boolean isLevelEnabled(Level level) {
    return configuration.isLevelEnabled(level);
  }

  private boolean isLevelEnabled(Level level, Marker marker) {
    return configuration.isLevelEnabled(level, marker);
  }

  private Object readResolve() throws ObjectStreamException {
    return LoggerFactory.getLogger(getName());
  }

}
