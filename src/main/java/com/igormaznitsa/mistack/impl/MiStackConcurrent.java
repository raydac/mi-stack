package com.igormaznitsa.mistack.impl;

import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;

public class MiStackConcurrent<T> extends MiStackDeque<T> {

  public MiStackConcurrent() {
    this(UUID.randomUUID().toString());
  }

  public MiStackConcurrent(final String name) {
    super(name, new ConcurrentLinkedDeque<>());
  }

}
