package com.ctrip.ferriswheel.core;

import com.ctrip.ferriswheel.core.asset.FilingClerk;
import com.ctrip.ferriswheel.core.bean.ChartData;
import com.ctrip.ferriswheel.core.bean.DefaultEnvironment;
import com.ctrip.ferriswheel.core.bean.Value;
import com.ctrip.ferriswheel.core.intf.Table;
import com.ctrip.ferriswheel.core.intf.Workbook;
import com.ctrip.ferriswheel.core.util.ChartConsultantHelper;

import java.net.MalformedURLException;

public class Test {

    public static void main(String[] args) throws MalformedURLException {
        Workbook wb = new FilingClerk(new DefaultEnvironment.Builder().build()).createWorkbook("test-workbook");
        Table t1 = wb.addSheet("Sheet1").addTable("Table1");
        t1.setCellValue(0, 0, new Value.DecimalValue(10));
        t1.setCellValue(1, 0, new Value.DecimalValue(20));
        Table t2 = wb.addSheet("Sheet2").addTable("Table2");
        t2.setCellValue(0, 0, new Value.StrValue("hello world"));
        t2.setCellFormula(0, 1, "SUM('Sheet1'!'Table1'!A1:A2)");
        ChartData chartData = ChartConsultantHelper.getSuggestedChartModel(t1, 0, 0, 0, 1);
        t1.getSheet().addChart("chart1", chartData);
        System.out.println(wb);
    }

}
