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

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.BasicMarkerFactory;

/**
 * The logger level container.
 */
public class AWSLambdaLoggerLevel {

  private final Level level;
  private final Marker[] markers;

  private AWSLambdaLoggerLevel(Builder builder) {
    level = builder.level;
    markers = builder.markers.toArray(new Marker[0]);
  }

  static Builder builder() {
    return new Builder();
  }

  /**
   * The logger level.
   *
   * @return logger level
   */
  public Level getLevel() {
    return level;
  }

  /**
   * The logger markers.
   *
   * @return logger markers
   */
  public Marker[] getMarkers() {
    return markers;
  }

  static class Builder {

    private final BasicMarkerFactory markerFactory = new BasicMarkerFactory();
    private final List<Marker> markers;
    private Level level;

    private Builder() {
      markers = new ArrayList<>();
    }

    AWSLambdaLoggerLevel build() {
      requireNonNull(level, "Logger level is null");
      return new AWSLambdaLoggerLevel(this);
    }

    Builder level(Level level) {
      this.level = level;
      return this;
    }

    Builder marker(String markerName) {
      markers.add(markerFactory.getMarker(markerName));
      return this;
    }

  }

}
