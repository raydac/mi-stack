package com.igormaznitsa.mistack;

import com.igormaznitsa.mistack.impl.MiStackArray;

class MiStackArrayTest extends AbstractMiStackTest {
  @Override
  MiStack<String> makeStack() {
    return new MiStackArray<>();
  }

  @Override
  MiStack<String> makeStack(final String name) {
    return new MiStackArray<>(name);
  }
}
