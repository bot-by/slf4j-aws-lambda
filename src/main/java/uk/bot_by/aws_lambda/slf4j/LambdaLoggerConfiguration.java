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

import static java.util.Objects.requireNonNull;

import java.text.DateFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.event.Level;

/**
 * {@link LambdaLogger}'s configuration.
 * <p>
 * The configuration is similar to <a
 * href="https://www.slf4j.org/api/org/slf4j/simple/SimpleLogger.html">SLF4J Simple</a>.
 * <p>
 * It looks for the {@code lambda-logger.properties} resource and read properties:
 * <ul>
 * <li><strong>dateTimeFormat</strong> - The date and time format to be used in the output messages.
 * The pattern describing the date and time format is defined by {@link java.text.SimpleDateFormat}.
 * If the format is not specified or is invalid, the number of milliseconds since start up
 * will be output.</li>
 * <li><strong>defaultLogLevel</strong> - Default log level for all instances of LambdaLogger.
 * Must be one of (<em>trace</em>, <em>debug</em>, <em>info</em>, <em>warn</em>, <em>error</em>),
 * a value is case-insensitive. If not specified, defaults to <em>info</em>.</li>
 * <li><strong>levelInBrackets</strong> - Should the level string be output in brackets?
 * Defaults to {@code false}.</li>
 * <li><strong>showDateTime</strong> - Set to {@code true} if you want the current date and time
 * to be included in output messages. Default is {@code false}.</li>
 * <li><strong>showLogName</strong> - Set to {@code true} if you want the Logger instance name
 * to be included in output messages. Defaults to {@code true}.</li>
 * <li><strong>showShortLogName</strong> - Set to {@code true} if you want the last component of the name
 * to be included in output messages. Defaults to {@code false}.</li>
 * <li><strong>showThreadId</strong> - If you would like to output the current thread id,
 * then set to {@code true}. Defaults to {@code false}.</li>
 * <li><strong>showThreadName</strong> - Set to {@code true} if you want to output
 * the current thread name. Defaults to {@code false}.</li>
 * </ul>
 * <p>
 * The environment variables overrides the properties: <strong>LOG_DATE_TIME_FORMAT</strong>,
 * <strong>LOG_DEFAULT_LEVEL</strong>, <strong>LOG_LEVEL_IN_BRACKETS</strong>,
 * <strong>LOG_SHOW_DATE_TIME</strong>, <strong>LOG_SHOW_NAME</strong>,
 * <strong>LOG_SHOW_SHORT_NAME</strong>, <strong>LOG_SHOW_THREAD_ID</strong>,
 * <strong>LOG_SHOW_THREAD_NAME</strong>.
 */
public class LambdaLoggerConfiguration {

  private static final String DOT = ".";

  private final DateFormat dateTimeFormat;
  private final boolean levelInBrackets;
  private final Level loggerLevel;
  private final String logName;
  private final String name;
  private final boolean showDateTime;
  private final boolean showThreadId;
  private final boolean showThreadName;

  private LambdaLoggerConfiguration(Builder builder) {
    dateTimeFormat = builder.dateTimeFormat;
    levelInBrackets = builder.levelInBrackets;
    loggerLevel = builder.loggerLevel;
    name = builder.name;
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

  public static Builder builder() {
    return new Builder();
  }

  public DateFormat dateTimeFormat() {
    return dateTimeFormat;
  }

  public boolean levelInBrackets() {
    return levelInBrackets;
  }

  public Level loggerLevel() {
    return loggerLevel;
  }

  public String logName() {
    return logName;
  }

  public String name() {
    return name;
  }

  public boolean showDateTime() {
    return showDateTime;
  }

  public boolean showThreadId() {
    return showThreadId;
  }

  public boolean showThreadName() {
    return showThreadName;
  }

  /**
   * {@link LambdaLogger} configuration's builder.
   */
  public static class Builder {

    private DateFormat dateTimeFormat;
    private boolean levelInBrackets;
    private Level loggerLevel;
    private String name;
    private boolean showDateTime;
    private boolean showLogName;
    private boolean showShortLogName;
    private boolean showThreadId;
    private boolean showThreadName;

    private Builder() {
    }

    /**
     * Build a LambdaLoggerConfiguration instance.
     *
     * @return a configuration instance
     */
    public LambdaLoggerConfiguration build() {
      requireNonNull(loggerLevel, "Logger level is null");
      requireNonNull(name, "Logger name is null");
      return new LambdaLoggerConfiguration(this);
    }

    /**
     * The date and time format to be used in the output messages.
     * <p>
     * The pattern describing the date and time format is defined by
     * {@link java.text.SimpleDateFormat}. If the format is not specified or is invalid, the number
     * of milliseconds since start up will be output.
     *
     * @param dateTimeFormat date and time format
     * @return a builder
     */
    public Builder dateTimeFormat(@Nullable DateFormat dateTimeFormat) {
      this.dateTimeFormat = dateTimeFormat;
      return this;
    }

    /**
     * Should the level string be output in brackets?
     * <p>
     * Defaults to {@code false}.
     *
     * @param levelInBrackets use brackets
     * @return a builder
     */
    public Builder levelInBrackets(boolean levelInBrackets) {
      this.levelInBrackets = levelInBrackets;
      return this;
    }

    /**
     * The log level of the Logger instance.
     * <p>
     * Must be one of (<em>trace</em>, <em>debug</em>, <em>info</em>, <em>warn</em>,
     * <em>error</em>), a value is case-insensitive. If not specified, defaults to <em>info</em>.
     *
     * @param level default log level
     * @return a builder
     */
    public Builder loggerLevel(@NotNull Level level) {
      this.loggerLevel = level;
      return this;
    }

    /**
     * Name of the Logger instance.
     *
     * @param name logger name
     * @return a builder
     */
    public Builder name(@NotNull String name) {
      this.name = name;
      return this;
    }

    /**
     * Set to {@code true} if you want the current date and time to be included in output messages.
     * <p>
     * Default is {@code false}.
     *
     * @param showDateTime show date and time
     * @return a builder
     */
    public Builder showDateTime(boolean showDateTime) {
      this.showDateTime = showDateTime;
      return this;
    }

    /**
     * Set to {@code true} if you want the Logger instance name to be included in output messages.
     * <p>
     * Defaults to {@code true}.
     *
     * @param showLogName show log name
     * @return a builder
     */
    public Builder showLogName(boolean showLogName) {
      this.showLogName = showLogName;
      return this;
    }

    /**
     * Set to {@code true} if you want the last component of the name to be included in output
     * messages.
     * <p>
     * Defaults to {@code false}.
     *
     * @param showShortLogName show short log name
     * @return a builder
     */
    public Builder showShortLogName(boolean showShortLogName) {
      this.showShortLogName = showShortLogName;
      return this;
    }

    /**
     * If you would like to output the current thread id, then set to {@code true}.
     * <p>
     * Defaults to {@code false}.
     *
     * @param showThreadId show the current thread ID
     * @return a builder
     */
    public Builder showThreadId(boolean showThreadId) {
      this.showThreadId = showThreadId;
      return this;
    }

    /**
     * Set to {@code true} if you want to output the current thread name.
     * <p>
     * Defaults to {@code false}.
     *
     * @param showThreadName show the current thread name
     * @return a builder
     */
    public Builder showThreadName(boolean showThreadName) {
      this.showThreadName = showThreadName;
      return this;
    }

  }

}
