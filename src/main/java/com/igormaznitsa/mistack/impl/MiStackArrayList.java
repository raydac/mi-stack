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
import java.util.List;
import java.util.UUID;

/**
 * Array list based implementation of Mi-Stack. <b>It is not thread safe</b>
 *
 * @param <T> item type to be saved on stack
 * @since 1.0.0
 */
public class MiStackArrayList<T> extends AbstractMiStackList<T> {

  private static final int INITIAL_CAPACITY = 16;

  public MiStackArrayList() {
    this(UUID.randomUUID().toString());
  }

  public MiStackArrayList(final String name) {
    super(name, INITIAL_CAPACITY);
  }

  @Override
  protected void afterClear() {
    ((ArrayList<?>) this.items).trimToSize();
  }

  @Override
  protected final List<MiStackItem<T>> createList(final Object value) {
    return new ArrayList<>((Integer) value);
  }
}
