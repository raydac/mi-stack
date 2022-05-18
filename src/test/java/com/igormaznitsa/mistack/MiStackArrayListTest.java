package com.igormaznitsa.mistack;

import com.igormaznitsa.mistack.impl.MiStackArrayList;

class MiStackArrayListTest extends AbstractMiStackTest {
  @Override
  MiStack makeStack() {
    return new MiStackArrayList();
  }

  @Override
  MiStack makeStack(final String name) {
    return new MiStackArrayList(name);
  }
}
