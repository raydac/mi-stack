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

package com.igormaznitsa.mistack;

import static com.igormaznitsa.mistack.MiStackPredicates.itemsAll;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A multi-context stack. It provides way to make iterations through tag marked elements.
 *
 * @param <T> type of item values on the stack
 * @author Igor Maznitsa
 * @since 1.0.0
 */
public interface MiStack<T> extends Iterable<MiStackItem<T>>, AutoCloseable {

  /**
   * Make predicate to check all tags presented for stack item.
   *
   * @param tags array of tags, must not contain null or be null
   * @param <T>  type of stack item values.
   * @return predicate returns true only if all tags presented for stack item.
   * @since 1.0.0
   */
  static <T> Predicate<MiStackItem<T>> allTags(final MiStackTag... tags) {
    return allTags(List.of(tags));
  }

  /**
   * Make predicate to check all tags presented for stack item.
   *
   * @param tags array of tag collections, must not contain null or be null
   * @param <T>  type of stack item values.
   * @return predicate returns true only if all tags presented for stack item.
   * @since 1.0.0
   */
  @SafeVarargs
  static <T> Predicate<MiStackItem<T>> allTags(final Collection<MiStackTag>... tags) {
    var setOfAllTags = Stream.of(tags).flatMap(Collection::stream).collect(Collectors.toSet());
    return e -> e.getTags().containsAll(setOfAllTags);
  }

  /**
   * Make predicate to check any tag presented for stack item.
   *
   * @param tags array of tags, must not contain null or be null
   * @param <T>  type of stack item values.
   * @return predicate returns true only if all tags presented for stack item.
   * @since 1.0.0
   */
  static <T> Predicate<MiStackItem<T>> anyTag(final MiStackTag... tags) {
    return anyTag(List.of(tags));
  }

  /**
   * Make predicate to check any tag presented for stack item.
   *
   * @param tags array of tag collections, must not contain null or be null
   * @param <T>  type of stack item values.
   * @return predicate returns true only if all tags presented for stack item.
   * @since 1.0.0
   */
  @SafeVarargs
  static <T> Predicate<MiStackItem<T>> anyTag(final Collection<MiStackTag>... tags) {
    var setOfTags = Stream.of(tags).flatMap(Collection::stream).collect(Collectors.toSet());
    return e -> e.getTags().stream().anyMatch(setOfTags::contains);
  }

  /**
   * Push multiple elements on the stack.
   *
   * @param items elements to be pushed on the stack, must not contain any null element.
   * @return the stack instance
   * @throws IllegalStateException thrown if stack already closed.
   * @since 1.0.0
   */
  @SuppressWarnings("unchecked")
  default MiStack<T> push(final MiStackItem<T>... items) {
    for (final MiStackItem<T> item : items) {
      this.push(item);
    }
    return this;
  }

  /**
   * Push single element on the stack.
   *
   * @param item element to be pushed on the stack, must not be null.
   * @return the stack instance
   * @throws IllegalStateException thrown if stack already closed.
   * @since 1.0.0
   */
  MiStack<T> push(MiStackItem<T> item);

  /**
   * Pop the first element from the stack which meet predicate condition.
   * The element will be removed from the stack.
   *
   * @param predicate condition for element search, must not be null
   * @return condition for element search, must not be null
   * @throws IllegalStateException thrown if stack already closed.
   * @since 1.0.0
   */
  default Optional<MiStackItem<T>> pop(final Predicate<MiStackItem<T>> predicate) {
    this.assertNotClosed();
    MiStackItem<T> result = null;
    var iterator = this.iterator(predicate, itemsAll());
    if (iterator.hasNext()) {
      result = iterator.next();
      iterator.remove();
    }
    return Optional.ofNullable(result);
  }

  /**
   * Assert that the stack is not closed.
   *
   * @throws IllegalStateException thrown if stack already closed.
   * @since 1.0.0
   */
  default void assertNotClosed() {
    if (this.isClosed()) {
      throw new IllegalStateException("Stack " + this.getName() + " is already closed");
    }
  }

  /**
   * Make iterator for stack elements which meet predicate with possibility to stop
   * iteration by predicate.
   *
   * @param predicate condition for elements, must not be null.
   * @param takeWhile condition predicate to take next element if true, if false then iteration
   *                  stopped, must not be null.
   * @return created iterator, must not be null.
   * @throws IllegalStateException thrown if stack already closed.
   * @since 1.0.0
   */
  TruncableIterator<MiStackItem<T>> iterator(Predicate<MiStackItem<T>> predicate,
                                             Predicate<MiStackItem<T>> takeWhile);

  /**
   * Allows to get information that the stack is closed.
   *
   * @return true if the stack is closed, false otherwise.
   * @since 1.0.0
   */
  boolean isClosed();

  /**
   * Get stack name.
   *
   * @return the stack name, it can't be null
   * @since 1.0.0
   */
  String getName();

  /**
   * Peek element on the stack which meet predicate condition.
   * The element won't be removed from stack.
   *
   * @param predicate condition for element search, must not be null
   * @param depth     how many elements must be skipped during search
   * @return found element
   * @throws IllegalStateException thrown if stack already closed.
   * @since 1.0.0
   */
  default Optional<MiStackItem<T>> peek(final Predicate<MiStackItem<T>> predicate, long depth) {
    this.assertNotClosed();
    MiStackItem<T> result = null;
    final Iterator<MiStackItem<T>> iterator = this.iterator(predicate, itemsAll());
    while (iterator.hasNext() && result == null) {
      result = iterator.next();
      if (predicate.test(result)) {
        if (depth > 0L) {
          result = null;
          depth--;
        }
      }
    }
    return Optional.ofNullable(result);
  }

  /**
   * Find and remove element on the stack which meet predicate condition.
   *
   * @param predicate condition for element search, must not be null
   * @param depth     how many elements must be skipped during search
   * @return removed element
   * @throws IllegalStateException thrown if stack already closed.
   * @since 1.0.0
   */
  default Optional<MiStackItem<T>> remove(final Predicate<MiStackItem<T>> predicate, long depth) {
    this.assertNotClosed();
    MiStackItem<T> result = null;
    final Iterator<MiStackItem<T>> iterator = this.iterator(predicate, itemsAll());
    while (iterator.hasNext() && result == null) {
      result = iterator.next();
      if (predicate.test(result)) {
        if (depth > 0L) {
          result = null;
          depth--;
        } else {
          iterator.remove();
        }
      }
    }
    return Optional.ofNullable(result);
  }

  /**
   * Remove all elements from the stack.
   *
   * @throws IllegalStateException thrown if stack already closed.
   * @since 1.0.0
   */
  void clear();

  /**
   * Remove all elements from the stack which meet predicate condition.
   *
   * @param predicate condition for elements, must not be null.
   * @throws IllegalStateException thrown if stack already closed.
   * @since 1.0.0
   */
  default void clear(final Predicate<MiStackItem<T>> predicate) {
    this.assertNotClosed();
    var iterator = this.iterator();
    while (iterator.hasNext()) {
      if (predicate.test(iterator.next())) {
        iterator.remove();
      }
    }
  }

  /**
   * Make iterator for all stack elements.
   *
   * @return created iterator, must not be null.
   * @throws IllegalStateException thrown if stack already closed.
   * @since 1.0.0
   */
  default TruncableIterator<MiStackItem<T>> iterator() {
    this.assertNotClosed();
    return this.iterator(itemsAll());
  }

  /**
   * Make iterator for stack elements which meet predicate.
   *
   * @param predicate condition for elements, must not be null.
   * @return created iterator, must not be null.
   * @throws IllegalStateException thrown if stack already closed.
   * @since 1.0.0
   */
  default TruncableIterator<MiStackItem<T>> iterator(Predicate<MiStackItem<T>> predicate) {
    this.assertNotClosed();
    return this.iterator(predicate, itemsAll());
  }

  /**
   * Make stream of all elements on the stack.
   *
   * @return stream with all stacked elements in their order on the stack, must not be null.
   * @throws IllegalStateException thrown if stack already closed.
   * @since 1.0.0
   */
  default Stream<MiStackItem<T>> stream() {
    this.assertNotClosed();
    return this.stream(itemsAll(), itemsAll());
  }

  /**
   * Get stream of stack items meet predicate.
   *
   * @param predicate condition for elements, must not be null.
   * @param takeWhile predicated to take elements while it is true, must not be null.
   * @return created stream of all stacked elements meet predicate in their stack order, must
   * not be null.
   * @throws IllegalStateException thrown if stack already closed.
   * @since 1.0.0
   */
  default Stream<MiStackItem<T>> stream(Predicate<MiStackItem<T>> predicate,
                                        Predicate<MiStackItem<T>> takeWhile) {
    return StreamSupport.stream(
        spliteratorUnknownSize(this.iterator(predicate, takeWhile), ORDERED), false);
  }

  /**
   * Check that there is no any element on the stack for predicate.
   *
   * @param  predicate it allows to select items which should take part in search
   * @return true if the stack is empty, false elsewhere
   * @throws IllegalStateException thrown if stack already closed.
   * @since 1.0.0
   */
  default boolean isEmpty(Predicate<MiStackItem<T>> predicate) {
    if (this.isEmpty()) {
      return true;
    }
    boolean foundAny = false;
    var iterator = this.iterator();
    while (iterator.hasNext() && !foundAny) {
      foundAny = predicate.test(iterator.next());
    }
    return !foundAny;
  }

  /**
   * Check that there is no any element on the stack.
   *
   * @return true if the stack is empty, false elsewhere
   * @throws IllegalStateException thrown if stack already closed.
   * @since 1.0.0
   */
  boolean isEmpty();

  /**
   * Find size of stack for elements meet predicate.
   *
   * @param predicate condition for elements, must not be null
   * @return number of found elements on the stack
   * @throws IllegalStateException thrown if stack already closed.
   * @since 1.0.0
   */
  default long size(Predicate<MiStackItem<T>> predicate) {
    this.assertNotClosed();
    return this.stream(predicate).count();
  }

  /**
   * Get stream of stack items meet predicate.
   *
   * @param predicate condition for elements, must not be null.
   * @return created stream of all stacked elements meet predicate in their stack order, must
   * not be null.
   * @throws IllegalStateException thrown if stack already closed.
   * @since 1.0.0
   */
  default Stream<MiStackItem<T>> stream(Predicate<MiStackItem<T>> predicate) {
    this.assertNotClosed();
    return this.stream(predicate, itemsAll());
  }

  /**
   * Get number of all stack elements.
   *
   * @return number of all stack elements, 0 for empty stack.
   * @throws IllegalStateException thrown if stack already closed.
   * @since 1.0.0
   */
  long size();

  /**
   * Close the stack and dispose its internal resources. After call the method.
   *
   * @throws IllegalStateException thrown if stack already closed.
   * @since 1.0.0
   */
  @Override
  void close();
}
