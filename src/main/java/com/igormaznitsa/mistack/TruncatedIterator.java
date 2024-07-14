package com.igormaznitsa.mistack;

import java.util.Iterator;

/**
 * Extensions of iterator to provide information that the iterator was truncated by some condition and may be there are more elements, but they are ignored.
 *
 * @param <T> type of iterable items
 * @since 1.0.2
 */
public interface TruncatedIterator<T> extends Iterator<T> {
  /**
   * If true then iterator was truncated by some external condition.
   *
   * @return true if iteration was truncated by some condition or false if not
   */
  boolean isTruncated();
}
