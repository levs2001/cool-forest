package ru.leo.forest.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class ByteMapImpl<T> implements Int2ObjectMap<T> {
    private final T[] values = (T[]) new Object[128];
    private final IntSet keys = new IntArraySet();

    @Override
    public T put(int key, T value) {
        values[key] = value;
        keys.add(key);
        return null;
    }

    @Override
    public T get(int key) {
        return values[key];
    }

    @Override
    public int size() {
        return keys.size();
    }

    @Override
    public boolean isEmpty() {
        return keys.isEmpty();
    }

    @Override
    @NotNull
    public IntSet keySet() {
        return keys;
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(@NotNull Map<? extends Integer, ? extends T> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void defaultReturnValue(T rv) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T defaultReturnValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ObjectSet<Entry<T>> int2ObjectEntrySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ObjectCollection<T> values() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsKey(int key) {
        throw new UnsupportedOperationException();
    }
}
