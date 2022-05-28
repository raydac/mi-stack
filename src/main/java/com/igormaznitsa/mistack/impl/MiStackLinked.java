package com.igormaznitsa.mistack.impl;

import static java.util.Objects.requireNonNull;

import com.igormaznitsa.mistack.MiStack;
import com.igormaznitsa.mistack.MiStackItem;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public class MiStackLinked<T> implements MiStack<T> {

  private final String name;
  private boolean closed;
  private long size;
  private StackChainNode<T> head;

  public MiStackLinked() {
    this(UUID.randomUUID().toString());
  }

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
    newNode.bottom = this.head;
    if (this.head != null) {
      this.head.top = newNode;
    }
    this.head = newNode;
    this.size++;
    return this;
  }

  @Override
  public MiStack<T> push(MiStackItem<T>... items) {
    for (final MiStackItem<T> item : items) {
      this.push(item);
    }
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
        node = node.bottom;
      } else {
        node = node.bottom;
      }
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
          node = node.bottom;
        }
      } else {
        node = node.bottom;
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
        node = node.bottom;
      }
    }
  }

  private void assertNotClosed() {
    if (this.closed) {
      throw new IllegalStateException("Already closed");
    }
  }

  @Override
  public Iterator<MiStackItem<T>> iterator(final Predicate<MiStackItem<T>> predicate,
                                           final Predicate<MiStackItem<T>> takeWhile) {

    return new Iterator<>() {
      private boolean completed;
      private StackChainNode<T> nextPointer = findNext(head);
      private StackChainNode<T> pointerForRemove = null;

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
          pointer = pointer.bottom;
        }
        return result;
      }

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
          this.nextPointer = findNext(this.nextPointer.bottom);
          return this.pointerForRemove.item;
        }
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
      node = node.bottom;
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

  private static final class StackChainNode<T> {
    final MiStackItem<T> item;
    private StackChainNode<T> top;
    private StackChainNode<T> bottom;

    StackChainNode(final MiStackItem<T> item) {
      this.item = item;
    }

    StackChainNode<T> remove() {
      if (this.top != null) {
        this.top.bottom = this.bottom;
      }
      if (this.bottom != null) {
        this.bottom.top = this.top;
      }
      var nextNode = this.bottom;
      this.bottom = null;
      this.top = null;
      return nextNode;
    }

  }
}
