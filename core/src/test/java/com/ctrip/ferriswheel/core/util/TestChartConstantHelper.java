package com.ctrip.ferriswheel.core.util;

import com.ctrip.ferriswheel.common.Environment;
import com.ctrip.ferriswheel.common.Workbook;
import com.ctrip.ferriswheel.common.chart.DataSeries;
import com.ctrip.ferriswheel.common.table.Table;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.core.asset.FilingClerk;
import com.ctrip.ferriswheel.core.bean.ChartData;
import com.ctrip.ferriswheel.core.bean.DefaultEnvironment;
import junit.framework.TestCase;

public class TestChartConstantHelper extends TestCase {
    private Workbook workbook;
    private Environment environment;

    @Override
    protected void setUp() throws Exception {
        environment = new DefaultEnvironment.Builder().build();
        this.workbook = new FilingClerk(environment).createWorkbook("test-workbook");
    }

    public void testGetSuggestedChartModel() {
        Table table = workbook.addSheet("sheet1").addAsset(Table.class, "table1");
        initTable(table);
        ChartData chart = ChartConsultantHelper.getSuggestedChartModel(table, 0, 0, 3, 3);
        assertEquals("sheet1!table1!$B$1:$D$1", chart.getCategories().getFormulaString());
//        assertEquals("a", chart.getCategories().item(0).strValue());
//        assertEquals("b", chart.getCategories().item(1).strValue());
//        assertEquals("c", chart.getCategories().item(2).strValue());

        assertEquals(3, chart.getSeriesList().size());

        DataSeries s = chart.getSeriesList().get(0);
        assertEquals("sheet1!table1!$A$2", s.getName().getFormulaString());
//        assertEquals("d", s.getName().strValue());
        assertEquals("sheet1!table1!$B$2:$D$2", s.getyValues().getFormulaString());
//        assertEquals(Integer.valueOf(11), s.getyValues().item(0).intValue());
//        assertEquals(Integer.valueOf(12), s.getyValues().item(1).intValue());
//        assertEquals(Integer.valueOf(13), s.getyValues().item(2).intValue());

        s = chart.getSeriesList().get(1);
        assertEquals("sheet1!table1!$A$3", s.getName().getFormulaString());
//        assertEquals("e", s.getName().strValue());
        assertEquals("sheet1!table1!$B$3:$D$3", s.getyValues().getFormulaString());
//        assertEquals(Integer.valueOf(21), s.getyValues().item(0).intValue());
//        assertEquals(Integer.valueOf(22), s.getyValues().item(1).intValue());
//        assertEquals(Integer.valueOf(23), s.getyValues().item(2).intValue());

        s = chart.getSeriesList().get(2);
        assertEquals("sheet1!table1!$A$4", s.getName().getFormulaString());
//        assertEquals("f", s.getName().strValue());
        assertEquals("sheet1!table1!$B$4:$D$4", s.getyValues().getFormulaString());
//        assertEquals(Integer.valueOf(31), s.getyValues().item(0).intValue());
//        assertEquals(Integer.valueOf(32), s.getyValues().item(1).intValue());
//        assertEquals(Integer.valueOf(33), s.getyValues().item(2).intValue());
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
