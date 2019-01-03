package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.api.*;
import com.ctrip.ferriswheel.api.action.Action;
import com.ctrip.ferriswheel.api.table.Table;
import com.ctrip.ferriswheel.core.bean.DefaultEnvironment;
import com.ctrip.ferriswheel.core.bean.Value;
import junit.framework.TestCase;

import java.util.List;

public class TestDefaultWorkbookWithReviseCollector extends TestCase {
    private Environment environment;

    @Override
    protected void setUp() throws Exception {
        environment = new DefaultEnvironment.Builder().build();
    }

    public void test() {
        ReviseCollector revListener = new ReviseCollector();
        Workbook workbook = new DefaultWorkbook(environment);
        workbook.addListener(revListener);
        Sheet sheet1 = workbook.addSheet("sheet1");
        Table table1 = sheet1.addAsset(Table.class, "table1");
        table1.setCellValue(0, 0, new Value.DecimalValue(1024));
        table1.setCellFormula(0, 1, "A1*2");
        System.out.println(workbook);

        List<Action> actions = revListener.drainRevises();
        actions.forEach(rev -> System.out.println(rev));

        table1.insertRows(0, 1);
        table1.setCellValue(0, 0, new Value.DecimalValue(3072));
        table1.setCellFormula(1, 1, null);
        table1.setCellFormula(2, 0, "SUM(A1:A2)");
        System.out.println(table1);

        actions = revListener.drainRevises();
        actions.forEach(rev -> System.out.println(rev));

        // TODO add assertions
    }
}
