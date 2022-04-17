package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

/**
 * Responsible for binding the {@link LambdaLoggerFactory}. This is used by the SLF4J API.
 */
public class StaticLoggerBinder implements LoggerFactoryBinder {

  private static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();
  /**
   * The version of the SLF4J API this implementation is compiled against.
   */
  // to avoid constant folding by the compiler, this field must not be final
  public static String REQUESTED_API_VERSION = "1.7.36";
  private final ILoggerFactory loggerFactory = new LambdaLoggerFactory();

  private StaticLoggerBinder() {
  }

  public static StaticLoggerBinder getSingleton() {
    return SINGLETON;
  }

  @Override
  public ILoggerFactory getLoggerFactory() {
    return loggerFactory;
  }

  @Override
  public String getLoggerFactoryClassStr() {
    return loggerFactory.getClass().getName();
  }

}
