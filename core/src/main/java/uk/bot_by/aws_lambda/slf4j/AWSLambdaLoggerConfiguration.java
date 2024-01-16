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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Marker;
import org.slf4j.event.Level;

/**
 * The configuration container.
 *
 * @see AWSLambdaLoggerFactory
 */
public class AWSLambdaLoggerConfiguration {

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

  private AWSLambdaLoggerConfiguration(Builder builder) {
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

  static Builder builder() {
    return new Builder();
  }

  /**
   * The date and time format to be used in the output messages. The pattern describing the date and
   * time format is defined by {@link java.text.SimpleDateFormat}.
   * <p>
   * If the format is not specified or is invalid, the number of milliseconds since start up will be
   * output.
   *
   * @return date and time format
   * @see #showDateTime()
   */
  @Nullable
  public DateFormat dateTimeFormat() {
    return dateTimeFormat;
  }

  /**
   * Test if the logging level is enabled.
   *
   * @param level logging level
   * @return true if this logging level is enabled
   */
  public boolean isLevelEnabled(Level level) {
    return isLevelEnabled(level, null);
  }

  /**
   * Test if the logging level with the marker is enabled.
   *
   * @param level  logging level
   * @param marker logging marker
   * @return true if this logging level with the marker is enabled
   */
  public boolean isLevelEnabled(Level level, Marker marker) {
    return loggerPredicates.stream()
        .anyMatch(loggerPredicate -> loggerPredicate.test(level, marker));
  }

  /**
   * Should the level string be output in brackets?
   *
   * @return true if the level string should be in brackets
   */
  public boolean levelInBrackets() {
    return levelInBrackets;
  }

  /**
   * The logger name.
   *
   * @return logger name
   */
  public String logName() {
    return logName;
  }

  /**
   * The full logger name.
   *
   * @return full logger name
   */
  public String name() {
    return name;
  }

  /**
   * The request ID.
   *
   * @return request ID
   */
  public String requestId() {
    return requestId;
  }

  /**
   * Should the date-time or timestamp be output?
   *
   * @return true if date-time or timestamp be output
   * @see #dateTimeFormat()
   */
  public boolean showDateTime() {
    return showDateTime;
  }

  /**
   * Should the thread ID be output?
   *
   * @return true if thread ID be output
   */
  public boolean showThreadId() {
    return showThreadId;
  }

  /**
   * Should the thread name be output?
   *
   * @return true if thread name be output
   */
  public boolean showThreadName() {
    return showThreadName;
  }

  static class Builder {

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

    AWSLambdaLoggerConfiguration build() {
      requireNonNull(loggerPredicates, "Logger level is null");
      requireNonNull(name, "Logger name is null");
      requireNonNull(requestId, "AWS request ID is null");
      return new AWSLambdaLoggerConfiguration(this);
    }

    Builder dateTimeFormat(@Nullable DateFormat dateTimeFormat) {
      this.dateTimeFormat = dateTimeFormat;
      return this;
    }

    Builder levelInBrackets(boolean levelInBrackets) {
      this.levelInBrackets = levelInBrackets;
      return this;
    }

    Builder loggerLevel(@NotNull Level loggerLevel) {
      if (isNull(loggerPredicates)) {
        loggerPredicates = new ArrayList<>();
      }
      loggerPredicates.add((level, marker) -> level.toInt() >= loggerLevel.toInt());
      return this;
    }

    Builder loggerLevel(@NotNull Level loggerLevel, @NotNull Marker... loggerMarkers) {
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

    Builder name(@NotNull String name) {
      this.name = name;
      return this;
    }

    Builder requestId(@NotNull String requestId) {
      this.requestId = requestId;
      return this;
    }

    Builder showDateTime(boolean showDateTime) {
      this.showDateTime = showDateTime;
      return this;
    }

    Builder showLogName(boolean showLogName) {
      this.showLogName = showLogName;
      return this;
    }

    Builder showShortLogName(boolean showShortLogName) {
      this.showShortLogName = showShortLogName;
      return this;
    }

    Builder showThreadId(boolean showThreadId) {
      this.showThreadId = showThreadId;
      return this;
    }

    Builder showThreadName(boolean showThreadName) {
      this.showThreadName = showThreadName;
      return this;
    }

  }

}
