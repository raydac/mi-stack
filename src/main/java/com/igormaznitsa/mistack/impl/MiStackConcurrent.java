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
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Concurrent thread safe version of MiStack. As base collection it uses ConcurrentLinkedDeque.
 *
 * @param <V> type of values placed on stack
 * @param <I> type of value wrapper placed on stack
 * @param <T> type of value stack tag
 * @see ConcurrentLinkedDeque
 * @since 1.0.0
 */
public class MiStackConcurrent<V, I extends MiStackItem<V, T>, T extends MiStackTag>
    extends AbstractMiStackDeque<V, I, T> {

  public MiStackConcurrent() {
    this(UUID.randomUUID().toString());
  }

  public MiStackConcurrent(final String name) {
    super(name, new ConcurrentLinkedDeque<>());
  }

}
