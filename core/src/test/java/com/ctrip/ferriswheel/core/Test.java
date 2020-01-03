package com.ctrip.ferriswheel.core;

import com.ctrip.ferriswheel.common.Workbook;
import com.ctrip.ferriswheel.common.chart.Chart;
import com.ctrip.ferriswheel.common.table.Table;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.core.asset.DefaultTable;
import com.ctrip.ferriswheel.core.asset.FilingClerk;
import com.ctrip.ferriswheel.core.bean.ChartData;
import com.ctrip.ferriswheel.core.bean.DefaultEnvironment;
import com.ctrip.ferriswheel.core.util.ChartConsultantHelper;

public class Test {
    public static void main(String[] args) {
        Workbook wb = new FilingClerk(new DefaultEnvironment.Builder().build()).createWorkbook("test-workbook");
        Table t1 = wb.addSheet("Sheet1").addAsset(Table.class, "Table1");
        t1.setCellValue(0, 0, new Value.DecimalValue(10));
        t1.setCellValue(1, 0, new Value.DecimalValue(20));
        Table t2 = wb.addSheet("Sheet2").addAsset(Table.class, "Table2");
        t2.setCellValue(0, 0, new Value.StrValue("hello world"));
        t2.setCellFormula(0, 1, "SUM('Sheet1'!'Table1'!A1:A2)");
        ChartData chartData = ChartConsultantHelper.getSuggestedChartModel(t1, 0, 0, 0, 1);
        ((DefaultTable) t1).getSheet().addAsset(Chart.class, chartData);
        System.out.println(wb);
    }

}
