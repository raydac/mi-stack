package com.igormaznitsa.mistack;

import com.igormaznitsa.mistack.impl.MiStackArrayList;
import com.igormaznitsa.mistack.impl.MiStackFlat;

class MiStackFlatTest extends AbstractMiStackTest {
  @Override
  MiStack<String> makeStack() {
    return this.makeStack("");
  }

  @Override
  MiStack<String> makeStack(String name) {
    final MiStackFlat<String> result = new MiStackFlat<>(name, null);
    result.pushStack(new MiStackArrayList<>("test1"));
    return result;
  }
}