package com.igormaznitsa.mistack.impl;

import com.igormaznitsa.mistack.MiStack;
import com.igormaznitsa.mistack.MiStackItem;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public class ConcurrentMiStack<T> implements MiStack<T> {

  private final AtomicBoolean closed = new AtomicBoolean();


  public ConcurrentMiStack() {

  }

  @Override
  public Predicate<MiStackItem<T>> forAll() {
    return null;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public MiStack<T> push(final MiStackItem<T> item) {
    return null;
  }

  @Override
  public MiStack<T> push(final MiStackItem<T>... items) {
    return null;
  }

  @Override
  public Optional<MiStackItem<T>> pop(final Predicate<MiStackItem<T>> predicate) {
    return Optional.empty();
  }

  @Override
  public Optional<MiStackItem<T>> peek(final Predicate<MiStackItem<T>> predicate,
                                       final long depth) {
    return Optional.empty();
  }

  @Override
  public Optional<MiStackItem<T>> remove(final Predicate<MiStackItem<T>> predicate,
                                         final long depth) {
    return Optional.empty();
  }

  @Override
  public void clear() {

  }

  @Override
  public void clear(final Predicate<MiStackItem<T>> predicate) {

  }

  @Override
  public Iterator<MiStackItem<T>> iterator(final Predicate<MiStackItem<T>> predicate,
                                           final Predicate<MiStackItem<T>> takeWhile) {
    return null;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public boolean isEmpty(final Predicate<MiStackItem<T>> predicate) {
    return false;
  }

  @Override
  public long size() {
    return 0;
  }

  @Override
  public void close() {
    if (this.closed.compareAndSet(false, true)) {
      this.assertNotClosed();
    }
  }

  private void assertNotClosed() {
    if (this.closed.get()) {
      throw new IllegalStateException("Stack closed");
    }
  }

  @Override
  public boolean isClosed() {
    return this.closed.get();
  }
}
