/**
 * Copyright (C) 2022 Igor A. Maznitsa
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igormaznitsa.mistack.impl;

import com.igormaznitsa.mistack.MiStackItem;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Linked list based implementation of Mi-Stack. <b>It is not thread safe</b>
 *
 * @param <T> item type to be saved on stack
 * @since 1.0.0
 */
public class MiStackLinkedList<T> extends AbstractMiStackList<T> {

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
   * @param name the name will be used as the stack name, must not be null but no any restrictions for emptiness.
   * @since 1.0.0
   */
  public MiStackLinkedList(final String name) {
    super(name, new LinkedList<>());
  }

  @Override
  protected Iterator<MiStackItem<T>> makeItemIterator(List<MiStackItem<T>> list) {
    return ((LinkedList<MiStackItem<T>>) list).descendingIterator();
  }
}
