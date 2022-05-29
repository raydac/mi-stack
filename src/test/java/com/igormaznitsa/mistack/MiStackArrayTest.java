package com.igormaznitsa.mistack;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.igormaznitsa.mistack.exception.MiStackOverflowException;
import com.igormaznitsa.mistack.impl.MiStackArray;
import com.igormaznitsa.mistack.impl.MiStackItemImpl;
import com.igormaznitsa.mistack.impl.MiStackTagImpl;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class MiStackArrayTest extends AbstractMiStackTest {
  @Override
  MiStack<String> makeStack() {
    return new TestMiStackArray<>();
  }

  @Override
  MiStack<String> makeStack(final String name) {
    return new TestMiStackArray<>(name);
  }

  @Test
  void testStaticSize() {
    final int elements = 16384;
    try (var stack = new TestMiStackArray<Integer>("test", elements, false)) {
      var tagEven = MiStackTagImpl.tagOf("even");
      var tagOdd = MiStackTagImpl.tagOf("odd");
      var tagThird = MiStackTagImpl.tagOf("third");
      IntStream.range(0, elements)
          .forEach(x -> stack.push(MiStackItemImpl.itemOf(x, (x & 1) == 0 ? tagEven : tagOdd)));
      assertEquals(elements, stack.size());
      assertEquals(elements, stack.getItemArray().length);
      stack.clear(MiStack.allTags(tagEven));
      assertEquals(8192, stack.size());
      assertEquals(elements, stack.getItemArray().length);

      IntStream.range(0, 8192)
          .forEach(x -> stack.push(MiStackItemImpl.itemOf(x, tagThird)));
      assertEquals(elements, stack.size());
      assertThrows(MiStackOverflowException.class,
          () -> stack.push(MiStackItemImpl.itemOf(666, tagThird)));
    }
  }

  @Test
  void testDynamicSize() {
    final int elements = 16384;
    try (var stack = new TestMiStackArray<Integer>("test", elements, true)) {
      var tagEven = MiStackTagImpl.tagOf("even");
      var tagOdd = MiStackTagImpl.tagOf("odd");
      var tagThird = MiStackTagImpl.tagOf("third");
      IntStream.range(0, elements)
          .forEach(x -> stack.push(MiStackItemImpl.itemOf(x, (x & 1) == 0 ? tagEven : tagOdd)));
      assertEquals(elements, stack.size());
      assertEquals(elements, stack.getItemArray().length);
      stack.clear(MiStack.allTags(tagEven));
      assertEquals(8192, stack.size());
      assertEquals((8192 / MiStackArray.CAPACITY_STEP + 1) * MiStackArray.CAPACITY_STEP,
          stack.getItemArray().length);

      IntStream.range(0, 8192)
          .forEach(x -> stack.push(MiStackItemImpl.itemOf(x, tagThird)));
      assertEquals(elements, stack.size());
      assertDoesNotThrow(() -> stack.push(MiStackItemImpl.itemOf(666, tagThird)));
    }
  }

  private static final class TestMiStackArray<T> extends MiStackArray<T> {

    public TestMiStackArray() {
      super();
    }

    public TestMiStackArray(final String name) {
      super(name);
    }

    public TestMiStackArray(String name, int capacity, boolean dynamic) {
      super(name, capacity, dynamic);
    }

    @Override
    public Object[] getItemArray() {
      return super.getItemArray();
    }
  }
}
