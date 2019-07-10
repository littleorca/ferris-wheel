package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.common.table.Table;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.core.bean.DefaultEnvironment;
import junit.framework.TestCase;

public abstract class DefaultTableTestSupport extends TestCase {
    protected ReviseCollector reviseCollector = new ReviseCollector();
    protected DefaultWorkbook workbook = new DefaultWorkbook(new DefaultEnvironment.Builder().build());

    public DefaultTableTestSupport() {
        workbook.addListener(reviseCollector);
    }

    protected DefaultTable createTable33() {
        DefaultTable table = (DefaultTable) workbook.addSheet("sheet1").addAsset(Table.class, "table1");
        table.addColumns(0, 3);
        table.addRows(0, 3);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                table.setCellValue(i, j, new Value.DecimalValue((i + 1) * 10 + j + 1));
            }
        }
        assertEquals(3, table.getRowCount());
        assertEquals(3, table.getColumnCount());
        return table;
    }

    protected void checkIntegerGrid(Integer[][] expected, Table table) {
        for (int r = 0; r < expected.length; r++) {
            Integer[] row = expected[r];
            for (int c = 0; c < row.length; c++) {
                Integer n = row[c];
                if (n == null) {
                    assertTrue(table.getCell(r, c).getData().isBlank());
                } else {
                    assertEquals((int) n, table.getCell(r, c).getData().intValue());
                }
            }
        }
    }

}
