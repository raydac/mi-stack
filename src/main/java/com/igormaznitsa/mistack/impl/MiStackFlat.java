package com.igormaznitsa.mistack.impl;

import static java.util.Objects.requireNonNull;

import com.igormaznitsa.mistack.MiStack;
import com.igormaznitsa.mistack.MiStackItem;
import com.igormaznitsa.mistack.MiStackTag;
import com.igormaznitsa.mistack.TruncableIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Implements stack of stacks with solid iterating their elements.
 * <b>Non Thread Safe</b>
 *
 * @param <V> type of values placed on stack
 * @param <I> type of value wrapper placed on stack
 * @param <T> type of value stack tag
 * @since 1.0.2
 */
public class MiStackFlat<V, I extends MiStackItem<V, T>, T extends MiStackTag>
    implements MiStack<V, I, T> {

  private final List<MiStack<V, I, T>> stackList;
  private final String name;
  private final BiPredicate<MiStack<V, I, T>, MiStack<V, I, T>> predicateSwitchNext;
  private boolean closed;

  /**
   * Constructor.
   *
   * @param name                name of the flat stack, must not be null
   * @param predicateSwitchNext nullable predicate allows to control switch next stacks during iterable operations
   */
  public MiStackFlat(final String name,
                     final BiPredicate<MiStack<V, I, T>, MiStack<V, I, T>> predicateSwitchNext) {
    this.name = requireNonNull(name);
    this.stackList = new ArrayList<>();
    this.predicateSwitchNext = predicateSwitchNext == null ? (a, b) -> true : predicateSwitchNext;
  }

  /**
   * Check that the stack store contains the stack.
   *
   * @param stack stack to find
   * @return true if contains the stack internally
   */
  public boolean contains(final MiStack<V, I, T> stack) {
    this.assertNotClosed();
    return this.stackList.contains(stack);
  }

  /**
   * Get an iterator to iterate over all wrapped stack objects instead of the elements of those stacks.
   *
   * @return iterator of wrapped stacks as iterable items.
   */
  public FilterableIterator<MiStack<V, I, T>> iteratorStacks() {
    return this.iteratorStacks(
        iterator -> new FilterableIterator<>(iterator, x -> true, x -> true));
  }

  /**
   * Get an iterator to iterate over all wrapped stack objects instead of the elements of those stacks.
   *
   * @param filter    filter to get iterable elements
   * @param takeWhile filter to truncate iteration
   * @return iterator of wrapped stacks as iterable items.
   */
  public FilterableIterator<MiStack<V, I, T>> iteratorStacks(
      final Predicate<MiStack<V, I, T>> filter,
      final Predicate<MiStack<V, I, T>> takeWhile) {
    return this.iteratorStacks(iterator -> new FilterableIterator<>(iterator, filter, takeWhile));
  }

  /**
   * Get an iterator to iterate over all wrapped stack objects instead of the elements of those stacks.
   *
   * @param iteratorFunction function to create a filterable iterator
   * @return iterator of wrapped stacks as iterable items.
   */
  public FilterableIterator<MiStack<V, I, T>> iteratorStacks(
      final Function<Iterator<MiStack<V, I, T>>, FilterableIterator<MiStack<V, I, T>>> iteratorFunction) {
    this.assertNotClosed();
    return iteratorFunction.apply(this.stackList.iterator());
  }

  /**
   * Pop the first stack from the internal stack store.
   *
   * @return optional stack item, can be empty
   */
  public Optional<MiStack<V, I, T>> popStack() {
    this.assertNotClosed();
    return this.stackList.isEmpty() ? Optional.empty() : Optional.of(this.stackList.remove(0));
  }

  /**
   * Push stack to the top of internal stack store. If the stack already presented then it is moved to the top of stack
   *
   * @param stack stack to push, can be null.
   * @return this instance
   */
  public MiStackFlat<V, I, T> pushStack(final MiStack<V, I, T> stack) {
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

  /**
   * Remove stack from internal store.
   *
   * @param stack stack to be removed, must not be null
   * @return optional wrapper for removed stack, empty if not found
   */
  public Optional<MiStack<V, I, T>> removeStack(final MiStack<V, I, T> stack) {
    this.assertNotClosed();
    this.assertNotEmpty();

    if (this.stackList.remove(stack)) {
      return Optional.of(stack);
    } else {
      return Optional.empty();
    }
  }

  @Override
  public MiStack<V, I, T> push(final I item) {
    this.assertNotClosed();
    this.assertNotEmpty();
    this.stackList.get(0).push(item);
    return this;
  }

  @Override
  public TruncableIterator<I> iterator() {
    return this.iterator(x -> true, x -> true);
  }

  @Override
  public TruncableIterator<I> iterator(
      final Predicate<I> predicate,
      final Predicate<I> takeWhile) {

    final Iterator<MiStack<V, I, T>> iterator = this.stackList.iterator();

    return new TruncableIterator<>() {

      private MiStack<V, I, T> currentList = null;
      private TruncableIterator<I> currentListIterator = null;
      private boolean completed;
      private boolean truncated;

      private void doComplete() {
        this.completed = true;
        this.currentList = null;
        this.currentListIterator = null;
      }

      @Override
      public boolean isTruncated() {
        return this.truncated;
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
        if (this.completed || this.truncated) {
          return;
        }

        if (this.currentListIterator != null && this.currentListIterator.isTruncated()) {
          this.completed = true;
          this.truncated = true;
          return;
        }

        MiStack<V, I, T> sourceList = this.currentList;

        do {
          if (iterator.hasNext()) {
            final MiStack<V, I, T> nextIteratorList = iterator.next();
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
        } while (!this.completed &&
            (this.currentListIterator == null || !this.currentListIterator.hasNext()));
      }


      @Override
      public boolean hasNext() {
        if (this.completed || closed || this.truncated) {
          return false;
        } else {
          if (this.currentListIterator == null) {
            this.tryInitNextListIterator();
          } else if (this.currentListIterator.isTruncated()) {
            this.truncated = true;
            this.completed = true;
            return false;
          } else if (!this.currentListIterator.hasNext()) {
            this.tryInitNextListIterator();
          }
        }
        if (this.completed || this.truncated) {
          return false;
        } else {
          final boolean next = this.currentListIterator.hasNext();
          this.truncated = this.currentListIterator.isTruncated();
          return next && !this.truncated;
        }
      }

      @Override
      public I next() {
        assertNotClosed();
        if (this.completed || this.truncated) {
          throw new NoSuchElementException();
        } else {
          if (this.currentListIterator == null) {
            this.tryInitNextListIterator();
          }
          if (this.completed) {
            throw new NoSuchElementException();
          } else {
            final I result = this.currentListIterator.next();
            this.truncated = this.currentListIterator.isTruncated();
            return result;
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
