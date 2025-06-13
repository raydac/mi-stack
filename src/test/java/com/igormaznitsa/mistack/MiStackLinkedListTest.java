package com.igormaznitsa.mistack;

import com.igormaznitsa.mistack.impl.MiStackLinkedList;

class MiStackLinkedListTest extends AbstractMiStackTest {
  @Override
  MiStack<String, MiStackItem<String, MiStackTag>, MiStackTag> makeStack() {
    return new MiStackLinkedList<>();
  }

  @Override
  MiStack<String, MiStackItem<String, MiStackTag>, MiStackTag> makeStack(final String name) {
    return new MiStackLinkedList<>(name);
  }
}
