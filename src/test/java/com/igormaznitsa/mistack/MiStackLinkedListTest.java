package com.igormaznitsa.mistack;

import com.igormaznitsa.mistack.impl.MiStackLinkedList;

class MiStackLinkedListTest extends AbstractMiStackTest {
  @Override
  MiStack<String> makeStack() {
    return new MiStackLinkedList<>();
  }

  @Override
  MiStack<String> makeStack(final String name) {
    return new MiStackLinkedList<>(name);
  }
}
