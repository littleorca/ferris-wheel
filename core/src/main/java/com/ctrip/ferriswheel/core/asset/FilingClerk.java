package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.common.Environment;
import com.ctrip.ferriswheel.common.Workbook;

import java.util.function.Consumer;

/**
 * Filing clerk focus on createWorkbook/open/save/delete of workbooks.
 */
public class FilingClerk {
    private final Environment environment;

    public FilingClerk(Environment environment) {
        this.environment = environment;
    }

    public DefaultWorkbook createWorkbook(String name) {
        DefaultWorkbook workbook = new DefaultWorkbook(environment);
//        workbook.setName(name);
        workbook.refresh();
        return workbook;
    }

    public Workbook createWorkbook(String name, Consumer<Workbook> initAction) {
        DefaultWorkbook workbook = new DefaultWorkbook(environment);
//        workbook.setName(name);
        workbook.batch(initAction);
        workbook.refresh(true);
        return workbook;
    }

}
