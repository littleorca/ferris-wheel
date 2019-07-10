package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.common.Environment;
import com.ctrip.ferriswheel.common.Sheet;
import com.ctrip.ferriswheel.common.Workbook;
import com.ctrip.ferriswheel.common.action.Action;
import com.ctrip.ferriswheel.common.table.Table;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.core.bean.DefaultEnvironment;
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
        table1.addRows(0, 2);
        table1.addColumns(0, 2);
        table1.setCellValue(0, 0, new Value.DecimalValue(1024));
        table1.setCellFormula(0, 1, "A1*2");
        System.out.println(workbook);

        List<Action> actions = revListener.drainRevises();
        actions.forEach(rev -> System.out.println(rev));

        table1.addRows(0, 1);
        table1.setCellValue(0, 0, new Value.DecimalValue(3072));
        table1.setCellFormula(1, 1, null);
        table1.setCellFormula(2, 0, "SUM(A1:A2)");
        System.out.println(table1);

        actions = revListener.drainRevises();
        actions.forEach(rev -> System.out.println(rev));

        // TODO add assertions
    }
}
