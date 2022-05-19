package com.igormaznitsa.mistack;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A multi-context stack. It provides way to make iterations through tag marked elements.
 *
 * @author Igor Maznitsa
 * @since 1.0.0
 */
public interface MiStack<T> extends Iterable<MiStackItem<T>>, AutoCloseable {

  /**
   * Get predicate matches for all items in the stack.
   *
   * @return predicate matches for all items in the stack, must not be null.
   * @since 1.0.0
   */
  Predicate<MiStackItem<T>> forAll();

  /**
   * Get stack name.
   *
   * @return the stack name, it can't be null
   * @since 1.0.0
   */
  String getName();

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
   * Push multiple elements on the stack.
   *
   * @param items elements to be pushed on the stack, must not contain any null element.
   * @return the stack instance
   * @throws IllegalStateException if stack is closed
   * @since 1.0.0
   */
  MiStack<T> push(MiStackItem<T>... items);

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
  Iterator<MiStackItem<T>> iterator();

  /**
   * Make iterator for stack elements which meet predicate.
   *
   * @param predicate condition for elements, must not be null.
   * @return created iterator, must not be null.
   * @throws IllegalStateException if stack is closed
   * @since 1.0.0
   */
  Iterator<MiStackItem<T>> iterator(Predicate<MiStackItem<T>> predicate);

  /**
   * Make iterator for stack elements which meet predicate with possibility to stop iteration by predicate.
   *
   * @param predicate condition for elements, must not be null.
   * @param takeWhile condition predicate to take next element if true, if false then iteration stopped, must not be null.
   * @return created iterator, must not be null.
   * @throws IllegalStateException if stack is closed
   * @since 1.0.0
   */
  Iterator<MiStackItem<T>> iterator(Predicate<MiStackItem<T>> predicate,
                                    Predicate<MiStackItem<T>> takeWhile);

  /**
   * Get stream of stack items meet predicate.
   *
   * @param predicate condition for elements, must not be null.
   * @return created stream of all stacked elements meet predicate in their stack order, must not be null.
   * @throws IllegalStateException if stack is closed
   * @since 1.0.0
   */
  Stream<MiStackItem<T>> stream(Predicate<MiStackItem<T>> predicate);

  /**
   * Get stream of stack items meet predicate.
   *
   * @param predicate condition for elements, must not be null.
   * @param takeWhile predicated to take elements while it is true, must not be null.
   * @return created stream of all stacked elements meet predicate in their stack order, must not be null.
   * @throws IllegalStateException if stack is closed
   * @since 1.0.0
   */
  Stream<MiStackItem<T>> stream(Predicate<MiStackItem<T>> predicate,
                                Predicate<MiStackItem<T>> takeWhile);

  /**
   * Make stream of all elements on the stack.
   *
   * @return stream with all stacked elements in their order on the stack, must not be null.
   * @throws IllegalStateException if stack is closed
   * @since 1.0.0
   */
  Stream<MiStackItem<T>> stream();

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
  long size(Predicate<MiStackItem<T>> predicate);

  long size();

  @Override
  void close();

  /**
   * Create predicate matches if all listed tags met in item.
   *
   * @param tags array of tags
   * @return predicate which is true if item contains all tags, must not be null
   * @since 1.0.0
   */
  default Predicate<MiStackItem<T>> allTags(final MiStackTag... tags) {
    return this.allTags(List.of(tags));
  }

  /**
   * Create predicate matches if all listed tags met in item.
   *
   * @param tags collections of tags
   * @return predicate which is true if item contains all tags, must not be null
   * @since 1.0.0
   */
  default Predicate<MiStackItem<T>> allTags(final Collection<MiStackTag> tags) {
    return e -> e.getTags().containsAll(tags);
  }

  /**
   * Create predicate matches if any tag met in item
   *
   * @param tags array of tags
   * @return predicate which is true if item contains any tag, must not be null
   * @since 1.0.0
   */
  default Predicate<MiStackItem<T>> anyTag(final MiStackTag... tags) {
    return this.anyTag(List.of(tags));
  }

  /**
   * Create predicate matches if any tag met in item
   *
   * @param tags collections of tags
   * @return predicate which is true if item contains any tag, must not be null
   * @since 1.0.0
   */
  default Predicate<MiStackItem<T>> anyTag(final Collection<MiStackTag> tags) {
    var setOfTags = Set.copyOf(tags);
    return e -> e.getTags().stream().anyMatch(setOfTags::contains);
  }
}
