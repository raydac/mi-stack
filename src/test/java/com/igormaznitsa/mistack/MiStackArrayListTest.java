package com.igormaznitsa.mistack;

import com.igormaznitsa.mistack.impl.MiStackArrayList;

class MiStackArrayListTest extends AbstractMiStackTest {
  @Override
  MiStack<String, MiStackItem<String, MiStackTag>, MiStackTag> makeStack() {
    return new MiStackArrayList<>();
  }

  @Override
  MiStack<String, MiStackItem<String, MiStackTag>, MiStackTag> makeStack(final String name) {
    return new MiStackArrayList<>(name);
  }
}
