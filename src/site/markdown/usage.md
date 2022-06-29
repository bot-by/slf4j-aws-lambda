# Usage

There is a great original [manual][manual].

The configuration is similar to [SLF4J Simple][slf4j-simple].

It looks for the `lambda-logger.properties` resource and read properties:

* **dateTimeFormat** - The date and time format to be used in the output messages. The pattern
  describing the date and time format is defined by [SimpleDateFormat][]. If the format is not
  specified or is invalid, the number of milliseconds since start up will be output.
* **defaultLogLevel** - Default log level for all instances of LambdaLogger.
  Must be one of (_trace_, _debug_, _info_, _warn_, _error_), a value is case-insensitive.
  If not specified, defaults to _info_.
* **levelInBrackets** - Should the level string be output in brackets? Defaults to `false`.
* **requestId** - Set the context name of AWS request ID. Defaults to `AWS_REQUEST_ID`.
* **showDateTime** - Set to `true` if you want the current date and time to be included in output
  messages. Defaults to `false`.
* **showLogName** - Set to `true` if you want the Logger instance name to be included in output
  messages. Defaults to `true`.
* **showShortLogName** - Set to `true` if you want the last component of the name to be included in
  output messages. Defaults to `false`.
* **showThreadId** - If you would like to output the current thread id, then set to `true`.
  Defaults to `false`.
* **showThreadName** - Set to `true` if you want to output the current thread name.
  Defaults to `false`.

The environment variables overrides the properties: **LOG_AWS_REQUEST_ID**,
**LOG_DATE_TIME_FORMAT**, **LOG_DEFAULT_LEVEL**, **LOG_LEVEL_IN_BRACKETS**, **LOG_SHOW_DATE_TIME**,
**LOG_SHOW_NAME**, **LOG_SHOW_SHORT_NAME**, **LOG_SHOW_THREAD_ID**, **LOG_SHOW_THREAD_NAME**.

[manual]: https://www.slf4j.org/manual.html

[slf4j-simple]: https://www.slf4j.org/api/org/slf4j/simple/SimpleLogger.html

[SimpleDateFormat]: https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/text/SimpleDateFormat.html
