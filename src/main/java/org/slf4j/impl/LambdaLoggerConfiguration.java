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

import static java.util.Objects.requireNonNull;

import java.text.DateFormat;
import org.jetbrains.annotations.NotNull;
import org.slf4j.event.Level;

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

    public LambdaLoggerConfiguration build() {
      requireNonNull(loggerLevel, "Logger level is null");
      requireNonNull(name, "Logger name is null");
      return new LambdaLoggerConfiguration(this);
    }

    Builder dateTimeFormat(DateFormat dateTimeFormat) {
      this.dateTimeFormat = dateTimeFormat;
      return this;
    }

    Builder levelInBrackets(boolean levelInBrackets) {
      this.levelInBrackets = levelInBrackets;
      return this;
    }

    Builder loggerLevel(@NotNull Level level) {
      this.loggerLevel = level;
      return this;
    }

    Builder name(@NotNull String name) {
      this.name = name;
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
