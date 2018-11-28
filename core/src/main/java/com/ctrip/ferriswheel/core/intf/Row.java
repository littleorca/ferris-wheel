package com.ctrip.ferriswheel.core.intf;

/**
 * Holds cells and provides methods for getting them.
 * To manipulate a row, please refer to {@link Table}.
 *
 * @see Table
 * @see Cell
 */
public interface Row extends Iterable<Cell>, Asset {
    /**
     * Get sheet that this row belongs to.
     *
     * @return
     */
    Table getTable();

    /**
     * Get this row's index in the sheet.
     *
     * @return
     */
    int getRowIndex();

    /**
     * Get cell by index.
     *
     * @param index
     * @return
     */
    Cell getCell(int index);

    /**
     * Whether it is a blank row.
     *
     * @return
     */
    boolean isBlank();

    /**
     * Get row size. Uninitialized cells before the last initialized cell will be count in.
     * In another word, row size equals to last initialized cell index plus one.
     *
     * @return
     */
    int size();
}
