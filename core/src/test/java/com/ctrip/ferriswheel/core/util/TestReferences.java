package com.ctrip.ferriswheel.core.util;

import com.ctrip.ferriswheel.core.intf.Asset;
import com.ctrip.ferriswheel.core.ref.CellRef;
import com.ctrip.ferriswheel.core.ref.RangeRef;
import junit.framework.TestCase;

public class TestReferences extends TestCase {
    public void testParseSimpleCellRef() {
        CellRef ref = References.parseSimpleCellRef("$A$1");
        assertTrue(ref.isValid());
        assertNull(ref.getSheetName());
        assertNull(ref.getTableName());
        assertEquals(Asset.UNSPECIFIED_ASSET_ID, ref.getCellId());
        assertEquals(0, ref.getRowIndex());
        assertEquals(0, ref.getColumnIndex());
        assertTrue(ref.isRowAbsolute());
        assertTrue(ref.isColumnAbsolute());

        ref = References.parseSimpleCellRef("$Z9");
        assertTrue(ref.isValid());
        assertNull(ref.getSheetName());
        assertNull(ref.getTableName());
        assertEquals(Asset.UNSPECIFIED_ASSET_ID, ref.getCellId());
        assertEquals(8, ref.getRowIndex());
        assertEquals(25, ref.getColumnIndex());
        assertFalse(ref.isRowAbsolute());
        assertTrue(ref.isColumnAbsolute());

        ref = References.parseSimpleCellRef("AA$11");
        assertTrue(ref.isValid());
        assertNull(ref.getSheetName());
        assertNull(ref.getTableName());
        assertEquals(Asset.UNSPECIFIED_ASSET_ID, ref.getCellId());
        assertEquals(10, ref.getRowIndex());
        assertEquals(26, ref.getColumnIndex());
        assertTrue(ref.isRowAbsolute());
        assertFalse(ref.isColumnAbsolute());


        ref = References.parseSimpleCellRef("AZ19");
        assertTrue(ref.isValid());
        assertNull(ref.getSheetName());
        assertNull(ref.getTableName());
        assertEquals(Asset.UNSPECIFIED_ASSET_ID, ref.getCellId());
        assertEquals(18, ref.getRowIndex());
        assertEquals(51, ref.getColumnIndex());
        assertFalse(ref.isRowAbsolute());
        assertFalse(ref.isColumnAbsolute());

        ref = References.parseSimpleCellRef("AZ");
        assertFalse(ref.isValid());
        assertNull(ref.getSheetName());
        assertNull(ref.getTableName());
        assertEquals(Asset.UNSPECIFIED_ASSET_ID, ref.getCellId());
        assertEquals(-1, ref.getRowIndex());
        assertEquals(-1, ref.getColumnIndex());
        assertFalse(ref.isRowAbsolute());
        assertFalse(ref.isColumnAbsolute());

        ref = References.parseSimpleCellRef("19");
        assertFalse(ref.isValid());
        assertNull(ref.getSheetName());
        assertNull(ref.getTableName());
        assertEquals(Asset.UNSPECIFIED_ASSET_ID, ref.getCellId());
        assertEquals(-1, ref.getRowIndex());
        assertEquals(-1, ref.getColumnIndex());
        assertFalse(ref.isRowAbsolute());
        assertFalse(ref.isColumnAbsolute());
    }

    public void testParseRangeEndRef() {
        CellRef ref = References.parseRangeEndRef("$A$1");
        assertTrue(ref.isValid());
        assertNull(ref.getSheetName());
        assertNull(ref.getTableName());
        assertEquals(Asset.UNSPECIFIED_ASSET_ID, ref.getCellId());
        assertEquals(0, ref.getRowIndex());
        assertEquals(0, ref.getColumnIndex());
        assertTrue(ref.isRowAbsolute());
        assertTrue(ref.isColumnAbsolute());

        ref = References.parseRangeEndRef("A1");
        assertTrue(ref.isValid());
        assertNull(ref.getSheetName());
        assertNull(ref.getTableName());
        assertEquals(Asset.UNSPECIFIED_ASSET_ID, ref.getCellId());
        assertEquals(0, ref.getRowIndex());
        assertEquals(0, ref.getColumnIndex());
        assertFalse(ref.isRowAbsolute());
        assertFalse(ref.isColumnAbsolute());

        ref = References.parseRangeEndRef("$A");
        assertTrue(ref.isValid());
        assertNull(ref.getSheetName());
        assertNull(ref.getTableName());
        assertEquals(Asset.UNSPECIFIED_ASSET_ID, ref.getCellId());
        assertEquals(-1, ref.getRowIndex());
        assertEquals(0, ref.getColumnIndex());
        assertFalse(ref.isRowAbsolute());
        assertTrue(ref.isColumnAbsolute());

        ref = References.parseRangeEndRef("Z");
        assertTrue(ref.isValid());
        assertNull(ref.getSheetName());
        assertNull(ref.getTableName());
        assertEquals(Asset.UNSPECIFIED_ASSET_ID, ref.getCellId());
        assertEquals(-1, ref.getRowIndex());
        assertEquals(25, ref.getColumnIndex());
        assertFalse(ref.isRowAbsolute());
        assertFalse(ref.isColumnAbsolute());

        ref = References.parseRangeEndRef("$1");
        assertTrue(ref.isValid());
        assertNull(ref.getSheetName());
        assertNull(ref.getTableName());
        assertEquals(Asset.UNSPECIFIED_ASSET_ID, ref.getCellId());
        assertEquals(0, ref.getRowIndex());
        assertEquals(-1, ref.getColumnIndex());
        assertTrue(ref.isRowAbsolute());
        assertFalse(ref.isColumnAbsolute());

        ref = References.parseRangeEndRef("9");
        assertTrue(ref.isValid());
        assertNull(ref.getSheetName());
        assertNull(ref.getTableName());
        assertEquals(Asset.UNSPECIFIED_ASSET_ID, ref.getCellId());
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
        assertEquals("A1", References.toFormula(new CellRef(null, null,
                0, false, 0, false, -1)));
        assertEquals("Z9", References.toFormula(new CellRef(null, null,
                8, false, 25, false, -1)));
        assertEquals("AA10", References.toFormula(new CellRef(null, null,
                9, false, 26, false, -1)));
        assertEquals("AZ19", References.toFormula(new CellRef(null, null,
                18, false, 51, false, -1)));
        assertEquals("BA20", References.toFormula(new CellRef(null, null,
                19, false, 52, false, -1)));
        assertEquals("ZZ99", References.toFormula(new CellRef(null, null,
                98, false, 701, false, -1)));
        assertEquals("AAA100", References.toFormula(new CellRef(null, null,
                99, false, 702, false, -1)));

        assertEquals("AAB101", References.toFormula(new CellRef(null, null,
                99, false, 702, false, -1), 1, 1));

        // absolute reference will not shift
        assertEquals("$AAA$100", References.toFormula(new CellRef(null, null,
                99, true, 702, true, -1), 1, 1));
        assertEquals("foobar!$AAA$100", References.toFormula(new CellRef(null, "foobar",
                99, true, 702, true, -1), 1, 1));
        // relative reference will be shifted
        assertEquals("hello!world!AAB101", References.toFormula(new CellRef("hello", "world",
                99, false, 702, false, -1), 1, 1));
        assertEquals("\"你好\"!\"世界\"!$AAA$100", References.toFormula(new CellRef("你好", "世界",
                99, true, 702, true, -1), 1, 1));

        try {
            References.toFormula(new CellRef(null, null,
                    -1, false, 0, false, -1));
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            References.toFormula(new CellRef(null, null,
                    0, false, -1, false, -1));
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            References.toFormula(new CellRef(null, null,
                    -1, false, -1, false, -1));
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testRangeRefToFormula() {
        assertEquals("$A$1:$Z$9", References.toFormula(new RangeRef(null, null,
                0, 0, 25, 8, -1, -1)));
        assertEquals("foobar!$A$1:$Z$9", References.toFormula(new RangeRef(null, "foobar",
                0, 0, 25, 8, -1, -1)));
        assertEquals("\"你好\"!\"世界\"!$A$1:$Z$9", References.toFormula(new RangeRef("你好", "世界",
                0, 0, 25, 8, -1, -1)));
        assertEquals("$A:$Z", References.toFormula(new RangeRef(null, null,
                0, -1, 25, -1, -1, -1)));
        assertEquals("$1:$9", References.toFormula(new RangeRef(null, null,
                -1, 0, -1, 8, -1, -1)));

        try {
            References.toFormula(new RangeRef(null, null,
                    -1, -1, 0, 1, -1, -1));
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            References.toFormula(new RangeRef(null, null,
                    0, 1, -1, -1, -1, -1));
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            References.toFormula(new RangeRef(null, null,
                    -1, 0, 0, -1, -1, -1));
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            References.toFormula(new RangeRef(null, null,
                    0, -1, -1, 0, -1, -1));
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            References.toFormula(new RangeRef(null, null,
                    -1, -1, -1, -1, -1, -1));
            fail();
        } catch (IllegalArgumentException e) {
        }
    }
}
