package com.igormaznitsa.mistack.impl;

import static java.util.Objects.requireNonNull;

import com.igormaznitsa.mistack.MiStack;
import com.igormaznitsa.mistack.MiStackItem;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;


/**
 * Internal base class for list based mi-stacks.
 *
 * @param <T> type of values placed on stack
 * @since 1.0.0
 */
abstract class AbstractMiStackList<T> implements MiStack<T> {

  protected final Predicate<MiStackItem<T>> all = e -> true;
  protected final List<MiStackItem<T>> items;
  private final String name;
  private boolean closed = false;

  AbstractMiStackList(final String name, final Object initValue) {
    this.name = requireNonNull(name);
    this.items = this.createList(initValue);
  }

  protected void assertNotClosed() {
    if (this.closed) {
      throw new IllegalStateException("Stack '" + this.name + "' is closed");
    }
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public MiStack<T> push(final MiStackItem<T> item) {
    this.assertNotClosed();
    this.items.add(requireNonNull(item));
    return this;
  }

  @SafeVarargs
  @Override
  public final MiStack<T> push(final MiStackItem<T>... items) {
    this.assertNotClosed();
    for (final MiStackItem<T> s : items) {
      this.items.add(requireNonNull(s));
    }
    return this;
  }

  @Override
  public Predicate<MiStackItem<T>> forAll() {
    return this.all;
  }

  @Override
  public Optional<MiStackItem<T>> pop(final Predicate<MiStackItem<T>> predicate) {
    this.assertNotClosed();
    MiStackItem<T> result = null;
    for (int i = this.items.size() - 1; result == null && i >= 0; i--) {
      final MiStackItem<T> item = this.items.get(i);
      if (predicate.test(item)) {
        result = item;
        this.items.remove(i);
      }
    }
    return Optional.ofNullable(result);
  }

  @Override
  public Optional<MiStackItem<T>> peek(final Predicate<MiStackItem<T>> predicate,
                                       final long depth) {
    this.assertNotClosed();
    return this.stream(predicate).skip(depth).findFirst();
  }

  @Override
  public Optional<MiStackItem<T>> remove(final Predicate<MiStackItem<T>> predicate, long depth) {
    this.assertNotClosed();
    MiStackItem<T> result = null;
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

  @Override
  public void clear() {
    this.assertNotClosed();
    this.items.clear();
    this.afterClear();
  }

  @Override
  public void clear(final Predicate<MiStackItem<T>> predicate) {
    this.assertNotClosed();
    final Iterator<MiStackItem<T>> iterator = this.iterator(predicate);
    while (iterator.hasNext()) {
      iterator.next();
      iterator.remove();
    }
    this.afterClear();
  }

  @Override
  public Iterator<MiStackItem<T>> iterator(final Predicate<MiStackItem<T>> predicate,
                                           final Predicate<MiStackItem<T>> takeWhile) {
    this.assertNotClosed();
    return new Iterator<>() {
      private int index = this.findNext(items.size() - 1);
      private int indexRemove = -1;

      private int findNext(int from) {
        assertNotClosed();
        int result = -1;
        while (result < 0 && from >= 0) {
          var nextItem = items.get(from);
          if (predicate.test(nextItem)) {
            if (takeWhile.test(nextItem)) {
              result = from;
            } else {
              break;
            }
          }
          from--;
        }
        return result;
      }

      @Override
      public boolean hasNext() {
        assertNotClosed();
        return this.index >= 0;
      }

      @Override
      public MiStackItem<T> next() {
        assertNotClosed();
        if (this.index < 0) {
          this.indexRemove = -1;
          throw new NoSuchElementException();
        } else {
          final MiStackItem<T> result = items.get(this.index);
          this.indexRemove = this.index;
          this.index = findNext(this.index - 1);
          return result;
        }
      }

      @Override
      public void remove() {
        assertNotClosed();
        if (this.indexRemove < 0) {
          throw new IllegalStateException();
        } else {
          items.remove(this.indexRemove);
          this.indexRemove = -1;
        }
      }
    };
  }

  @Override
  public boolean isClosed() {
    return this.closed;
  }

  @Override
  public boolean isEmpty() {
    this.assertNotClosed();
    return this.items.isEmpty();
  }

  @Override
  public boolean isEmpty(final Predicate<MiStackItem<T>> predicate) {
    this.assertNotClosed();
    for (final MiStackItem<T> i : this.items) {
      if (predicate.test(i)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Find number of all elements on the stack.
   *
   * @return number of all elements on the stack.
   * @throws IllegalStateException if stack is closed
   * @since 1.0.0
   */
  @Override
  public long size() {
    this.assertNotClosed();
    return this.items.size();
  }

  /**
   * Dispose the stack and free its resources.
   *
   * @throws IllegalStateException if stack is closed
   * @since 1.0.0
   */
  @Override
  public void close() {
    if (this.closed) {
      this.assertNotClosed();
    } else {
      this.closed = true;
      this.items.clear();
      this.afterClear();
    }
  }

  protected void afterClear() {

  }

  protected abstract List<MiStackItem<T>> createList(final Object value);
}
