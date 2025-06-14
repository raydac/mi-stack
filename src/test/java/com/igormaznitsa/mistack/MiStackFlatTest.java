package com.igormaznitsa.mistack;

import static com.igormaznitsa.mistack.impl.MiStackItemImpl.itemOf;
import static com.igormaznitsa.mistack.impl.MiStackTagImpl.tagOf;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.igormaznitsa.mistack.impl.MiStackArrayList;
import com.igormaznitsa.mistack.impl.MiStackFlat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;
import org.junit.jupiter.api.Test;

class MiStackFlatTest extends AbstractMiStackTest {

  private static MiStackFlat<String, MiStackItem<String, MiStackTag>, MiStackTag> makePrefilledFlatStack() {
    return makePrefilledFlatStack((x, y) -> true);
  }

  private static MiStackFlat<String, MiStackItem<String, MiStackTag>, MiStackTag> makePrefilledFlatStack(
      final BiPredicate<MiStack<String, MiStackItem<String, MiStackTag>, MiStackTag>, MiStack<String, MiStackItem<String, MiStackTag>, MiStackTag>> switchPredicate) {
    final MiStackFlat<String, MiStackItem<String, MiStackTag>, MiStackTag> flatStack =
        new MiStackFlat<>("testFlat", switchPredicate);

    final MiStack<String, MiStackItem<String, MiStackTag>, MiStackTag> stack1 =
        new MiStackArrayList<>("stack1");
    stack1.push(itemOf("item3", tagOf("A")));
    stack1.push(itemOf("item2", tagOf("A")));
    stack1.push(itemOf("item1", tagOf("A")));

    final MiStack<String, MiStackItem<String, MiStackTag>, MiStackTag> stack2 =
        new MiStackArrayList<>("stack2");
    stack2.push(itemOf("item6", tagOf("B")));
    stack2.push(itemOf("item5", tagOf("B")));
    stack2.push(itemOf("item4", tagOf("B")));

    final MiStack<String, MiStackItem<String, MiStackTag>, MiStackTag> stack3 =
        new MiStackArrayList<>("stack3");
    stack3.push(itemOf("item9", tagOf("C")));
    stack3.push(itemOf("item8", tagOf("C")));
    stack3.push(itemOf("item7", tagOf("C")));

    flatStack.pushStack(stack3).pushStack(stack2).pushStack(stack1);

    return flatStack;
  }

  private static void assertAllItems(
      final MiStackFlat<String, MiStackItem<String, MiStackTag>, MiStackTag> stack,
                                     String... expectedItems) {
    var iterator = stack.iterator();
    final List<String> found = new ArrayList<>();
    while (iterator.hasNext()) {
      found.add(iterator.next().getValue());
    }
    assertArrayEquals(expectedItems, found.toArray(
        String[]::new));
  }

  @Override
  MiStack<String, MiStackItem<String, MiStackTag>, MiStackTag> makeStack() {
    return this.makeStack("");
  }

  @Override
  MiStack<String, MiStackItem<String, MiStackTag>, MiStackTag> makeStack(String name) {
    final MiStackFlat<String, MiStackItem<String, MiStackTag>, MiStackTag> result =
        new MiStackFlat<>(name, (x, y) -> true);
    result.pushStack(new MiStackArrayList<>("test1"));
    return result;
  }

  @Test
  void testIterateOverEmpty() {
    try (
        final MiStackFlat<String, MiStackItem<String, MiStackTag>, MiStackTag> flatStack = new MiStackFlat<>(
            "testFlat",
            (x, y) -> true)) {
      assertFalse(flatStack.iterator().hasNext());
    }
  }

  @Test
  void testPopStack() {
    try (
        final MiStackFlat<String, MiStackItem<String, MiStackTag>, MiStackTag> flatStack = makePrefilledFlatStack()) {
      final List<MiStack<String, MiStackItem<String, MiStackTag>, MiStackTag>> stacks =
          new ArrayList<>();
      flatStack.iteratorStacks().forEachRemaining(stacks::add);

      assertSame(stacks.get(0), flatStack.removeStack(stacks.get(0)).orElseThrow());
      assertSame(stacks.get(1), flatStack.popStack().orElseThrow());

      final List<String> found = new ArrayList<>();
      for (MiStackItem<String, MiStackTag> stringMiStackTagMiStackItem : flatStack) {
        found.add(stringMiStackTagMiStackItem.getValue());
      }
      assertArrayEquals(new String[] {"item7", "item8", "item9"}, found.toArray(
          String[]::new));
    }
  }

  @Test
  void testSwitchStack() {
    final AtomicInteger stacks = new AtomicInteger();
    try (
        final MiStackFlat<String, MiStackItem<String, MiStackTag>, MiStackTag> flatStack = makePrefilledFlatStack(
            (x, y) -> {
              if (x != null) {
                stacks.incrementAndGet();
                assertFalse(x.getName().endsWith("2"));
              }
              assertNotNull(y);
              return !y.getName().endsWith("2");
            })) {
      assertAllItems(flatStack, "item1", "item2", "item3", "item7", "item8", "item9");
    }
    assertEquals(2, stacks.get());
  }

  @Test
  void testRemoveItemsDuringIteration() {
    try (
        final MiStackFlat<String, MiStackItem<String, MiStackTag>, MiStackTag> flatStack = makePrefilledFlatStack()) {
      final Iterator<MiStackItem<String, MiStackTag>> iterator = flatStack.iterator();
      int index = 1;
      while (iterator.hasNext()) {
        assertNotNull(iterator.next());
        if ((index & 1) == 0) {
          iterator.remove();
        }
        index++;
      }
      assertAllItems(flatStack, "item1", "item3", "item5", "item7", "item9");
    }
  }

  @Test
  void testPopItems() {
    try (
        final MiStackFlat<String, MiStackItem<String, MiStackTag>, MiStackTag> flatStack = makePrefilledFlatStack()) {
      for (int i = 1; i < 10; i++) {
        final MiStackItem<String, MiStackTag> item = flatStack.pop(x -> true).orElseThrow();
        assertEquals("item" + i, item.getValue());
      }
      assertTrue(flatStack.isEmpty());
    }
  }

  @Test
  void testIterateWithCut() {
    try (
        final MiStackFlat<String, MiStackItem<String, MiStackTag>, MiStackTag> flatStack = makePrefilledFlatStack()) {
      final List<String> found = new ArrayList<>();
      var iterator = flatStack.iterator(x -> true, x -> !x.getValue().equals("item5"));
      while (iterator.hasNext()) {
        found.add(iterator.next().getValue());
      }
      assertArrayEquals(new String[] {"item1", "item2", "item3", "item4"}, found.toArray(
          String[]::new));
    }
  }

  @Test
  void testIterateWithFiltering() {
    try (
        final MiStackFlat<String, MiStackItem<String, MiStackTag>, MiStackTag> flatStack = makePrefilledFlatStack()) {
      final MiStackTag filteredTag = tagOf("B");

      final List<String> found = new ArrayList<>();
      var iterator = flatStack.iterator(x -> !x.getTags().contains(filteredTag));
      while (iterator.hasNext()) {
        found.add(iterator.next().getValue());
      }
      assertArrayEquals(new String[] {"item1", "item2", "item3", "item7", "item8", "item9"},
          found.toArray(
              String[]::new));
    }
  }


  @Test
  void testIterateOverAllFlattenStacks() {
    try (
        final MiStackFlat<String, MiStackItem<String, MiStackTag>, MiStackTag> flatStack = makePrefilledFlatStack()) {
      var iterator = flatStack.iterator();
      int index = 1;
      while (iterator.hasNext()) {
        final MiStackItem<String, MiStackTag> nextValue = iterator.next();
        assertEquals("item" + index, nextValue.getValue());
        index++;
      }
      assertEquals(10, index);
    }
  }
}