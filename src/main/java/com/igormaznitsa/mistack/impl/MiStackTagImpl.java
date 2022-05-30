/*
 * Copyright 2022 Igor Maznitsa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igormaznitsa.mistack.impl;

import com.igormaznitsa.mistack.MiStackTag;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Parametrized Mi-Stack tag implementation.
 *
 * @since 1.0.0
 */
public class MiStackTagImpl<T> implements MiStackTag {
  private final T value;

  /**
   * Constructor.
   *
   * @param value value to be used as tag. Must not be null.
   * @since 1.0.0
   */
  public MiStackTagImpl(final T value) {
    this.value = Objects.requireNonNull(value);
  }

  /**
   * Make set of tags from array of objects.
   *
   * @param values array of objects to be converted into tags.
   * @return generated tags as a set, can't be null.
   * @since 1.0.0
   */
  @SafeVarargs
  public static <T> Set<MiStackTag> tagsOf(final T... values) {
    return Stream.of(values).map(MiStackTagImpl::tagOf).collect(Collectors.toSet());
  }

  /**
   * Make string based tag from text representation of object.
   *
   * @param value object which will be used as tag.
   * @return generated tag, can't be null.
   * @since 1.0.0
   */
  public static <T> MiStackTag tagOf(final T value) {
    return new MiStackTagImpl<>(value);
  }

  /**
   * Get tag value.
   *
   * @return value of the tag, can't be null.
   * @since 1.0.0
   */
  public T getValue() {
    return this.value;
  }

  @Override
  public int hashCode() {
    return this.value.hashCode();
  }

  @Override
  public boolean equals(final Object that) {
    if (this == that) {
      return true;
    }
    if (that instanceof MiStackTagImpl) {
      return this.value.equals(((MiStackTagImpl<?>) that).value);
    }
    return false;
  }

  @Override
  public String toString() {
    return "MiStackTag(" + this.value + ')';
  }
}
