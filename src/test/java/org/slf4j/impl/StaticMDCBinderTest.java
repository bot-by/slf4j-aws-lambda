package org.slf4j.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("slow")
class StaticMDCBinderTest {

  @Test
  void getSingleton() {
    assertNotNull(StaticMDCBinder.getSingleton());
  }

  @Test
  void getMDCA() {
    assertNotNull(StaticMDCBinder.getSingleton().getMDCA());
  }

  @Test
  void getMDCAdapterClassStr() {
    assertEquals("org.slf4j.helpers.BasicMDCAdapter",
        StaticMDCBinder.getSingleton().getMDCAdapterClassStr());
  }

}
