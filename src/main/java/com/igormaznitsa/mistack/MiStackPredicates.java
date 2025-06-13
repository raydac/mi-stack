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

package com.igormaznitsa.mistack;

import java.util.function.Predicate;

/**
 * Auxiliary class contains functions for predicate combinations.
 *
 * @since 1.0.0
 */
public final class MiStackPredicates {
  private MiStackPredicates() {
  }

  /**
   * Get predicate which matches with all items.
   *
   * @param <T> type of stack values
   * @return predicate matched with all items, can't be null
   * @since 1.0.0
   */
  public static <V extends MiStackItem<?>> Predicate<V> itemsAll() {
    return e -> true;
  }

  /**
   * Make predicate joining two predicates by logical AND.
   *
   * @param alpha the left predicate, must not be null.
   * @param beta  the right predicate, must not be null.
   * @param <T>   type of stack values.
   * @return predicate joining two predicates by logical AND.
   * @since 1.0.0
   */
  public static <V extends MiStackItem<?>> Predicate<V> itemsAnd(final Predicate<V> alpha,
                                                                 final Predicate<V> beta) {
    return e -> alpha.test(e) && beta.test(e);
  }

  /**
   * Make predicate joining two predicates by logical OR.
   *
   * @param alpha the left predicate, must not be null.
   * @param beta  the right predicate, must not be null.
   * @param <T>   type of stack values.
   * @return predicate joining two predicates by logical OR.
   * @since 1.0.0
   */
  public static <V extends MiStackItem<?>> Predicate<V> itemsOr(final Predicate<V> alpha,
                                                                final Predicate<V> beta) {
    return e -> alpha.test(e) || beta.test(e);
  }

  /**
   * Make predicate joining two predicates by logical XOR.
   *
   * @param alpha the left predicate, must not be null
   * @param beta  the right predicate, must not be null
   * @param <T>   type of stack values
   * @return predicate joining two predicates by logical XOR.
   * @since 1.0.0
   */
  public static <V extends MiStackItem<?>> Predicate<V> itemsXor(final Predicate<V> alpha,
                                                                 final Predicate<V> beta) {
    return e -> alpha.test(e) ^ beta.test(e);
  }

  /**
   * Make predicate inverses result of base predicate.
   *
   * @param alpha the base predicate, must not be null
   * @param <T>   type of stack values
   * @return predicate which result will be inverted, can't be null.
   * @since 1.0.0
   */
  public static <V extends MiStackItem<?>> Predicate<V> itemsNot(final Predicate<V> alpha) {
    return e -> !alpha.test(e);
  }
}
