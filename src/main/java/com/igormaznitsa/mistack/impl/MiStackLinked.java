package com.igormaznitsa.mistack.impl;

import static java.util.Objects.requireNonNull;

import com.igormaznitsa.mistack.MiStack;
import com.igormaznitsa.mistack.MiStackItem;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Implementation of thread-unsafe Mi-Stack with linked chained items (like LinkedList).
 *
 * @param <T> type of item saved in the stack.
 * @since 1.0.0
 */
public class MiStackLinked<T> implements MiStack<T> {

  private final String name;
  private boolean closed;
  private long size;
  private StackChainNode<T> head;

  /**
   * Default constructor, as name will be used random UUID text representation.
   *
   * @since 1.0.0
   */
  public MiStackLinked() {
    this(UUID.randomUUID().toString());
  }

  /**
   * Constructor allows to provide name for the stack.
   *
   * @param name the name will be used as the stack name, must not be null but no any restrictions for emptiness.
   * @since 1.0.0
   */
  public MiStackLinked(final String name) {
    this.name = requireNonNull(name);
    this.head = null;
  }

  @Override
  public Predicate<MiStackItem<T>> forAll() {
    return e -> true;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public MiStack<T> push(final MiStackItem<T> item) {
    this.assertNotClosed();
    var newNode = new StackChainNode<>(item);
    newNode.next = this.head;
    if (this.head != null) {
      this.head.prev = newNode;
    }
    this.head = newNode;
    this.size++;
    return this;
  }

  @Override
  public Optional<MiStackItem<T>> pop(final Predicate<MiStackItem<T>> predicate) {
    return this.remove(predicate, 0);
  }

  @Override
  public Optional<MiStackItem<T>> peek(final Predicate<MiStackItem<T>> predicate, long depth) {
    this.assertNotClosed();
    MiStackItem<T> result = null;
    StackChainNode<T> node = this.head;
    while (node != null && result == null) {
      var nodeValue = node.item;
      if (predicate.test(nodeValue)) {
        if (depth <= 0L) {
          result = nodeValue;
        } else {
          depth--;
        }
      }
      node = node.next;
    }
    return Optional.ofNullable(result);
  }

  @Override
  public Optional<MiStackItem<T>> remove(final Predicate<MiStackItem<T>> predicate, long depth) {
    this.assertNotClosed();
    MiStackItem<T> result = null;
    StackChainNode<T> node = this.head;
    while (node != null && result == null) {
      var nodeValue = node.item;
      if (predicate.test(nodeValue)) {
        if (depth <= 0L) {
          result = nodeValue;
          if (node == this.head) {
            this.head = node.remove();
            node = this.head;
          } else {
            node = node.remove();
          }
          this.size--;
        } else {
          depth--;
          node = node.next;
        }
      } else {
        node = node.next;
      }
    }
    return Optional.ofNullable(result);
  }

  @Override
  public void clear() {
    this.assertNotClosed();
    this.head = null;
    this.size = 0L;
  }

  @Override
  public void clear(final Predicate<MiStackItem<T>> predicate) {
    this.assertNotClosed();
    StackChainNode<T> node = this.head;
    while (node != null) {
      var nodeValue = node.item;
      if (predicate.test(nodeValue)) {
        if (node == this.head) {
          this.head = node.remove();
          node = this.head;
        } else {
          node = node.remove();
        }
        this.size--;
      } else {
        node = node.next;
      }
    }
  }

  @Override
  public Iterator<MiStackItem<T>> iterator(final Predicate<MiStackItem<T>> predicate,
                                           final Predicate<MiStackItem<T>> takeWhile) {

    return new Iterator<>() {
      private boolean completed;
      private StackChainNode<T> nextPointer = findNext(head);
      private StackChainNode<T> pointerForRemove = null;

      @Override
      public boolean hasNext() {
        assertNotClosed();
        return !this.completed && this.nextPointer != null;
      }

      @Override
      public MiStackItem<T> next() {
        assertNotClosed();
        if (this.completed || this.nextPointer == null) {
          throw new NoSuchElementException();
        } else {
          this.pointerForRemove = this.nextPointer;
          this.nextPointer = findNext(this.nextPointer.next);
          return this.pointerForRemove.item;
        }
      }

      private StackChainNode<T> findNext(StackChainNode<T> pointer) {
        if (this.completed) {
          return null;
        }
        StackChainNode<T> result = null;
        while (pointer != null) {
          var value = pointer.item;
          if (predicate.test(value)) {
            if (takeWhile.test(value)) {
              result = pointer;
            } else {
              this.completed = true;
            }
            break;
          }
          pointer = pointer.next;
        }
        return result;
      }

      @Override
      public void remove() {
        assertNotClosed();
        if (this.pointerForRemove == null) {
          throw new IllegalStateException();
        } else {
          if (head == this.pointerForRemove) {
            head = this.pointerForRemove.remove();
          } else {
            this.pointerForRemove.remove();
          }
          this.pointerForRemove = null;
          size--;
        }
      }
    };
  }

  @Override
  public boolean isEmpty() {
    this.assertNotClosed();
    return this.size == 0L;
  }

  @Override
  public boolean isEmpty(final Predicate<MiStackItem<T>> predicate) {
    this.assertNotClosed();
    StackChainNode<T> node = this.head;
    while (node != null) {
      if (predicate.test(node.item)) {
        return false;
      }
      node = node.next;
    }
    return true;
  }

  @Override
  public long size() {
    this.assertNotClosed();
    return this.size;
  }

  @Override
  public void close() {
    this.assertNotClosed();
    this.closed = true;
    this.head = null;
    this.size = 0L;
  }

  @Override
  public boolean isClosed() {
    return this.closed;
  }

  private void assertNotClosed() {
    if (this.closed) {
      throw new IllegalStateException("Already closed");
    }
  }

  /**
   * Internal auxiliary class describing one stack item saved in heap.
   *
   * @param <T> type of value saved by stack item
   * @since 1.0.0
   */
  private static final class StackChainNode<T> {
    /**
     * Stack item value saved by the node. Must not be null.
     */
    private final MiStackItem<T> item;
    /**
     * Previous node in the stack (upper element).
     */
    private StackChainNode<T> prev;
    /**
     * Next node in the stack (underlying element).
     */
    private StackChainNode<T> next;

    private StackChainNode(final MiStackItem<T> item) {
      this.item = requireNonNull(item);
    }

    private StackChainNode<T> remove() {
      if (this.prev != null) {
        this.prev.next = this.next;
      }
      if (this.next != null) {
        this.next.prev = this.prev;
      }
      var nextNode = this.next;
      this.next = null;
      this.prev = null;
      return nextNode;
    }

  }
}
