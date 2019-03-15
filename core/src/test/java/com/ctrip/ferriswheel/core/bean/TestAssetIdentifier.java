package com.ctrip.ferriswheel.core.bean;

import junit.framework.TestCase;

public class TestAssetIdentifier extends TestCase {
    public void testDeserialize() {
        AssetIdentifier ap = new AssetIdentifier("hello!world");
        assertEquals(2, ap.getLocators().size());
        assertEquals("hello", ((AssetIdentifier.NameLocator) ap.getLocators().get(0)).getName());
        assertEquals("world", ((AssetIdentifier.NameLocator) ap.getLocators().get(1)).getName());

        ap = new AssetIdentifier("[1]![2]");
        assertEquals(2, ap.getLocators().size());
        assertEquals(1, ((AssetIdentifier.ArrayLocator) ap.getLocators().get(0)).getIndex());
        assertEquals(2, ((AssetIdentifier.ArrayLocator) ap.getLocators().get(1)).getIndex());

        ap = new AssetIdentifier("[11][12]![21][22]");
        assertEquals(2, ap.getLocators().size());
        assertEquals(11, ((AssetIdentifier.GridLocator) ap.getLocators().get(0)).getRowIndex());
        assertEquals(12, ((AssetIdentifier.GridLocator) ap.getLocators().get(0)).getColumnIndex());
        assertEquals(21, ((AssetIdentifier.GridLocator) ap.getLocators().get(1)).getRowIndex());
        assertEquals(22, ((AssetIdentifier.GridLocator) ap.getLocators().get(1)).getColumnIndex());

        ap = new AssetIdentifier("'工作\t''表''1'!'表\n格 2'![1][2]");
        assertEquals(3, ap.getLocators().size());
        assertEquals("工作\t'表'1", ((AssetIdentifier.NameLocator) ap.getLocators().get(0)).getName());
        assertEquals("表\n格 2", ((AssetIdentifier.NameLocator) ap.getLocators().get(1)).getName());
        assertEquals(1, ((AssetIdentifier.GridLocator) ap.getLocators().get(2)).getRowIndex());
        assertEquals(2, ((AssetIdentifier.GridLocator) ap.getLocators().get(2)).getColumnIndex());
    }

    public void testAppendString() {
        AssetIdentifier ap = new AssetIdentifier()
                .append("hello")
                .append("world");
        assertEquals("hello!world", ap.serialize());
    }

    public void testAppendIndex() {
        AssetIdentifier ap = new AssetIdentifier()
                .append(10)
                .append(12);
        assertEquals("[10]![12]", ap.serialize());
    }

    public void testAppendRowColumn() {
        AssetIdentifier ap = new AssetIdentifier()
                .append(11, 13)
                .append(15, 17);
        assertEquals("[11][13]![15][17]", ap.serialize());
    }
}
