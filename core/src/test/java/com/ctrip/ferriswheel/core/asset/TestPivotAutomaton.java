package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.common.Environment;
import com.ctrip.ferriswheel.common.aggregate.AggregateType;
import com.ctrip.ferriswheel.common.table.Table;
import com.ctrip.ferriswheel.common.variant.DynamicValue;
import com.ctrip.ferriswheel.common.variant.ErrorCodes;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.core.bean.DefaultEnvironment;
import com.ctrip.ferriswheel.core.bean.PivotFieldImpl;
import com.ctrip.ferriswheel.core.bean.PivotValueImpl;
import com.ctrip.ferriswheel.core.bean.TableAutomatonInfo;
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
        workbook = new FilingClerk(environment).createWorkbook("test");
        DefaultSheet s1 = workbook.addSheet("s1");
        normalTable = (DefaultTable) s1.addAsset(Table.class, "normal");
        autoTable = (DefaultTable) s1.addAsset(Table.class, "auto");
        workbook.refresh();
    }

    public void testSimpleCase() {
        normalTable.addColumns(0, 3);
        normalTable.addRows(0, 6);

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

        workbook.refresh();
        System.out.println(normalTable);

        TableAutomatonInfo.PivotAutomatonInfo pivot = new TableAutomatonInfo.PivotAutomatonInfo();
        pivot.setData(new DynamicValue("normal!A:C"));
        pivot.setRows(Arrays.asList(new PivotFieldImpl("a", "")));
        pivot.setColumns(Arrays.asList(new PivotFieldImpl("b", "")));
        pivot.setValues(Arrays.asList(new PivotValueImpl("c", AggregateType.SUMMARY, "SUM:c", "")));
        autoTable.automate(pivot);

        workbook.refresh();
        System.out.println(autoTable);

        assertEquals("a", autoTable.getCell(0, 0).getData().strValue());
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

        workbook.refresh();
        System.out.println(normalTable);

        TableAutomatonInfo.PivotAutomatonInfo pivot = new TableAutomatonInfo.PivotAutomatonInfo();
        pivot.setData(new DynamicValue("normal!A:F"));
        pivot.setRows(Arrays.asList(
                new PivotFieldImpl("f1", ""),
                new PivotFieldImpl("f2", "")
        ));
        pivot.setColumns(Arrays.asList(
                new PivotFieldImpl("f3", ""),
                new PivotFieldImpl("f4", "")
        ));
        pivot.setValues(Arrays.asList(
                new PivotValueImpl("f5", AggregateType.SUMMARY, "SUM:f5", ""),
                new PivotValueImpl("f6", AggregateType.STANDARD_DEVIATION, "STD:f6", "")
        ));

        autoTable.automate(pivot);

        workbook.refresh();
        System.out.println(autoTable);

        assertEquals(5, autoTable.getRowCount());
        assertEquals(9, autoTable.getColumnCount());

        int row = 0;
        assertEquals("f1/f2", autoTable.getCell(row, 0).strValue());
        assertEquals("f3-bar\nf4-bar\nSUM:f5", autoTable.getCell(row, 1).strValue());
        assertEquals("f3-bar\nf4-bar\nSTD:f6", autoTable.getCell(row, 2).strValue());
        assertEquals("f3-bar\nf4-foo\nSUM:f5", autoTable.getCell(row, 3).strValue());
        assertEquals("f3-bar\nf4-foo\nSTD:f6", autoTable.getCell(row, 4).strValue());
        assertEquals("f3-foo\nf4-bar\nSUM:f5", autoTable.getCell(row, 5).strValue());
        assertEquals("f3-foo\nf4-bar\nSTD:f6", autoTable.getCell(row, 6).strValue());
        assertEquals("f3-foo\nf4-foo\nSUM:f5", autoTable.getCell(row, 7).strValue());
        assertEquals("f3-foo\nf4-foo\nSTD:f6", autoTable.getCell(row, 8).strValue());

        row++;
        assertEquals("f1-bar/f2-bar", autoTable.getCell(row, 0).strValue());
        assertEquals(35, autoTable.getCell(row, 1).intValue());
        assertEquals(Value.err(ErrorCodes.DIV), autoTable.getCell(row, 2).getData().getVariant());
        assertSame(Value.BLANK, autoTable.getCell(row, 3).getData().getVariant());
        assertSame(Value.BLANK, autoTable.getCell(row, 4).getData().getVariant());
        assertEquals(30, autoTable.getCell(row, 5).intValue());
        assertEquals(Value.err(ErrorCodes.DIV), autoTable.getCell(row, 6).getData().getVariant());
        assertSame(Value.BLANK, autoTable.getCell(row, 7).getData().getVariant());
        assertSame(Value.BLANK, autoTable.getCell(row, 8).getData().getVariant());

        row++;
        assertEquals("f1-bar/f2-foo", autoTable.getCell(row, 0).strValue());
        assertEquals(25, autoTable.getCell(row, 1).intValue());
        assertEquals(Value.err(ErrorCodes.DIV), autoTable.getCell(row, 2).getData().getVariant());
        assertEquals(242, autoTable.getCell(row, 3).intValue());
        assertEquals(32.52691193, autoTable.getCell(row, 4).doubleValue(), 0.00000001);
        assertSame(Value.BLANK, autoTable.getCell(row, 5).getData().getVariant());
        assertSame(Value.BLANK, autoTable.getCell(row, 6).getData().getVariant());
        assertSame(Value.BLANK, autoTable.getCell(row, 7).getData().getVariant());
        assertSame(Value.BLANK, autoTable.getCell(row, 8).getData().getVariant());

        row++;
        assertEquals("f1-foo/f2-bar", autoTable.getCell(row, 0).strValue());
        assertSame(Value.BLANK, autoTable.getCell(row, 1).getData().getVariant());
        assertSame(Value.BLANK, autoTable.getCell(row, 2).getData().getVariant());
        assertSame(Value.BLANK, autoTable.getCell(row, 3).getData().getVariant());
        assertSame(Value.BLANK, autoTable.getCell(row, 4).getData().getVariant());
        assertEquals(140, autoTable.getCell(row, 5).intValue());
        assertEquals(4.242640687, autoTable.getCell(row, 6).doubleValue(), 0.00000001);
        assertEquals(20, autoTable.getCell(row, 7).intValue());
        assertEquals(Value.err(ErrorCodes.DIV), autoTable.getCell(row, 8).getData().getVariant());

        row++;
        assertEquals("f1-foo/f2-foo", autoTable.getCell(row, 0).strValue());
        assertEquals(5, autoTable.getCell(row, 1).intValue());
        assertEquals(Value.err(ErrorCodes.DIV), autoTable.getCell(row, 2).getData().getVariant());
        assertEquals(10, autoTable.getCell(row, 3).intValue());
        assertEquals(Value.err(ErrorCodes.DIV), autoTable.getCell(row, 4).getData().getVariant());
        assertEquals(15, autoTable.getCell(row, 5).intValue());
        assertEquals(Value.err(ErrorCodes.DIV), autoTable.getCell(row, 6).getData().getVariant());
        assertEquals(40, autoTable.getCell(row, 7).intValue());
        assertEquals(Value.err(ErrorCodes.DIV), autoTable.getCell(row, 8).getData().getVariant());
    }

    private void prepareSampleData(DefaultTable table) {
        table.addColumns(0, 6);
        table.addRows(0, 13);

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
