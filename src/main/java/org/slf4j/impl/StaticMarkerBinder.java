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

import org.slf4j.IMarkerFactory;
import org.slf4j.MarkerFactory;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.spi.MarkerFactoryBinder;

/**
 * Responsible for binding the {@link MarkerFactory}. This is used by the SLF4J API.
 */
public class StaticMarkerBinder implements MarkerFactoryBinder {

  /**
   * The single instance of this class.
   */
  public static final StaticMarkerBinder SINGLETON = new StaticMarkerBinder();

  private final IMarkerFactory markerFactory = new BasicMarkerFactory();

  private StaticMarkerBinder() {
  }

  public static StaticMarkerBinder getSingleton() {
    return SINGLETON;
  }

  /**
   * This method always returns a {@link BasicMarkerFactory}.
   *
   * @return the marker factory instance
   */
  @Override
  public IMarkerFactory getMarkerFactory() {
    return markerFactory;
  }

  @Override
  public String getMarkerFactoryClassStr() {
    return markerFactory.getClass().getName();
  }

}
