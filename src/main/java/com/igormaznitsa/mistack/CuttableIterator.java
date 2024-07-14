package com.igormaznitsa.mistack;

import java.util.Iterator;

/**
 * Extensions of iterator to provide information that the iterator was cut by some condition and may be there are more elements, but they are ignored.
 *
 * @param <T> type of iterable items
 * @since 1.0.2
 */
public interface CuttableIterator<T> extends Iterator<T> {
  /**
   * If true then iterator was cut by some external condition.
   *
   * @return true if iteration was cut by some condition or false if not
   */
  boolean isCut();
}
