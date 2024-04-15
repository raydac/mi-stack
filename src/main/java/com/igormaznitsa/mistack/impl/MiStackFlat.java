package com.igormaznitsa.mistack.impl;

import com.igormaznitsa.mistack.MiStack;
import com.igormaznitsa.mistack.MiStackItem;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * Implements stack of stacks with solid iterating their elements.
 * <b<Non Thread Safe</b>
 * @param <T> type of wrapped stacks.
 *
 * @since 1.0.2
 */
public class MiStackFlat<T> implements MiStack<T> {

  private final List<MiStack<T>> stackList;
  private boolean closed;
  private final String name;
  private final BiPredicate<MiStack<T>, MiStack<T>> predicateSwitchNext;

  public MiStackFlat(final String name,
                     final BiPredicate<MiStack<T>, MiStack<T>> predicateSwitchNext) {
    this.name = Objects.requireNonNull(name);
    this.stackList = new ArrayList<>();
    this.predicateSwitchNext = predicateSwitchNext == null ? (a, b) -> true : predicateSwitchNext;
  }

  public boolean contains(final MiStack<T> stack) {
    this.assertNotClosed();
    return this.stackList.contains(stack);
  }

  public Optional<MiStack<T>> popStack() {
    this.assertNotClosed();
    return this.stackList.isEmpty() ? Optional.empty() : Optional.of(this.stackList.remove(0));
  }

  public MiStackFlat<T> pushStack(final MiStack<T> stack) {
    this.assertNotClosed();
    if (stack != null) {
      this.stackList.remove(stack);
      this.stackList.add(0, stack);
    }
    return this;
  }

  protected void assertNotEmpty() {
    if (this.stackList.isEmpty()) {
      throw new IllegalStateException("Stack " + this.getName() + " is empty");
    }
  }

  public MiStack<T> popupStack(final MiStack<T> stack) {
    this.assertNotClosed();
    this.assertNotEmpty();

    if (this.stackList.remove(stack)) {
      this.stackList.add(0, stack);
      return this;
    } else {
      throw new IllegalArgumentException("Stack not found");
    }
  }

  @Override
  public MiStack<T> push(final MiStackItem<T> item) {
    this.assertNotClosed();
    this.assertNotEmpty();
    this.stackList.get(0).push(item);
    return this;
  }

  @Override
  public Iterator<MiStackItem<T>> iterator(
      final Predicate<MiStackItem<T>> predicate,
      final Predicate<MiStackItem<T>> takeWhile) {

    final Iterator<MiStack<T>> iterator = this.stackList.iterator();

    return new Iterator<>() {

      private MiStack<T> currentList = null;
      private Iterator<MiStackItem<T>> currentListIterator = null;
      private boolean completed;

      private void doComplete() {
        this.completed = true;
        this.currentList = null;
        this.currentListIterator = null;
      }

      @Override
      public void remove() {
        assertNotClosed();
        if (this.completed || this.currentListIterator == null) {
          throw new IllegalStateException();
        }
        this.currentListIterator.remove();
      }

      private void tryInitNextListIterator() {
        if (this.completed) {
          return;
        }

        MiStack<T> sourceList = this.currentList;

        do {
          if (iterator.hasNext()) {
            final MiStack<T> nextIteratorList = iterator.next();
            if (predicateSwitchNext.test(sourceList, nextIteratorList)) {
              sourceList = this.currentList;
              this.currentList = nextIteratorList;
              this.currentListIterator = this.currentList.iterator(predicate, takeWhile);
            } else {
              this.currentListIterator = null;
            }
          } else {
            this.doComplete();
          }
        } while (!this.completed && this.currentListIterator != null &&
            !this.currentListIterator.hasNext());
      }


      @Override
      public boolean hasNext() {
        if (this.completed || closed) {
          return false;
        } else {
          if (this.currentListIterator == null) {
            this.tryInitNextListIterator();
          }
        }
        return !this.completed && this.currentListIterator.hasNext();
      }

      @Override
      public MiStackItem<T> next() {
        assertNotClosed();
        if (this.completed) {
          throw new NoSuchElementException();
        } else {
          if (this.currentListIterator == null) {
            this.tryInitNextListIterator();
          }
          if (this.completed) {
            throw new NoSuchElementException();
          } else {
            return this.currentListIterator.next();
          }
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
  public void clear() {
    this.stackList.forEach(MiStack::clear);
  }

  @Override
  public boolean isEmpty() {
    return this.stackList.stream().allMatch(MiStack::isEmpty);
  }

  @Override
  public long size() {
    return this.stackList.stream().mapToLong(MiStack::size).sum();
  }

  @Override
  public void close() {
    this.assertNotClosed();
    if (!this.closed) {
      this.closed = true;
      this.stackList.forEach(MiStack::close);
      this.stackList.clear();
    }
  }
}
