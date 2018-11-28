package com.ctrip.ferriswheel.core.action;

import com.ctrip.ferriswheel.core.intf.Action;

// TODO this is not a real action since it won't effect  the workbook, and chart consult not introduced in none of workbook/sheet/table interface
public class ChartConsult extends SheetAction implements Action {
    private String tableName;
    private String type;
    private int left;
    private int top;
    private int right;
    private int bottom;

    public ChartConsult() {
    }

    public ChartConsult(String sheetName, String tableName, String type, int left, int top, int right, int bottom) {
        super(sheetName);
        this.tableName = tableName;
        this.type = type;
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getRight() {
        return right;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public int getBottom() {
        return bottom;
    }

    public void setBottom(int bottom) {
        this.bottom = bottom;
    }
}
