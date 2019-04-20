package com.ctrip.ferriswheel.core.util;

import com.ctrip.ferriswheel.core.asset.VariantNode;
import com.ctrip.ferriswheel.core.ref.RangeReference;

public class AreaPatternAnalyzer {
    private RangeReference startArea = null;
    private RangeReference endArea = null;
    private int rowDelta;
    private int columnDelta;

    /**
     * -1: no state or pattern at all
     * 0: has initial state, but without pattern
     * 1: has state and initial pattern
     * >1: has definite pattern
     */
    private int repeated = -1;

    public int feed(RangeReference curArea) {
        if (repeated == -1) { // init (no last state, no pattern)
            startArea = curArea;
            repeated = 0; // has last state, but no pattern

        } else if (repeated == 0) {
            if (endArea.width() == curArea.width()
                    && endArea.height() == curArea.height()) { // size matches
                rowDelta = curArea.getTop() - endArea.getTop();
                columnDelta = curArea.getLeft() - endArea.getLeft();
                repeated = 1;

            } else {
                startArea = curArea;
                repeated = 0;
            }

        } else { // repeated > 0
            if (endArea.getLeft() + columnDelta == curArea.getLeft()
                    && endArea.getTop() + rowDelta == curArea.getTop()
                    && endArea.getRight() + columnDelta == curArea.getRight()
                    && endArea.getBottom() + rowDelta == curArea.getBottom()) {
                repeated++;

            } else if (endArea.width() == curArea.width()
                    && endArea.height() == curArea.height()) {
                startArea = endArea;
                rowDelta = curArea.getTop() - endArea.getTop();
                columnDelta = curArea.getLeft() - endArea.getLeft();
                repeated = 1;

            } else {
                startArea = curArea;
                repeated = 0;
            }
        }
        endArea = curArea;

        return repeated;
    }

    public RangeReference predictNextArea() {
        if (repeated <= 0) {
            return null;
        }
        return new RangeReference(null,
                null,
                getEndArea().getLeft() + getColumnDelta(),
                getEndArea().getTop() + getRowDelta(),
                getEndArea().getRight() + getColumnDelta(),
                getEndArea().getBottom() + getRowDelta(),
                VariantNode.UNSPECIFIED_ASSET_ID,
                VariantNode.UNSPECIFIED_ASSET_ID);
    }

    public void reset() {
        repeated = -1;
    }

    public RangeReference getStartArea() {
        return startArea;
    }

    protected void setStartArea(RangeReference startArea) {
        this.startArea = startArea;
    }

    public RangeReference getEndArea() {
        return endArea;
    }

    protected void setEndArea(RangeReference endArea) {
        this.endArea = endArea;
    }

    public int getRowDelta() {
        return rowDelta;
    }

    protected void setRowDelta(int rowDelta) {
        this.rowDelta = rowDelta;
    }

    public int getColumnDelta() {
        return columnDelta;
    }

    protected void setColumnDelta(int columnDelta) {
        this.columnDelta = columnDelta;
    }

    public int getRepeated() {
        return repeated;
    }

    protected void setRepeated(int repeated) {
        this.repeated = repeated;
    }
}
