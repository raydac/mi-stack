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

import com.igormaznitsa.mistack.MiStack;
import com.igormaznitsa.mistack.MiStackItem;
import com.igormaznitsa.mistack.MiStackTag;
import com.igormaznitsa.mistack.TruncableIterator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;


/**
 * Class allows to build MiStacks based on java.util.List collections.
 *
 * @param <V> type of values placed on stack
 * @param <I> type of value wrapper placed on stack
 * @param <T> type of value stack tag
 * @see java.util.List
 * @since 2.0.0
 */
public abstract class AbstractMiStackList<V, I extends MiStackItem<V, T>, T extends MiStackTag>
    implements MiStack<V, I, T> {

  protected final List<I> list;
  private final String name;
  private boolean closed = false;

  /**
   * Constructor for name and list collection which will be used as the store for items.
   *
   * @param name name of the stack, can't be null
   * @param list internal storage for items, can't be null
   * @since 1.0.0
   */
  public AbstractMiStackList(final String name, final List<I> list) {
    this.name = requireNonNull(name);
    this.list = requireNonNull(list);
  }

  /**
   * Get the list used as store for items.
   *
   * @return the base list store, can't be null
   * @since 1.0.0
   */
  protected List<I> getList() {
    return this.list;
  }

  @Override
  public MiStack<V, I, T> push(final I item) {
    this.assertNotClosed();
    this.list.add(requireNonNull(item));
    return this;
  }

  @Override
  public Optional<I> pop(final Predicate<I> predicate) {
    this.assertNotClosed();
    var iterator = this.makeItemIterator(this.list);
    I result = null;
    while (iterator.hasNext() && result == null) {
      result = iterator.next();
      if (predicate.test(result)) {
        iterator.remove();
      } else {
        result = null;
      }
    }
    return Optional.ofNullable(result);
  }

  @Override
  public TruncableIterator<I> iterator(final Predicate<I> predicate,
                                       final Predicate<I> takeWhile) {

    var listIterator = this.makeItemIterator(this.list);
    return new TruncableIterator<>() {

      private boolean completed = false;
      private boolean truncated = false;
      private I foundItem = null;

      @Override
      public boolean hasNext() {
        if (isClosed()) {
          this.foundItem = null;
          this.completed = true;
          return false;
        }
        if (this.foundItem == null) {
          this.foundItem = findNext();
        }
        return this.foundItem != null;
      }

      private I findNext() {
        assertNotClosed();
        if (this.completed) {
          return null;
        }
        I result = null;
        while (result == null && listIterator.hasNext()) {
          result = listIterator.next();
          if (predicate.test(result)) {
            if (!takeWhile.test(result)) {
              result = null;
              this.truncated = true;
              this.completed = true;
              break;
            }
          } else {
            result = null;
          }
        }
        return result;
      }

      @Override
      public boolean isTruncated() {
        return this.truncated;
      }

      @Override
      public I next() {
        assertNotClosed();
        if (this.foundItem == null) {
          this.foundItem = this.findNext();
          if (this.foundItem == null) {
            throw new NoSuchElementException();
          }
        }
        var result = this.foundItem;
        this.foundItem = null;
        return result;
      }

      @Override
      public void remove() {
        assertNotClosed();
        listIterator.remove();
      }
    };
  }

  @Override
  public boolean isClosed() {
    return this.closed;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public Optional<I> peek(final Predicate<I> predicate,
                          final long depth) {
    this.assertNotClosed();
    return this.stream(predicate).skip(depth).findFirst();
  }

  @Override
  public Optional<I> remove(final Predicate<I> predicate, long depth) {
    this.assertNotClosed();
    I result = null;
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
    this.list.clear();
    this.afterClear();
  }

  @Override
  public void clear(final Predicate<I> predicate) {
    this.assertNotClosed();
    final Iterator<I> iterator = this.iterator(predicate);
    while (iterator.hasNext()) {
      iterator.next();
      iterator.remove();
    }
    this.afterClear();
  }

  @Override
  public boolean isEmpty() {
    this.assertNotClosed();
    return this.list.isEmpty();
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
    return this.list.size();
  }

  /**
   * Dispose the stack and free its resources.
   *
   * @throws IllegalStateException if stack is closed
   * @since 1.0.0
   */
  @Override
  public void close() {
    this.assertNotClosed();
    this.closed = true;
    this.list.clear();
    this.afterClear();
  }

  /**
   * Method is called after clear operations. Allow for instance to trim collection.
   *
   * @since 1.0.0
   */
  protected void afterClear() {

  }

  /**
   * Make iterator for stack items in appropriate order for stack.
   *
   * @param list list to be sourced for the iterator, must not be null
   * @return created iterator for the list, must not be null
   * @since 1.0.0
   */
  protected abstract Iterator<I> makeItemIterator(final List<I> list);

}
