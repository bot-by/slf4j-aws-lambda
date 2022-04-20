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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.event.Level;
import org.slf4j.helpers.Util;

/**
 * Responsible for building {@link Logger} using the {@link LambdaLogger} implementation.
 */
public class LambdaLoggerFactory implements ILoggerFactory {

  private static final String CONFIGURATION_FILE = "lambda-logger.properties";

  private final ConcurrentMap<String, Logger> loggers;
  private final DateFormat dateTimeFormat;
  private final Level defaultLogLevel;
  private final boolean levelInBrackets;
  private final Properties properties;
  private final boolean showDateTime;
  private final boolean showLogName;
  private final boolean showShortLogName;
  private final boolean showThreadId;
  private final boolean showThreadName;

  public LambdaLoggerFactory() {
    loggers = new ConcurrentHashMap<>();
    properties = loadProperties();
    dateTimeFormat = getDateTimeFormat(ConfigurationProperty.DateTimeFormat);
    defaultLogLevel = getLevelProperty(ConfigurationProperty.DefaultLogLevel);
    levelInBrackets = getBooleanProperty(ConfigurationProperty.LevelInBrackets);
    showDateTime = getBooleanProperty(ConfigurationProperty.ShowDateTime);
    showLogName = getBooleanProperty(ConfigurationProperty.ShowLogName);
    showShortLogName = getBooleanProperty(ConfigurationProperty.ShowShortLogName);
    showThreadId = getBooleanProperty(ConfigurationProperty.ShowThreadId);
    showThreadName = getBooleanProperty(ConfigurationProperty.ShowThreadName);
  }

  @Override
  public Logger getLogger(String name) {
    return loggers.computeIfAbsent(name, loggerName -> {
      var configuration = LambdaLoggerConfiguration.builder().name(loggerName)
          .dateTimeFormat(dateTimeFormat).levelInBrackets(levelInBrackets)
          .loggerLevel(defaultLogLevel).showDateTime(showDateTime).showLogName(showLogName)
          .showShortLogName(showShortLogName).showThreadId(showThreadId)
          .showThreadName(showThreadName).build();

      return new LambdaLogger(configuration, getPrintStream());
    });
  }

  @VisibleForTesting
  PrintStream getPrintStream() {
    return System.out;
  }

  private boolean getBooleanProperty(ConfigurationProperty configurationProperty) {
    return Boolean.parseBoolean(getStringProperty(configurationProperty));
  }

  private DateFormat getDateTimeFormat(ConfigurationProperty configurationProperty) {
    String dateTimeFormatString = getStringProperty(configurationProperty);

    if (nonNull(dateTimeFormatString)) {
      try {
        return new SimpleDateFormat(dateTimeFormatString);
      } catch (IllegalArgumentException exception) {
        Util.report("Bad date format in " + CONFIGURATION_FILE + "; will output relative time",
            exception);
      }
    }

    return null;
  }

  private Level getLevelProperty(ConfigurationProperty configurationProperty) {
    String value = System.getenv(configurationProperty.variableName);

    if (nonNull(value)) {
      try {
        return Level.valueOf(value);
      } catch (IllegalArgumentException exception) {
        Util.report("Bad log level in the variable " + configurationProperty.variableName,
            exception);
      }
    }

    value = getProperties().getProperty(configurationProperty.propertyName);
    if (nonNull(value)) {
      try {
        return Level.valueOf(value);
      } catch (IllegalArgumentException exception) {
        Util.report("Bad log level in the property " + configurationProperty.propertyName + " of "
            + CONFIGURATION_FILE, exception);
      }
    }

    return Level.valueOf(configurationProperty.defaultValue);
  }

  private String getStringProperty(ConfigurationProperty configurationProperty) {
    String value = System.getenv(configurationProperty.variableName);

    if (isNull(value)) {
      value = getProperties().getProperty(configurationProperty.propertyName);
    }
    if (isNull(value)) {
      value = configurationProperty.defaultValue;
    }

    return value;
  }

  private Properties getProperties() {
    return properties;
  }

  private Properties loadProperties() {
    Properties properties = new Properties();

    InputStream in = AccessController.doPrivileged((PrivilegedAction<InputStream>) () -> {
      ClassLoader threadCL = Thread.currentThread().getContextClassLoader();
      if (threadCL != null) {
        return threadCL.getResourceAsStream(CONFIGURATION_FILE);
      } else {
        return ClassLoader.getSystemResourceAsStream(CONFIGURATION_FILE);
      }
    });
    if (null != in) {
      try (in) {
        properties.load(in);
      } catch (IOException exception) {
        // ignored
      }
    }

    return properties;
  }

  public enum ConfigurationProperty {

    DateTimeFormat("dateTimeFormat", "LOG_DATE_TIME_FORMAT", null),
    DefaultLogLevel("defaultLogLevel", "LOG_DEFAULT_LEVEL", "INFO"),
    LevelInBrackets("levelInBrackets", "LOG_LEVEL_IN_BRACKETS", "false"),
    ShowDateTime("showDateTime", "LOG_SHOW_DATE_TIME", "false"),
    ShowLogName("showLogName", "LOG_SHOW_NAME", "true"),
    ShowShortLogName("showShortLogName", "LOG_SHOW_SHORT_NAME", "false"),
    ShowThreadId("showThreadId", "LOG_SHOW_THREAD_ID", "false"),
    ShowThreadName("showThreadName", "LOG_SHOW_THREAD_NAME", "false");

    public final String defaultValue;
    public final String propertyName;
    public final String variableName;

    ConfigurationProperty(String propertyName, String variableName, String defaultValue) {
      this.propertyName = propertyName;
      this.variableName = variableName;
      this.defaultValue = defaultValue;
    }

  }

}
