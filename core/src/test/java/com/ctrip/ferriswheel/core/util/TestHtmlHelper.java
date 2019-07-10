package com.ctrip.ferriswheel.core.util;

import com.ctrip.ferriswheel.common.Environment;
import com.ctrip.ferriswheel.common.Sheet;
import com.ctrip.ferriswheel.common.table.Table;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.core.asset.DefaultWorkbook;
import com.ctrip.ferriswheel.core.asset.FilingClerk;
import com.ctrip.ferriswheel.core.bean.DefaultEnvironment;
import junit.framework.TestCase;

public class TestHtmlHelper extends TestCase {
    private Environment environment;

    @Override
    protected void setUp() throws Exception {
        environment = new DefaultEnvironment.Builder().build();
    }

    // WARN: demo only, no assertion
    public void testWorkbookToHtml() {
        DefaultWorkbook workbook = new FilingClerk(environment).createWorkbook("test-workbook");
        Sheet s1 = workbook.addSheet("sheet1");
        Table t11 = s1.addAsset(Table.class, "table1");
        t11.addRows(0, 4);
        t11.addColumns(0, 4);

        t11.setCellValue(0, 1, Value.str("foo"));
        t11.setCellValue(0, 2, Value.str("bar"));
        t11.setCellValue(0, 3, Value.str("foobar"));

        t11.setCellValue(1, 0, Value.str("apple"));
        t11.setCellValue(1, 1, Value.dec(11));
        t11.setCellValue(1, 2, Value.dec(12));
        t11.setCellValue(1, 3, Value.dec(13));

        t11.setCellValue(2, 0, Value.str("boy"));
        t11.setCellValue(2, 1, Value.dec(21));
        t11.setCellValue(2, 2, Value.dec(22));
        t11.setCellValue(2, 3, Value.dec(23));

        t11.setCellValue(3, 0, Value.str("cat"));
        t11.setCellValue(3, 1, Value.dec(31));
        t11.setCellValue(3, 2, Value.dec(32));
        t11.setCellValue(3, 3, Value.dec(33));

        Sheet s2 = workbook.addSheet("sheet2");
        Table t21 = s2.addAsset(Table.class, "table1");
        t21.addColumns(0, 1);
        t21.addRows(0, 1);
        t21.setCellFormula(0, 0, "SUM(sheet1!table1!$B2:$D4)");

        String html = new HtmlHelper().workbookToHtml(workbook);
        System.out.println(html);
    }
}
