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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.event.Level;
import org.slf4j.helpers.Util;

/**
 * Responsible for building {@link Logger} using the {@link LambdaLogger} implementation.
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
 * <li><strong>log.a.b.c</strong> - Logging detail level for a LambdaLogger instance named <em>a.b.c</em></li>
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
 *
 * <h4>Fine-grained configuration with markers</h4>
 * <p>
 * The AWS Lambda Logger supports markers since <em>v2.0.0</em>.
 * The log level (default or detail) can have some log level and each level can have some markers.
 * <p>
 * Example:
 * <pre><code class="language-properties">
 * log.org.test.Class=warn,info@iAmMarker,trace@important:notify-admin
 * </pre>
 * The logger for {@code org.test.Class} has the common <em>warn</em> log level.
 * Also, it has additional levels <em>info</em> with the marker <em>iAmMarker</em>
 * and <em>trace</em> with markers <em>important</em> and <em>notify-admin</em>.
 */
public class LambdaLoggerFactory implements ILoggerFactory {

  private static final String AT = "@";
  private static final String COLON = ":";
  private static final String COMMA = ",";
  private static final String CONFIGURATION_FILE = "lambda-logger.properties";
  private static final char DOT = '.';
  private static final String DOTS = "\\.+";
  private static final String NONE = "";
  private static final String UNDERSCORE = "_";
  private static final String SPACES = "\\s+";

  private final ConcurrentMap<String, Logger> loggers;
  private final DateFormat dateTimeFormat;
  private final List<LoggerLevel> defaultLoggerLevel;
  private final boolean levelInBrackets;
  private final Properties properties;
  private final String requestId;
  private final boolean showDateTime;
  private final boolean showLogName;
  private final boolean showShortLogName;
  private final boolean showThreadId;
  private final boolean showThreadName;

  public LambdaLoggerFactory() {
    this(CONFIGURATION_FILE);
  }

  @VisibleForTesting
  LambdaLoggerFactory(String configurationFile) {
    loggers = new ConcurrentHashMap<>();
    properties = loadProperties(configurationFile);
    dateTimeFormat = getDateTimeFormat(ConfigurationProperty.DateTimeFormat);
    defaultLoggerLevel = getLoggerLevelProperty(ConfigurationProperty.DefaultLogLevel);
    levelInBrackets = getBooleanProperty(ConfigurationProperty.LevelInBrackets);
    requestId = getStringProperty(ConfigurationProperty.RequestId);
    showDateTime = getBooleanProperty(ConfigurationProperty.ShowDateTime);
    showLogName = getBooleanProperty(ConfigurationProperty.ShowLogName);
    showShortLogName = getBooleanProperty(ConfigurationProperty.ShowShortLogName);
    showThreadId = getBooleanProperty(ConfigurationProperty.ShowThreadId);
    showThreadName = getBooleanProperty(ConfigurationProperty.ShowThreadName);
  }

  @Override
  public Logger getLogger(@NotNull String name) {
    return loggers.computeIfAbsent(name, loggerName -> {
      var configuration = LoggerConfiguration.builder().name(loggerName)
          .dateTimeFormat(dateTimeFormat).levelInBrackets(levelInBrackets).requestId(requestId)
          .showDateTime(showDateTime).showLogName(showLogName).showShortLogName(showShortLogName)
          .showThreadId(showThreadId).showThreadName(showThreadName);

      for (LoggerLevel loggerLevel : getLoggerLevels(name)) {
        configuration.loggerLevel(loggerLevel.getLevel(), loggerLevel.getMarkers());
      }

      return new LambdaLogger(configuration.build(), getPrintStream());
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

  private List<LoggerLevel> getLoggerLevelProperty(ConfigurationProperty configurationProperty) {
    String value = System.getenv(configurationProperty.variableName);

    if (nonNull(value)) {
      try {
        return parseLoggerLevelString(value);
      } catch (IllegalArgumentException exception) {
        Util.report("Bad log level in the variable " + configurationProperty.variableName,
            exception);
      }
    }

    value = getProperties().getProperty(configurationProperty.propertyName);
    if (nonNull(value)) {
      try {
        return parseLoggerLevelString(value);
      } catch (IllegalArgumentException exception) {
        Util.report("Bad log level in the property " + configurationProperty.propertyName + " of "
            + CONFIGURATION_FILE, exception);
      }
    }

    return List.of(
        LoggerLevel.builder().level(Level.valueOf(configurationProperty.defaultValue)).build());
  }

  private List<LoggerLevel> getLoggerLevels(String loggerName) {
    var name = loggerName;
    int indexOfLastDot = name.length();
    String loggerLevelString = null;

    while (isNull(loggerLevelString) && indexOfLastDot > -1) {
      name = name.substring(0, indexOfLastDot);
      loggerLevelString = getStringProperty(ConfigurationProperty.LogLevel, name);
      indexOfLastDot = name.lastIndexOf(DOT);
    }

    List<LoggerLevel> loggerLevels = null;

    if (nonNull(loggerLevelString)) {
      try {
        loggerLevels = parseLoggerLevelString(loggerLevelString);
      } catch (IllegalArgumentException exception) {
        Util.report("Bad log level of the logger " + loggerName, exception);
      }
    }

    if (isNull(loggerLevels)) {
      loggerLevels = defaultLoggerLevel;
    }

    return loggerLevels;
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

  private String getStringProperty(ConfigurationProperty configurationProperty, String name) {
    var normalizedName = name.replaceAll(SPACES, NONE);
    var value = System.getenv(configurationProperty.variableName + normalizedName.toUpperCase()
        .replaceAll(DOTS, UNDERSCORE));

    if (isNull(value)) {
      value = getProperties().getProperty(configurationProperty.propertyName + normalizedName);
    }
    if (isNull(value)) {
      value = configurationProperty.defaultValue;
    }

    return value;
  }

  private Properties getProperties() {
    return properties;
  }

  private Properties loadProperties(String configurationFile) {
    var properties = new Properties();

    try (InputStream configurationInputStream = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream(configurationFile)) {
      properties.load(configurationInputStream);
    } catch (IOException e) {
      // ignored
    }

    return properties;
  }

  private List<LoggerLevel> parseLoggerLevelString(String loggerLevelString)
      throws IllegalArgumentException {
    var loggerLevels = new ArrayList<LoggerLevel>();

    for (String loggerLevel : loggerLevelString.split(COMMA)) {
      var loggerLevelBuilder = LoggerLevel.builder();
      var loggerLevelWithMarkers = loggerLevel.split(AT);

      loggerLevelBuilder.level(Level.valueOf(loggerLevelWithMarkers[0].toUpperCase()));
      if (loggerLevelWithMarkers.length > 1) {
        for (String markerName : loggerLevelWithMarkers[1].split(COLON)) {
          loggerLevelBuilder.marker(markerName);
        }
      }
      loggerLevels.add(loggerLevelBuilder.build());
    }

    return loggerLevels;
  }

  public enum ConfigurationProperty {

    DateTimeFormat("dateTimeFormat", "LOG_DATE_TIME_FORMAT", null), DefaultLogLevel(
        "defaultLogLevel", "LOG_DEFAULT_LEVEL", "INFO"), LevelInBrackets("levelInBrackets",
        "LOG_LEVEL_IN_BRACKETS", "false"), LogLevel("log.", "LOG_", null), RequestId("requestId",
        "LOG_AWS_REQUEST_ID", "AWS_REQUEST_ID"), ShowDateTime("showDateTime", "LOG_SHOW_DATE_TIME",
        "false"), ShowLogName("showLogName", "LOG_SHOW_NAME", "true"), ShowShortLogName(
        "showShortLogName", "LOG_SHOW_SHORT_NAME", "false"), ShowThreadId("showThreadId",
        "LOG_SHOW_THREAD_ID", "false"), ShowThreadName("showThreadName", "LOG_SHOW_THREAD_NAME",
        "false");

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
