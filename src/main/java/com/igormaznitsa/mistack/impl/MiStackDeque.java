package com.igormaznitsa.mistack.impl;

import static java.util.Objects.requireNonNull;

import com.igormaznitsa.mistack.MiStack;
import com.igormaznitsa.mistack.MiStackItem;
import java.util.Deque;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public class MiStackDeque<T> implements MiStack<T> {

    private final Deque<MiStackItem<T>> deque;
    private final String name;
    private final AtomicBoolean closed = new AtomicBoolean();

    public MiStackDeque(final Deque<MiStackItem<T>> deque) {
        this(UUID.randomUUID().toString(), deque);
    }

    public MiStackDeque(final String name, final Deque<MiStackItem<T>> deque) {
        this.name = requireNonNull(name);
        this.deque = requireNonNull(deque);
    }

    @Override
    public MiStack<T> push(final MiStackItem<T> item) {
        this.assertNotClosed();
        this.deque.addFirst(requireNonNull(item));
        return this;
    }

    @Override
    public Optional<MiStackItem<T>> pop(final Predicate<MiStackItem<T>> predicate) {
        this.assertNotClosed();

        MiStackItem<T> result = null;
        var iterator = this.deque.iterator();
        while (iterator.hasNext() && result == null) {
            var item = iterator.next();
            if (predicate.test(item)) {
                iterator.remove();
                result = item;
            }
        }

        return Optional.ofNullable(result);
    }

    @Override
    public Iterator<MiStackItem<T>> iterator(final Predicate<MiStackItem<T>> filter,
                                             final Predicate<MiStackItem<T>> takeWhile) {
        return new MiStackIterator<>(this, this.deque.iterator(), filter, takeWhile);
    }

    @Override
    public boolean isClosed() {
        return this.closed.get();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void clear() {
        this.assertNotClosed();
        this.deque.clear();
    }

    @Override
    public boolean isEmpty() {
        this.assertNotClosed();
        return this.deque.isEmpty();
    }

    @Override
    public long size() {
        this.assertNotClosed();
        return this.deque.size();
    }

    @Override
    public void close() {
        if (this.closed.compareAndSet(false, true)) {
            this.deque.clear();
        } else {
            this.assertNotClosed();
        }
    }
}
