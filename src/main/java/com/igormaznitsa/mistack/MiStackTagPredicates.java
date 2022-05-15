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
public final class MiStackTagPredicates {

  public static final Predicate<MiStackItem> ALL = item -> true;
  public static final Predicate<MiStackItem> EMPTY = item -> item.getTags().isEmpty();

  private MiStackTagPredicates() {

  }

  public static Predicate<MiStackItem> and(final Predicate<MiStackItem> alpha,
                                           final Predicate<MiStackItem> beta) {
    return item -> alpha.test(item) && beta.test(item);
  }

  public static Predicate<MiStackItem> xor(final Predicate<MiStackItem> alpha,
                                           final Predicate<MiStackItem> beta) {
    return item -> alpha.test(item) ^ beta.test(item);
  }

  public static Predicate<MiStackItem> or(final Predicate<MiStackItem> alpha,
                                          final Predicate<MiStackItem> beta) {
    return item -> alpha.test(item) || beta.test(item);
  }

  public static Predicate<MiStackItem> not(final Predicate<MiStackItem> predicate) {
    return item -> !predicate.test(item);
  }

  public static Predicate<MiStackItem> anyTag(final MiStackTag... tags) {
    return anyTag(List.of(tags));
  }

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

  public static Predicate<MiStackItem> allTags(final MiStackTag... tags) {
    return allTags(List.of(tags));
  }

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
