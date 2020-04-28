package org.molguin.utils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class ReversibleMap<K extends Comparable, V extends Comparable> extends ConcurrentHashMap<K, V> {
    private final ConcurrentMap<V, Set<K>> revmap = new ConcurrentHashMap<>();

    @Override
    public V put(K k, V v) {
        V result = super.put(k, v);
        Set<K> kset = this.revmap.get(v);
        if (kset == null) kset = new ConcurrentSkipListSet<>();
        kset.add(k);
        this.revmap.put(v, kset);
        return result;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        for (Entry<? extends K, ? extends V> e : map.entrySet()) {
            this.put(e.getKey(), e.getValue());
        }
    }

    @Override
    public V putIfAbsent(K k, V v) {
        if (!this.contains(k)) return this.put(k, v);
        else return null;
    }

    public Set<K> getKeysForValue(V v) {
        return new HashSet<>(this.revmap.get(v));
    }
}
