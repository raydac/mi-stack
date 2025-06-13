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

import com.igormaznitsa.mistack.MiStackItem;
import com.igormaznitsa.mistack.MiStackTag;

/**
 * Auxiliary class describing one stack item saved in heap.
 *
 * @param <V> type of values placed on stack
 * @param <I> type of value wrapper placed on stack
 * @param <T> type of value stack tag
 * @see MiStackLinked
 * @since 1.0.0
 */
public final class StackChainNode<V, I extends MiStackItem<V, T>, T extends MiStackTag> {
  /**
   * Stack item value saved by the node. Must not be null.
   */
  private final I item;
  /**
   * Previous node in the stack (upper element).
   */
  private StackChainNode<V, I, T> prev;
  /**
   * Next node in the stack (underlying element).
   */
  private StackChainNode<V, I, T> next;

  public StackChainNode(final I item) {
    this.item = requireNonNull(item);
  }

  /**
   * Get previous chained node if presented.
   *
   * @return previous chained mode if presented or null.
   * @since 1.0.0
   */
  public StackChainNode<V, I, T> getPrevious() {
    return this.prev;
  }

  /**
   * Set previous chained node.
   *
   * @param value node to be linked as previous chain node, can be null.
   * @since 1.0.0
   */
  public void setPrevious(final StackChainNode<V, I, T> value) {
    this.prev = value;
  }

  /**
   * Get next chained node if presented.
   *
   * @return next chained mode if presented or null.
   * @since 1.0.0
   */
  public StackChainNode<V, I, T> getNext() {
    return this.next;
  }

  /**
   * Set next chained node.
   *
   * @param value node to be linked as next chain node, can be null.
   * @since 1.0.0
   */
  public void setNext(final StackChainNode<V, I, T> value) {
    this.next = value;
  }

  /**
   * Get value saved by the node.
   *
   * @return the value saved by the node, can't be null
   * @since 1.0.0
   */
  public I getItem() {
    return this.item;
  }

  /**
   * Cut the node from chain and relink previous and next nodes to each other.
   *
   * @return the next node if present, else null
   * @since 1.0.0
   */
  public StackChainNode<V, I, T> remove() {
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
