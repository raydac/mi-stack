package com.igormaznitsa.mistack;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import com.igormaznitsa.mistack.impl.MiStackArrayList;
import com.igormaznitsa.mistack.impl.MiStackItemImpl;
import com.igormaznitsa.mistack.impl.MiStackTagImpl;
import org.junit.jupiter.api.Test;

public class CodeForReadmeTest {

  @Test
  public void testJustCreate() {

    var tagStar = MiStackTagImpl.tagsOf("star");
    var tagPlanet = MiStackTagImpl.tagsOf("planet");
    var tagPlanetoid = MiStackTagImpl.tagsOf("planetoid");
    var tagAsteroid = MiStackTagImpl.tagsOf("asteroid");
    var tagSatellite = MiStackTagImpl.tagsOf("satellite");

    try (var stack = new MiStackArrayList<>()) {
      stack.push(MiStackItemImpl.itemOf("Sun", tagStar));
      stack.push(MiStackItemImpl.itemOf("Mercury", tagPlanet));
      stack.push(MiStackItemImpl.itemOf("Venus", tagPlanet, tagSatellite));
      stack.push(MiStackItemImpl.itemOf("Earth", tagPlanet, tagSatellite));
      stack.push(MiStackItemImpl.itemOf("Moon", tagPlanetoid, tagSatellite));
      stack.push(MiStackItemImpl.itemOf("Mars", tagPlanet));
      stack.push(MiStackItemImpl.itemOf("Phobos", tagAsteroid, tagSatellite));
      stack.push(MiStackItemImpl.itemOf("Demos", tagAsteroid, tagSatellite));
      stack.push(MiStackItemImpl.itemOf("Jupiter", tagPlanet));
      stack.push(MiStackItemImpl.itemOf("Saturn", tagPlanet));
      stack.push(MiStackItemImpl.itemOf("Uranus", tagPlanet));
      stack.push(MiStackItemImpl.itemOf("Neptune", tagPlanet));
      stack.push(MiStackItemImpl.itemOf("Pluto", tagPlanetoid));

      assertArrayEquals(new Object[] {"Sun"}, stack.stream(MiStack.allTags(tagStar)).map(
          MiStackItem::getValue).toArray());
      assertArrayEquals(new Object[] {"Pluto", "Moon"},
          stack.stream(MiStack.allTags(tagPlanetoid)).map(
              MiStackItem::getValue).toArray());
      assertArrayEquals(
          new Object[] {"Neptune", "Uranus", "Saturn", "Jupiter", "Mars", "Earth", "Venus",
              "Mercury"}, stack.stream(MiStack.allTags(tagPlanet)).map(
              MiStackItem::getValue).toArray());
      assertArrayEquals(
          new Object[] {"Pluto", "Neptune", "Uranus", "Saturn", "Jupiter", "Mars", "Moon", "Earth",
              "Venus", "Mercury"}, stack.stream(MiStack.anyTag(tagPlanet, tagPlanetoid)).map(
              MiStackItem::getValue).toArray());
    }
  }

}
