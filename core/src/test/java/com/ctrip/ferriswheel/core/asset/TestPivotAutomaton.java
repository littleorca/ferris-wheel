package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.core.bean.DynamicValue;
import com.ctrip.ferriswheel.core.bean.PivotField;
import com.ctrip.ferriswheel.core.bean.*;
import com.ctrip.ferriswheel.core.formula.ErrorCode;
import com.ctrip.ferriswheel.core.intf.AggregateType;
import com.ctrip.ferriswheel.core.intf.Environment;
import junit.framework.TestCase;

import java.util.Arrays;

public class TestPivotAutomaton extends TestCase {
    private DefaultWorkbook workbook;
    private DefaultTable normalTable;
    private DefaultTable autoTable;
    private Environment environment;

    @Override
    protected void setUp() throws Exception {
        environment = new DefaultEnvironment.Builder().build();
        workbook = (DefaultWorkbook) new FilingClerk(environment).createWorkbook("test");
        DefaultSheet s1 = workbook.addSheet("s1");
        normalTable = s1.addTable("normal");
        autoTable = s1.addTable("auto");
    }

    public void testSimpleCase() {
        int row = 0;
        normalTable.setCellValue(row, 0, Value.str("a"));
        normalTable.setCellValue(row, 1, Value.str("b"));
        normalTable.setCellValue(row, 2, Value.str("c"));

        row++;
        normalTable.setCellValue(row, 0, Value.str("a1"));
        normalTable.setCellValue(row, 1, Value.str("b1"));
        normalTable.setCellValue(row, 2, Value.dec(2));

        row++;
        normalTable.setCellValue(row, 0, Value.str("a1"));
        normalTable.setCellValue(row, 1, Value.str("b2"));
        normalTable.setCellValue(row, 2, Value.dec(3));

        row++;
        normalTable.setCellValue(row, 0, Value.str("a2"));
        normalTable.setCellValue(row, 1, Value.str("b1"));
        normalTable.setCellValue(row, 2, Value.dec(5));

        row++;
        normalTable.setCellValue(row, 0, Value.str("a2"));
        normalTable.setCellValue(row, 1, Value.str("b2"));
        normalTable.setCellValue(row, 2, Value.dec(8));

        row++;
        normalTable.setCellValue(row, 0, Value.str("a2"));
        normalTable.setCellValue(row, 1, Value.str("b1"));
        normalTable.setCellValue(row, 2, Value.dec(11));

        System.out.println(normalTable);

        TableAutomatonInfo.PivotAutomatonInfo pivot = new TableAutomatonInfo.PivotAutomatonInfo();
        pivot.setData(new DynamicValue("normal!A:C"));
        pivot.setRows(Arrays.asList(new PivotField("a")));
        pivot.setColumns(Arrays.asList(new PivotField("b")));
        pivot.setValues(Arrays.asList(new PivotValue("c", AggregateType.SUMMARY, "SUM:c")));
        autoTable.automate(pivot);

        System.out.println(autoTable);

        assertEquals(Value.BLANK, autoTable.getCell(0, 0).getValue());
        assertEquals("b1", autoTable.getCell(0, 1).strValue());
        assertEquals("b2", autoTable.getCell(0, 2).strValue());
        assertEquals("a1", autoTable.getCell(1, 0).strValue());
        assertEquals(2, autoTable.getCell(1, 1).intValue());
        assertEquals(3, autoTable.getCell(1, 2).intValue());
        assertEquals("a2", autoTable.getCell(2, 0).strValue());
        assertEquals(16, autoTable.getCell(2, 1).intValue());
        assertEquals(8, autoTable.getCell(2, 2).intValue());
    }

    public void testMultipleValues() {
        prepareSampleData(normalTable);

        System.out.println(normalTable);

        TableAutomatonInfo.PivotAutomatonInfo pivot = new TableAutomatonInfo.PivotAutomatonInfo();
        pivot.setData(new DynamicValue("normal!A:F"));
        pivot.setRows(Arrays.asList(
                new PivotField("f1"),
                new PivotField("f2")
        ));
        pivot.setColumns(Arrays.asList(
                new PivotField("f3"),
                new PivotField("f4")
        ));
        pivot.setValues(Arrays.asList(
                new PivotValue("f5", AggregateType.SUMMARY, "SUM:f5"),
                new PivotValue("f6", AggregateType.STANDARD_DEVIATION, "STD:f6")
        ));

        autoTable.automate(pivot);

        System.out.println(autoTable);

        assertEquals(7, autoTable.getRowCount());
        assertEquals(10, autoTable.getColumnCount());

        int row = 0;
        assertEquals(Value.BLANK, autoTable.getCell(row, 0).getValue());
        assertEquals(Value.BLANK, autoTable.getCell(row, 1).getValue());
        assertEquals("f3-bar", autoTable.getCell(row, 2).strValue());
        assertEquals("f3-bar", autoTable.getCell(row, 3).strValue());
        assertEquals("f3-bar", autoTable.getCell(row, 4).strValue());
        assertEquals("f3-bar", autoTable.getCell(row, 5).strValue());
        assertEquals("f3-foo", autoTable.getCell(row, 6).strValue());
        assertEquals("f3-foo", autoTable.getCell(row, 7).strValue());
        assertEquals("f3-foo", autoTable.getCell(row, 8).strValue());
        assertEquals("f3-foo", autoTable.getCell(row, 9).strValue());

        row++;
        assertEquals(Value.BLANK, autoTable.getCell(row, 0).getValue());
        assertEquals(Value.BLANK, autoTable.getCell(row, 1).getValue());
        assertEquals("f4-bar", autoTable.getCell(row, 2).strValue());
        assertEquals("f4-bar", autoTable.getCell(row, 3).strValue());
        assertEquals("f4-foo", autoTable.getCell(row, 4).strValue());
        assertEquals("f4-foo", autoTable.getCell(row, 5).strValue());
        assertEquals("f4-bar", autoTable.getCell(row, 6).strValue());
        assertEquals("f4-bar", autoTable.getCell(row, 7).strValue());
        assertEquals("f4-foo", autoTable.getCell(row, 8).strValue());
        assertEquals("f4-foo", autoTable.getCell(row, 9).strValue());

        row++;
        assertEquals(Value.BLANK, autoTable.getCell(row, 0).getValue());
        assertEquals(Value.BLANK, autoTable.getCell(row, 1).getValue());
        assertEquals("SUM:f5", autoTable.getCell(row, 2).strValue());
        assertEquals("STD:f6", autoTable.getCell(row, 3).strValue());
        assertEquals("SUM:f5", autoTable.getCell(row, 4).strValue());
        assertEquals("STD:f6", autoTable.getCell(row, 5).strValue());
        assertEquals("SUM:f5", autoTable.getCell(row, 6).strValue());
        assertEquals("STD:f6", autoTable.getCell(row, 7).strValue());
        assertEquals("SUM:f5", autoTable.getCell(row, 8).strValue());
        assertEquals("STD:f6", autoTable.getCell(row, 9).strValue());

        row++;
        assertEquals("f1-bar", autoTable.getCell(row, 0).strValue());
        assertEquals("f2-bar", autoTable.getCell(row, 1).strValue());
        assertEquals(35, autoTable.getCell(row, 2).intValue());
        assertEquals(Value.err(ErrorCode.DIV_0), autoTable.getCell(row, 3).getValue());
        assertEquals(Value.BLANK, autoTable.getCell(row, 4).getValue());
        assertEquals(Value.BLANK, autoTable.getCell(row, 5).getValue());
        assertEquals(30, autoTable.getCell(row, 6).intValue());
        assertEquals(Value.err(ErrorCode.DIV_0), autoTable.getCell(row, 7).getValue());
        assertEquals(Value.BLANK, autoTable.getCell(row, 8).getValue());
        assertEquals(Value.BLANK, autoTable.getCell(row, 9).getValue());

        row++;
        assertEquals("f1-bar", autoTable.getCell(row, 0).strValue());
        assertEquals("f2-foo", autoTable.getCell(row, 1).strValue());
        assertEquals(25, autoTable.getCell(row, 2).intValue());
        assertEquals(Value.err(ErrorCode.DIV_0), autoTable.getCell(row, 3).getValue());
        assertEquals(242, autoTable.getCell(row, 4).intValue());
        assertEquals(32.52691193, autoTable.getCell(row, 5).doubleValue(), 0.00000001);
        assertEquals(Value.BLANK, autoTable.getCell(row, 6).getValue());
        assertEquals(Value.BLANK, autoTable.getCell(row, 7).getValue());
        assertEquals(Value.BLANK, autoTable.getCell(row, 8).getValue());
        assertEquals(Value.BLANK, autoTable.getCell(row, 9).getValue());

        row++;
        assertEquals("f1-foo", autoTable.getCell(row, 0).strValue());
        assertEquals("f2-bar", autoTable.getCell(row, 1).strValue());
        assertEquals(Value.BLANK, autoTable.getCell(row, 2).getValue());
        assertEquals(Value.BLANK, autoTable.getCell(row, 3).getValue());
        assertEquals(Value.BLANK, autoTable.getCell(row, 4).getValue());
        assertEquals(Value.BLANK, autoTable.getCell(row, 5).getValue());
        assertEquals(140, autoTable.getCell(row, 6).intValue());
        assertEquals(4.242640687, autoTable.getCell(row, 7).doubleValue(), 0.00000001);
        assertEquals(20, autoTable.getCell(row, 8).intValue());
        assertEquals(Value.err(ErrorCode.DIV_0), autoTable.getCell(row, 9).getValue());

        row++;
        assertEquals("f1-foo", autoTable.getCell(row, 0).strValue());
        assertEquals("f2-foo", autoTable.getCell(row, 1).strValue());
        assertEquals(5, autoTable.getCell(row, 2).intValue());
        assertEquals(Value.err(ErrorCode.DIV_0), autoTable.getCell(row, 3).getValue());
        assertEquals(10, autoTable.getCell(row, 4).intValue());
        assertEquals(Value.err(ErrorCode.DIV_0), autoTable.getCell(row, 5).getValue());
        assertEquals(15, autoTable.getCell(row, 6).intValue());
        assertEquals(Value.err(ErrorCode.DIV_0), autoTable.getCell(row, 7).getValue());
        assertEquals(40, autoTable.getCell(row, 8).intValue());
        assertEquals(Value.err(ErrorCode.DIV_0), autoTable.getCell(row, 9).getValue());
    }

    private void prepareSampleData(DefaultTable table) {
        int row = 0;

        table.setCellValue(row, 0, Value.str("f1"));
        table.setCellValue(row, 1, Value.str("f2"));
        table.setCellValue(row, 2, Value.str("f3"));
        table.setCellValue(row, 3, Value.str("f4"));
        table.setCellValue(row, 4, Value.str("f5"));
        table.setCellValue(row, 5, Value.str("f6"));

        row++;
        table.setCellValue(row, 0, Value.str("f1-foo"));
        table.setCellValue(row, 1, Value.str("f2-foo"));
        table.setCellValue(row, 2, Value.str("f3-bar"));
        table.setCellValue(row, 3, Value.str("f4-bar"));
        table.setCellValue(row, 4, Value.dec(5 * row));
        table.setCellValue(row, 5, Value.dec(6 * row));

        row++;
        table.setCellValue(row, 0, Value.str("f1-foo"));
        table.setCellValue(row, 1, Value.str("f2-foo"));
        table.setCellValue(row, 2, Value.str("f3-bar"));
        table.setCellValue(row, 3, Value.str("f4-foo"));
        table.setCellValue(row, 4, Value.dec(5 * row));
        table.setCellValue(row, 5, Value.dec(6 * row));

        row++;
        table.setCellValue(row, 0, Value.str("f1-foo"));
        table.setCellValue(row, 1, Value.str("f2-foo"));
        table.setCellValue(row, 2, Value.str("f3-foo"));
        table.setCellValue(row, 3, Value.str("f4-bar"));
        table.setCellValue(row, 4, Value.dec(5 * row));
        table.setCellValue(row, 5, Value.dec(6 * row));

        row++;
        table.setCellValue(row, 0, Value.str("f1-foo"));
        table.setCellValue(row, 1, Value.str("f2-bar"));
        table.setCellValue(row, 2, Value.str("f3-foo"));
        table.setCellValue(row, 3, Value.str("f4-foo"));
        table.setCellValue(row, 4, Value.dec(5 * row));
        table.setCellValue(row, 5, Value.dec(6 * row));

        row++;
        table.setCellValue(row, 0, Value.str("f1-bar"));
        table.setCellValue(row, 1, Value.str("f2-foo"));
        table.setCellValue(row, 2, Value.str("f3-bar"));
        table.setCellValue(row, 3, Value.str("f4-bar"));
        table.setCellValue(row, 4, Value.dec(5 * row));
        table.setCellValue(row, 5, Value.dec(6 * row));

        row++;
        table.setCellValue(row, 0, Value.str("f1-bar"));
        table.setCellValue(row, 1, Value.str("f2-bar"));
        table.setCellValue(row, 2, Value.str("f3-foo"));
        table.setCellValue(row, 3, Value.str("f4-bar"));
        table.setCellValue(row, 4, Value.dec(5 * row));
        table.setCellValue(row, 5, Value.dec(6 * row));

        row++;
        table.setCellValue(row, 0, Value.str("f1-bar"));
        table.setCellValue(row, 1, Value.str("f2-bar"));
        table.setCellValue(row, 2, Value.str("f3-bar"));
        table.setCellValue(row, 3, Value.str("f4-bar"));
        table.setCellValue(row, 4, Value.dec(5 * row));
        table.setCellValue(row, 5, Value.dec(6 * row));

        row++;
        table.setCellValue(row, 0, Value.str("f1-foo"));
        table.setCellValue(row, 1, Value.str("f2-foo"));
        table.setCellValue(row, 2, Value.str("f3-foo"));
        table.setCellValue(row, 3, Value.str("f4-foo"));
        table.setCellValue(row, 4, Value.dec(5 * row));
        table.setCellValue(row, 5, Value.dec(6 * row));

        for (int i = 0; i < 2; i++) {
            row++;
            table.setCellValue(row, 0, Value.str("f1-foo"));
            table.setCellValue(row, 1, Value.str("f2-bar"));
            table.setCellValue(row, 2, Value.str("f3-foo"));
            table.setCellValue(row, 3, Value.str("f4-bar"));
            table.setCellValue(row, 4, Value.dec(7 * row));
            table.setCellValue(row, 5, Value.dec(3 * row));

            row++;
            table.setCellValue(row, 0, Value.str("f1-bar"));
            table.setCellValue(row, 1, Value.str("f2-foo"));
            table.setCellValue(row, 2, Value.str("f3-bar"));
            table.setCellValue(row, 3, Value.str("f4-foo"));
            table.setCellValue(row, 4, Value.dec(11 * row));
            table.setCellValue(row, 5, Value.dec(23 * row));
        }
    }
}
