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

import com.igormaznitsa.mistack.MiStackItem;
import com.igormaznitsa.mistack.MiStackTag;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Linked list based implementation of Mi-Stack. <b>It is not thread safe</b>
 *
 * @param <V> type of values placed on stack
 * @param <I> type of value wrapper placed on stack
 * @param <T> type of value stack tag
 * @since 1.0.0
 */
public class MiStackLinkedList<V, I extends MiStackItem<V, T>, T extends MiStackTag>
    extends AbstractMiStackList<V, I, T> {

  /**
   * Default constructor, as name will be used random UUID text representation.
   *
   * @since 1.0.0
   */
  public MiStackLinkedList() {
    this(UUID.randomUUID().toString());
  }

  /**
   * Constructor allows to provide name for the stack.
   *
   * @param name the name will be used as the stack name, must not be null but no any
   *             restrictions for emptiness.
   * @since 1.0.0
   */
  public MiStackLinkedList(final String name) {
    super(name, new LinkedList<>());
  }

  @Override
  protected Iterator<I> makeItemIterator(List<I> list) {
    return ((LinkedList<I>) list).descendingIterator();
  }
}
