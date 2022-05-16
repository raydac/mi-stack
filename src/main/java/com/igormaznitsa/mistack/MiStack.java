package com.igormaznitsa.mistack;

import static com.igormaznitsa.mistack.Predicates.ALL_TAGS;
import static java.util.Objects.requireNonNull;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A multi-context stack. It provides way to make iterations through tag marked elements.
 * The stack is not thread safe, so you must provide extra synchronization if going to use
 * it in multi-thread environment.
 *
 * @author Igor Maznitsa
 * @since 1.0.0
 */
public class MiStack implements Iterable<MiStackItem> {

  private final String name;
  private final List<MiStackItem> items;

  /**
   * Default constructor. Name of stack will be generated automatically.
   *
   * @see UUID
   * @since 1.0.0
   */
  public MiStack() {
    this(UUID.randomUUID().toString());
  }

  /**
   * Constructor allows to provide name of stack.
   *
   * @param name name of stack, must not be null
   * @since 1.0.0
   */
  public MiStack(final String name) {
    this.name = requireNonNull(name);
    this.items = new ArrayList<>();
  }

  /**
   * Get stack name.
   *
   * @return the stack name, it can't be null
   * @since 1.0.0
   */
  public String getName() {
    return this.name;
  }

  /**
   * Push single element on the stack.
   *
   * @param item element to be pushed on the stack, must not be null.
   * @return the stack instance
   * @since 1.0.0
   */
  public MiStack push(final MiStackItem item) {
    this.items.add(requireNonNull(item));
    return this;
  }

  /**
   * Push multiple elements on the stack.
   *
   * @param items elements to be pushed on the stack, must not contain any null element.
   * @return the stack instance
   * @since 1.0.0
   */
  public MiStack push(final MiStackItem... items) {
    for (final MiStackItem s : items) {
      this.items.add(requireNonNull(s));
    }
    return this;
  }

  /**
   * Pop the first element from the stack which meet predicate condition.
   * The element will be removed from the stack.
   *
   * @param predicate condition for element search, must not be null
   * @return condition for element search, must not be null
   * @since 1.0.0
   */
  public Optional<MiStackItem> pop(final Predicate<MiStackItem> predicate) {
    MiStackItem result = null;
    for (int i = this.items.size() - 1; result == null && i >= 0; i--) {
      final MiStackItem item = this.items.get(i);
      if (predicate.test(item)) {
        result = item;
        this.items.remove(i);
      }
    }
    return Optional.ofNullable(result);
  }

  /**
   * Peek element on the stack which meet predicate condition.
   * The element won't be removed from stack.
   *
   * @param predicate condition for element search, must not be null
   * @param depth     how many elements must be skipped during search
   * @return found element
   * @since 1.0.0
   */
  public Optional<MiStackItem> peek(final Predicate<MiStackItem> predicate, final long depth) {
    return this.stream(predicate).skip(depth).findFirst();
  }

  /**
   * Find and remove element on the stack which meet predicate condition.
   *
   * @param predicate condition for element search, must not be null
   * @param depth     how many elements must be skipped during search
   * @return removed element
   * @since 1.0.0
   */
  public Optional<MiStackItem> remove(final Predicate<MiStackItem> predicate, long depth) {
    MiStackItem result = null;
    var iterator = this.iterator(predicate);
    while (depth >= 0 && iterator.hasNext()) {
      result = iterator.next();
      if (depth == 0) {
        depth = -1L;
        iterator.remove();
      } else {
        depth--;
      }
    }
    return Optional.ofNullable(result);
  }

  /**
   * Remove all elements from the stack.
   *
   * @since 1.0.0
   */
  public void clear() {
    this.items.clear();
    ((ArrayList<?>) this.items).trimToSize();
  }

  /**
   * Remove all elements from the stack which meet predicate condition.
   *
   * @param predicate condition for elements, must not be null.
   * @since 1.0.0
   */
  public void clear(final Predicate<MiStackItem> predicate) {
    final Iterator<MiStackItem> iterator = this.iterator(predicate);
    while (iterator.hasNext()) {
      iterator.next();
      iterator.remove();
    }
    ((ArrayList<?>) this.items).trimToSize();
  }

  /**
   * Get iterator for all stack elements.
   *
   * @return created iterator, must not be null.
   * @since 1.0.0
   */
  @Override
  public Iterator<MiStackItem> iterator() {
    return this.iterator(ALL_TAGS);
  }

  /**
   * Get iterator for stack elements which meet predicate.
   *
   * @param predicate condition for elements, must not be null.
   * @return created iterator, must not be null.
   * @since 1.0.0
   */
  public Iterator<MiStackItem> iterator(final Predicate<MiStackItem> predicate) {
    return new Iterator<>() {
      private int index = this.findNext(items.size() - 1);
      private int indexRemove = -1;

      private int findNext(int from) {
        int result = -1;
        while (result < 0 && from >= 0) {
          if (predicate.test(items.get(from))) {
            result = from;
          }
          from--;
        }
        return result;
      }

      @Override
      public boolean hasNext() {
        return this.index >= 0;
      }

      @Override
      public MiStackItem next() {
        if (this.index < 0) {
          this.indexRemove = -1;
          throw new NoSuchElementException();
        } else {
          final MiStackItem result = items.get(this.index);
          this.indexRemove = this.index;
          this.index = findNext(this.index - 1);
          return result;
        }
      }

      @Override
      public void remove() {
        if (this.indexRemove < 0) {
          throw new IllegalStateException();
        } else {
          items.remove(this.indexRemove);
          this.indexRemove = -1;
        }
      }
    };
  }

  /**
   * Get stream of stack items meet predicate.
   *
   * @param predicate condition for elements, must not be null.
   * @return created stream of all stacked elements meet predicate in their stack order, must not be null.
   * @since 1.0.0
   */
  public Stream<MiStackItem> stream(final Predicate<MiStackItem> predicate) {
    return StreamSupport.stream(spliteratorUnknownSize(this.iterator(predicate), ORDERED), false);
  }

  /**
   * Make stream of all elements on the stack.
   *
   * @return stream with all stacked elements in their order on the stack, must not be null.
   * @since 1.0.0
   */
  public Stream<MiStackItem> stream() {
    return StreamSupport.stream(spliteratorUnknownSize(this.iterator(ALL_TAGS), ORDERED), false);
  }

  /**
   * Check that there is no any element on the stack.
   *
   * @return true if the stack is empty, false elsewhere
   * @since 1.0.0
   */
  public boolean isEmpty() {
    return this.items.isEmpty();
  }

  /**
   * Check that there is no any element on the stack for predicate.
   *
   * @return true if the stack is empty, false elsewhere
   * @since 1.0.0
   */
  public boolean isEmpty(final Predicate<MiStackItem> predicate) {
    return this.items.stream().noneMatch(predicate);
  }

  /**
   * Find size of stack for elements meet predicate.
   *
   * @param predicate condition for elements, must not be null
   * @return number of found elements on the stack
   * @since 1.0.0
   */
  public long size(final Predicate<MiStackItem> predicate) {
    return this.stream(predicate).count();
  }

  /**
   * Find number of all elements on the stack.
   *
   * @return number of all elements on the stack.
   * @since 1.0.0
   */
  public long size() {
    return this.items.size();
  }

}
