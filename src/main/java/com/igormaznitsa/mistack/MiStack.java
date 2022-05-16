package com.igormaznitsa.mistack;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A multi-context stack. It provides way to make iterations through tag marked elements.
 *
 * @author Igor Maznitsa
 * @since 1.0.0
 */
public interface MiStack extends Iterable<MiStackItem>, AutoCloseable {

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
  MiStack push(MiStackItem item);

  /**
   * Push multiple elements on the stack.
   *
   * @param items elements to be pushed on the stack, must not contain any null element.
   * @return the stack instance
   * @throws IllegalStateException if stack is closed
   * @since 1.0.0
   */
  MiStack push(MiStackItem... items);

  /**
   * Pop the first element from the stack which meet predicate condition.
   * The element will be removed from the stack.
   *
   * @param predicate condition for element search, must not be null
   * @return condition for element search, must not be null
   * @throws IllegalStateException if stack is closed
   * @since 1.0.0
   */
  Optional<MiStackItem> pop(Predicate<MiStackItem> predicate);

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
  Optional<MiStackItem> peek(Predicate<MiStackItem> predicate, long depth);

  /**
   * Find and remove element on the stack which meet predicate condition.
   *
   * @param predicate condition for element search, must not be null
   * @param depth     how many elements must be skipped during search
   * @return removed element
   * @throws IllegalStateException if stack is closed
   * @since 1.0.0
   */
  Optional<MiStackItem> remove(Predicate<MiStackItem> predicate, long depth);

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
  void clear(final Predicate<MiStackItem> predicate);

  /**
   * Get iterator for all stack elements.
   *
   * @return created iterator, must not be null.
   * @throws IllegalStateException if stack is closed
   * @since 1.0.0
   */
  Iterator<MiStackItem> iterator();

  /**
   * Get iterator for stack elements which meet predicate.
   *
   * @param predicate condition for elements, must not be null.
   * @return created iterator, must not be null.
   * @throws IllegalStateException if stack is closed
   * @since 1.0.0
   */
  Iterator<MiStackItem> iterator(Predicate<MiStackItem> predicate);


  /**
   * Get stream of stack items meet predicate.
   *
   * @param predicate condition for elements, must not be null.
   * @return created stream of all stacked elements meet predicate in their stack order, must not be null.
   * @throws IllegalStateException if stack is closed
   * @since 1.0.0
   */
  Stream<MiStackItem> stream(Predicate<MiStackItem> predicate);

  /**
   * Make stream of all elements on the stack.
   *
   * @return stream with all stacked elements in their order on the stack, must not be null.
   * @throws IllegalStateException if stack is closed
   * @since 1.0.0
   */
  Stream<MiStackItem> stream();

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
  boolean isEmpty(Predicate<MiStackItem> predicate);

  /**
   * Find size of stack for elements meet predicate.
   *
   * @param predicate condition for elements, must not be null
   * @return number of found elements on the stack
   * @throws IllegalStateException if stack is closed
   * @since 1.0.0
   */
  long size(Predicate<MiStackItem> predicate);

  long size();

}
