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
 * @param <T> type of object carried by item
 * @since 1.0.0
 */
public class MiStackItemImpl<T> implements MiStackItem<T>, Serializable {

  private final Set<MiStackTag> tags;
  private final T value;

  /**
   * Constructor.
   *
   * @param value value to be carried by the item, must not be null
   * @param tags  tags associated with th item, must not be null but can be empty
   * @since 1.0.0
   */
  public MiStackItemImpl(final T value, final Set<MiStackTag> tags) {
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
  public static <V> MiStackItem<V> itemOf(final V value, final MiStackTag... tags) {
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
  public static <V> MiStackItem<V> itemOf(final V value, final Collection<MiStackTag>... tags) {
    return new MiStackItemImpl<>(value,
        Stream.of(tags).flatMap(Collection::stream).collect(Collectors.toSet()));
  }

  @Override
  public Set<MiStackTag> getTags() {
    return this.tags;
  }

  @Override
  public T getValue() {
    return this.value;
  }

  @Override
  public String toString() {
    return "MiStackItem(" + this.value + ";" + this.tags + ')';
  }
}
