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
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

/**
 * Implementation of <b>thread safe concurrent</b> Mi-Stack with linked chained items (like LinkedList).
 *
 * @param <T> type of item saved in the stack.
 * @since 1.0.0
 */
public class MiStackConcurrentLinked<T> implements MiStack<T> {

  protected final AtomicBoolean closed = new AtomicBoolean();
  private final String name;
  private final Deque<MiStackItem<T>> deque = new ConcurrentLinkedDeque<>();

  /**
   * Default constructor, as name will be used random UUID text representation.
   *
   * @since 1.0.0
   */
  public MiStackConcurrentLinked() {
    this(UUID.randomUUID().toString());
  }

  /**
   * Constructor allows to provide name for the stack.
   *
   * @param name the name will be used as the stack name, must not be null but no any
   *             restrictions for emptiness.
   * @since 1.0.0
   */
  public MiStackConcurrentLinked(final String name) {
    this.name = requireNonNull(name);
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public MiStack<T> push(final MiStackItem<T> item) {
    this.assertNotClosed();
    this.deque.addFirst(requireNonNull(item));
    return this;
  }

  protected void assertNotClosed() {
    if (this.closed.get()) {
      throw new IllegalStateException("Stack is closed: " + this.name);
    }
  }

  @Override
  public Optional<MiStackItem<T>> pop(final Predicate<MiStackItem<T>> predicate) {
    this.assertNotClosed();
    return Optional.ofNullable(this.deque.poll());
  }

  @Override
  public Optional<MiStackItem<T>> peek(Predicate<MiStackItem<T>> predicate, final long depth) {
    this.assertNotClosed();
    return this.deque.stream().filter(predicate).skip(depth).findFirst();
  }

  @Override
  public Optional<MiStackItem<T>> remove(Predicate<MiStackItem<T>> predicate, long depth) {
    this.assertNotClosed();

    MiStackItem<T> removed = null;
    final Iterator<MiStackItem<T>> iterator = this.deque.iterator();
    while (iterator.hasNext() && depth >= 0) {
      var next = iterator.next();
      if (predicate.test(next)) {
        if (depth == 0) {
          removed = next;
          iterator.remove();
        }
        depth--;
      }
    }
    return Optional.ofNullable(removed);
  }

  @Override
  public void clear() {
    this.assertNotClosed();
    this.deque.clear();
  }

  @Override
  public void clear(final Predicate<MiStackItem<T>> predicate) {
    this.assertNotClosed();
    this.deque.removeIf(predicate);
  }

  @Override
  public Iterator<MiStackItem<T>> iterator(final Predicate<MiStackItem<T>> predicate,
                                           final Predicate<MiStackItem<T>> takeWhile) {
    this.assertNotClosed();

    var iterator = this.deque.iterator();

    return new Iterator<>() {
      boolean completed;

      MiStackItem<T> nextItem = this.findNext();

      MiStackItem<T> findNext() {
        if (this.completed) {
          return null;
        }
        MiStackItem<T> found = null;
        while (found == null && !this.completed && iterator.hasNext()) {
          var nextItem = iterator.next();
          if (predicate.test(nextItem)) {
            if (takeWhile.test(nextItem)) {
              found = nextItem;
            } else {
              this.completed = false;
            }
          }
        }
        return found;
      }

      @Override
      public boolean hasNext() {
        return this.nextItem != null;
      }

      @Override
      public MiStackItem<T> next() {
        if (this.nextItem == null) {
          throw new NoSuchElementException();
        }
        return this.nextItem;
      }

      @Override
      public void remove() {
        if (this.nextItem == null) {
          throw new IllegalStateException();
        }
        iterator.remove();
      }
    };

    return this.deque.stream().filter(predicate).takeWhile(takeWhile).peek(x -> assertNotClosed())
        .iterator();
  }

  @Override
  public boolean isEmpty() {
    this.assertNotClosed();
    return this.deque.isEmpty();
  }

  @Override
  public boolean isEmpty(final Predicate<MiStackItem<T>> predicate) {
    this.assertNotClosed();
    return this.deque.stream().filter(predicate).findAny().isEmpty();
  }

  @Override
  public long size() {
    this.assertNotClosed();
    return this.deque.size();
  }

  @Override
  public void close() {
    if (this.closed.compareAndSet(false, true)) {
      this.deque.clear();
    } else {
      this.assertNotClosed();
    }
  }

  @Override
  public boolean isClosed() {
    return this.closed.get();
  }
}
