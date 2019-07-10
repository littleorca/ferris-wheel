package com.ctrip.ferriswheel.core.util;

import com.ctrip.ferriswheel.core.asset.Asset;
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
        assertEquals(18, ref.getRowIndex());
        assertEquals(51, ref.getColumnIndex());
        assertFalse(ref.isRowAbsolute());
        assertFalse(ref.isColumnAbsolute());

        ref = References.parsePositionRef("AZ");
//        assertFalse(ref.isAlive());
//        assertNull(ref.getSheetName());
//        assertNull(ref.getAssetName());
//        assertEquals(Asset.UNSPECIFIED_ASSET_ID, ref.getCellId());
        assertEquals(-1, ref.getRowIndex());
        assertEquals(-1, ref.getColumnIndex());
        assertFalse(ref.isRowAbsolute());
        assertFalse(ref.isColumnAbsolute());

        ref = References.parsePositionRef("19");
//        assertFalse(ref.isAlive());
//        assertNull(ref.getSheetName());
//        assertNull(ref.getAssetName());
//        assertEquals(Asset.UNSPECIFIED_ASSET_ID, ref.getCellId());
        assertEquals(-1, ref.getRowIndex());
        assertEquals(-1, ref.getColumnIndex());
        assertFalse(ref.isRowAbsolute());
        assertFalse(ref.isColumnAbsolute());
    }

    public void testParseRangeEndRef() {
        PositionRef ref = References.parseRangeEndRef("$A$1");
//        assertTrue(ref.isAlive());
//        assertNull(ref.getSheetName());
//        assertNull(ref.getAssetName());
//        assertEquals(Asset.UNSPECIFIED_ASSET_ID, ref.getCellId());
        assertEquals(0, ref.getRowIndex());
        assertEquals(0, ref.getColumnIndex());
        assertTrue(ref.isRowAbsolute());
        assertTrue(ref.isColumnAbsolute());

        ref = References.parseRangeEndRef("A1");
//        assertTrue(ref.isAlive());
//        assertNull(ref.getSheetName());
//        assertNull(ref.getAssetName());
//        assertEquals(Asset.UNSPECIFIED_ASSET_ID, ref.getCellId());
        assertEquals(0, ref.getRowIndex());
        assertEquals(0, ref.getColumnIndex());
        assertFalse(ref.isRowAbsolute());
        assertFalse(ref.isColumnAbsolute());

        ref = References.parseRangeEndRef("$A");
//        assertTrue(ref.isAlive());
//        assertNull(ref.getSheetName());
//        assertNull(ref.getAssetName());
//        assertEquals(Asset.UNSPECIFIED_ASSET_ID, ref.getCellId());
        assertEquals(-1, ref.getRowIndex());
        assertEquals(0, ref.getColumnIndex());
        assertFalse(ref.isRowAbsolute());
        assertTrue(ref.isColumnAbsolute());

        ref = References.parseRangeEndRef("Z");
//        assertTrue(ref.isAlive());
//        assertNull(ref.getSheetName());
//        assertNull(ref.getAssetName());
//        assertEquals(Asset.UNSPECIFIED_ASSET_ID, ref.getCellId());
        assertEquals(-1, ref.getRowIndex());
        assertEquals(25, ref.getColumnIndex());
        assertFalse(ref.isRowAbsolute());
        assertFalse(ref.isColumnAbsolute());

        ref = References.parseRangeEndRef("$1");
//        assertTrue(ref.isAlive());
//        assertNull(ref.getSheetName());
//        assertNull(ref.getAssetName());
//        assertEquals(Asset.UNSPECIFIED_ASSET_ID, ref.getCellId());
        assertEquals(0, ref.getRowIndex());
        assertEquals(-1, ref.getColumnIndex());
        assertTrue(ref.isRowAbsolute());
        assertFalse(ref.isColumnAbsolute());

        ref = References.parseRangeEndRef("9");
//        assertTrue(ref.isAlive());
//        assertNull(ref.getSheetName());
//        assertNull(ref.getAssetName());
//        assertEquals(Asset.UNSPECIFIED_ASSET_ID, ref.getCellId());
        assertEquals(8, ref.getRowIndex());
        assertEquals(-1, ref.getColumnIndex());
        assertFalse(ref.isRowAbsolute());
        assertFalse(ref.isColumnAbsolute());
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
                Asset.UNSPECIFIED_ASSET_ID, true)));
        assertEquals("Z9", References.toFormula(new CellReference(null, null,
                new PositionRef(8, false, 25, false),
                Asset.UNSPECIFIED_ASSET_ID, true)));
        assertEquals("AA10", References.toFormula(new CellReference(null, null,
                new PositionRef(9, false, 26, false),
                Asset.UNSPECIFIED_ASSET_ID, true)));
        assertEquals("AZ19", References.toFormula(new CellReference(null, null,
                new PositionRef(18, false, 51, false),
                Asset.UNSPECIFIED_ASSET_ID, true)));
        assertEquals("BA20", References.toFormula(new CellReference(null, null,
                new PositionRef(19, false, 52, false),
                Asset.UNSPECIFIED_ASSET_ID, true)));
        assertEquals("ZZ99", References.toFormula(new CellReference(null, null,
                new PositionRef(98, false, 701, false),
                Asset.UNSPECIFIED_ASSET_ID, true)));
        assertEquals("AAA100", References.toFormula(new CellReference(null, null,
                new PositionRef(99, false, 702, false),
                Asset.UNSPECIFIED_ASSET_ID, true)));

        assertEquals("AAB101", References.toFormula(new CellReference(null, null,
                        new PositionRef(99, false, 702, false),
                        Asset.UNSPECIFIED_ASSET_ID, true),
                1, 1));

        // absolute reference will not shift
        assertEquals("$AAA$100", References.toFormula(new CellReference(null, null,
                        new PositionRef(99, true, 702, true),
                        Asset.UNSPECIFIED_ASSET_ID, true),
                1, 1));
        assertEquals("foobar!$AAA$100", References.toFormula(new CellReference(null, "foobar",
                        new PositionRef(99, true, 702, true),
                        Asset.UNSPECIFIED_ASSET_ID, true),
                1, 1));
        // relative reference will be shifted
        assertEquals("hello!world!AAB101", References.toFormula(new CellReference("hello", "world",
                        new PositionRef(99, false, 702, false),
                        Asset.UNSPECIFIED_ASSET_ID, true),
                1, 1));
        assertEquals("'你好'!'世界'!$AAA$100", References.toFormula(new CellReference("你好", "世界",
                        new PositionRef(99, true, 702, true),
                        Asset.UNSPECIFIED_ASSET_ID, true),
                1, 1));

        try {
            References.toFormula(new CellReference(null, null,
                    new PositionRef(-1, false, 0, false),
                    Asset.UNSPECIFIED_ASSET_ID, true));
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            References.toFormula(new CellReference(null, null,
                    new PositionRef(0, false, -1, false),
                    Asset.UNSPECIFIED_ASSET_ID, true));
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            References.toFormula(new CellReference(null, null,
                    new PositionRef(-1, false, -1, false),
                    Asset.UNSPECIFIED_ASSET_ID, true));
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testRangeRefToFormula() {
        assertEquals("$A$1:$Z$9", References.toFormula(new RangeReference(null, null,
                0, 0, 25, 8, -1, -1)));
        assertEquals("foobar!$A$1:$Z$9", References.toFormula(new RangeReference(null, "foobar",
                0, 0, 25, 8, -1, -1)));
        assertEquals("'你好'!'世界'!$A$1:$Z$9", References.toFormula(new RangeReference("你好", "世界",
                0, 0, 25, 8, -1, -1)));
        assertEquals("$A:$Z", References.toFormula(new RangeReference(null, null,
                0, -1, 25, -1, -1, -1)));
        assertEquals("$1:$9", References.toFormula(new RangeReference(null, null,
                -1, 0, -1, 8, -1, -1)));
        assertEquals("'\"你\"好'!'''世界''!'!$1:$9", References.toFormula(new RangeReference("\"你\"好", "'世界'!",
                -1, 0, -1, 8, -1, -1)));

        try {
            References.toFormula(new RangeReference(null, null,
                    -1, -1, 0, 1, -1, -1));
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            References.toFormula(new RangeReference(null, null,
                    0, 1, -1, -1, -1, -1));
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            References.toFormula(new RangeReference(null, null,
                    -1, 0, 0, -1, -1, -1));
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            References.toFormula(new RangeReference(null, null,
                    0, -1, -1, 0, -1, -1));
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            References.toFormula(new RangeReference(null, null,
                    -1, -1, -1, -1, -1, -1));
            fail();
        } catch (IllegalArgumentException e) {
        }
    }
}
