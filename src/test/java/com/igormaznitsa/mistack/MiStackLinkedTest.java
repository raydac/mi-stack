package com.igormaznitsa.mistack;

import com.igormaznitsa.mistack.impl.MiStackLinked;

class MiStackLinkedTest extends AbstractMiStackTest {
  @Override
  MiStack<String, MiStackItem<String>> makeStack() {
    return new MiStackLinked<>();
  }

  @Override
  MiStack<String, MiStackItem<String>> makeStack(final String name) {
    return new MiStackLinked<>(name);
  }
}
