package org.slf4j.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("slow")
class StaticLoggerBinderTest {

  @Test
  void getSingleton() {
    assertNotNull(StaticLoggerBinder.getSingleton());
  }

  @Test
  void getLoggerFactory() {
    assertNotNull(StaticLoggerBinder.getSingleton().getLoggerFactory());
  }

  @Test
  void getLoggerFactoryClassStr() {
    assertEquals("uk.bot_by.aws_lambda.slf4j.LambdaLoggerFactory",
        StaticLoggerBinder.getSingleton().getLoggerFactoryClassStr());
  }

}
