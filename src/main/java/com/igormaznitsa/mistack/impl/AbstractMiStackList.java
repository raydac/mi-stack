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
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;


/**
 * Class allows to build MiStacks based on java.util.List collections.
 *
 * @param <T> type of values placed on stack
 * @see java.util.List
 * @since 1.0.0
 */
public abstract class AbstractMiStackList<T> implements MiStack<T> {

  protected final List<MiStackItem<T>> list;
  private final String name;
  private boolean closed = false;

  /**
   * Constructor for name and list collection which will be used as the store for items.
   *
   * @param name name of the stack, can't be null
   * @param list internal storage for items, can't be null
   * @since 1.0.0
   */
  public AbstractMiStackList(final String name, final List<MiStackItem<T>> list) {
    this.name = requireNonNull(name);
    this.list = requireNonNull(list);
  }

  /**
   * Get the list used as store for items.
   *
   * @return the base list store, can't be null
   * @since 1.0.0
   */
  protected List<MiStackItem<T>> getList() {
    return this.list;
  }

  @Override
  public MiStack<T> push(final MiStackItem<T> item) {
    this.assertNotClosed();
    this.list.add(requireNonNull(item));
    return this;
  }

  @Override
  public Optional<MiStackItem<T>> pop(final Predicate<MiStackItem<T>> predicate) {
    this.assertNotClosed();
    var iterator = this.makeItemIterator(this.list);
    MiStackItem<T> result = null;
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
  public Iterator<MiStackItem<T>> iterator(final Predicate<MiStackItem<T>> predicate,
                                           final Predicate<MiStackItem<T>> takeWhile) {

    var listIterator = this.makeItemIterator(this.list);
    return new Iterator<>() {

      private boolean completed = false;
      private MiStackItem<T> foundItem = null;

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

      private MiStackItem<T> findNext() {
        assertNotClosed();
        if (this.completed) {
          return null;
        }
        MiStackItem<T> result = null;
        while (result == null && listIterator.hasNext()) {
          result = listIterator.next();
          if (predicate.test(result)) {
            if (!takeWhile.test(result)) {
              result = null;
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
      public MiStackItem<T> next() {
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
    this.list.clear();
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
  protected abstract Iterator<MiStackItem<T>> makeItemIterator(final List<MiStackItem<T>> list);

}
