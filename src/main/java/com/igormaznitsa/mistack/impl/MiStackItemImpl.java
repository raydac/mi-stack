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

import static java.util.Objects.requireNonNull;

import com.igormaznitsa.mistack.MiStackItem;
import com.igormaznitsa.mistack.MiStackTag;
import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Parametrized implementation of mi-stack item.
 *
 * @param <V> type of object carried by item
 * @since 1.0.0
 */
public class MiStackItemImpl<V, T extends MiStackTag> implements MiStackItem<V, T>, Serializable {

  private final Set<T> tags;
  private final V value;

  /**
   * Constructor.
   *
   * @param value value to be carried by the item, must not be null
   * @param tags  tags associated with th item, must not be null but can be empty
   * @since 1.0.0
   */
  public MiStackItemImpl(final V value, final Set<T> tags) {
    this.tags = requireNonNull(tags);
    this.value = requireNonNull(value);
  }

  /**
   * Auxiliary method to create an item for its value with tags provided as array.
   *
   * @param value value to be saved in the item, must not be null
   * @param tags  array of tags to be associated with the item, must not be null
   * @param <V>   type of item value
   * @return generated created mi-stack item with value and tags, can't be null
   * @since 1.0.0
   */
  @SafeVarargs
  public static <T, V extends MiStackItem<T, G>, G extends MiStackTag> V itemOf(final T value,
                                                                                final G... tags) {
    return itemOf(value, Set.of(tags));
  }

  /**
   * Auxiliary method to create an item for its value with tags provided as collection.
   *
   * @param value value to be saved in the item, must not be null
   * @param tags  array of tag collections to be associated with the item, must not be null
   * @param <V>   type of item value
   * @return generated created mi-stack item with value and tags, can't be null
   * @since 1.0.0
   */
  @SafeVarargs
  @SuppressWarnings("unchecked")
  public static <T, V extends MiStackItem<T, G>, G extends MiStackTag> V itemOf(final T value,
                                                                                final Collection<G>... tags) {
    return (V) new MiStackItemImpl<>(value,
        Stream.of(tags).flatMap(Collection::stream).collect(Collectors.toSet()));
  }

  @Override
  public Set<T> getTags() {
    return this.tags;
  }

  @Override
  public V getValue() {
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
    if (that instanceof MiStackItem) {
      return this.tags.equals(((MiStackItem<?, ?>) that).getTags()) &&
          this.value.equals(((MiStackItem<?, ?>) that).getValue());
    }
    return false;
  }

  @Override
  public String toString() {
    return "MiStackItem(" + this.value + ";" + this.tags + ')';
  }
}
