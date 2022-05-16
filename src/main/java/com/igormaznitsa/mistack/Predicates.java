package com.igormaznitsa.mistack;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * Auxiliary class to make predicates for tags and their combinations.
 *
 * @see MiStack
 * @see MiStackTag
 * @since 1.0.0
 */
public final class Predicates {

  public static final Predicate<MiStackItem> ALL_TAGS = item -> true;
  public static final Predicate<MiStackItem> EMPTY_TAGS = item -> item.getTags().isEmpty();

  private Predicates() {

  }

  /**
   * Concatenate two predicate through AND logical operation.
   *
   * @param alpha a predicate, must not be null
   * @param beta  a predicate, must not be null
   * @param <T>   parameter to be used for predicates
   * @return combined predicate, must not be null
   * @since 1.0.0
   */
  public static <T> Predicate<T> and(final Predicate<T> alpha,
                                     final Predicate<T> beta) {
    return item -> alpha.test(item) && beta.test(item);
  }

  /**
   * Concatenate two predicate through XOR logical operation.
   *
   * @param alpha a predicate, must not be null
   * @param beta  a predicate, must not be null
   * @param <T>   parameter to be used for predicates
   * @return combined predicate, must not be null
   * @since 1.0.0
   */
  public static <T> Predicate<T> xor(final Predicate<T> alpha,
                                     final Predicate<T> beta) {
    return item -> alpha.test(item) ^ beta.test(item);
  }

  /**
   * Concatenate two predicate through OR logical operation.
   *
   * @param alpha a predicate, must not be null
   * @param beta  a predicate, must not be null
   * @param <T>   parameter to be used for predicates
   * @return combined predicate, must not be null
   * @since 1.0.0
   */
  public static <T> Predicate<T> or(final Predicate<T> alpha,
                                    final Predicate<T> beta) {
    return item -> alpha.test(item) || beta.test(item);
  }

  /**
   * Makes negative predicate version.
   *
   * @param predicate a predicate, must not be null
   * @param <T>       parameter to be used for predicates
   * @return predicate working as negative for provided one, must not be null
   * @since 1.0.0
   */
  public static <T> Predicate<T> not(final Predicate<T> predicate) {
    return item -> !predicate.test(item);
  }

  /**
   * Generate predicate which is true if any tag among provided ones is met in a stack item.
   *
   * @param tags array of tags to be used for predicate, must not contain null
   * @return generated predicate, must not be null
   * @since 1.0.0
   */
  public static Predicate<MiStackItem> anyTag(final MiStackTag... tags) {
    return anyTag(List.of(tags));
  }

  /**
   * Generate predicate which is true if any tag among provided ones is met in a stack item.
   *
   * @param tags collection of tags to be used for predicate, must not contain null
   * @return generated predicate, must not be null
   * @since 1.0.0
   */
  public static Predicate<MiStackItem> anyTag(final Collection<MiStackTag> tags) {
    return item -> {
      boolean result = false;
      var itemTags = item.getTags();
      for (final MiStackTag t : tags) {
        if (itemTags.contains(t)) {
          result = true;
          break;
        }
      }
      return result;
    };
  }

  /**
   * Generate predicate which is true if all tags are met in a stack item.
   *
   * @param tags array of tags to be used for predicate, must not contain null
   * @return generated predicate, must not be null
   * @since 1.0.0
   */
  public static Predicate<MiStackItem> allTags(final MiStackTag... tags) {
    return allTags(List.of(tags));
  }

  /**
   * Generate predicate which is true if all tags are met in a stack item.
   *
   * @param tags collection of tags to be used for predicate, must not contain null
   * @return generated predicate, must not be null
   * @since 1.0.0
   */
  public static Predicate<MiStackItem> allTags(final Collection<MiStackTag> tags) {
    return item -> {
      var itemTags = item.getTags();
      boolean result = true;
      for (final MiStackTag t : tags) {
        if (!itemTags.contains(t)) {
          result = false;
          break;
        }
      }
      return result;
    };
  }

}
