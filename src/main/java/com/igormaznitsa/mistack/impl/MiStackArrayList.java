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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Array list based implementation of Mi-Stack. <b>It is not thread safe</b>
 *
 * @param <T> item type to be saved on stack
 * @since 1.0.0
 */
public class MiStackArrayList<T> extends AbstractMiStackList<T> {

  /**
   * Default constructor, as name will be used random UUID text representation.
   *
   * @since 1.0.0
   */
  public MiStackArrayList() {
    this(UUID.randomUUID().toString());
  }

  /**
   * Constructor allows to provide name for the stack.
   *
   * @param name the name will be used as the stack name, must not be null but no any
   *             restrictions for emptiness.
   * @since 1.0.0
   */
  public MiStackArrayList(final String name) {
    super(name, new ArrayList<>());
  }

  @Override
  public Optional<MiStackItem<T>> pop(final Predicate<MiStackItem<T>> predicate) {
    this.assertNotClosed();
    MiStackItem<T> result = null;
    for (int i = this.list.size() - 1; result == null && i >= 0; i--) {
      final MiStackItem<T> item = this.list.get(i);
      if (predicate.test(item)) {
        result = item;
        this.list.remove(i);
      }
    }
    return Optional.ofNullable(result);
  }

  @Override
  protected Iterator<MiStackItem<T>> makeItemIterator(final List<MiStackItem<T>> list) {
    var listIterator = list.listIterator(list.size());
    return new Iterator<>() {
      @Override
      public boolean hasNext() {
        return listIterator.hasPrevious();
      }

      @Override
      public MiStackItem<T> next() {
        return listIterator.previous();
      }

      @Override
      public void remove() {
        listIterator.remove();
      }
    };
  }

  @Override
  protected void afterClear() {
    ((ArrayList<?>) this.list).trimToSize();
  }

}
