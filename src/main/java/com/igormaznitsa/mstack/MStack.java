package com.igormaznitsa.mstack;

import java.util.Objects;
import java.util.Optional;

public class MStack {

  private final String id;

  private MStack(final String id) {
    this.id = Objects.requireNonNull(id);
  }

  public String getId() {
    return this.id;
  }

  public MStack push(final MStackItem item) {
    return this;
  }

  public Optional<MStackItem> pop(final MStackRequest request) {
    return null;
  }

  public Optional<MStackItem> peek(final MStackRequest request, final long depth) {
    return null;
  }

  public long size(final MStackRequest request) {
    return 0;
  }

}
