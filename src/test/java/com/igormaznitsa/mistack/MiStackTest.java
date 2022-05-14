package com.igormaznitsa.mistack;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MiStackTest {

  @Test
  public void testPushPop() {
    final MiStack stack = new MiStack();

    var item = MiStackItemImpl.itemOf("Test", MiStackTagImpl.tagsOf("Hello", "World"));

    Assertions.assertTrue(stack.isEmpty());
    stack.push(item);

    Assertions.assertEquals(1, stack.size());
    Assertions.assertEquals(1, stack.size(Predicates.ALL));

    var poppedItem = stack.pop(Predicates.ALL);

    Assertions.assertSame(item, poppedItem.orElseThrow());

    Assertions.assertEquals(0, stack.size());
  }

  public static final class MiStackItemImpl implements MiStackItem {
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
  }

  public static final class MiStackTagImpl implements MiStackTag {
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
  }

}