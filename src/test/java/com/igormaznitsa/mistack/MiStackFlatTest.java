package com.igormaznitsa.mistack;

import static com.igormaznitsa.mistack.impl.MiStackItemImpl.itemOf;
import static com.igormaznitsa.mistack.impl.MiStackTagImpl.tagOf;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.igormaznitsa.mistack.impl.MiStackArrayList;
import com.igormaznitsa.mistack.impl.MiStackFlat;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

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

  @Test
  void testIterateOverEmpty() {
    final MiStackFlat<String> flatStack = new MiStackFlat<>("testFlat", null);
    assertFalse(flatStack.iterator().hasNext());
  }

  @Test
  void testIterateWithCut() {
    final MiStackFlat<String> flatStack = new MiStackFlat<>("testFlat", null);

    final MiStack<String> stack1 = new MiStackArrayList<>("stack1");
    stack1.push(itemOf("item3", tagOf("A")));
    stack1.push(itemOf("item2", tagOf("A")));
    stack1.push(itemOf("item1", tagOf("A")));

    final MiStack<String> stack2 = new MiStackArrayList<>("stack2");
    stack2.push(itemOf("item6", tagOf("B")));
    stack2.push(itemOf("item5", tagOf("B")));
    stack2.push(itemOf("item4", tagOf("B")));

    final MiStack<String> stack3 = new MiStackArrayList<>("stack3");
    stack3.push(itemOf("item9", tagOf("C")));
    stack3.push(itemOf("item8", tagOf("C")));
    stack3.push(itemOf("item7", tagOf("C")));

    flatStack.pushStack(stack3).pushStack(stack2).pushStack(stack1);

    final List<String> found = new ArrayList<>();
    var iterator = flatStack.iterator(x -> true, x -> !x.getValue().equals("item5"));
    while (iterator.hasNext()) {
      found.add(iterator.next().getValue());
    }
    assertArrayEquals(new String[] {"item1", "item2", "item3", "item4"}, found.toArray(
        String[]::new));
  }

  @Test
  void testIterateWithFiltering() {
    final MiStackFlat<String> flatStack = new MiStackFlat<>("testFlat", null);

    final MiStack<String> stack1 = new MiStackArrayList<>("stack1");
    stack1.push(itemOf("item3", tagOf("A")));
    stack1.push(itemOf("item2", tagOf("A")));
    stack1.push(itemOf("item1", tagOf("A")));

    final MiStack<String> stack2 = new MiStackArrayList<>("stack2");
    stack2.push(itemOf("item6", tagOf("B")));
    stack2.push(itemOf("item5", tagOf("B")));
    stack2.push(itemOf("item4", tagOf("B")));

    final MiStack<String> stack3 = new MiStackArrayList<>("stack3");
    stack3.push(itemOf("item9", tagOf("C")));
    stack3.push(itemOf("item8", tagOf("C")));
    stack3.push(itemOf("item7", tagOf("C")));

    flatStack.pushStack(stack3).pushStack(stack2).pushStack(stack1);

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


  @Test
  void testIterateOverAllFlattenStacks() {
    final MiStackFlat<String> flatStack = new MiStackFlat<>("testFlat", null);

    final MiStack<String> stack1 = new MiStackArrayList<>("stack1");
    stack1.push(itemOf("item3", tagOf("A")));
    stack1.push(itemOf("item2", tagOf("A")));
    stack1.push(itemOf("item1", tagOf("A")));

    final MiStack<String> stack2 = new MiStackArrayList<>("stack2");
    stack2.push(itemOf("item6", tagOf("B")));
    stack2.push(itemOf("item5", tagOf("B")));
    stack2.push(itemOf("item4", tagOf("B")));

    final MiStack<String> stack3 = new MiStackArrayList<>("stack3");
    stack3.push(itemOf("item9", tagOf("C")));
    stack3.push(itemOf("item8", tagOf("C")));
    stack3.push(itemOf("item7", tagOf("C")));

    flatStack.pushStack(stack3).pushStack(stack2).pushStack(stack1);

    var iterator = flatStack.iterator();
    int index = 1;
    while (iterator.hasNext()) {
      final MiStackItem<String> nextValue = iterator.next();
      assertEquals("item" + index, nextValue.getValue());
      index++;
    }
    assertEquals(10, index);

  }
}