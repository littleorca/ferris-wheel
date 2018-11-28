package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.core.intf.Environment;
import com.ctrip.ferriswheel.core.intf.Workbook;

import java.util.function.Consumer;

/**
 * Filing clerk focus on createWorkbook/open/save/delete of workbooks.
 */
public class FilingClerk {
    private final Environment environment;

    public FilingClerk(Environment environment) {
        this.environment = environment;
    }

    public Workbook createWorkbook(String name) {
        DefaultWorkbook workbook = new DefaultWorkbook(environment);
//        workbook.setName(name);
        return workbook;
    }

    public Workbook createWorkbook(String name, Consumer<Workbook> initAction) {
        DefaultWorkbook workbook = new DefaultWorkbook(environment);
//        workbook.setName(name);
        workbook.batch(initAction, true);
        return workbook;
    }

}
