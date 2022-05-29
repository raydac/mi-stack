package com.igormaznitsa.mistack.impl;

import static java.util.Objects.requireNonNull;

import com.igormaznitsa.mistack.MiStack;
import com.igormaznitsa.mistack.MiStackItem;
import com.igormaznitsa.mistack.exception.MiStackOverflowException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public class MiStackArray<T> implements MiStack<T> {

  private static final int CAPACITY_STEP = 16;
  private final boolean dynamic;
  private final String name;
  private Object[] array;
  private int pointer;
  private boolean closed;

  private int elementCounter;

  public MiStackArray() {
    this(UUID.randomUUID().toString());
  }

  public MiStackArray(final String name) {
    this(name, CAPACITY_STEP, true);
  }

  public MiStackArray(final String name, final int capacity, final boolean dynamic) {
    if (capacity <= 0) {
      throw new IllegalArgumentException("Capacity can't be less or equals zero: " + capacity);
    }
    this.array = new Object[capacity];
    this.pointer = 0;
    this.elementCounter = 0;
    this.name = requireNonNull(name);
    this.dynamic = dynamic;
  }

  public boolean isDynamic() {
    return this.dynamic;
  }

  @Override
  public Predicate<MiStackItem<T>> forAll() {
    return e -> true;
  }

  @Override
  public String getName() {
    return this.name;
  }

  private void assertNotClosed() {
    if (this.closed) {
      throw new IllegalStateException("Stack already closed");
    }
  }

  @Override
  public MiStack<T> push(final MiStackItem<T> item) {
    this.assertNotClosed();
    this.tryPack();
    if (this.pointer < this.array.length) {
      if (this.isDynamic()) {
        this.array = Arrays.copyOf(this.array, this.array.length + CAPACITY_STEP);
      } else {
        throw new MiStackOverflowException(this);
      }
    }
    this.array[this.pointer++] = requireNonNull(item);
    this.elementCounter++;
    return this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Optional<MiStackItem<T>> pop(final Predicate<MiStackItem<T>> predicate) {
    this.assertNotClosed();
    final int lastElement = this.pointer - 1;
    int index = lastElement;
    MiStackItem<T> result = null;
    while (result == null && index >= 0) {
      final MiStackItem<T> item = (MiStackItem<T>) this.array[index];
      if (item != null && predicate.test(item)) {
        if (index == lastElement) {
          this.pointer--;
        }
        this.array[index] = null;
        this.elementCounter--;
        result = item;
      }
      index--;
    }
    if (this.dynamic) {
      this.tryDynamicTrim();
    }
    this.tryPack();
    return Optional.ofNullable(result);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Optional<MiStackItem<T>> peek(final Predicate<MiStackItem<T>> predicate, long depth) {
    this.assertNotClosed();
    int index = this.pointer;
    MiStackItem<T> result = null;
    while (result == null && index > 0) {
      final MiStackItem<T> item = (MiStackItem<T>) this.array[--index];
      if (item != null && predicate.test(item)) {
        if (depth <= 0L) {
          result = item;
        } else {
          depth--;
        }
      }
    }
    this.tryPack();
    return Optional.ofNullable(result);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Optional<MiStackItem<T>> remove(final Predicate<MiStackItem<T>> predicate, long depth) {
    this.assertNotClosed();
    int index = this.pointer;
    MiStackItem<T> result = null;
    while (result == null && index > 0) {
      final MiStackItem<T> item = (MiStackItem<T>) this.array[--index];
      if (item != null && predicate.test(item)) {
        if (depth <= 0L) {
          if (index == this.pointer - 1) {
            this.pointer--;
          }
          this.array[index] = null;
          this.elementCounter--;
          result = item;
        } else {
          depth--;
        }
      }
    }
    if (this.dynamic) {
      this.tryDynamicTrim();
    }
    this.tryPack();
    return Optional.ofNullable(result);
  }

  @Override
  public void clear() {
    this.assertNotClosed();
    this.pointer = 0;
    this.elementCounter = 0;
    if (this.dynamic) {
      this.tryDynamicTrim();
    }
    Arrays.fill(this.array, null);
  }

  private void tryDynamicTrim() {
    final int delta = this.array.length - this.pointer;
    if (delta > (CAPACITY_STEP << 1)) {
      this.array = Arrays.copyOf(this.array, this.array.length - CAPACITY_STEP);
    }
  }

  private void tryPack() {
    while (this.pointer > 0 && this.array[this.pointer - 1] == null) {
      this.pointer--;
    }
    if (this.pointer - this.elementCounter > (CAPACITY_STEP << 1)) {
      int indexNull = -1;
      int processedCounter = 0;
      for (int i = 0; i < this.array.length && processedCounter < this.elementCounter; i++) {
        if (this.array[i] == null) {
          if (indexNull < 0) {
            indexNull = i;
          }
        } else {
          processedCounter++;
          if (indexNull >= 0) {
            this.array[indexNull] = this.array[i];
            this.array[i] = null;
            indexNull++;
            indexNull = this.array[indexNull] == null ? indexNull : -1;
          }
        }
      }
      while (this.pointer > 0 && this.array[this.pointer - 1] == null) {
        this.pointer--;
      }
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void clear(final Predicate<MiStackItem<T>> predicate) {
    this.assertNotClosed();
    int index = this.pointer - 1;
    while (index >= 0) {
      final MiStackItem<T> item = (MiStackItem<T>) this.array[index];
      if (item != null && predicate.test(item)) {
        this.array[index] = null;
        this.elementCounter--;
      }
      index--;
    }
    if (this.isDynamic()) {
      this.tryDynamicTrim();
    }
    tryPack();
  }

  @Override
  @SuppressWarnings("unchecked")
  public Iterator<MiStackItem<T>> iterator(final Predicate<MiStackItem<T>> predicate,
                                           final Predicate<MiStackItem<T>> takeWhile) {
    this.assertNotClosed();

    return new Iterator<MiStackItem<T>>() {
      private boolean completed;
      private int indexNext = this.findNextIndex(pointer - 1);
      private int removeIndex = -1;

      int findNextIndex(final int since) {
        if (this.completed) {
          return -1;
        }
        int foundIndex = -1;
        int index = since;
        while (!completed && index >= 0) {
          final MiStackItem<T> value = (MiStackItem<T>) array[index];
          if (value != null && predicate.test(value)) {
            if (takeWhile.test(value)) {
              foundIndex = index;
              break;
            } else {
              this.completed = true;
            }
          }
          index--;
        }
        return foundIndex;
      }

      @Override
      public boolean hasNext() {
        assertNotClosed();
        return this.indexNext >= 0;
      }

      @Override
      @SuppressWarnings("unchecked")
      public MiStackItem<T> next() {
        assertNotClosed();
        if (this.completed || this.indexNext < 0) {
          throw new NoSuchElementException();
        } else {
          this.removeIndex = this.indexNext;
          this.indexNext = this.findNextIndex(this.indexNext - 1);
          return (MiStackItem<T>) array[this.removeIndex];
        }
      }

      @Override
      public void remove() {
        assertNotClosed();
        if (this.completed || this.removeIndex < 0) {
          throw new IllegalStateException();
        } else {
          array[this.removeIndex] = null;
          elementCounter--;
          this.removeIndex = -1;
        }
      }
    };
  }

  @Override
  public boolean isEmpty() {
    this.assertNotClosed();
    this.tryPack();
    return this.elementCounter == 0;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean isEmpty(final Predicate<MiStackItem<T>> predicate) {
    this.assertNotClosed();
    int index = this.pointer - 1;
    while (index >= 0) {
      final MiStackItem<T> item = (MiStackItem<T>) this.array[index--];
      if (item != null && predicate.test(item)) {
        return false;
      }
    }
    this.tryPack();
    return true;
  }

  @Override
  public long size() {
    this.assertNotClosed();
    this.tryPack();
    return this.elementCounter;
  }

  @Override
  public void close() {
    if (this.closed) {
      throw new IllegalStateException("Stack already closed");
    } else {
      this.elementCounter = 0;
      this.closed = true;
      this.pointer = 0;
      this.array = new Object[0];
    }
  }

  @Override
  public boolean isClosed() {
    return this.closed;
  }
}
