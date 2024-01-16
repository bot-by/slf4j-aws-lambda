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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Marker;
import org.slf4j.event.Level;

/**
 * The logger output.
 */
public interface AWSLambdaLoggerOutput {

  /**
   * Write a message to the log.
   *
   * @param configuration logging configuration
   * @param marker        logging marker
   * @param level         logging level
   * @param message       logging message
   * @param throwable     exception
   */
  void log(@NotNull AWSLambdaLoggerConfiguration configuration, @Nullable Marker marker,
      @NotNull Level level, @NotNull String message, @Nullable Throwable throwable);

}
