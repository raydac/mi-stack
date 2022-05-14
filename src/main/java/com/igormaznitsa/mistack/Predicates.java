package com.igormaznitsa.mistack;

import java.util.function.Predicate;

public final class Predicates {

  public static final Predicate<MiStackItem> ALL = item -> true;
  public static final Predicate<MiStackItem> EMPTY = item -> item.getTags().isEmpty();

  private Predicates() {

  }

  public static Predicate<MiStackItem> and(final Predicate<MiStackItem> alpha,
                                           final Predicate<MiStackItem> beta) {
    return item -> alpha.test(item) && beta.test(item);
  }

  public static Predicate<MiStackItem> or(final Predicate<MiStackItem> alpha,
                                          final Predicate<MiStackItem> beta) {
    return item -> alpha.test(item) || beta.test(item);
  }

  public static Predicate<MiStackItem> not(final Predicate<MiStackItem> predicate) {
    return item -> !predicate.test(item);
  }

  public static Predicate<MiStackItem> any(final MiStackTag... tags) {
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

  public static Predicate<MiStackItem> all(final MiStackTag... tags) {
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
