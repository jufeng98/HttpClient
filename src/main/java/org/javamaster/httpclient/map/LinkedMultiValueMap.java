package org.javamaster.httpclient.map;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Simple implementation of {@link MultiValueMap} that wraps a {@link LinkedHashMap},
 * storing multiple values in a {@link LinkedList}.
 *
 * <p>This Map implementation is generally not thread-safe. It is primarily designed
 * for data structures exposed from request objects, for use in a single thread only.
 *
 * @param <K> the key type
 * @param <V> the value element type
 * @author Arjen Poutsma
 * @author Juergen Hoeller
 * @since 3.0
 */
public class LinkedMultiValueMap<K, V> implements MultiValueMap<K, V>, Serializable, Cloneable {

    @Serial
    private static final long serialVersionUID = 3801124242820219131L;

    private final Map<K, List<V>> targetMap;


    /**
     * Create a new LinkedMultiValueMap that wraps a {@link LinkedHashMap}.
     */
    public LinkedMultiValueMap() {
        targetMap = new LinkedHashMap<>();
    }

    /**
     * Create a new LinkedMultiValueMap that wraps a {@link LinkedHashMap}
     * with the given initial capacity.
     *
     * @param initialCapacity the initial capacity
     */
    public LinkedMultiValueMap(int initialCapacity) {
        targetMap = new LinkedHashMap<>(initialCapacity);
    }

    /**
     * Copy constructor: Create a new LinkedMultiValueMap with the same mappings as
     * the specified Map. Note that this will be a shallow copy; its value-holding
     * List entries will get reused and therefore cannot get modified independently.
     *
     * @param otherMap the Map whose mappings are to be placed in this Map
     * @see #clone()
     * @see #deepCopy()
     */
    public LinkedMultiValueMap(Map<K, List<V>> otherMap) {
        targetMap = new LinkedHashMap<>(otherMap);
    }


    // MultiValueMap implementation

    @Override
    @Nullable
    public V getFirst(K key) {
        List<V> values = targetMap.get(key);
        return (values != null && !values.isEmpty() ? values.get(0) : null);
    }

    @Override
    public void add(K key, @Nullable V value) {
        List<V> values = targetMap.computeIfAbsent(key, k -> new LinkedList<>());
        values.add(value);
    }

    @Override
    public void addAll(K key, List<? extends V> values) {
        List<V> currentValues = targetMap.computeIfAbsent(key, k -> new LinkedList<>());
        currentValues.addAll(values);
    }

    @Override
    public void addAll(MultiValueMap<K, V> values) {
        for (Entry<K, List<V>> entry : values.entrySet()) {
            addAll(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void set(K key, @Nullable V value) {
        List<V> values = new LinkedList<>();
        values.add(value);
        targetMap.put(key, values);
    }

    @Override
    public void setAll(Map<K, V> values) {
        values.forEach(this::set);
    }

    @Override
    public Map<K, V> toSingleValueMap() {
        LinkedHashMap<K, V> singleValueMap = new LinkedHashMap<>(targetMap.size());
        targetMap.forEach((key, values) -> {
            if (values != null && !values.isEmpty()) {
                singleValueMap.put(key, values.get(0));
            }
        });
        return singleValueMap;
    }


    // Map implementation

    @Override
    public int size() {
        return targetMap.size();
    }

    @Override
    public boolean isEmpty() {
        return targetMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return targetMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return targetMap.containsValue(value);
    }

    @Override
    @Nullable
    public List<V> get(Object key) {
        return targetMap.get(key);
    }

    @Override
    @Nullable
    public List<V> put(K key, List<V> value) {
        return targetMap.put(key, value);
    }

    @Override
    @Nullable
    public List<V> remove(Object key) {
        return targetMap.remove(key);
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends List<V>> map) {
        targetMap.putAll(map);
    }

    @Override
    public void clear() {
        targetMap.clear();
    }

    @Override
    public @NotNull Set<K> keySet() {
        return targetMap.keySet();
    }

    @Override
    public @NotNull Collection<List<V>> values() {
        return targetMap.values();
    }

    @Override
    public @NotNull Set<Entry<K, List<V>>> entrySet() {
        return targetMap.entrySet();
    }


    /**
     * Create a deep copy of this Map.
     *
     * @return a copy of this Map, including a copy of each value-holding List entry
     * (consistently using an independent modifiable {@link LinkedList} for each entry)
     * along the lines of {@code MultiValueMap.addAll} semantics
     * @see #addAll(MultiValueMap)
     * @see #clone()
     * @since 4.2
     */
    public LinkedMultiValueMap<K, V> deepCopy() {
        LinkedMultiValueMap<K, V> copy = new LinkedMultiValueMap<>(targetMap.size());
        targetMap.forEach((key, value) -> copy.put(key, new LinkedList<>(value)));
        return copy;
    }

    /**
     * Create a regular copy of this Map.
     *
     * @return a shallow copy of this Map, reusing this Map's value-holding List entries
     * (even if some entries are shared or unmodifiable) along the lines of standard
     * {@code Map.put} semantics
     * @see #put(Object, List)
     * @see #putAll(Map)
     * @see LinkedMultiValueMap#LinkedMultiValueMap(Map)
     * @see #deepCopy()
     * @since 4.2
     */
    @Override
    public LinkedMultiValueMap<K, V> clone() {
        return new LinkedMultiValueMap<>(this);
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj instanceof LinkedMultiValueMap<?, ?>)) {
            return false;
        }

        return targetMap.equals(obj);
    }

    @Override
    public int hashCode() {
        return targetMap.hashCode();
    }

    @Override
    public String toString() {
        return targetMap.toString();
    }

}
