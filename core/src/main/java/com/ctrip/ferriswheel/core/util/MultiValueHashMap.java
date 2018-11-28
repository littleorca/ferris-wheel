package com.ctrip.ferriswheel.core.util;

import com.ctrip.ferriswheel.core.intf.MultiValueMap;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MultiValueHashMap<K, V> extends HashMap<K, List<V>> implements MultiValueMap<K, V> {
    @Override
    public V getFirst(K key) {
        List<V> values = get(key);
        return (values == null || values.isEmpty()) ? null : values.get(0);
    }

    @Override
    public List<V> add(K key, V value) {
        List<V> values = get(key);
        if (values == null) {
            return set(key, value);
        }
        values.add(value);
        return values;
    }

    @Override
    public List<V> set(K key, V value) {
        List<V> values = new LinkedList<>();
        values.add(value);
        return put(key, values);
    }

    @Override
    public void setAll(Map<K, V> values) {
        clear();
        for (Map.Entry<K, V> entry : values.entrySet()) {
            set(entry.getKey(), entry.getValue());
        }
    }
}
