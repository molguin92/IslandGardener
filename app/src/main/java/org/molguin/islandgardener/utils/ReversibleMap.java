/*
 *     Copyright © 2020 Manuel Olguín Muñoz <manuel@olguin.se>
 *
 *     ReversibleMap.java is part of Island Gardener
 *
 *     Island Gardener is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Island Gardener is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Island Gardener.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.molguin.islandgardener.utils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class ReversibleMap<K extends Comparable, V extends Comparable> extends ConcurrentHashMap<K, V> {
    private final ConcurrentMap<V, Set<K>> revmap = new ConcurrentHashMap<V, Set<K>>();

    public Set<K> getKeysForValue(V v) {
        return new HashSet<K>(this.revmap.get(v));
    }

    @Override
    public V put(K k, V v) {
        V result = super.put(k, v);
        this.putIntoRevMap(k, v);
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

    private void removeFromRevMap(K k, V v) {
        Set<K> revResult = this.revmap.get(v);
        revResult.remove(k);
        if (revResult.isEmpty()) this.revmap.remove(v);
        else this.revmap.put(v, revResult);
    }

    private void putIntoRevMap(K k, V v) {
        Set<K> kset = this.revmap.get(v);
        if (kset == null) kset = new ConcurrentSkipListSet<K>();
        kset.add(k);
        this.revmap.put(v, kset);
    }


    @Override
    public V remove(Object k) {
        V result = super.remove(k);
        if (result != null)
            this.removeFromRevMap((K) k, result);
        return result;
    }

    @Override
    public boolean remove(Object k, Object v) {
        boolean result = super.remove(k, v);
        if (result)
            this.removeFromRevMap((K) k, (V) v);
        return result;
    }

    @Override
    public V replace(K k, V v) {
        V old = super.replace(k, v);
        if (old != null) {
            this.removeFromRevMap(k, old);
            this.putIntoRevMap(k, v);
        }
        return old;
    }

    @Override
    public boolean replace(K k, V o, V n) {
        boolean replaced = super.replace(k, o, n);
        if (replaced) {
            this.removeFromRevMap(k, o);
            this.putIntoRevMap(k, n);
        }
        return replaced;
    }
}
