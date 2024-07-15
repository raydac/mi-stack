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

import com.igormaznitsa.mistack.TruncableIterator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Wrapper around iterator to provide filtering and stop predicates, also allows get signals about element remove.
 *
 * @param <T> type of elements returned by wrapped iterator
 * @since 1.0.0
 */
public class FilterableIterator<T> implements TruncableIterator<T> {
  private final Iterator<T> baseIterator;
  private final Predicate<T> filter;
  private final Predicate<T> takeWhile;

  private final BooleanSupplier supplierClose;
  private final Consumer<T> consumerRemove;
  private boolean completed;
  private boolean cut;
  private T item;
  private T itemForRemove;

  /**
   * Constructor requires only base iterator.
   *
   * @param baseIterator base iterator, must not be null
   * @throws NullPointerException if base iterator is null
   * @since 1.0.0
   */
  public FilterableIterator(final Iterator<T> baseIterator) {
    this(baseIterator, x -> true, x -> true, () -> false, x -> {
    });
  }

  /**
   * Full main constructor.
   *
   * @param baseIterator   base iterator, must not be null
   * @param filter         predicate to filter values, must not be null
   * @param takeWhile      predicate to take values from base iterator, must not be null
   * @param supplierClose  supplier which allows provide control signal that source is closed, if source is closed then true must be return, false otherwise, can't be null
   * @param consumerRemove consumer for removing elements, must not be null
   * @throws NullPointerException if any parameter is null
   * @since 1.0.0
   */
  public FilterableIterator(
      final Iterator<T> baseIterator,
      final Predicate<T> filter,
      final Predicate<T> takeWhile,
      final BooleanSupplier supplierClose,
      final Consumer<T> consumerRemove
  ) {
    this.baseIterator = requireNonNull(baseIterator);
    this.filter = requireNonNull(filter);
    this.takeWhile = requireNonNull(takeWhile);
    this.supplierClose = requireNonNull(supplierClose);
    this.consumerRemove = requireNonNull(consumerRemove);
  }

  /**
   * Constructor requires only base iterator and predicates.
   *
   * @param baseIterator base iterator, must not be null
   * @param filter       predicate to filter values, must not be null
   * @param takeWhile    predicate to take values from base iterator, must not be null
   * @throws NullPointerException if any parameter is null
   * @since 1.0.0
   */
  public FilterableIterator(
      final Iterator<T> baseIterator,
      final Predicate<T> filter,
      final Predicate<T> takeWhile
  ) {
    this(baseIterator, filter, takeWhile, () -> false, x -> {
    });
  }

  public Consumer<T> getConsumerRemove() {
    return this.consumerRemove;
  }

  public BooleanSupplier getSupplierClose() {
    return this.supplierClose;
  }

  public Predicate<T> getFilter() {
    return this.filter;
  }

  public Predicate<T> getTakeWhile() {
    return this.takeWhile;
  }

  public Iterator<T> getBaseIterator() {
    return this.baseIterator;
  }

  @Override
  public boolean hasNext() {
    if (this.completed) {
      return false;
    }

    if (this.supplierClose.getAsBoolean()) {
      this.completed = true;
      this.item = null;
      this.itemForRemove = null;
      return false;
    }

    if (this.item == null) {
      this.item = this.findNext();
      this.itemForRemove = null;
    }

    return this.item != null;
  }

  @Override
  public T next() {
    if (this.checkAndCompleteIfClose()) {
      throw new IllegalStateException();
    }

    if (this.completed) {
      throw new NoSuchElementException();
    }

    if (this.item == null) {
      this.item = this.findNext();
      if (this.item == null) {
        throw new NoSuchElementException();
      }
      this.itemForRemove = this.item;
    } else {
      this.itemForRemove = this.item;
    }

    var result = this.item;
    this.item = null;
    return result;
  }

  protected boolean checkAndCompleteIfClose() {
    boolean result = false;
    if (this.supplierClose.getAsBoolean()) {
      this.completed = true;
      this.item = null;
      this.itemForRemove = null;
      result = true;
    }
    return result;
  }

  @Override
  public void remove() {
    if (this.checkAndCompleteIfClose() || this.itemForRemove == null) {
      throw new IllegalStateException();
    }

    this.baseIterator.remove();
    this.consumerRemove.accept(this.itemForRemove);
    this.itemForRemove = null;
    this.item = null;
  }

  /**
   * Make positioning of base iterator to next appropriate item
   *
   * @return found item or null if iteration already completed
   * @since 1.0.0
   */
  protected T findNext() {
    if (this.completed) {
      return null;
    }

    T result = null;
    while (!this.completed && this.baseIterator.hasNext() && result == null) {
      var item = this.baseIterator.next();
      if (this.filter.test(item)) {
        if (this.takeWhile.test(item)) {
          result = item;
        } else {
          this.cut = true;
          this.completed = true;
        }
      }
    }
    return result;
  }

  @Override
  public boolean isTruncated() {
    return this.cut;
  }
}
