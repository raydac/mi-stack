package com.igormaznitsa.mistack;

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
   * Get stack name.
   *
   * @return the stack name, it can't be null
   * @since 1.0.0
   */
  String getName();

  /**
   * Push multiple elements on the stack.
   *
   * @param items elements to be pushed on the stack, must not contain any null element.
   * @return the stack instance
   * @throws IllegalStateException if stack is closed
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
   * @throws IllegalStateException if stack is closed
   * @since 1.0.0
   */
  MiStack<T> push(MiStackItem<T> item);

  /**
   * Pop the first element from the stack which meet predicate condition.
   * The element will be removed from the stack.
   *
   * @param predicate condition for element search, must not be null
   * @return condition for element search, must not be null
   * @throws IllegalStateException if stack is closed
   * @since 1.0.0
   */
  Optional<MiStackItem<T>> pop(Predicate<MiStackItem<T>> predicate);

  /**
   * Peek element on the stack which meet predicate condition.
   * The element won't be removed from stack.
   *
   * @param predicate condition for element search, must not be null
   * @param depth     how many elements must be skipped during search
   * @return found element
   * @throws IllegalStateException if stack is closed
   * @since 1.0.0
   */
  Optional<MiStackItem<T>> peek(Predicate<MiStackItem<T>> predicate, long depth);

  /**
   * Find and remove element on the stack which meet predicate condition.
   *
   * @param predicate condition for element search, must not be null
   * @param depth     how many elements must be skipped during search
   * @return removed element
   * @throws IllegalStateException if stack is closed
   * @since 1.0.0
   */
  Optional<MiStackItem<T>> remove(Predicate<MiStackItem<T>> predicate, long depth);

  /**
   * Remove all elements from the stack.
   *
   * @throws IllegalStateException if stack is closed
   * @since 1.0.0
   */
  void clear();

  /**
   * Remove all elements from the stack which meet predicate condition.
   *
   * @param predicate condition for elements, must not be null.
   * @throws IllegalStateException if stack is closed
   * @since 1.0.0
   */
  void clear(final Predicate<MiStackItem<T>> predicate);

  /**
   * Make iterator for all stack elements.
   *
   * @return created iterator, must not be null.
   * @throws IllegalStateException if stack is closed
   * @since 1.0.0
   */
  default Iterator<MiStackItem<T>> iterator() {
    return this.iterator(this.forAll());
  }

  /**
   * Make iterator for stack elements which meet predicate.
   *
   * @param predicate condition for elements, must not be null.
   * @return created iterator, must not be null.
   * @throws IllegalStateException if stack is closed
   * @since 1.0.0
   */
  default Iterator<MiStackItem<T>> iterator(Predicate<MiStackItem<T>> predicate) {
    return this.iterator(predicate, this.forAll());
  }

  /**
   * Make stream of all elements on the stack.
   *
   * @return stream with all stacked elements in their order on the stack, must not be null.
   * @throws IllegalStateException if stack is closed
   * @since 1.0.0
   */
  default Stream<MiStackItem<T>> stream() {
    return this.stream(this.forAll(), this.forAll());
  }

  /**
   * Get stream of stack items meet predicate.
   *
   * @param predicate condition for elements, must not be null.
   * @param takeWhile predicated to take elements while it is true, must not be null.
   * @return created stream of all stacked elements meet predicate in their stack order, must
   * not be null.
   * @throws IllegalStateException if stack is closed
   * @since 1.0.0
   */
  default Stream<MiStackItem<T>> stream(Predicate<MiStackItem<T>> predicate,
                                        Predicate<MiStackItem<T>> takeWhile) {
    return StreamSupport.stream(
        spliteratorUnknownSize(this.iterator(predicate, takeWhile), ORDERED), false);
  }

  /**
   * Get predicate matches for all items in the stack.
   *
   * @return predicate matches for all items in the stack, must not be null.
   * @since 1.0.0
   */
  Predicate<MiStackItem<T>> forAll();

  /**
   * Make iterator for stack elements which meet predicate with possibility to stop
   * iteration by predicate.
   *
   * @param predicate condition for elements, must not be null.
   * @param takeWhile condition predicate to take next element if true, if false then iteration
   *                 stopped, must not be null.
   * @return created iterator, must not be null.
   * @throws IllegalStateException if stack is closed
   * @since 1.0.0
   */
  Iterator<MiStackItem<T>> iterator(Predicate<MiStackItem<T>> predicate,
                                    Predicate<MiStackItem<T>> takeWhile);

  /**
   * Check that there is no any element on the stack.
   *
   * @return true if the stack is empty, false elsewhere
   * @throws IllegalStateException if stack is closed
   * @since 1.0.0
   */
  boolean isEmpty();

  /**
   * Check that there is no any element on the stack for predicate.
   *
   * @return true if the stack is empty, false elsewhere
   * @throws IllegalStateException if stack is closed
   * @since 1.0.0
   */
  boolean isEmpty(Predicate<MiStackItem<T>> predicate);

  /**
   * Find size of stack for elements meet predicate.
   *
   * @param predicate condition for elements, must not be null
   * @return number of found elements on the stack
   * @throws IllegalStateException if stack is closed
   * @since 1.0.0
   */
  default long size(Predicate<MiStackItem<T>> predicate) {
    return this.stream(predicate).count();
  }

  /**
   * Get stream of stack items meet predicate.
   *
   * @param predicate condition for elements, must not be null.
   * @return created stream of all stacked elements meet predicate in their stack order, must
   *         not be null.
   * @throws IllegalStateException if stack is closed
   * @since 1.0.0
   */
  default Stream<MiStackItem<T>> stream(Predicate<MiStackItem<T>> predicate) {
    return this.stream(predicate, this.forAll());
  }

  /**
   * Get number of all stack elements.
   *
   * @return number of all stack elements, 0 for empty stack.
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

  /**
   * Allows to get information that the stack is closed.
   *
   * @return true if the stack is closed, false otherwise.
   * @since 1.0.0
   */
  boolean isClosed();
}
