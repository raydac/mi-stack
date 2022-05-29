package com.igormaznitsa.mistack;

import static com.igormaznitsa.mistack.impl.MiStackItemImpl.itemOf;
import static com.igormaznitsa.mistack.impl.MiStackTagImpl.tagOf;
import static com.igormaznitsa.mistack.impl.MiStackTagImpl.tagsOf;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.NoSuchElementException;
import java.util.Set;
import org.junit.jupiter.api.Test;

abstract class AbstractMiStackTest {

  @Test
  void testExceptionAfterClose() {
    MiStack<String> stack = this.makeStack();

    var tags1 = tagsOf("hello", "world");
    var tags2 = tagsOf("universe");

    var item1 = itemOf("item1", tags1);
    var item2 = itemOf("item2", tags1);
    var item3 = itemOf("item3", tags2);
    var item4 = itemOf("item4", tags1);

    stack.push(item1, item2, item3, item4);

    var iterator = stack.iterator();
    assertTrue(iterator.hasNext());
    assertSame(item4, iterator.next());

    assertTrue(iterator.hasNext());
    assertSame(item3, iterator.next());

    assertFalse(stack.isClosed());
    assertDoesNotThrow(stack::close);
    assertTrue(stack.isClosed());

    assertThrows(IllegalStateException.class, iterator::hasNext);
    assertThrows(IllegalStateException.class, iterator::next);
    assertThrows(IllegalStateException.class, iterator::remove);
    assertThrows(IllegalStateException.class, stack::close);
    assertThrows(IllegalStateException.class, () -> stack.push(item1));
    assertThrows(IllegalStateException.class, () -> stack.pop(stack.forAll()));
  }

  abstract MiStack<String> makeStack();

  @Test
  void testIteratorWithTakeWhile() {
    try (MiStack<String> stack = this.makeStack()) {

      var tags1 = tagsOf("hello", "world");
      var tags2 = tagsOf("universe");

      var item1 = itemOf("item1", tags1);
      var item2 = itemOf("item2", tags1);
      var item3 = itemOf("item3", tags2);
      var item4 = itemOf("item4", tags1);

      stack.push(item1, item2, item3, item4);

      var iterator = stack.iterator(stack.forAll(), x -> x != item2);
      assertTrue(iterator.hasNext());
      assertSame(item4, iterator.next());
      assertTrue(iterator.hasNext());
      assertSame(item3, iterator.next());
      assertFalse(iterator.hasNext());
      assertThrows(NoSuchElementException.class, iterator::next);
    }
  }

  @Test
  void testIsEmpty() {
    try (MiStack<String> stack = this.makeStack()) {

      assertTrue(stack.isEmpty());

      var tags1 = tagsOf("hello", "world");
      var item1 = itemOf("item1", tags1);

      stack.push(item1);

      assertFalse(stack.isEmpty());
      stack.clear();
      assertTrue(stack.isEmpty());
    }
  }

  @Test
  void testIsEmptyForPredicate() {
    final MiStack<String> stack = this.makeStack();

    var tags1 = tagsOf("hello", "world");
    var tags2 = tagsOf("universe");

    var item1 = itemOf("item1", tags1);
    var item2 = itemOf("item2", tags1);
    var item3 = itemOf("item3", tags2);
    var item4 = itemOf("item4", tags1);

    stack.push(item1, item2, item3, item4);

    assertTrue(stack.isEmpty(MiStack.anyTag(tagOf("unknown"))));
    assertFalse(stack.isEmpty(MiStack.anyTag(tagOf("universe"))));
    assertFalse(stack.isEmpty(MiStack.allTags(tagOf("hello"), tagOf("world"))));
  }

  @Test
  void testName() {
    try (MiStack<String> stack = this.makeStack()) {
      assertNotNull(stack.getName());
    }
    try (MiStack<String> named = this.makeStack("hello")) {
      assertEquals("hello", named.getName());
    }
  }

  abstract MiStack<String> makeStack(String name);

  @Test
  void testRemove() {
    try (MiStack<String> stack = this.makeStack()) {

      var tags1 = tagsOf("hello", "world");
      var tags2 = tagsOf("universe");

      var item1 = itemOf("item1", tags1);
      var item2 = itemOf("item2", tags1);
      var item3 = itemOf("item3", tags2);
      var item4 = itemOf("item4", tags1);

      stack.push(item1, item2, item3, item4);

      assertEquals(item3,
          stack.remove(MiStack.anyTag(tagOf("universe")), 0).orElseThrow());
      assertArrayEquals(new MiStackItem[] {item4, item2, item1}, stack.stream().toArray());

      assertEquals(item2,
          stack.remove(MiStack.allTags(tagOf("hello"), tagOf("world")), 1)
              .orElseThrow());
      assertArrayEquals(new MiStackItem[] {item4, item1}, stack.stream().toArray());
    }
  }

  @Test
  void testIteratorAll() {
    try (MiStack<String> stack = this.makeStack()) {

      var tags1 = tagsOf("hello", "world");
      var tags2 = tagsOf("universe");

      var item1 = itemOf("item1", tags1);
      var item2 = itemOf("item2", tags1);
      var item3 = itemOf("item3", tags2);
      var item4 = itemOf("item4", tags1);

      stack.push(item1, item2, item3, item4);

      var iterator = stack.iterator();
      assertTrue(iterator.hasNext());
      assertSame(item4, iterator.next());

      assertTrue(iterator.hasNext());
      assertSame(item3, iterator.next());

      assertTrue(iterator.hasNext());
      assertSame(item2, iterator.next());

      assertTrue(iterator.hasNext());
      assertSame(item1, iterator.next());

      assertFalse(iterator.hasNext());
      assertThrows(NoSuchElementException.class, iterator::next);
    }
  }

  @Test
  void testIteratorForPredicate() {
    try (MiStack<String> stack = this.makeStack()) {

      var tags1 = tagsOf("hello", "world");
      var tags2 = tagsOf("universe");

      var item1 = itemOf("item1", tags1);
      var item2 = itemOf("item2", tags1);
      var item3 = itemOf("item3", tags2);
      var item4 = itemOf("item4", tags1);

      stack.push(item1, item2, item3, item4);

      var iterator = stack.iterator(MiStack.allTags(tagOf("hello"), tagOf("world")));
      assertTrue(iterator.hasNext());
      assertSame(item4, iterator.next());

      assertTrue(iterator.hasNext());
      assertSame(item2, iterator.next());

      assertTrue(iterator.hasNext());
      assertSame(item1, iterator.next());

      assertFalse(iterator.hasNext());
      assertThrows(NoSuchElementException.class, iterator::next);
    }
  }

  @Test
  void testIteratorForPredicateWithRemoveCall() {
    try (MiStack<String> stack = this.makeStack()) {

      var tags1 = tagsOf("hello", "world");
      var tags2 = tagsOf("universe");

      var item1 = itemOf("item1", tags1);
      var item2 = itemOf("item2", tags1);
      var item3 = itemOf("item3", tags2);
      var item4 = itemOf("item4", tags1);

      stack.push(item1, item2, item3, item4);

      var iterator = stack.iterator(MiStack.allTags(tagOf("hello"), tagOf("world")));
      assertTrue(iterator.hasNext());
      assertSame(item4, iterator.next());

      assertTrue(iterator.hasNext());
      assertSame(item2, iterator.next());
      iterator.remove();

      assertTrue(iterator.hasNext());
      assertSame(item1, iterator.next());

      assertFalse(iterator.hasNext());
      assertThrows(NoSuchElementException.class, iterator::next);

      assertArrayEquals(new MiStackItem[] {item4, item3, item1}, stack.stream().toArray());
    }
  }

  @Test
  void testPeek() {
    try (MiStack<String> stack = this.makeStack()) {

      var tags1 = tagsOf("hello", "world");
      var tags2 = tagsOf("universe");

      var item1 = itemOf("item1", tags1);
      var item2 = itemOf("item2", tags1);
      var item3 = itemOf("item3", tags2);
      var item4 = itemOf("item4", tags1);

      stack.push(item1, item2, item3, item4);

      assertEquals(item3,
          stack.peek(MiStack.anyTag(tagOf("universe")), 0).orElseThrow());
      assertTrue(stack.peek(MiStack.anyTag(tagOf("universe")), 1).isEmpty());
      assertEquals(item4,
          stack.peek(MiStack.allTags(tagOf("hello"), tagOf("world")), 0).orElseThrow());
      assertEquals(item2,
          stack.peek(MiStack.allTags(tagOf("hello"), tagOf("world")), 1).orElseThrow());
      assertEquals(item1,
          stack.peek(MiStack.allTags(tagOf("hello"), tagOf("world")), 2).orElseThrow());
    }
  }

  @Test
  void testClear() {
    try (MiStack<String> stack = this.makeStack()) {

      var tags1 = tagsOf("hello", "world");
      var tags2 = tagsOf("universe");

      var item1 = itemOf("item1", tags1);
      var item2 = itemOf("item2", tags1);
      var item3 = itemOf("item3", tags2);
      var item4 = itemOf("item4", tags1);

      stack.push(item1, item2, item3, item4);

      assertEquals(4L, stack.size());
      stack.clear();
      assertEquals(0L, stack.size());
    }
  }

  @Test
  void testClearForPredicate() {
    try (MiStack<String> stack = this.makeStack()) {

      var tags1 = tagsOf("hello", "world");
      var tags2 = tagsOf("universe");

      var item1 = itemOf("item1", tags1);
      var item2 = itemOf("item2", tags1);
      var item3 = itemOf("item3", tags2);
      var item4 = itemOf("item4", tags1);

      stack.push(item1, item2, item3, item4);

      assertEquals(4, stack.size());
      assertEquals(1L, stack.size(MiStack.anyTag(tagOf("universe"))));
      assertEquals(3L, stack.size(MiStack.allTags(tagOf("hello"), tagOf("world"))));

      stack.clear(MiStack.anyTag(tagOf("universe")));
      assertEquals(3, stack.size());
      assertEquals(0L, stack.size(MiStack.anyTag(tagOf("universe"))));
      assertEquals(3L, stack.size(MiStack.allTags(tagOf("hello"), tagOf("world"))));

      stack.clear(MiStack.allTags(tagOf("hello"), tagOf("world")));
      assertEquals(0L, stack.size(MiStack.allTags(tagOf("hello"), tagOf("world"))));
      assertEquals(0L, stack.size());
    }
  }

  @Test
  void testSize() {
    try (MiStack<String> stack = this.makeStack()) {

      var tags1 = tagsOf("hello", "world");
      var tags2 = tagsOf("universe");

      var item1 = itemOf("item1", tags1);
      var item2 = itemOf("item2", tags1);
      var item3 = itemOf("item3", tags2);
      var item4 = itemOf("item4", tags1);

      stack.push(item1, item2, item3, item4);

      assertEquals(4, stack.size());
      assertEquals(1L, stack.size(MiStack.anyTag(tagOf("universe"))));
      assertEquals(3L, stack.size(MiStack.allTags(tagOf("hello"), tagOf("world"))));
    }
  }

  @Test
  void testPushPopSingleElement() {
    try (MiStack<String> stack = this.makeStack()) {

      var item = itemOf("Test", tagsOf("Hello", "World"));

      assertTrue(stack.isEmpty());
      assertSame(stack, stack.push(item));

      assertEquals(1, stack.size());
      assertEquals(1, stack.size(stack.forAll()));

      var poppedItem = stack.pop(stack.forAll());

      assertSame(item, poppedItem.orElseThrow());

      assertEquals(0, stack.size());
    }
  }

  @Test
  void testStreamAllElements() {
    try (MiStack<String> stack = this.makeStack()) {

      var tags = tagsOf("hello", "world");
      var item1 = itemOf("item1", tags);
      var item2 = itemOf("item2", tags);
      var item3 = itemOf("item3", tags);
      var item4 = itemOf("item4", tags);

      assertSame(stack, stack.push(item1, item2, item3, item4));

      assertArrayEquals(new MiStackItem[] {item4, item3, item2, item1}, stack.stream().toArray());
    }
  }

  @Test
  void testStreamAllElementsWithTakeWhile() {
    try (MiStack<String> stack = this.makeStack()) {

      var tags = tagsOf("hello", "world");
      var item1 = itemOf("item1", tags);
      var item2 = itemOf("item2", tags);
      var item3 = itemOf("item3", tags);
      var item4 = itemOf("item4", tags);

      assertSame(stack, stack.push(item1, item2, item3, item4));

      assertArrayEquals(new MiStackItem[] {item4, item3},
          stack.stream(stack.forAll(), e -> e != item2).toArray());
    }
  }

  @Test
  void testStreamForPredicate() {
    try (MiStack<String> stack = this.makeStack()) {

      var tags = tagsOf("hello", "world");
      var item1 = itemOf("item1", tags);
      var item2 = itemOf("item2", tags);
      var item3 = itemOf("item3", tags);
      var item4 = itemOf("item4", tags);

      stack.push(item1, item2, item3, item4);

      assertArrayEquals(new MiStackItem[] {item4, item3, item2, item1},
          stack.stream(MiStack.allTags(Set.of())).toArray());
      assertArrayEquals(new MiStackItem[] {item4, item3, item2, item1},
          stack.stream(MiStack.anyTag(tagOf("hello"))).toArray());
      assertEquals(0, stack.stream(MiStack.anyTag(tagOf("universe"))).toArray().length);
    }
  }

}