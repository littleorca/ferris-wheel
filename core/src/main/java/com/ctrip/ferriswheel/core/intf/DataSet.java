package com.ctrip.ferriswheel.core.intf;

public interface DataSet {

    SetMeta getSetMeta();

    boolean next();

    RowMeta getRowMeta();

    Variant getColumn(int index);

    Variant getColumn(String name);

    interface SetMeta {
        boolean hasRowMeta();

        boolean hasColumnMeta();

        int getColumnCount();

        ColumnMeta getColumnMeta(int index);
    }

    interface ColumnMeta {
        String getName();

        VariantType getType();
    }

    interface RowMeta {
        String getName();
    }
}
