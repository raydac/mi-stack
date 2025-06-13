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
import com.igormaznitsa.mistack.TruncableIterator;
import com.igormaznitsa.mistack.exception.MiStackOverflowException;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Implementation of thread-unsafe Mi-Stack based on internal array of objects (like ArrayList).
 * Array can be either fixed size or dynamically growing.
 *
 * @param <T> type of item saved in the stack.
 * @since 1.0.0
 */
public class MiStackArray<T, V extends MiStackItem<T>> implements MiStack<T, V> {

  public static final int CAPACITY_STEP = 16;
  /**
   * Flag shows that array can change its size for content.
   */
  protected final boolean dynamic;
  private final String name;
  protected int pointer;
  /**
   * Flag shows that stack is closed.
   */
  protected boolean closed;
  /**
   * Counter shows how many elements on stack.
   */
  protected int elementCounter;
  /**
   * Current array containing stack items.
   */
  private Object[] stackItemArray;

  /**
   * Default constructor, dynamic one will be created with initial capacity in one capacity step,
   * as name will be used random UUID text representation.
   *
   * @since 1.0.0
   */
  public MiStackArray() {
    this(UUID.randomUUID().toString());
  }

  /**
   * Constructor allows to provide name for new stack, dynamic one will be created with
   * initial capacity in one capacity step, as name will be used random UUID text representation.
   *
   * @param name text identifier of the stack, must not be null
   * @since 1.0.0
   */
  public MiStackArray(final String name) {
    this(name, CAPACITY_STEP, true);
  }

  /**
   * Constructor for new stack. It allows to define all main options.
   *
   * @param name     text identifier of the stack, must not be null
   * @param capacity initial capacity for internal array, it must be greater tan zero
   * @param dynamic  flag shows that internal array should be growing if its size is not
   *                 enough or too big for saved elements.
   * @throws IllegalArgumentException for inappropriate capacity value
   * @see #CAPACITY_STEP
   * @since 1.0.0
   */
  public MiStackArray(final String name, final int capacity, final boolean dynamic) {
    if (capacity <= 0) {
      throw new IllegalArgumentException("Capacity can't be less or equals zero: " + capacity);
    }
    this.stackItemArray = new Object[capacity];
    this.pointer = 0;
    this.elementCounter = 0;
    this.name = requireNonNull(name);
    this.dynamic = dynamic;
  }

  /**
   * Push single element on the stack.
   *
   * @param item element to be pushed on the stack, must not be null.
   * @return the stack instance
   * @throws IllegalStateException    if stack is closed
   * @throws MiStackOverflowException if stack is not dynamic one and there is no space
   *                                  for new element.
   * @see #push(MiStackItem[])
   * @since 1.0.0
   */
  @Override
  public MiStack<T, V> push(final V item) {
    this.assertNotClosed();
    this.makeDefragmentation(false);

    var workArray = this.getItemArray();

    if (this.pointer == workArray.length) {
      if (this.isDynamic()) {
        workArray = Arrays.copyOf(workArray, workArray.length + CAPACITY_STEP);
        this.setItemArray(workArray);
      } else {
        throw new MiStackOverflowException(this,
            String.format("Stack is non-dynamic one, array size is %d but index is %d",
                workArray.length, this.pointer));
      }
    }
    workArray[this.pointer++] = requireNonNull(item);
    this.elementCounter++;
    return this;
  }

  /**
   * Try to make defragmentation of underlying array and remove null cells.
   *
   * @param trim if true then try trimming of the underlying array after defragmentation, do
   *             nothing if false.
   * @since 1.0.0
   */
  protected void makeDefragmentation(final boolean trim) {
    var workArray = this.getItemArray();

    while (this.pointer > 0 && workArray[this.pointer - 1] == null) {
      this.pointer--;
    }
    if (!this.dynamic || this.pointer - this.elementCounter > (CAPACITY_STEP << 1)) {
      int indexNull = -1;
      int processedCounter = 0;

      for (int i = 0; i < workArray.length && processedCounter < this.elementCounter; i++) {
        if (workArray[i] == null) {
          if (indexNull < 0) {
            indexNull = i;
          }
        } else {
          processedCounter++;
          if (indexNull >= 0) {
            workArray[indexNull] = workArray[i];
            workArray[i] = null;
            indexNull++;
            indexNull = workArray[indexNull] == null ? indexNull : -1;
          }
        }
      }
      while (this.pointer > 0 && workArray[this.pointer - 1] == null) {
        this.pointer--;
      }

      if (trim) {
        final int diff = workArray.length - this.pointer;
        if (diff > (CAPACITY_STEP << 1)) {
          workArray =
              Arrays.copyOf(workArray, ((this.pointer / CAPACITY_STEP) + 1) * CAPACITY_STEP);
          this.setItemArray(workArray);
        }
      }
    }
  }

  /**
   * Get current object array.
   *
   * @return the array, must not be null.
   * @since 1.0.0
   */
  protected Object[] getItemArray() {
    return this.stackItemArray;
  }

  /**
   * Set working object array.
   *
   * @param array the array to be used to keep stack items, must not be null.
   * @since 1.0.0
   */
  protected void setItemArray(final Object[] array) {
    this.stackItemArray = requireNonNull(array);
  }

  public boolean isDynamic() {
    return this.dynamic;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Optional<V> pop(final Predicate<V> predicate) {
    this.assertNotClosed();
    var workArray = this.getItemArray();
    final int lastElement = this.pointer - 1;
    int index = lastElement;
    V result = null;
    while (result == null && index >= 0) {
      final V item = (V) workArray[index];
      if (item != null && predicate.test(item)) {
        if (index == lastElement) {
          this.pointer--;
        }
        workArray[index] = null;
        this.elementCounter--;
        result = item;
      }
      index--;
    }
    this.makeDefragmentation(this.dynamic);
    return Optional.ofNullable(result);
  }

  @Override
  @SuppressWarnings("unchecked")
  public TruncableIterator<V> iterator(final Predicate<V> predicate,
                                       final Predicate<V> takeWhile) {
    this.assertNotClosed();

    var workArray = this.getItemArray();

    return new TruncableIterator<V>() {
      private boolean completed;
      private boolean truncated;
      private int indexNext = this.findNextIndex(pointer - 1);
      private int removeIndex = -1;

      @Override
      public boolean hasNext() {
        if (isClosed()) {
          this.indexNext = -1;
          this.completed = true;
        }
        return this.indexNext >= 0;
      }

      @Override
      @SuppressWarnings("unchecked")
      public V next() {
        assertNotClosed();
        if (this.completed || this.indexNext < 0) {
          throw new NoSuchElementException();
        } else {
          this.removeIndex = this.indexNext;
          this.indexNext = this.findNextIndex(this.indexNext - 1);
          return (V) workArray[this.removeIndex];
        }
      }

      int findNextIndex(final int since) {
        if (this.completed) {
          return -1;
        }
        int foundIndex = -1;
        int index = since;
        while (!completed && index >= 0) {
          final V value = (V) workArray[index];
          if (value != null && predicate.test(value)) {
            if (takeWhile.test(value)) {
              foundIndex = index;
              break;
            } else {
              this.truncated = true;
              this.completed = true;
            }
          }
          index--;
        }
        return foundIndex;
      }

      @Override
      public boolean isTruncated() {
        return this.truncated;
      }

      @Override
      public void remove() {
        assertNotClosed();
        if (this.completed || this.removeIndex < 0) {
          throw new IllegalStateException();
        } else {
          workArray[this.removeIndex] = null;
          elementCounter--;
          this.removeIndex = -1;
        }
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
  @SuppressWarnings("unchecked")
  public Optional<V> peek(final Predicate<V> predicate, long depth) {
    this.assertNotClosed();
    var workArray = this.getItemArray();
    int index = this.pointer;
    V result = null;
    while (result == null && index > 0) {
      final V item = (V) workArray[--index];
      if (item != null && predicate.test(item)) {
        if (depth <= 0L) {
          result = item;
        } else {
          depth--;
        }
      }
    }
    this.makeDefragmentation(this.dynamic);
    return Optional.ofNullable(result);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Optional<V> remove(final Predicate<V> predicate, long depth) {
    this.assertNotClosed();
    var workArray = this.getItemArray();
    int index = this.pointer;
    V result = null;
    while (result == null && index > 0) {
      final V item = (V) workArray[--index];
      if (item != null && predicate.test(item)) {
        if (depth <= 0L) {
          if (index == this.pointer - 1) {
            this.pointer--;
          }
          workArray[index] = null;
          this.elementCounter--;
          result = item;
        } else {
          depth--;
        }
      }
    }
    this.makeDefragmentation(this.dynamic);
    return Optional.ofNullable(result);
  }

  @Override
  public void clear() {
    this.assertNotClosed();
    var workArray = this.getItemArray();
    this.pointer = 0;
    this.elementCounter = 0;
    if (workArray.length > (CAPACITY_STEP << 3)) {
      this.setItemArray(new Object[CAPACITY_STEP]);
    } else {
      Arrays.fill(workArray, null);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void clear(final Predicate<V> predicate) {
    this.assertNotClosed();
    var workArray = this.getItemArray();
    int index = this.pointer - 1;
    while (index >= 0) {
      final V item = (V) workArray[index];
      if (item != null && predicate.test(item)) {
        workArray[index] = null;
        this.elementCounter--;
      }
      index--;
    }
    makeDefragmentation(this.dynamic);
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean isEmpty(final Predicate<V> predicate) {
    this.assertNotClosed();
    var workArray = this.getItemArray();
    int index = this.pointer - 1;
    while (index >= 0) {
      final V item = (V) workArray[index--];
      if (item != null && predicate.test(item)) {
        return false;
      }
    }
    this.makeDefragmentation(this.dynamic);
    return true;
  }

  @Override
  public boolean isEmpty() {
    this.assertNotClosed();
    return this.elementCounter == 0;
  }

  @Override
  public long size() {
    this.assertNotClosed();
    return this.elementCounter;
  }

  @Override
  public void close() {
    this.assertNotClosed();
    this.elementCounter = 0;
    this.closed = true;
    this.pointer = 0;
    this.setItemArray(new Object[0]);
  }

  /**
   * Force check, optimization and internal trimming of array.
   * If stack uses dynamic array then internal array can be changed, otherwise only pack.
   *
   * @since 1.0.0
   */
  public void trim() {
    this.assertNotClosed();
    this.makeDefragmentation(this.dynamic);
  }
}
