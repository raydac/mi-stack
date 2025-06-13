package com.igormaznitsa.mistack;

import static com.igormaznitsa.mistack.MiStack.allTags;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.igormaznitsa.mistack.impl.MiStackConcurrent;
import com.igormaznitsa.mistack.impl.MiStackItemImpl;
import com.igormaznitsa.mistack.impl.MiStackTagImpl;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

public class MiStackConcurrentTest extends AbstractMiStackTest {
  @Override
  MiStack<String, MiStackItem<String>> makeStack() {
    return new MiStackConcurrent<>();
  }

  @Override
  MiStack<String, MiStackItem<String>> makeStack(final String name) {
    return new MiStackConcurrent<>(name);
  }

  @Test
  public void testConcurrentUse() {
    final int ELEMENTS = 500_000;
    final int THREADS = 32;

    try (final MiStack<Integer, MiStackItem<Integer>> stack = new MiStackConcurrent<>()) {

      var latch = new CountDownLatch(THREADS);
      var barrier = new CyclicBarrier(THREADS);

      final AtomicLong successful = new AtomicLong();

      final Runnable monkey = () -> {
        final Set<MiStackTag> tag = MiStackTagImpl.tagsOf(Thread.currentThread().getName());
        assertEquals(0, stack.size(allTags(tag)));
        IntStream.range(0, ELEMENTS).forEach(x -> {
          if (System.nanoTime() % 2 == 0L) {
            Thread.yield();
          }
          stack.push(MiStackItemImpl.itemOf(x, tag));
        });

        var aloneItem = MiStackItemImpl.itemOf(92837432, tag);
        stack.push(aloneItem);
        assertSame(aloneItem, stack.pop(allTags(tag)).orElseThrow());

        assertEquals(ELEMENTS, stack.size(allTags(tag)));
        assertTrue(stack.size() >= ELEMENTS);

        var iterator = stack.iterator(allTags(tag));
        for (int i = ELEMENTS - 1; i >= 0; i--) {
          assertTrue(iterator.hasNext());
          assertEquals(i, iterator.next().getValue());
          if (System.nanoTime() % 2 == 0L) {
            Thread.yield();
          }
          iterator.remove();
        }
        assertFalse(iterator.hasNext());

        assertEquals(0, stack.size(allTags(tag)));
        assertTrue(stack.isEmpty(allTags(tag)));
      };

      for (int i = 0; i < THREADS; i++) {
        final Thread testThread = new Thread(() -> {
          try {
            barrier.await();
            monkey.run();
            successful.incrementAndGet();
          } catch (Throwable ex) {
            ex.printStackTrace();
          } finally {
            latch.countDown();
          }
        }, "test-thread-" + i);
        testThread.setDaemon(true);
        testThread.start();
      }

      try {
        latch.await();
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      }

      assertEquals(THREADS, successful.get());
    }
  }

}
