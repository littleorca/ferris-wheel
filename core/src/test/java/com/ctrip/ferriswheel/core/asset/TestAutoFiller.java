package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.common.table.Table;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.core.bean.DefaultEnvironment;
import junit.framework.TestCase;

public class TestAutoFiller extends TestCase {
    private DefaultWorkbook workbook = new DefaultWorkbook(new DefaultEnvironment.Builder().build());
    private AutoFiller autoFiller = new AutoFiller(workbook);

    public void testAutoFillDown() {
        DefaultTable table = (DefaultTable) workbook.addSheet("test").addAsset(Table.class, "test");

        table.addRows(0, 3);
        table.addColumns(0, 3);

        table.setCellValue(0, 0, new Value.DecimalValue(1));
        table.setCellValue(1, 0, new Value.DecimalValue(2));
        table.setCellValue(2, 0, new Value.DecimalValue(3));

        table.setCellValue(0, 1, new Value.DecimalValue(10));
        table.setCellFormula(0, 2, "A1*B1");
        table.setCellFillDown(0, 1, true);
        table.setCellFillDown(0, 2, true);
        System.out.println(workbook);

        autoFiller.autoFillCellIfPossible(table, 1, 1);
        autoFiller.autoFillCellIfPossible(table, 1, 2);
        autoFiller.autoFillCellIfPossible(table, 2, 1);
        autoFiller.autoFillCellIfPossible(table, 2, 2);
        System.out.println(workbook);

        DefaultCell c11 = table.getCell(1, 1);
        DefaultCell c12 = table.getCell(1, 2);
        DefaultCell c21 = table.getCell(2, 1);
        DefaultCell c22 = table.getCell(2, 2);

        assertTrue(c11.isFillDown());
        assertTrue(c12.isFillDown());
        assertTrue(c21.isFillDown());
        assertTrue(c22.isFillDown());

        assertEquals(10, c11.intValue());
        assertEquals(10, c21.intValue());

        assertEquals("A2*B2", c12.getFormulaString());
        assertEquals(20, c12.intValue());
        assertEquals("A3*B3", c22.getFormulaString());
        assertEquals(30, c22.intValue());
    }

    public void testFillUp() {
        DefaultTable table = (DefaultTable) workbook.addSheet("test").addAsset(Table.class, "test");

        table.addColumns(0, 6);
        table.addRows(0, 7);

        table.setCellValue(0, 1, new Value.DecimalValue(1));
        table.setCellValue(1, 1, new Value.DecimalValue(3));
        table.setCellValue(2, 1, new Value.DecimalValue(5));
        table.setCellValue(3, 1, new Value.DecimalValue(7));
        table.setCellValue(4, 1, new Value.DecimalValue(9));

        table.setCellValue(5, 0, new Value.StrValue("hello"));
        table.setCellValue(5, 1, new Value.DecimalValue(10));
        table.setCellFormula(5, 2, "B6+1");
        table.setCellFormula(5, 3, "B$6+10");
        table.setCellFormula(5, 4, "SUM(B6:D6)");

        System.out.println(workbook);

        autoFiller.fillUp(table, 5, 0, 4);
        autoFiller.fillUp(table, 5, 2, 4);
        autoFiller.fillUp(table, 5, 3, 4);
        autoFiller.fillUp(table, 5, 4, 4);
        System.out.println(workbook);

        // these cells should not be filled
        assertTrue(table.getCell(0, 0).isBlank());
        assertTrue(table.getCell(0, 2).isBlank());
        assertTrue(table.getCell(0, 3).isBlank());
        assertTrue(table.getCell(0, 4).isBlank());

        for (int i = 1; i < 4; i++) {
            // constant string column
            assertEquals("hello", table.getCell(i, 0).strValue());
            // simple relative row ref
            assertEquals("B" + (i + 1) + "+1", table.getCell(i, 2).getFormulaString());
            // absolute row ref formula
            assertEquals("B$6+10", table.getCell(i, 3).getFormulaString());
            // sum with relative range
            assertEquals("SUM(B" + (i + 1) + ":D" + (i + 1) + ")", table.getCell(i, 4).getFormulaString());
        }

        // check some of the cell values
        assertEquals(6, table.getCell(2, 2).intValue());
        assertEquals(20, table.getCell(2, 3).intValue());
        assertEquals(31, table.getCell(2, 4).intValue());

        // check auto-fill flags
        for (int i = 1; i < 6; i++) {
            for (int j = 0; j < 5; j++) {
                if (j == 1) {
                    continue;
                }
                assertTrue(table.getCell(i, j).isFillUp());
                assertFalse(table.getCell(i, j).isFillDown());
                assertFalse(table.getCell(i, j).isFillLeft());
                assertFalse(table.getCell(i, j).isFillRight());
            }
        }
    }

    public void testFillDown() {
    }

    public void testFillLeft() {
    }

    public void testFillRight() {
    }
}
