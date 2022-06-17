package com.igormaznitsa.mistack.impl;

import static java.util.Objects.requireNonNull;

import com.igormaznitsa.mistack.MiStack;
import com.igormaznitsa.mistack.MiStackItem;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public final class MiStackIterator<T> implements Iterator<MiStackItem<T>> {
    private final Iterator<MiStackItem<T>> delegate;
    private final Predicate<MiStackItem<T>> filter;
    private final Predicate<MiStackItem<T>> takeWhile;

    private final MiStack<T> stack;
    private boolean completed;
    private MiStackItem<T> item;
    private MiStackItem<T> itemForRemove;

    public MiStackIterator(
        final MiStack<T> stack,
        final Iterator<MiStackItem<T>> delegate,
        final Predicate<MiStackItem<T>> filter,
        final Predicate<MiStackItem<T>> takeWhile
    ) {
        this.stack = requireNonNull(stack);
        this.delegate = requireNonNull(delegate);
        this.filter = requireNonNull(filter);
        this.takeWhile = requireNonNull(takeWhile);
    }

    public Predicate<MiStackItem<T>> getFilter() {
        return filter;
    }

    public Predicate<MiStackItem<T>> getTakeWhile() {
        return takeWhile;
    }

    public Iterator<MiStackItem<T>> getDelegate() {
        return this.delegate;
    }

    @Override
    public boolean hasNext() {
        if (this.completed) {
            return false;
        }

        if (this.stack.isClosed()) {
            this.completed = true;
            this.item = null;
            this.itemForRemove = null;
            return false;
        }

        if (this.item == null) {
            this.item = this.findNext();
            this.itemForRemove = null;
        }

        return this.item != null;
    }

    private MiStackItem<T> findNext() {
        if (this.completed) {
            return null;
        }

        MiStackItem<T> result = null;
        while (!this.completed && this.delegate.hasNext() && result == null) {
            var item = this.delegate.next();
            if (this.filter.test(item)) {
                if (this.takeWhile.test(item)) {
                    result = item;
                } else {
                    this.completed = true;
                }
            }
        }
        return result;
    }

    @Override
    public MiStackItem<T> next() {
        if (this.isSourceClosed()) {
            throw new IllegalStateException();
        }

        if (this.completed) {
            throw new NoSuchElementException();
        }

        if (this.item == null) {
            this.item = this.findNext();
            if (this.item == null) {
                throw new NoSuchElementException();
            }
            this.itemForRemove = this.item;
        } else {
            this.itemForRemove = this.item;
        }

        var result = this.item;
        this.item = null;
        return result;
    }

    private boolean isSourceClosed() {
        boolean result = false;
        if (this.stack.isClosed()) {
            this.completed = true;
            this.item = null;
            this.itemForRemove = null;
            result = true;
        }
        return result;
    }

    @Override
    public void remove() {
        if (this.isSourceClosed() || this.itemForRemove == null) {
            throw new IllegalStateException();
        }

        this.delegate.remove();
        this.itemForRemove = null;
        this.item = null;
    }
}
