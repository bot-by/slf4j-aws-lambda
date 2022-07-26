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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiPredicate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Marker;
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
 * <li><strong>requestId</strong> - Set the context name of <strong>AWS request ID</strong>.
 * Defaults to {@code AWS_REQUEST_ID}.</li>
 * <li><strong>showDateTime</strong> - Set to {@code true} if you want the current date and time
 * to be included in output messages. Defaults to {@code false}.</li>
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
 * The environment variables overrides the properties: <strong>LOG_AWS_REQUEST_ID</strong>,
 * <strong>LOG_DATE_TIME_FORMAT</strong>, <strong>LOG_DEFAULT_LEVEL</strong>,
 * <strong>LOG_LEVEL_IN_BRACKETS</strong>, <strong>LOG_SHOW_DATE_TIME</strong>,
 * <strong>LOG_SHOW_NAME</strong>, <strong>LOG_SHOW_SHORT_NAME</strong>,
 * <strong>LOG_SHOW_THREAD_ID</strong>, <strong>LOG_SHOW_THREAD_NAME</strong>.
 */
public class LambdaLoggerConfiguration {

  private static final String DOT = ".";

  private final DateFormat dateTimeFormat;
  private final boolean levelInBrackets;
  private final List<BiPredicate<Level, Marker>> loggerPredicates;
  private final String logName;
  private final String name;
  private final String requestId;
  private final boolean showDateTime;
  private final boolean showThreadId;
  private final boolean showThreadName;

  private LambdaLoggerConfiguration(Builder builder) {
    dateTimeFormat = builder.dateTimeFormat;
    levelInBrackets = builder.levelInBrackets;
    loggerPredicates = List.copyOf(builder.loggerPredicates);
    name = builder.name;
    if (builder.showShortLogName) {
      logName = name.substring(name.lastIndexOf(DOT) + 1);
    } else if (builder.showLogName) {
      logName = name;
    } else {
      logName = null;
    }
    requestId = builder.requestId;
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

  public boolean isLevelEnabled(Level level) {
    return isLevelEnabled(level, null);
  }

  public boolean isLevelEnabled(Level level, Marker marker) {
    return loggerPredicates.stream()
        .anyMatch(loggerPredicate -> loggerPredicate.test(level, marker));
  }

  public boolean levelInBrackets() {
    return levelInBrackets;
  }

  public String logName() {
    return logName;
  }

  public String name() {
    return name;
  }

  public String requestId() {
    return requestId;
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
    private List<BiPredicate<Level, Marker>> loggerPredicates;
    private String name;
    private String requestId;
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
      requireNonNull(loggerPredicates, "Logger level is null");
      requireNonNull(name, "Logger name is null");
      requireNonNull(requestId, "AWS request ID is null");
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
     * <p>
     * You can add some different log levels: it does not overwrite previous levels but add new one
     * to a list. There has to be at least one log level.
     *
     * @param loggerLevel log level
     * @return a builder
     */
    public Builder loggerLevel(@NotNull Level loggerLevel) {
      if (isNull(loggerPredicates)) {
        loggerPredicates = new ArrayList<>();
      }
      loggerPredicates.add((level, marker) -> level.toInt() >= loggerLevel.toInt());
      return this;
    }

    /**
     * The marked log level of the Logger instance.
     * <p>
     * Must be one of (<em>trace</em>, <em>debug</em>, <em>info</em>, <em>warn</em>,
     * <em>error</em>), a value is case-insensitive. If not specified, defaults to <em>info</em>.
     * <p>
     * You can add some different log levels: it does not overwrite previous levels but add new one
     * to a list. There has to be at least one log level.
     *
     * @param loggerLevel   default log level
     * @param loggerMarkers markers
     * @return a builder
     */
    public Builder loggerLevel(@NotNull Level loggerLevel, @NotNull Marker... loggerMarkers) {
      if (isNull(loggerPredicates)) {
        loggerPredicates = new ArrayList<>();
      }
      this.loggerPredicates.add((level, marker) -> {
        if (level.toInt() >= loggerLevel.toInt()) {
          if (loggerMarkers.length == 0) {
            return true;
          }
          if (nonNull(marker)) {
            return Arrays.stream(loggerMarkers).anyMatch(marker::contains);
          }
        }
        return false;
      });
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
     * Context name of AWS request ID.
     *
     * @param requestId context name of AWS request ID
     * @return a builder
     */
    public Builder requestId(@NotNull String requestId) {
      this.requestId = requestId;
      return this;
    }

    /**
     * Set to {@code true} if you want the current date and time to be included in output messages.
     * <p>
     * Defaults to {@code false}.
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
