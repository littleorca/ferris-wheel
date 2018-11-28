package com.ctrip.ferriswheel.core.intf;

import java.util.List;
import java.util.Map;

public interface MultiValueMap<K, V> extends Map<K, List<V>> {
    V getFirst(K key);

    List<V> add(K key, V value);

    List<V> set(K key, V value);

    void setAll(Map<K, V> values);
}
