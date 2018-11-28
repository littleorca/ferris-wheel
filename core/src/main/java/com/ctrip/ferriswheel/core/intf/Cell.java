package com.ctrip.ferriswheel.core.intf;

import com.ctrip.ferriswheel.core.view.CellStyle;

/**
 * Stores cell data and provides methods for getting them.
 * To manipulate a cell, please refer to {@link Table}.
 *
 * @see Table
 * @see Row
 */
public interface Cell extends VariantNode {

    Row getRow();

    int getRowIndex();

    int getColumnIndex();

    CellStyle getStyle();

    boolean isFillUp();

    boolean isFillDown();

    boolean isFillLeft();

    boolean isFillRight();

}
