/*
 * MIT License
 *
 * Copyright (c) 2018-2019 Ctrip.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.core.ref.SimpleTableRange;

/**
 * Hot area delegate is an asset that represents a certain table area. It
 * simplify dependency management by keep table range tracing as an inner
 * logic of the table implementation. For dependents, an table range
 * reference is just like normal single node reference.
 */
class HotAreaDelegate extends AssetNode {
    private final HotAreaManager manager;
    private SimpleTableRange range;
    private HotAreaAnchor areaAnchor;

    HotAreaDelegate(HotAreaManager manager, SimpleTableRange range) {
        super(manager.getAssetManager());
        this.manager = manager;
        this.range = range;
    }

    @Override
    protected EvaluationState doEvaluate(EvaluationContext context) {
        return EvaluationState.DONE;
    }

    @Override
    protected void onExternalDependentChange() {
        if (getDependents().isEmpty()) {
            manager.clearRange(range);
        }
    }

    public HotAreaAnchor getAreaAnchor() {
        return areaAnchor;
    }

    public void setAreaAnchor(HotAreaAnchor areaAnchor) {
        this.areaAnchor = areaAnchor;
    }

    public void refresh(Integer alignLeft, Integer alignTop, Integer alignRight, Integer alignBottom) {
        if (!isValid()) {
            return;
        }

        markDirty();

        if (areaAnchor == null) { // phantom
            return;
        }

        DefaultTable table = parent(DefaultTable.class);

        checkAnchorState(areaAnchor.getStart());
        checkAnchorState(areaAnchor.getEnd());

        if (!areaAnchor.getStart().isValid() && !areaAnchor.getEnd().isValid()) {
            setValid(false);
            clearDependencies();
            return;
        }

        if (!areaAnchor.getStart().isValid()) {
            Integer backupRowIndex = alignBottom == null ? range.getTop() : alignBottom;
            Integer backupColumnIndex = alignRight == null ? range.getLeft() : alignRight;

            if (backupRowIndex != null && backupColumnIndex != null) {
                fixAnchor(areaAnchor.getStart(), table, backupColumnIndex, backupRowIndex);
            }
        }

        if (!areaAnchor.getEnd().isValid()) {
            Integer backupRowIndex = alignTop == null ? range.getBottom() : alignTop;
            Integer backupColumnIndex = alignLeft == null ? range.getRight() : alignLeft;

            if (backupColumnIndex != null && backupRowIndex != null) {
                fixAnchor(areaAnchor.getEnd(), table, backupColumnIndex, backupRowIndex);
            }
        }

        if (!areaAnchor.isValid()) {
            setValid(false);
            clearDependencies();
            return;
        }

        SimpleTableRange newRange = recalculateRange();
        manager.updateHotAreaRange(range, newRange);
        range = newRange;
        updateDependencies();
    }

    private boolean checkAnchorState(HotAreaAnchor.EndpointAnchor anchor) {
        if (anchor instanceof HotAreaAnchor.CellAnchor) {
            HotAreaAnchor.CellAnchor cellAnchor = (HotAreaAnchor.CellAnchor) anchor;
            if (cellAnchor.isValid()) {
                Asset cell = getAssetManager().get(cellAnchor.getCellId());
                if (cell == null) {
                    cellAnchor.setCellId(Asset.UNSPECIFIED_ASSET_ID);
                }
            }
        }
        return anchor.isValid();
    }

    private void fixAnchor(HotAreaAnchor.EndpointAnchor anchor, DefaultTable table,
                           int backupColumnIndex, int backupRowIndex) {
        if (anchor instanceof HotAreaAnchor.CellAnchor) {
            fixCellAnchor((HotAreaAnchor.CellAnchor) anchor, table, backupColumnIndex, backupRowIndex);
        }
    }

    private void fixCellAnchor(HotAreaAnchor.CellAnchor anchor, DefaultTable table,
                               int backupColumnIndex, int backupRowIndex) {
        if (table.isValidColumnIndex(backupColumnIndex) &&
                table.isValidRowIndex(backupRowIndex)) {
            DefaultCell cell = table.getCell(backupRowIndex, backupColumnIndex);
            anchor.setCellId(cell.getAssetId());
        }
    }

    private SimpleTableRange recalculateRange() {
        SimpleTableRange newRange = new SimpleTableRange(range);

        if (areaAnchor != null) {
            HotAreaAnchor.EndpointAnchor startAnchor = areaAnchor.getStart();
            if (startAnchor.isValid()) {
                if (startAnchor instanceof HotAreaAnchor.CellAnchor) {
                    DefaultCell cell = (DefaultCell) getAssetManager().get(((HotAreaAnchor.CellAnchor) startAnchor).getCellId());
                    newRange.setLeft(cell.getColumnIndex());
                    newRange.setTop(cell.getRowIndex());

                    /* } else if () { */
                }
            }

            HotAreaAnchor.EndpointAnchor endAnchor = areaAnchor.getEnd();
            if (endAnchor.isValid()) {
                if (endAnchor instanceof HotAreaAnchor.CellAnchor) {
                    DefaultCell cell = (DefaultCell) getAssetManager().get(((HotAreaAnchor.CellAnchor) endAnchor).getCellId());
                    newRange.setRight(cell.getColumnIndex());
                    newRange.setBottom(cell.getRowIndex());

                    /* } else if () { */
                }
            }
        }

        return newRange;
    }

    void updateDependencies() {
        clearDependencies();
        collectDependencies();
    }

    private void collectDependencies() {
        SimpleTableRange validRange = getValidRange();
        if (validRange == null) {
            return;
        }

        DefaultTable table = parent(DefaultTable.class);

        for (int row = validRange.getTop(); row <= validRange.getBottom(); row++) {
            for (int col = validRange.getLeft(); col <= validRange.getRight(); col++) {
                DefaultCell depCell = table.getCell(row, col);
                if (depCell != null) {
                    addDependency(depCell);
                }
            }
        }
    }

    SimpleTableRange getRange() {
        return new SimpleTableRange(range);
    }

    SimpleTableRange getValidRange() {
        SimpleTableRange tableRange = getTableRange();
        if (tableRange == null) {
            return null;
        }

        return tableRange.intersection(range);
    }

    SimpleTableRange getTableRange() {
        DefaultTable table = parent(DefaultTable.class);
        final int left = 0;
        final int top = 0;
        final int right = table.getColumnCount() - 1;
        final int bottom = table.getRowCount() - 1;
        if (left > right || top > bottom) {
            return null;
        }
        return new SimpleTableRange(left, top, right, bottom);
    }

}
