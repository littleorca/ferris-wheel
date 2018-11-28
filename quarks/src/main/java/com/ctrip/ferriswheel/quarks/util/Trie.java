package com.ctrip.ferriswheel.quarks.util;

import java.io.Serializable;

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
 * @param <T>
 */
public class Trie<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    public static class Entry<T> implements Serializable {
        private static final long serialVersionUID = 1L;

        char ch;
        String key;
        T value;
        Entry<T> nextSibling;
        Entry<T> firstChild;

        Entry(char ch) {
            this.ch = ch;
        }

        Entry(String key, int pos) {
            this.ch = key.charAt(pos);
            this.key = key.substring(0, pos + 1);
        }

        Entry(char ch, String key, T value) {
            this.ch = ch;
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public T getValue() {
            return value;
        }
    }

    private int size;
    private Entry<T> root;

    public Trie() {
        this.root = new Entry<T>('\0');
    }

    /**
     * Add key value pair.
     * 
     * @param key
     * @param value
     * @return old value associated with the key, or null if the key didn't
     *         exist before.
     */
    public T put(String key, T value) {
        Entry<T> entry = root;
        for (int i = 0; i < key.length(); i++) {
            char ch = key.charAt(i);
            if (entry.firstChild == null) {
                entry.firstChild = new Entry<T>(key, i);
                entry = entry.firstChild;
                continue;

            } else {
                entry = entry.firstChild;
            }

            while (entry.ch != ch) {
                if (entry.nextSibling != null) {
                    entry = entry.nextSibling;

                } else {
                    entry.nextSibling = new Entry<T>(key, i);
                    entry = entry.nextSibling;
                    break;
                }
            }
        }

        T old = entry.value;
        entry.value = value;

        if (old == null)
            size++;

        return old;
    }

    /**
     * Get value by exact key.
     * 
     * @param key
     * @return
     */
    public T get(String key) {
        return get(key, 0, key.length());
    }

    /**
     * Get value by exact key.
     * 
     * @param s
     * @param begin
     * @param end
     * @return
     */
    public T get(String s, int begin, int end) {
        Entry<T> entry = root;
        for (int i = begin; i < end; i++) {
            char ch = s.charAt(i);
            if (entry.firstChild == null) {
                return null;

            } else {
                entry = entry.firstChild;
            }

            while (entry.ch != ch) {
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
     * @param s
     * @param pos
     * @return
     * @see {@link #matches(String, int, boolean, boolean)}
     */
    public Entry<T> matches(String s, int pos) {
        return matches(s, pos, true, true);
    }

    /**
     * Find matches using lower case. Note that when put key-value pairs to the
     * trie, the original key will be used, if you want a case-insensitive
     * match, you should pre-process the key to lower case.
     * 
     * @param s
     * @param pos
     * @param maxMode
     * @see {@link #matches(String, int, boolean, boolean)}
     * @return
     */
    public Entry<T> matches(String s, int pos, boolean maxMode) {
        return matches(s, pos, maxMode, true);
    }

    /**
     * Find matches. Note that when put key-value pairs to the trie, the
     * original key will be used, if you want a case-insensitive match, you
     * should pre-process the key to lower case.
     * 
     * @param s
     * @param pos
     * @param maxMode
     * @param useLowerCase
     *            if set to true, lower case of the give string will be used.
     * @return
     */
    public Entry<T> matches(String s, int pos, boolean maxMode,
            boolean useLowerCase) {
        Entry<T> entry = root;
        Entry<T> result = null;

        for (int i = pos; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (useLowerCase)
                ch = Character.toLowerCase(ch);

            if (entry.firstChild == null) {
                return result;

            } else {
                entry = entry.firstChild;
            }

            while (entry.ch != ch) {
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
