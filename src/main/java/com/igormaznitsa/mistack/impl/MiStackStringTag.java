package com.igormaznitsa.mistack.impl;

import com.igormaznitsa.mistack.MiStackTag;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * String based Mi-Stack tag implementation.
 *
 * @since 1.0.0
 */
public class MiStackStringTag implements MiStackTag {
  private final String name;

  /**
   * Constructor.
   *
   * @param name string value to be used as tag. Must not be null.
   * @since 1.0.0
   */
  public MiStackStringTag(final String name) {
    this.name = Objects.requireNonNull(name);
  }

  /**
   * Make string based tag from text representation of object.
   *
   * @param obj object which text representation will be used as tag name.
   * @return generated string based tag, can't be null.
   * @since 1.0.0
   */
  public static MiStackTag tagOf(final Object obj) {
    return new MiStackStringTag(String.valueOf(obj));
  }

  /**
   * Make set of string based tags from array of objects.
   *
   * @param objs array of objects to be converted into string based tags.
   * @return generated tags as a set, can't be null.
   * @since 1.0.0
   */
  public static Set<MiStackTag> tagsOf(final Object... objs) {
    return Stream.of(objs).map(MiStackStringTag::tagOf).collect(Collectors.toSet());
  }

  @Override
  public boolean equals(final Object that) {
    if (this == that) {
      return true;
    }
    if (that instanceof MiStackStringTag) {
      return this.name.equals(((MiStackStringTag) that).name);
    }
    return false;
  }

  /**
   * Get tag name.
   *
   * @return name of the tag, can't be null.
   * @since 1.0.0
   */
  public String getName() {
    return this.name;
  }

  @Override
  public int hashCode() {
    return this.name.hashCode();
  }

  @Override
  public String toString() {
    return this.name;
  }
}
