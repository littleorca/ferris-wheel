package com.ctrip.ferriswheel.core.util;

import com.ctrip.ferriswheel.core.ref.Anchor;
import com.ctrip.ferriswheel.core.ref.CellReference;
import com.ctrip.ferriswheel.core.ref.PositionRef;
import com.ctrip.ferriswheel.core.ref.RangeReference;
import junit.framework.TestCase;

public class TestReferences extends TestCase {
    public void testParseSimpleCellRef() {
        PositionRef ref = References.parsePositionRef("$A$1");
//        assertTrue(ref.isAlive());
//        assertNull(ref.getSheetName());
//        assertNull(ref.getAssetName());
//        assertEquals(Asset.UNSPECIFIED_ASSET_ID, ref.getCellId());
        assertEquals(0, ref.getRowIndex());
        assertEquals(0, ref.getColumnIndex());
        assertTrue(ref.isRowAbsolute());
        assertTrue(ref.isColumnAbsolute());

        ref = References.parsePositionRef("$Z9");
//        assertTrue(ref.isAlive());
//        assertNull(ref.getSheetName());
//        assertNull(ref.getAssetName());
//        assertEquals(Asset.UNSPECIFIED_ASSET_ID, ref.getCellId());
        assertEquals(8, ref.getRowIndex());
        assertEquals(25, ref.getColumnIndex());
        assertFalse(ref.isRowAbsolute());
        assertTrue(ref.isColumnAbsolute());

        ref = References.parsePositionRef("AA$11");
//        assertTrue(ref.isAlive());
//        assertNull(ref.getSheetName());
//        assertNull(ref.getAssetName());
//        assertEquals(Asset.UNSPECIFIED_ASSET_ID, ref.getCellId());
        assertEquals(10, ref.getRowIndex());
        assertEquals(26, ref.getColumnIndex());
        assertTrue(ref.isRowAbsolute());
        assertFalse(ref.isColumnAbsolute());


        ref = References.parsePositionRef("AZ19");
//        assertTrue(ref.isAlive());
//        assertNull(ref.getSheetName());
//        assertNull(ref.getAssetName());
//        assertEquals(Asset.UNSPECIFIED_ASSET_ID, ref.getCellId());
        assertEquals(new Anchor(18, false), ref.getRowAnchor());
        assertEquals(new Anchor(51, false), ref.getColumnAnchor());

        ref = References.parsePositionRef("AZ");
//        assertFalse(ref.isAlive());
//        assertNull(ref.getSheetName());
//        assertNull(ref.getAssetName());
//        assertEquals(Asset.UNSPECIFIED_ASSET_ID, ref.getCellId());
        assertNull(ref.getRowAnchor());
        assertNull(ref.getColumnAnchor());

        ref = References.parsePositionRef("19");
//        assertFalse(ref.isAlive());
//        assertNull(ref.getSheetName());
//        assertNull(ref.getAssetName());
//        assertEquals(Asset.UNSPECIFIED_ASSET_ID, ref.getCellId());
        assertNull(ref.getRowAnchor());
        assertNull(ref.getColumnAnchor());
    }

    public void testParseRangeEndRef() {
        PositionRef ref = References.parseRangeEndRef("$A$1");
//        assertTrue(ref.isAlive());
//        assertNull(ref.getSheetName());
//        assertNull(ref.getAssetName());
//        assertEquals(Asset.UNSPECIFIED_ASSET_ID, ref.getCellId());
        assertEquals(new Anchor(0, true), ref.getRowAnchor());
        assertEquals(new Anchor(0, true), ref.getColumnAnchor());

        ref = References.parseRangeEndRef("A1");
//        assertTrue(ref.isAlive());
//        assertNull(ref.getSheetName());
//        assertNull(ref.getAssetName());
//        assertEquals(Asset.UNSPECIFIED_ASSET_ID, ref.getCellId());
        assertEquals(new Anchor(0, false), ref.getRowAnchor());
        assertEquals(new Anchor(0, false), ref.getColumnAnchor());

        ref = References.parseRangeEndRef("$A");
//        assertTrue(ref.isAlive());
//        assertNull(ref.getSheetName());
//        assertNull(ref.getAssetName());
//        assertEquals(Asset.UNSPECIFIED_ASSET_ID, ref.getCellId());
        assertNull(ref.getRowAnchor());
        assertEquals(new Anchor(0, true), ref.getColumnAnchor());

        ref = References.parseRangeEndRef("Z");
//        assertTrue(ref.isAlive());
//        assertNull(ref.getSheetName());
//        assertNull(ref.getAssetName());
//        assertEquals(Asset.UNSPECIFIED_ASSET_ID, ref.getCellId());
        assertNull(ref.getRowAnchor());
        assertEquals(new Anchor(25, false), ref.getColumnAnchor());

        ref = References.parseRangeEndRef("$1");
//        assertTrue(ref.isAlive());
//        assertNull(ref.getSheetName());
//        assertNull(ref.getAssetName());
//        assertEquals(Asset.UNSPECIFIED_ASSET_ID, ref.getCellId());
        assertEquals(new Anchor(0, true), ref.getRowAnchor());
        assertNull(ref.getColumnAnchor());

        ref = References.parseRangeEndRef("9");
//        assertTrue(ref.isAlive());
//        assertNull(ref.getSheetName());
//        assertNull(ref.getAssetName());
//        assertEquals(Asset.UNSPECIFIED_ASSET_ID, ref.getCellId());
        assertEquals(new Anchor(8, false), ref.getRowAnchor());
        assertNull(ref.getColumnAnchor());
    }

    public void testToRowCode() {
        assertEquals("1", References.toRowCode(0));
        assertEquals("10", References.toRowCode(9));
        assertEquals("11", References.toRowCode(10));
        assertEquals("100", References.toRowCode(99));

        try {
            References.toRowCode(-1);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testToColumnCode() {
        assertEquals("A", References.toColumnCode(0));
        assertEquals("Z", References.toColumnCode(25));
        assertEquals("AA", References.toColumnCode(26));
        assertEquals("AZ", References.toColumnCode(51));
        assertEquals("BA", References.toColumnCode(52));
        assertEquals("BA", References.toColumnCode(52));
        assertEquals("ZZ", References.toColumnCode(701));
        assertEquals("AAA", References.toColumnCode(702));
    }

    public void testCellRefToFormula() {
        assertEquals("A1", References.toFormula(new CellReference(null, null,
                new PositionRef(0, false, 0, false),
                true, false)));
        assertEquals("Z9", References.toFormula(new CellReference(null, null,
                new PositionRef(8, false, 25, false),
                true, false)));
        assertEquals("AA10", References.toFormula(new CellReference(null, null,
                new PositionRef(9, false, 26, false),
                true, false)));
        assertEquals("AZ19", References.toFormula(new CellReference(null, null,
                new PositionRef(18, false, 51, false),
                true, false)));
        assertEquals("BA20", References.toFormula(new CellReference(null, null,
                new PositionRef(19, false, 52, false),
                true, false)));
        assertEquals("ZZ99", References.toFormula(new CellReference(null, null,
                new PositionRef(98, false, 701, false),
                true, false)));
        assertEquals("AAA100", References.toFormula(new CellReference(null, null,
                new PositionRef(99, false, 702, false),
                true, false)));

        assertEquals("AAB101", References.toFormula(new CellReference(null, null,
                        new PositionRef(99, false, 702, false),
                        true, false),
                1, 1));

        // absolute reference will not shift
        assertEquals("$AAA$100", References.toFormula(new CellReference(null, null,
                        new PositionRef(99, true, 702, true),
                        true, false),
                1, 1));
        assertEquals("foobar!$AAA$100", References.toFormula(new CellReference(null, "foobar",
                        new PositionRef(99, true, 702, true),
                        true, false),
                1, 1));
        // relative reference will be shifted
        assertEquals("hello!world!AAB101", References.toFormula(new CellReference("hello", "world",
                        new PositionRef(99, false, 702, false),
                        true, false),
                1, 1));
        assertEquals("'你好'!'世界'!$AAA$100", References.toFormula(new CellReference("你好", "世界",
                        new PositionRef(99, true, 702, true),
                        true, false),
                1, 1));

        try {
            References.toFormula(new CellReference(null, null,
                    new PositionRef(-1, false, 0, false),
                    true, false));
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            References.toFormula(new CellReference(null, null,
                    new PositionRef(0, false, -1, false),
                    true, false));
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            References.toFormula(new CellReference(null, null,
                    new PositionRef(-1, false, -1, false),
                    true, false));
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testRangeRefToFormula() {
        assertEquals("A1:Z9", References.toFormula(new RangeReference(null, null,
                0, 0, 25, 8)));
        assertEquals("$A$1:$Z$9", References.toFormula(new RangeReference(null, null,
                new Anchor(0, true),
                new Anchor(0, true),
                new Anchor(25, true),
                new Anchor(8, true))));
        assertEquals("foobar!A1:$Z$9", References.toFormula(new RangeReference(null, "foobar",
                new Anchor(0, false),
                new Anchor(0, false),
                new Anchor(25, true),
                new Anchor(8, true))));
        assertEquals("'你好'!'世界'!$A1:Z$9", References.toFormula(new RangeReference("你好", "世界",
                new Anchor(0, true),
                new Anchor(0, false),
                new Anchor(25, false),
                new Anchor(8, true))));
        assertEquals("A:Z", References.toFormula(new RangeReference(null, null,
                0, null, 25, null)));
        assertEquals("$A:$Z", References.toFormula(new RangeReference(null, null,
                new Anchor(0, true), null, new Anchor(25, true), null)));
        assertEquals("1:9", References.toFormula(new RangeReference(null, null,
                null, 0, null, 8)));
        assertEquals("$1:$9", References.toFormula(new RangeReference(null, null,
                null, new Anchor(0, true), null, new Anchor(8, true))));
        assertEquals("'\"你\"好'!'''世界''!'!$1:$9", References.toFormula(new RangeReference("\"你\"好", "'世界'!",
                null, new Anchor(0, true), null, new Anchor(8, true))));

        try {
            References.toFormula(new RangeReference(null, null,
                    -1, -1, 0, 1));
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            References.toFormula(new RangeReference(null, null,
                    0, 1, -1, -1));
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            References.toFormula(new RangeReference(null, null,
                    -1, 0, 0, -1));
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            References.toFormula(new RangeReference(null, null,
                    0, -1, -1, 0));
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            References.toFormula(new RangeReference(null, null,
                    -1, -1, -1, -1));
            fail();
        } catch (IllegalArgumentException e) {
        }
    }
}
