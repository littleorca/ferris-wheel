package com.ctrip.ferriswheel.example.web;

import com.ctrip.ferriswheel.core.asset.ReviseCollector;
import com.ctrip.ferriswheel.common.Workbook;

public class WorkContext {
    private Workbook workbook;
    private ReviseCollector collector;

    public WorkContext() {
    }

    public WorkContext(Workbook workbook, ReviseCollector collector) {
        this.workbook = workbook;
        this.collector = collector;
    }

    public Workbook getWorkbook() {
        return workbook;
    }

    public void setWorkbook(Workbook workbook) {
        this.workbook = workbook;
    }

    public ReviseCollector getCollector() {
        return collector;
    }

    public void setCollector(ReviseCollector collector) {
        this.collector = collector;
    }
}
