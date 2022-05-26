package com.igormaznitsa.mistack;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import com.igormaznitsa.mistack.impl.MiStackArrayList;
import com.igormaznitsa.mistack.impl.MiStackItemImpl;
import com.igormaznitsa.mistack.impl.MiStackTagImpl;
import org.junit.jupiter.api.Test;

public class CodeForReadmeTest {

  @Test
  public void testJustCreate() {

    var tagSun = MiStackTagImpl.tagsOf("Solar");
    var tagSatellite = MiStackTagImpl.tagsOf("Satellite");
    var tagGalaxy = MiStackTagImpl.tagsOf("Galaxy");

    try (var stack = new MiStackArrayList<>()) {
      stack.push(MiStackItemImpl.itemOf("Earth", tagSun));
      stack.push(MiStackItemImpl.itemOf("Moon", tagSun, tagSatellite));
      stack.push(MiStackItemImpl.itemOf("Antares", tagGalaxy));
      stack.push(MiStackItemImpl.itemOf("Vega", tagGalaxy));

      assertArrayEquals(new Object[] {"Moon", "Earth"}, stack.stream(stack.allTags(tagSun)).map(
          MiStackItem::getValue).toArray());
      assertArrayEquals(new Object[] {"Moon"}, stack.stream(stack.allTags(tagSatellite)).map(
          MiStackItem::getValue).toArray());
      assertArrayEquals(new Object[] {"Moon", "Earth"}, stack.stream(stack.anyTag(tagSun)).map(
          MiStackItem::getValue).toArray());
      assertArrayEquals(new Object[] {"Vega", "Antares"},
          stack.stream(stack.allTags(tagGalaxy)).map(
              MiStackItem::getValue).toArray());
    }
  }

}
