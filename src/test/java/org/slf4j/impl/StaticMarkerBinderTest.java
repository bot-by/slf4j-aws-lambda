package org.slf4j.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("slow")
class StaticMarkerBinderTest {

  @Test
  void getSingleton() {
    assertNotNull(StaticMarkerBinder.getSingleton());
  }

  @Test
  void getMarkerFactory() {
    assertNotNull(StaticMarkerBinder.getSingleton().getMarkerFactory());
  }

  @Test
  void getMarkerFactoryClassStr() {
    assertEquals("org.slf4j.helpers.BasicMarkerFactory",
        StaticMarkerBinder.getSingleton().getMarkerFactoryClassStr());
  }

}
