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

package com.igormaznitsa.mistack;

import com.igormaznitsa.mistack.impl.MiStackArrayList;
import java.util.Set;

/**
 * Tagged item of stack.
 *
 * @see MiStackArrayList
 * @since 1.0.0
 */
public interface MiStackItem {
  /**
   * Get all tags for the item.
   *
   * @return all tags as a set, the set can be empty but can't be null.
   * @since 1.0.0
   */
  Set<MiStackTag> getTags();
}
