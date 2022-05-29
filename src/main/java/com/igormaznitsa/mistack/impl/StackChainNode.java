package com.igormaznitsa.mistack.impl;

import static java.util.Objects.requireNonNull;

import com.igormaznitsa.mistack.MiStackItem;

/**
 * Internal auxiliary class describing one stack item saved in heap.
 *
 * @param <T> type of value saved by stack item
 * @since 1.0.0
 */
public final class StackChainNode<T> {
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

  public StackChainNode(final MiStackItem<T> item) {
    this.item = requireNonNull(item);
  }

  public StackChainNode<T> getPrev() {
    return this.prev;
  }

  public void setPrev(final StackChainNode<T> prev) {
    this.prev = prev;
  }

  public StackChainNode<T> getNext() {
    return this.next;
  }

  public void setNext(final StackChainNode<T> next) {
    this.next = next;
  }

  public MiStackItem<T> getItem() {
    return this.item;
  }

  public StackChainNode<T> remove() {
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
