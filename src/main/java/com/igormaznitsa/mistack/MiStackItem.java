package com.igormaznitsa.mistack;

import java.util.Set;

/**
 * Tagged item of stack.
 *
 * @see MiStack
 * @since 1.0.0
 */
public interface MiStackItem {
  Set<MiStackTag> getTags();
}
