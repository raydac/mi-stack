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
import com.igormaznitsa.mistack.MiStackTag;
import com.igormaznitsa.mistack.TruncableIterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Implementation of thread-unsafe Mi-Stack with linked chained items (like LinkedList).
 *
 * @param <V> type of item saved in the stack.
 * @since 1.0.0
 */
public class MiStackLinked<V, I extends MiStackItem<V, T>, T extends MiStackTag>
    implements MiStack<V, I, T> {

  private final String name;
  protected boolean closed;
  /**
   * Counter of elements on the stack.
   */
  protected long size;
  /**
   * First element on the stack, can be null if stack empty.
   */
  protected StackChainNode<V, I, T> head;

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
   * @param name the name will be used as the stack name, must not be null but no any
   *             restrictions for emptiness.
   * @since 1.0.0
   */
  public MiStackLinked(final String name) {
    this.name = requireNonNull(name);
    this.head = null;
  }

  @Override
  public MiStack<V, I, T> push(final I item) {
    this.assertNotClosed();
    var newNode = new StackChainNode<>(item);
    newNode.setNext(this.head);
    if (this.head != null) {
      this.head.setPrevious(newNode);
    }
    this.head = newNode;
    this.size++;
    return this;
  }

  @Override
  public Optional<I> pop(final Predicate<I> predicate) {
    return this.remove(predicate, 0);
  }

  @Override
  public TruncableIterator<I> iterator(final Predicate<I> predicate,
                                       final Predicate<I> takeWhile) {

    return new TruncableIterator<>() {
      private boolean completed;
      private boolean truncated;
      private StackChainNode<V, I, T> nextPointer = findNext(head);
      private StackChainNode<V, I, T> pointerForRemove = null;

      @Override
      public boolean hasNext() {
        if (isClosed()) {
          this.completed = true;
          this.nextPointer = null;
        }
        return !this.completed && this.nextPointer != null;
      }

      @Override
      public boolean isTruncated() {
        return this.truncated;
      }

      @Override
      public I next() {
        assertNotClosed();
        if (this.completed || this.nextPointer == null) {
          throw new NoSuchElementException();
        } else {
          this.pointerForRemove = this.nextPointer;
          this.nextPointer = findNext(this.nextPointer.getNext());
          return this.pointerForRemove.getItem();
        }
      }

      private StackChainNode<V, I, T> findNext(StackChainNode<V, I, T> pointer) {
        if (this.completed) {
          return null;
        }
        StackChainNode<V, I, T> result = null;
        while (pointer != null) {
          var value = pointer.getItem();
          if (predicate.test(value)) {
            if (takeWhile.test(value)) {
              result = pointer;
            } else {
              this.truncated = true;
              this.completed = true;
            }
            break;
          }
          pointer = pointer.getNext();
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
  public boolean isClosed() {
    return this.closed;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public void clear() {
    this.assertNotClosed();
    this.head = null;
    this.size = 0L;
  }

  @Override
  public void clear(final Predicate<I> predicate) {
    this.assertNotClosed();
    StackChainNode<V, I, T> node = this.head;
    while (node != null) {
      var nodeValue = node.getItem();
      if (predicate.test(nodeValue)) {
        if (node == this.head) {
          this.head = node.remove();
          node = this.head;
        } else {
          node = node.remove();
        }
        this.size--;
      } else {
        node = node.getNext();
      }
    }
  }

  @Override
  public boolean isEmpty(final Predicate<I> predicate) {
    this.assertNotClosed();
    StackChainNode<V, I, T> node = this.head;
    while (node != null) {
      if (predicate.test(node.getItem())) {
        return false;
      }
      node = node.getNext();
    }
    return true;
  }

  @Override
  public boolean isEmpty() {
    this.assertNotClosed();
    return this.size == 0L;
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

}
