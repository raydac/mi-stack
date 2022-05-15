package com.igormaznitsa.mistack;

import static com.igormaznitsa.mistack.MiStackTest.MiStackItemImpl.itemOf;
import static com.igormaznitsa.mistack.MiStackTest.MiStackTagImpl.tagOf;
import static com.igormaznitsa.mistack.MiStackTest.MiStackTagImpl.tagsOf;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MiStackTest {

  @Test
  void testIsEmpty() {
    final MiStack stack = new MiStack();

    assertTrue(stack.isEmpty());

    var tags1 = tagsOf("hello", "world");
    var item1 = itemOf("item1", tags1);

    stack.push(item1);

    assertFalse(stack.isEmpty());
    stack.clear();
    assertTrue(stack.isEmpty());
  }

  @Test
  void testIsEmptyForPredicate() {
    final MiStack stack = new MiStack();

    var tags1 = tagsOf("hello", "world");
    var tags2 = tagsOf("universe");

    var item1 = itemOf("item1", tags1);
    var item2 = itemOf("item2", tags1);
    var item3 = itemOf("item3", tags2);
    var item4 = itemOf("item4", tags1);

    stack.push(item1, item2, item3, item4);

    assertTrue(stack.isEmpty(Predicates.anyTag(tagOf("unknown"))));
    assertFalse(stack.isEmpty(Predicates.anyTag(tagOf("universe"))));
    assertFalse(stack.isEmpty(Predicates.allTags(tagOf("hello"), tagOf("world"))));
  }

  @Test
  void testName() {
    assertNotNull(new MiStack().getName());
    assertEquals("hello", new MiStack("hello").getName());
  }

  @Test
  void testRemove() {
    final MiStack stack = new MiStack();

    var tags1 = tagsOf("hello", "world");
    var tags2 = tagsOf("universe");

    var item1 = itemOf("item1", tags1);
    var item2 = itemOf("item2", tags1);
    var item3 = itemOf("item3", tags2);
    var item4 = itemOf("item4", tags1);

    stack.push(item1, item2, item3, item4);

    assertEquals(item3,
        stack.remove(Predicates.anyTag(tagOf("universe")), 0).orElseThrow());
    assertArrayEquals(new MiStackItem[] {item4, item2, item1}, stack.stream().toArray());

    assertEquals(item2,
        stack.remove(Predicates.allTags(tagOf("hello"), tagOf("world")), 1)
            .orElseThrow());
    assertArrayEquals(new MiStackItem[] {item4, item1}, stack.stream().toArray());
  }

  @Test
  void testIteratorAll() {
    final MiStack stack = new MiStack();

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

  @Test
  void testIteratorForPredicate() {
    final MiStack stack = new MiStack();

    var tags1 = tagsOf("hello", "world");
    var tags2 = tagsOf("universe");

    var item1 = itemOf("item1", tags1);
    var item2 = itemOf("item2", tags1);
    var item3 = itemOf("item3", tags2);
    var item4 = itemOf("item4", tags1);

    stack.push(item1, item2, item3, item4);

    var iterator = stack.iterator(Predicates.allTags(tagOf("hello"), tagOf("world")));
    assertTrue(iterator.hasNext());
    assertSame(item4, iterator.next());

    assertTrue(iterator.hasNext());
    assertSame(item2, iterator.next());

    assertTrue(iterator.hasNext());
    assertSame(item1, iterator.next());

    assertFalse(iterator.hasNext());
    assertThrows(NoSuchElementException.class, iterator::next);
  }

  @Test
  void testIteratorForPredicateWithRemoveCall() {
    final MiStack stack = new MiStack();

    var tags1 = tagsOf("hello", "world");
    var tags2 = tagsOf("universe");

    var item1 = itemOf("item1", tags1);
    var item2 = itemOf("item2", tags1);
    var item3 = itemOf("item3", tags2);
    var item4 = itemOf("item4", tags1);

    stack.push(item1, item2, item3, item4);

    var iterator = stack.iterator(Predicates.allTags(tagOf("hello"), tagOf("world")));
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

  @Test
  void testPeek() {
    final MiStack stack = new MiStack();

    var tags1 = tagsOf("hello", "world");
    var tags2 = tagsOf("universe");

    var item1 = itemOf("item1", tags1);
    var item2 = itemOf("item2", tags1);
    var item3 = itemOf("item3", tags2);
    var item4 = itemOf("item4", tags1);

    stack.push(item1, item2, item3, item4);

    assertEquals(item3,
        stack.peek(Predicates.anyTag(tagOf("universe")), 0).orElseThrow());
    assertTrue(stack.peek(Predicates.anyTag(tagOf("universe")), 1).isEmpty());
    assertEquals(item4,
        stack.peek(Predicates.allTags(tagOf("hello"), tagOf("world")), 0).orElseThrow());
    assertEquals(item2,
        stack.peek(Predicates.allTags(tagOf("hello"), tagOf("world")), 1).orElseThrow());
    assertEquals(item1,
        stack.peek(Predicates.allTags(tagOf("hello"), tagOf("world")), 2).orElseThrow());
  }

  @Test
  void testClear() {
    final MiStack stack = new MiStack();

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

  @Test
  void testClearForPredicate() {
    final MiStack stack = new MiStack();

    var tags1 = tagsOf("hello", "world");
    var tags2 = tagsOf("universe");

    var item1 = itemOf("item1", tags1);
    var item2 = itemOf("item2", tags1);
    var item3 = itemOf("item3", tags2);
    var item4 = itemOf("item4", tags1);

    stack.push(item1, item2, item3, item4);

    assertEquals(4, stack.size());
    assertEquals(1L, stack.size(Predicates.anyTag(tagOf("universe"))));
    assertEquals(3L, stack.size(Predicates.allTags(tagOf("hello"), tagOf("world"))));

    stack.clear(Predicates.anyTag(tagOf("universe")));
    assertEquals(3, stack.size());
    assertEquals(0L, stack.size(Predicates.anyTag(tagOf("universe"))));
    assertEquals(3L, stack.size(Predicates.allTags(tagOf("hello"), tagOf("world"))));

    stack.clear(Predicates.allTags(tagOf("hello"), tagOf("world")));
    assertEquals(0L, stack.size(Predicates.allTags(tagOf("hello"), tagOf("world"))));
    assertEquals(0L, stack.size());
  }

  @Test
  void testSize() {
    final MiStack stack = new MiStack();

    var tags1 = tagsOf("hello", "world");
    var tags2 = tagsOf("universe");

    var item1 = itemOf("item1", tags1);
    var item2 = itemOf("item2", tags1);
    var item3 = itemOf("item3", tags2);
    var item4 = itemOf("item4", tags1);

    stack.push(item1, item2, item3, item4);

    assertEquals(4, stack.size());
    assertEquals(1L, stack.size(Predicates.anyTag(tagOf("universe"))));
    assertEquals(3L, stack.size(Predicates.allTags(tagOf("hello"), tagOf("world"))));
  }

  @Test
  void testPushPopSingleElement() {
    final MiStack stack = new MiStack();

    var item = itemOf("Test", tagsOf("Hello", "World"));

    assertTrue(stack.isEmpty());
    assertSame(stack, stack.push(item));

    assertEquals(1, stack.size());
    assertEquals(1, stack.size(Predicates.ALL_TAGS));

    var poppedItem = stack.pop(Predicates.ALL_TAGS);

    assertSame(item, poppedItem.orElseThrow());

    assertEquals(0, stack.size());
  }

  @Test
  void testStreamAllElements() {
    final MiStack stack = new MiStack();

    var tags = tagsOf("hello", "world");
    var item1 = itemOf("item1", tags);
    var item2 = itemOf("item2", tags);
    var item3 = itemOf("item3", tags);
    var item4 = itemOf("item4", tags);

    assertSame(stack, stack.push(item1, item2, item3, item4));

    assertArrayEquals(new MiStackItem[] {item4, item3, item2, item1}, stack.stream().toArray());
  }

  @Test
  void testStreamForPredicate() {
    final MiStack stack = new MiStack();

    var tags = tagsOf("hello", "world");
    var item1 = itemOf("item1", tags);
    var item2 = itemOf("item2", tags);
    var item3 = itemOf("item3", tags);
    var item4 = itemOf("item4", tags);

    stack.push(item1, item2, item3, item4);

    assertArrayEquals(new MiStackItem[] {item4, item3, item2, item1},
        stack.stream(Predicates.ALL_TAGS).toArray());
    assertArrayEquals(new MiStackItem[] {item4, item3, item2, item1},
        stack.stream(Predicates.anyTag(tagOf("hello"))).toArray());
    assertEquals(0, stack.stream(Predicates.anyTag(tagOf("universe"))).toArray().length);
  }

  static final class MiStackItemImpl implements MiStackItem {
    private final Object value;
    private final Set<MiStackTag> tags;

    private MiStackItemImpl(final Object value, final Set<MiStackTag> tags) {
      this.value = Objects.requireNonNull(value);
      this.tags = Objects.requireNonNull(tags);
    }

    public static MiStackItemImpl itemOf(final Object value, final Set<MiStackTag> tags) {
      return new MiStackItemImpl(value, tags);
    }

    @Override
    public Set<MiStackTag> getTags() {
      return this.tags;
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue() {
      return (T) this.value;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }
      final MiStackItemImpl that = (MiStackItemImpl) obj;
      return this.value.equals(that.value) && this.tags.equals(that.tags);
    }

    @Override
    public int hashCode() {
      return Objects.hash(value, tags);
    }

    @Override
    public String toString() {
      return this.value.toString() + ' ' + this.tags;
    }
  }

  static final class MiStackTagImpl implements MiStackTag {
    private final String text;

    private MiStackTagImpl(final String text) {
      this.text = Objects.requireNonNull(text);
    }

    public static Set<MiStackTag> tagsOf(final String... tags) {
      final Set<MiStackTag> set = new HashSet<>();
      for (final String s : tags) {
        set.add(tagOf(s));
      }
      return Set.copyOf(set);
    }

    public static MiStackTagImpl tagOf(final String text) {
      return new MiStackTagImpl(text);
    }

    @Override
    public boolean equals(final Object that) {
      if (this == that) {
        return true;
      }
      if (that == null || getClass() != that.getClass()) {
        return false;
      }
      return this.text.equals(((MiStackTagImpl) that).text);
    }

    @Override
    public int hashCode() {
      return this.text.hashCode();
    }

    @Override
    public String toString() {
      return this.text;
    }
  }

}