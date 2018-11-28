package com.ctrip.ferriswheel.core.intf;

import java.io.IOException;

public interface WorkbookStore {

    Workbook read(String name) throws IOException;

    void write(String name, Workbook workbook) throws IOException;

    boolean delete(String name) throws IOException;
}
