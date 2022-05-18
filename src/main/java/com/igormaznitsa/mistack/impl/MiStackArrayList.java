/**
 * Copyright (C) 2022 Igor A. Maznitsa
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igormaznitsa.mistack.impl;

import static com.igormaznitsa.mistack.Predicates.ALL_ITEMS;
import static java.util.Objects.requireNonNull;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;

import com.igormaznitsa.mistack.MiStack;
import com.igormaznitsa.mistack.MiStackItem;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * ArrayList based implementation of Mi-Stack.
 * <b>The stack is not thread safe, so you must provide extra synchronization if going to use
 * it in multi-thread environment.</b>
 *
 * @author Igor Maznitsa
 * @since 1.0.0
 */
public class MiStackArrayList implements MiStack {

  /**
   * Default capacity of array list if not provided direct value.
   *
   * @since 1.0.0
   */
  public static final int DEFAULT_INITIAL_CAPACITY = 16;
  private final String name;
  private final ArrayList<MiStackItem> items;
  private boolean closed = false;

  /**
   * Default constructor. Name of stack will be generated automatically.
   * Name is generated through UUID.
   *
   * @see UUID
   * @since 1.0.0
   */
  public MiStackArrayList() {
    this(UUID.randomUUID().toString());
  }

  /**
   * Constructor allows to provide name of stack.
   *
   * @param name name of stack, must not be null
   * @since 1.0.0
   */
  public MiStackArrayList(final String name) {
    this(name, DEFAULT_INITIAL_CAPACITY);
  }

  /**
   * Constructor allows define name and capacity.
   *
   * @param name     name of the stack, can't be null
   * @param capacity initial capacity of internal array list
   * @since 1.0.0
   */
  public MiStackArrayList(final String name, final int capacity) {
    this.name = requireNonNull(name);
    this.items = new ArrayList<>(capacity);
  }

  private void assertNotClosed() {
    if (this.closed) {
      throw new IllegalStateException("Stack '" + this.name + "' is closed");
    }
  }


  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public MiStackArrayList push(final MiStackItem item) {
    this.assertNotClosed();
    this.items.add(requireNonNull(item));
    return this;
  }

  @Override
  public MiStackArrayList push(final MiStackItem... items) {
    this.assertNotClosed();
    for (final MiStackItem s : items) {
      this.items.add(requireNonNull(s));
    }
    return this;
  }

  @Override
  public Optional<MiStackItem> pop(final Predicate<MiStackItem> predicate) {
    this.assertNotClosed();
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

  @Override
  public Optional<MiStackItem> peek(final Predicate<MiStackItem> predicate, final long depth) {
    this.assertNotClosed();
    return this.stream(predicate).skip(depth).findFirst();
  }

  @Override
  public Optional<MiStackItem> remove(final Predicate<MiStackItem> predicate, long depth) {
    this.assertNotClosed();
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

  @Override
  public void clear() {
    this.assertNotClosed();
    this.items.clear();
    this.items.trimToSize();
  }

  @Override
  public void clear(final Predicate<MiStackItem> predicate) {
    this.assertNotClosed();
    final Iterator<MiStackItem> iterator = this.iterator(predicate);
    while (iterator.hasNext()) {
      iterator.next();
      iterator.remove();
    }
    this.items.trimToSize();
  }

  @Override
  public Iterator<MiStackItem> iterator() {
    return this.iterator(ALL_ITEMS, ALL_ITEMS);
  }

  @Override
  public Iterator<MiStackItem> iterator(final Predicate<MiStackItem> predicate,
                                        final Predicate<MiStackItem> takeWhile) {
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
      public MiStackItem next() {
        assertNotClosed();
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
  public Iterator<MiStackItem> iterator(Predicate<MiStackItem> predicate) {
    return this.iterator(predicate, ALL_ITEMS);
  }

  @Override
  public Stream<MiStackItem> stream(Predicate<MiStackItem> predicate,
                                    Predicate<MiStackItem> takeWhile) {
    this.assertNotClosed();
    return StreamSupport.stream(
        spliteratorUnknownSize(this.iterator(predicate, takeWhile), ORDERED), false);
  }

  @Override
  public Stream<MiStackItem> stream(final Predicate<MiStackItem> predicate) {
    return this.stream(predicate, ALL_ITEMS);
  }

  @Override
  public Stream<MiStackItem> stream() {
    return this.stream(ALL_ITEMS, ALL_ITEMS);
  }

  @Override
  public boolean isEmpty() {
    this.assertNotClosed();
    return this.items.isEmpty();
  }

  @Override
  public boolean isEmpty(final Predicate<MiStackItem> predicate) {
    this.assertNotClosed();
    return this.items.stream().noneMatch(predicate);
  }

  @Override
  public long size(final Predicate<MiStackItem> predicate) {
    this.assertNotClosed();
    return this.stream(predicate).count();
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
      this.items.trimToSize();
    }
  }

  /**
   * Method returns direct internal array list container for the stack.
   *
   * @return the internal array list container, can't be null.
   * @since 1.0.0
   */
  protected ArrayList<MiStackItem> getInternalArrayList() {
    return this.items;
  }
}
