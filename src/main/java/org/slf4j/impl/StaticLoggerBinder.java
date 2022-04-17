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
