package com.igormaznitsa.mistack;

import com.igormaznitsa.mistack.impl.MiStackArrayList;

class MiStackArrayListTest extends AbstractMiStackTest {
  @Override
  MiStack<String> makeStack() {
    return new MiStackArrayList<>();
  }

  @Override
  MiStack<String> makeStack(final String name) {
    return new MiStackArrayList<>(name);
  }
}
