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
@SuppressWarnings("unused")
public final class MiStackPredicates {
  private MiStackPredicates() {
  }

  /**
   * Get predicate which matches with all items.
   *
   * @param <I> type of stack items.
   * @return predicate matched with all items, can't be null
   * @since 1.0.0
   */
  public static <I extends MiStackItem<?, ?>> Predicate<I> itemsAll() {
    return e -> true;
  }

  /**
   * Make predicate joining two predicates by logical AND.
   *
   * @param alpha the left predicate, must not be null.
   * @param beta  the right predicate, must not be null.
   * @param <V>   type of stack values.
   * @param <T>   type of stack value tags.
   * @param <I>   type of stack items
   * @return predicate joining two predicates by logical AND.
   * @since 2.0.0
   */
  public static <V, T extends MiStackTag, I extends MiStackItem<V, T>> Predicate<I> itemsAnd(
      final Predicate<I> alpha,
      final Predicate<I> beta) {
    return e -> alpha.test(e) && beta.test(e);
  }

  /**
   * Make predicate joining two predicates by logical OR.
   *
   * @param alpha the left predicate, must not be null.
   * @param beta  the right predicate, must not be null.
   * @param <V>   type of stack values.
   * @param <T>   type of stack value tags.
   * @param <I>   type of stack items
   * @return predicate joining two predicates by logical OR.
   * @since 2.0.0
   */
  public static <V, T extends MiStackTag, I extends MiStackItem<V, T>> Predicate<I> itemsOr(
      final Predicate<I> alpha,
      final Predicate<I> beta) {
    return e -> alpha.test(e) || beta.test(e);
  }

  /**
   * Make predicate joining two predicates by logical XOR.
   *
   * @param alpha the left predicate, must not be null
   * @param beta  the right predicate, must not be null
   * @param <V>   type of stack values.
   * @param <T>   type of stack value tags.
   * @param <I>   type of stack items
   * @return predicate joining two predicates by logical XOR.
   * @since 2.0.0
   */
  public static <V, T extends MiStackTag, I extends MiStackItem<V, T>> Predicate<I> itemsXor(
      final Predicate<I> alpha,
      final Predicate<I> beta) {
    return e -> alpha.test(e) ^ beta.test(e);
  }

  /**
   * Make predicate inverses result of base predicate.
   *
   * @param alpha the base predicate, must not be null
   * @param <V>   type of stack values.
   * @param <T>   type of stack value tags.
   * @param <I>   type of stack items
   * @return predicate which result will be inverted, can't be null.
   * @since 2.0.0
   */
  public static <V, T extends MiStackTag, I extends MiStackItem<V, T>> Predicate<I> itemsNot(
      final Predicate<I> alpha) {
    return e -> !alpha.test(e);
  }
}
