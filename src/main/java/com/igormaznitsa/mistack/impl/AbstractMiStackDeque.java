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
import java.util.Deque;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

/**
 * Class allows to build MiStacks based on java.util.Deque collections.
 *
 * @param <T> type of values placed on stack
 * @since 1.0.0
 */
public abstract class AbstractMiStackDeque<T> implements MiStack<T> {

  protected final Deque<MiStackItem<T>> deque;
  private final String name;
  private final AtomicBoolean closed = new AtomicBoolean();

  /**
   * Constructor requires name and base deque.
   *
   * @param name  name for the stack instance, must not be null
   * @param deque base deque, must not be null
   * @throws NullPointerException if any parameter is null
   * @since 1.0.0
   */
  public AbstractMiStackDeque(final String name, final Deque<MiStackItem<T>> deque) {
    this.name = requireNonNull(name);
    this.deque = requireNonNull(deque);
  }

  /**
   * Get the base deque for the stack.
   *
   * @return the base deque, can't be null
   * @since 1.0.0
   */
  protected Deque<MiStackItem<T>> getDeque() {
    return this.deque;
  }

  @Override
  public MiStack<T> push(final MiStackItem<T> item) {
    this.assertNotClosed();
    this.deque.addFirst(requireNonNull(item));
    return this;
  }

  @Override
  public Optional<MiStackItem<T>> pop(final Predicate<MiStackItem<T>> predicate) {
    this.assertNotClosed();

    MiStackItem<T> result = null;
    var iterator = this.deque.iterator();
    while (iterator.hasNext() && result == null) {
      var item = iterator.next();
      if (predicate.test(item)) {
        iterator.remove();
        result = item;
      }
    }

    return Optional.ofNullable(result);
  }

  @Override
  public TruncableIterator<MiStackItem<T>> iterator(final Predicate<MiStackItem<T>> filter,
                                                    final Predicate<MiStackItem<T>> takeWhile) {
    return new FilterableIterator<>(this.deque.iterator(), filter, takeWhile, this.closed::get,
        x -> {
        });
  }

  @Override
  public boolean isClosed() {
    return this.closed.get();
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public void clear() {
    this.assertNotClosed();
    this.deque.clear();
  }

  @Override
  public boolean isEmpty() {
    this.assertNotClosed();
    return this.deque.isEmpty();
  }

  @Override
  public long size() {
    this.assertNotClosed();
    int result = this.deque.size();
    if (result < Integer.MAX_VALUE) {
      return result;
    } else {
      return this.deque.stream().count();
    }
  }

  @Override
  public void close() {
    if (this.closed.compareAndSet(false, true)) {
      this.deque.clear();
    } else {
      this.assertNotClosed();
    }
  }
}
