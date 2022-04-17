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
