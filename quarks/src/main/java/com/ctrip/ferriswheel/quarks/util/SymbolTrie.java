package com.ctrip.ferriswheel.quarks.util;

import java.io.Serializable;
import java.util.List;

/**
 * Simple trie implementation, aim for keywords/operators/delimiters match,
 * which won't be a huge set.
 * <p>
 * This implementation use single linked list for sibling entries, and parents
 * will only hold a reference to the first child entry. When token amount is not
 * big, this design should works well.
 * 
 * @author liuhaifeng
 * 
 * @param <K>
 * @param <V>
 */
public class SymbolTrie<K, V> implements Serializable {
    private static final long serialVersionUID = 1L;

    public static class Entry<K, V> implements Serializable {
        private static final long serialVersionUID = 1L;

        K key;
        V value;
        int depth;
        Entry<K, V> nextSibling;
        Entry<K, V> firstChild;

        Entry(K key) {
            this.key = key;
            this.depth = 0;
        }

        Entry(K key, V value) {
            this.key = key;
            this.value = value;
            this.depth = 0;
        }

        Entry(K key, V value, int depth) {
            this.key = key;
            this.value = value;
            this.depth = depth;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public int getDepth() {
            return depth;
        }
    }

    private int size;
    private Entry<K, V> root;

    public SymbolTrie() {
        this.root = new Entry<K, V>(null);
    }

    /**
     * Add key value pair.
     * 
     * @param keyList
     * @param value
     * @return old value associated with the key, or null if the key didn't
     *         exist before.
     */
    public V put(List<K> keyList, V value) {
        Entry<K, V> entry = root;
        for (int i = 0; i < keyList.size(); i++) {
            K key = keyList.get(i);
            if (entry.firstChild == null) {
                entry.firstChild = new Entry<K, V>(key, null, i + 1);
                entry = entry.firstChild;
                continue;

            } else {
                entry = entry.firstChild;
            }

            while (!entry.key.equals(key)) {
                if (entry.nextSibling != null) {
                    entry = entry.nextSibling;

                } else {
                    entry.nextSibling = new Entry<K, V>(key, null, i + 1);
                    entry = entry.nextSibling;
                    break;
                }
            }
        }

        V old = entry.value;
        entry.value = value;

        if (old == null)
            size++;

        return old;
    }

    /**
     * Get value by exact key.
     * 
     * @param keyList
     * @return
     */
    public V get(List<K> keyList) {
        return get(keyList, 0, keyList.size());
    }

    /**
     * Get value by exact key.
     * 
     * @param keyList
     * @param begin
     * @param end
     * @return
     */
    public V get(List<K> keyList, int begin, int end) {
        Entry<K, V> entry = root;
        for (int i = begin; i < end; i++) {
            K key = keyList.get(i);
            if (entry.firstChild == null) {
                return null;

            } else {
                entry = entry.firstChild;
            }

            while (!entry.key.equals(key)) {
                if (entry.nextSibling != null) {
                    entry = entry.nextSibling;

                } else {
                    return null;
                }
            }
        }

        return entry.value;
    }

    /**
     * Find the max matches using lower case. Note that when put key-value pairs
     * to the trie, the original key will be used, if you want a
     * case-insensitive match, you should pre-process the key to lower case.
     * 
     * @param keyList
     * @param pos
     * @return
     * @see {@link #matches(List<K>, int, boolean)}
     */
    public Entry<K, V> matches(List<K> keyList, int pos) {
        return matches(keyList, pos, true);
    }

    /**
     * Find matches using lower case. Note that when put key-value pairs to the
     * trie, the original key will be used, if you want a case-insensitive
     * match, you should pre-process the key to lower case.
     * 
     * @param keyList
     * @param pos
     * @param maxMode
     * @return
     */
    public Entry<K, V> matches(List<K> keyList, int pos, boolean maxMode) {
        Entry<K, V> entry = root;
        Entry<K, V> result = null;

        for (int i = pos; i < keyList.size(); i++) {
            K key = keyList.get(i);

            if (entry.firstChild == null) {
                return result;

            } else {
                entry = entry.firstChild;
            }

            while (!entry.key.equals(key)) {
                if (entry.nextSibling != null) {
                    entry = entry.nextSibling;

                } else {
                    return result;
                }
            }

            if (entry.value != null) {
                result = entry;
                if (!maxMode)
                    return result;
            }
        }

        return result;
    }

    /**
     * Get number of entries.
     * 
     * @return
     */
    public int size() {
        return size;
    }

    /**
     * Clear all entries.
     */
    public void clear() {
        root.firstChild = null;
        size = 0;
    }

}
