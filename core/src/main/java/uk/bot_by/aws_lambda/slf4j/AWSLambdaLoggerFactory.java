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

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.event.Level;
import org.slf4j.helpers.Reporter;

/**
 * Responsible for building {@link Logger} using the {@link AWSLambdaLogger} implementation.
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
 * <p>
 * <strong>Fine-grained configuration with markers</strong>
 * <p>
 * The AWS Lambda Logger supports markers since <em>v2.0.0</em>.
 * The log level (default or detail) can have some log level and each level can have some markers.
 * <p>
 * Example:
 * <pre><code class="language-properties">
 * log.org.test.Class=warn,info@iAmMarker,trace@important:notify-admin
 * </code></pre>
 * The logger for {@code org.test.Class} has the common <em>warn</em> log level.
 * Also, it has additional levels <em>info</em> with the marker <em>iAmMarker</em>
 * and <em>trace</em> with markers <em>important</em> and <em>notify-admin</em>.
 * <p>
 * You can customize level and marker separators with properties <strong>logLevelSeparator</strong>
 * and <strong>markerSeparator</strong>. Remember that separators are not a single characters but
 * regular expressions. The environment variables are <strong>LOG_LEVEL_SEPARATOR</strong> and
 * <strong>LOG_MARKER_SEPARATOR</strong> accordingly.
 * <p>
 * Example:
 * <pre><code class="language-properties">
 * log.org.test.Class=warn  info@iAmMarker trace@important|notify-admin
 * # multi-space
 * logLevelSeparator=\\s+
 * # single pipe symbol
 * markerSeparator=\\|
 * </code></pre>
 *
 * @see AWSLambdaLoggerConfigurationProperty
 */
public class AWSLambdaLoggerFactory implements ILoggerFactory {

  private static final String AT = "@";
  private static final String CONFIGURATION_FILE = "lambda-logger.properties";
  private static final char DOT = '.';
  private static final String DOTS = "\\.+";
  private static final String NONE = "";
  private static final String UNDERSCORE = "_";
  private static final String SPACES = "\\s+";

  private final ConcurrentMap<String, Logger> loggers;
  private final DateFormat dateTimeFormat;
  private final List<AWSLambdaLoggerLevel> defaultLoggerLevel;
  private final boolean levelInBrackets;
  private final String logLevelSeparator;
  private final String markerSeparator;
  private final Properties properties;
  private final String requestId;
  private final boolean showDateTime;
  private final boolean showLogName;
  private final boolean showShortLogName;
  private final boolean showThreadId;
  private final boolean showThreadName;

  /**
   * AWS Lambda Logger Factory.
   * <p>
   * Looking for a configuration file <em>lambda-logger.properties</em>.
   */
  public AWSLambdaLoggerFactory() {
    this(CONFIGURATION_FILE);
  }

  @VisibleForTesting
  AWSLambdaLoggerFactory(String configurationFile) {
    loggers = new ConcurrentHashMap<>();
    properties = loadProperties(configurationFile);
    dateTimeFormat = getDateTimeFormat();
    // logLevelSeparator and markerSeparator should be resolved before defaultLoggerLevel
    logLevelSeparator = getStringProperty(AWSLambdaLoggerConfigurationProperty.LogLevelSeparator);
    markerSeparator = getStringProperty(AWSLambdaLoggerConfigurationProperty.MarkerSeparator);
    defaultLoggerLevel = getLoggerLevelProperty();
    levelInBrackets = getBooleanProperty(AWSLambdaLoggerConfigurationProperty.LevelInBrackets);
    requestId = getStringProperty(AWSLambdaLoggerConfigurationProperty.RequestId);
    showDateTime = getBooleanProperty(AWSLambdaLoggerConfigurationProperty.ShowDateTime);
    showLogName = getBooleanProperty(AWSLambdaLoggerConfigurationProperty.ShowLogName);
    showShortLogName = getBooleanProperty(AWSLambdaLoggerConfigurationProperty.ShowShortLogName);
    showThreadId = getBooleanProperty(AWSLambdaLoggerConfigurationProperty.ShowThreadId);
    showThreadName = getBooleanProperty(AWSLambdaLoggerConfigurationProperty.ShowThreadName);
  }

  @VisibleForTesting
  static AWSLambdaLoggerOutput getOutputServiceProvider() {
    return getOutputServiceProvider(AWSLambdaLoggerOutput.class);
  }

  @SuppressWarnings("SameParameterValue")
  @VisibleForTesting
  static AWSLambdaLoggerOutput getOutputServiceProvider(
      Class<? extends AWSLambdaLoggerOutput> clazz) {
    var serviceProviders = ServiceLoader.load(clazz, AWSLambdaLoggerFactory.class.getClassLoader())
        .iterator();
    if (serviceProviders.hasNext()) {
      return serviceProviders.next();
    } else {
      throw new IllegalStateException("No AWS Lambda Logger providers were found");
    }
  }

  @Override
  public Logger getLogger(@NotNull String name) {
    return loggers.computeIfAbsent(name, loggerName -> {
      var configuration = AWSLambdaLoggerConfiguration.builder().name(loggerName)
          .dateTimeFormat(dateTimeFormat).levelInBrackets(levelInBrackets).requestId(requestId)
          .showDateTime(showDateTime).showLogName(showLogName).showShortLogName(showShortLogName)
          .showThreadId(showThreadId).showThreadName(showThreadName);

      for (AWSLambdaLoggerLevel loggerLevel : getLoggerLevels(name)) {
        configuration.loggerLevel(loggerLevel.getLevel(), loggerLevel.getMarkers());
      }

      return new AWSLambdaLogger(configuration.build(), getOutput());
    });
  }

  @VisibleForTesting
  AWSLambdaLoggerOutput getOutput() {
    return getOutputServiceProvider();
  }

  private boolean getBooleanProperty(AWSLambdaLoggerConfigurationProperty configurationProperty) {
    return Boolean.parseBoolean(getStringProperty(configurationProperty));
  }

  private DateFormat getDateTimeFormat() {
    var dateTimeFormatString = getStringProperty(
        AWSLambdaLoggerConfigurationProperty.DateTimeFormat);

    if (nonNull(dateTimeFormatString)) {
      try {
        return new SimpleDateFormat(dateTimeFormatString);
      } catch (IllegalArgumentException exception) {
        Reporter.warn(
            "Bad date-time format in " + CONFIGURATION_FILE + "; will output relative time");
      }
    }

    return null;
  }

  private List<AWSLambdaLoggerLevel> getLoggerLevelProperty() {
    var defaultLogLevelProperty = AWSLambdaLoggerConfigurationProperty.DefaultLogLevel;
    var value = System.getenv(defaultLogLevelProperty.variableName);

    if (nonNull(value)) {
      try {
        return parseLoggerLevelString(value);
      } catch (IllegalArgumentException exception) {
        Reporter.warn("Bad log level in the variable " + defaultLogLevelProperty.variableName);
      }
    }

    value = getProperties().getProperty(defaultLogLevelProperty.propertyName);
    if (nonNull(value)) {
      try {
        return parseLoggerLevelString(value);
      } catch (IllegalArgumentException exception) {
        Reporter.warn(
            "Bad log level in the property " + defaultLogLevelProperty.propertyName + " of "
                + CONFIGURATION_FILE);
      }
    }

    return List.of(
        AWSLambdaLoggerLevel.builder().level(Level.valueOf(defaultLogLevelProperty.defaultValue))
            .build());
  }

  private List<AWSLambdaLoggerLevel> getLoggerLevels(String loggerName) {
    var name = loggerName;
    int indexOfLastDot = name.length();
    String loggerLevelString = null;

    while (isNull(loggerLevelString) && indexOfLastDot > -1) {
      name = name.substring(0, indexOfLastDot);
      loggerLevelString = getStringProperty(AWSLambdaLoggerConfigurationProperty.LogLevel, name);
      indexOfLastDot = name.lastIndexOf(DOT);
    }

    List<AWSLambdaLoggerLevel> loggerLevels = null;

    if (nonNull(loggerLevelString)) {
      try {
        loggerLevels = parseLoggerLevelString(loggerLevelString);
      } catch (IllegalArgumentException exception) {
        Reporter.warn("Bad log level of the logger " + loggerName);
      }
    }

    if (isNull(loggerLevels)) {
      loggerLevels = defaultLoggerLevel;
    }

    return loggerLevels;
  }

  private String getStringProperty(AWSLambdaLoggerConfigurationProperty configurationProperty) {
    String value = System.getenv(configurationProperty.variableName);

    if (isNull(value)) {
      value = getProperties().getProperty(configurationProperty.propertyName);
    }
    if (isNull(value)) {
      value = configurationProperty.defaultValue;
    }

    return value;
  }

  private String getStringProperty(AWSLambdaLoggerConfigurationProperty configurationProperty,
      String name) {
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
    } catch (IOException | NullPointerException e) {
      // ignored
      Reporter.warn(CONFIGURATION_FILE + " is missed");
    }

    return properties;
  }

  private List<AWSLambdaLoggerLevel> parseLoggerLevelString(String loggerLevelString)
      throws IllegalArgumentException {
    var loggerLevels = new ArrayList<AWSLambdaLoggerLevel>();

    for (String loggerLevel : loggerLevelString.split(logLevelSeparator)) {
      var loggerLevelBuilder = AWSLambdaLoggerLevel.builder();
      var loggerLevelWithMarkers = loggerLevel.split(AT);

      loggerLevelBuilder.level(Level.valueOf(loggerLevelWithMarkers[0].toUpperCase()));
      if (loggerLevelWithMarkers.length > 1) {
        for (String markerName : loggerLevelWithMarkers[1].split(markerSeparator)) {
          loggerLevelBuilder.marker(markerName);
        }
      }
      loggerLevels.add(loggerLevelBuilder.build());
    }

    return loggerLevels;
  }

}
