package com.ctrip.ferriswheel.core.util;

import com.ctrip.ferriswheel.common.Environment;
import com.ctrip.ferriswheel.common.Workbook;
import com.ctrip.ferriswheel.common.table.Table;
import com.ctrip.ferriswheel.common.variant.ErrorCodes;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.core.asset.FilingClerk;
import com.ctrip.ferriswheel.core.bean.DefaultEnvironment;
import com.ctrip.ferriswheel.core.view.Rectangle;
import junit.framework.TestCase;

public class TestTableScout extends TestCase {
    private Workbook workbook;
    private Environment environment;

    @Override
    protected void setUp() throws Exception {
        environment = new DefaultEnvironment.Builder().build();
        this.workbook = new FilingClerk(environment).createWorkbook("test-workbook");
    }

    public void testIllegalArgument() {
        Table table = workbook.addSheet("sheet1").addAsset(Table.class, "table1");
        try {
            TableScout.isDecimalCompatible(null, 0, 0, 0, 0);
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            TableScout.isDecimalCompatible(table, -1, -1, -1, -1);
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            TableScout.isDecimalCompatible(table, 1, 0, 0, 0);
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            TableScout.isDecimalCompatible(table, 0, 1, 0, 0);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            TableScout.getBiggestDecimalRectangleFromBottomRight(null, 0, 0, 0, 0);
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            TableScout.getBiggestDecimalRectangleFromBottomRight(table, -1, -1, -1, -1);
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            TableScout.getBiggestDecimalRectangleFromBottomRight(table, 1, 0, 0, 0);
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            TableScout.getBiggestDecimalRectangleFromBottomRight(table, 0, 1, 0, 0);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testIsDecimalCompatibleCell() {
        Table table = workbook.addSheet("sheet1").addAsset(Table.class, "table1");
        initTable(table);
        table.eraseCells(1, 2, 1, 2);
        table.setCellValue(2, 1, Value.err(ErrorCodes.DIV));
        assertTrue(TableScout.isDecimalCompatible(table.getCell(1, 2)));
        assertTrue(TableScout.isDecimalCompatible(table.getCell(2, 1)));
        assertTrue(TableScout.isDecimalCompatible(table.getCell(2, 2)));
        assertFalse(TableScout.isDecimalCompatible(table.getCell(0, 0)));
    }

    public void testIsDecimalCompatibleCells() {
        Table table = workbook.addSheet("sheet1").addAsset(Table.class, "table1");
        initTable(table);
        table.eraseCells(1, 2, 1, 2);
        table.setCellValue(2, 1, Value.err(ErrorCodes.DIV));
        table.addRows(1);
        table.addColumns(1);
        assertTrue(TableScout.isDecimalCompatible(table, 1, 1, 4, 4));
        assertFalse(TableScout.isDecimalCompatible(table, 0, 0, 3, 3));
    }

    public void testGetBiggestDecimalRectangleFromBottomRight() {
        Table table = workbook.addSheet("sheet1").addAsset(Table.class, "table1");
        initTable(table);

        Rectangle rc = TableScout.getBiggestDecimalRectangleFromBottomRight(table, 0, 0, 3, 3);
        assertRectangle(1, 1, 3, 3, rc);

        rc = TableScout.getBiggestDecimalRectangleFromBottomRight(table, 0, 0, 2, 3);
        assertRectangle(1, 1, 2, 3, rc);

        rc = TableScout.getBiggestDecimalRectangleFromBottomRight(table, 0, 0, 3, 2);
        assertRectangle(1, 1, 3, 2, rc);

        rc = TableScout.getBiggestDecimalRectangleFromBottomRight(table, 0, 0, 1, 3);
        assertRectangle(1, 1, 1, 3, rc);

        rc = TableScout.getBiggestDecimalRectangleFromBottomRight(table, 0, 0, 3, 1);
        assertRectangle(1, 1, 3, 1, rc);

        rc = TableScout.getBiggestDecimalRectangleFromBottomRight(table, 0, 0, 1, 1);
        assertRectangle(1, 1, 1, 1, rc);

        rc = TableScout.getBiggestDecimalRectangleFromBottomRight(table, 1, 0, 1, 3);
        assertRectangle(1, 1, 1, 3, rc);

        rc = TableScout.getBiggestDecimalRectangleFromBottomRight(table, 0, 1, 3, 1);
        assertRectangle(1, 1, 3, 1, rc);

        rc = TableScout.getBiggestDecimalRectangleFromBottomRight(table, 1, 1, 3, 3);
        assertRectangle(1, 1, 3, 3, rc);

        rc = TableScout.getBiggestDecimalRectangleFromBottomRight(table, 1, 1, 1, 3);
        assertRectangle(1, 1, 1, 3, rc);

        rc = TableScout.getBiggestDecimalRectangleFromBottomRight(table, 1, 1, 3, 1);
        assertRectangle(1, 1, 3, 1, rc);

        rc = TableScout.getBiggestDecimalRectangleFromBottomRight(table, 1, 1, 1, 1);
        assertRectangle(1, 1, 1, 1, rc);

        rc = TableScout.getBiggestDecimalRectangleFromBottomRight(table, 0, 0, 0, 3);
        assertNull(rc);

        rc = TableScout.getBiggestDecimalRectangleFromBottomRight(table, 0, 0, 3, 0);
        assertNull(rc);

        rc = TableScout.getBiggestDecimalRectangleFromBottomRight(table, 0, 0, 0, 0);
        assertNull(rc);

        table.setCellValue(1, 2, new Value.StrValue("foo"));
        table.setCellValue(2, 1, new Value.StrValue("bar"));

        rc = TableScout.getBiggestDecimalRectangleFromBottomRight(table, 0, 0, 3, 3);
        assertRectangle(2, 2, 3, 3, rc);

        rc = TableScout.getBiggestDecimalRectangleFromBottomRight(table, 0, 1, 3, 3);
        assertRectangle(2, 2, 3, 3, rc);

        rc = TableScout.getBiggestDecimalRectangleFromBottomRight(table, 1, 0, 3, 3);
        assertRectangle(2, 2, 3, 3, rc);

        rc = TableScout.getBiggestDecimalRectangleFromBottomRight(table, 1, 1, 3, 3);
        assertRectangle(2, 2, 3, 3, rc);
    }

    private void assertRectangle(int expectedLeft,
                                 int expectedTop,
                                 int expectedRight,
                                 int expectedBottom,
                                 Rectangle actual) {
        assertEquals(expectedLeft, actual.getLeft());
        assertEquals(expectedTop, actual.getTop());
        assertEquals(expectedRight, actual.getRight());
        assertEquals(expectedBottom, actual.getBottom());
    }

    private void initTable(Table table) {
        table.addRows(4);
        table.addColumns(4);
        table.setCellValue(0, 0, new Value.StrValue("test"));
        table.setCellValue(0, 1, new Value.StrValue("a"));
        table.setCellValue(0, 2, new Value.StrValue("b"));
        table.setCellValue(0, 3, new Value.StrValue("c"));
        table.setCellValue(1, 0, new Value.StrValue("d"));
        table.setCellValue(1, 1, new Value.DecimalValue("11"));
        table.setCellValue(1, 2, new Value.DecimalValue("12"));
        table.setCellValue(1, 3, new Value.DecimalValue("13"));
        table.setCellValue(2, 0, new Value.StrValue("e"));
        table.setCellValue(2, 1, new Value.DecimalValue("21"));
        table.setCellValue(2, 2, new Value.DecimalValue("22"));
        table.setCellValue(2, 3, new Value.DecimalValue("23"));
        table.setCellValue(3, 0, new Value.StrValue("f"));
        table.setCellValue(3, 1, new Value.DecimalValue("31"));
        table.setCellValue(3, 2, new Value.DecimalValue("32"));
        table.setCellValue(3, 3, new Value.DecimalValue("33"));
    }
}
