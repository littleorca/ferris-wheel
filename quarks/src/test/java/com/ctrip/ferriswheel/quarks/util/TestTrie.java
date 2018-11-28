package com.ctrip.ferriswheel.quarks.util;

import junit.framework.TestCase;

public class TestTrie extends TestCase {

    Trie<String> trie;

    protected void setUp() {
        this.trie = new Trie<String>();
    }

    public void testTrieAddGetClear() {
        assertNull(trie.get("apple"));
        assertNull(trie.matches("apple", 0));
        assertNull(trie.matches("apple", 0, false));
        assertEquals(0, trie.size());

        String old = trie.put("apple", "*apple*");
        assertNull(old);
        String value = trie.get("apple");
        assertEquals("*apple*", value);
        assertEquals(1, trie.size());

        value = trie.get("the apple fruit", 4, 9);
        assertEquals("*apple*", value);
        value = trie.get("the apple fruit", 4, 10);
        assertNull(value);
        value = trie.get("the apple fruit", 3, 9);
        assertNull(value);
        value = trie.get("the apple fruit", 3, 10);
        assertNull(value);
        value = trie.get("the apple fruit", 5, 9);
        assertNull(value);
        value = trie.get("the apple fruit", 4, 8);
        assertNull(value);
        value = trie.get("the apple fruit", 5, 8);
        assertNull(value);

        old = trie.put("apple", "#apple#");
        assertEquals("*apple*", old);
        value = trie.get("apple");
        assertEquals("#apple#", value);
        assertEquals(1, trie.size());

        trie.clear();
        assertEquals(0, trie.size());
        value = trie.get("apple");
        assertNull(value);
    }

    public void testTrieMatches() {
        trie.put("apple", "fruit");
        trie.put("apple tree", "plant");
        assertEquals(2, trie.size());

        assertNull(trie.get("app"));
        assertNull(trie.get("apple tre"));
        assertNull(trie.get("apple trees"));

        Trie.Entry<String> e = trie.matches("apple", 0);
        assertNotNull(e);
        assertEquals("apple", e.getKey());
        assertEquals("fruit", e.getValue());

        e = trie.matches("apple tree", 0);
        assertNotNull(e);
        assertEquals("apple tree", e.getKey());
        assertEquals("plant", e.getValue());

        e = trie.matches("apple tree", 0, false);
        assertNotNull(e);
        assertEquals("apple", e.getKey());
        assertEquals("fruit", e.getValue());

        e = trie.matches("# apple #", 2);
        assertNotNull(e);
        assertEquals("apple", e.getKey());
        assertEquals("fruit", e.getValue());

        e = trie.matches("# apple tree #", 2);
        assertNotNull(e);
        assertEquals("apple tree", e.getKey());
        assertEquals("plant", e.getValue());

        e = trie.matches("# apple tree #", 2, false);
        assertNotNull(e);
        assertEquals("apple", e.getKey());
        assertEquals("fruit", e.getValue());

        // case sensitive mode
        e = trie.matches("# Apple Tree #", 2, false, false);
        assertNull(e);
    }

}
