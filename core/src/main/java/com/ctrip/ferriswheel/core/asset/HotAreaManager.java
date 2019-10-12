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

import com.ctrip.ferriswheel.common.chart.Chart;
import com.ctrip.ferriswheel.common.chart.ChartBinder;
import com.ctrip.ferriswheel.common.chart.DataSeries;
import com.ctrip.ferriswheel.common.text.Text;
import com.ctrip.ferriswheel.common.variant.DynamicValue;
import com.ctrip.ferriswheel.core.action.AutomateTable;
import com.ctrip.ferriswheel.core.action.UpdateChart;
import com.ctrip.ferriswheel.core.action.UpdateForm;
import com.ctrip.ferriswheel.core.bean.TextData;
import com.ctrip.ferriswheel.core.formula.*;
import com.ctrip.ferriswheel.core.ref.*;

import java.util.*;

class HotAreaManager extends AssetNode {
    private Map<SimpleTableRange, HotAreaDelegate> hotAreaMap = new HashMap<>();

    protected HotAreaManager(AssetManager assetManager) {
        super(assetManager);
    }

    void hookCell(CellReference cellReference, long nodeId) {
        PositionRef pos = cellReference.getPositionRef();
        SimpleTableRange area = new SimpleTableRange(
                pos.getColumnIndex(),
                pos.getRowIndex(),
                pos.getColumnIndex(),
                pos.getRowIndex());
        HotAreaDelegate hotAreaDelegate = hookArea(area, nodeId);
        cellReference.setHotAreaId(hotAreaDelegate.getAssetId());
    }

    /**
     * Watch range.
     *
     * @param rangeReference Table range reference.
     * @param nodeId         ID of the node which depends on the specified range.
     */
    void hookRange(RangeReference rangeReference, long nodeId) {
        SimpleTableRange area = new SimpleTableRange(rangeReference);
        HotAreaDelegate hotAreaDelegate = hookArea(area, nodeId);
        rangeReference.setHotAreaId(hotAreaDelegate.getAssetId());
    }

    private HotAreaDelegate hookArea(SimpleTableRange area, long nodeId) {
        HotAreaDelegate hotAreaDelegate = hotAreaMap.get(area);
        if (hotAreaDelegate == null) {
            hotAreaDelegate = registerHotArea(area);
        }

        Asset asset = getAssetManager().get(nodeId);
        ((AssetNode) asset).addDependency(hotAreaDelegate);

        return hotAreaDelegate;
    }

    private HotAreaDelegate registerHotArea(SimpleTableRange range) {
        HotAreaDelegate hotAreaDelegate = new HotAreaDelegate(this, range);
        hotAreaMap.put(range, hotAreaDelegate);
        bindChild(hotAreaDelegate);

        DefaultTable table = parent(DefaultTable.class);
        if (table.getAutomaton() != null) {
            hotAreaDelegate.addDependency(table.getGridData());
            return hotAreaDelegate;
        }

        final int left = range.getLeft() == null ? 0 : range.getLeft();
        final int top = range.getTop() == null ? 0 : range.getTop();
        final int right = range.getRight() != null ? range.getRight() :
                table.getColumnCount() > 0 ? table.getColumnCount() - 1 : 0;
        final int bottom = range.getBottom() != null ? range.getBottom() :
                table.getRowCount() > 0 ? table.getRowCount() - 1 : 0;

        HotAreaAnchor areaAnchor = new HotAreaAnchor();
        if (top >= 0 && top < table.getRowCount() && left >= 0 && left < table.getColumnCount()) {
            DefaultCell upperLeft = table.getCell(top, left);
            areaAnchor.setStart(new HotAreaAnchor.CellAnchor(upperLeft.getAssetId()));
        }
        if (bottom >= 0 && bottom < table.getRowCount() && right >= 0 && right < table.getColumnCount()) {
            DefaultCell lowerRight = table.getCell(bottom, right);
            areaAnchor.setEnd(new HotAreaAnchor.CellAnchor(lowerRight.getAssetId()));
        }

        if (areaAnchor.getStart() != null && areaAnchor.getEnd() != null) {
            hotAreaDelegate.setAreaAnchor(areaAnchor);
        }

        hotAreaDelegate.updateDependencies();

        return hotAreaDelegate;
    }

    /**
     * Find overlapped areas.
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @return
     */
    List<HotAreaDelegate> findOverlappedAreas(Integer left,
                                              Integer top,
                                              Integer right,
                                              Integer bottom) {
        // TODO optimize algorithm
        List<HotAreaDelegate> areas = new LinkedList<>();
        for (HotAreaDelegate area : hotAreaMap.values()) {
            if (area.getRange().isOverlap(left, top, right, bottom)) {
                areas.add(area);
            }
        }
        return areas;
    }

    public void clearRange(SimpleTableRange range) {
        HotAreaDelegate hotAreaDelegate = hotAreaMap.remove(range);
        if (hotAreaDelegate != null) {
            unbindChild(hotAreaDelegate);
        }
    }

    @Override
    protected EvaluationState doEvaluate(EvaluationContext context) {
        return EvaluationState.DONE;
    }

    void updateHotAreaRange(SimpleTableRange oldRange, SimpleTableRange newRange) {
        HotAreaDelegate hotArea = hotAreaMap.remove(oldRange);
        HotAreaDelegate existedHotArea = hotAreaMap.get(newRange);
        if (existedHotArea == null) {
            hotAreaMap.put(newRange, hotArea);
        } else { // merge
            // FIXME merge hot area
        }
    }

    /**
     * Find overlapped ranges and dependencies, then update there formulas.
     * If dependency has gone, or both anchor cells of range dependency have gone,
     * set error info to the value node.
     * If one of the anchor cell of range dependency has gone, try to shrink the referred
     * area.
     * <p>
     * Changed area specified by left/top/right/bottom, contains removed area and moved area.
     * <p>
     * If an area has been removed, alignLeft/alignTop/alignRight/alignBottom indicates which
     * row/column should a removed anchor relocated to.
     * <p>
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @param alignLeft
     * @param alignTop
     * @param alignRight
     * @param alignBottom
     */
    void onTableAreaChange(int left, int top, Integer right, Integer bottom,
                           Integer alignLeft, Integer alignTop,
                           Integer alignRight, Integer alignBottom) {
        if (getWorkbook() != null && getWorkbook().isSkipWelding()) {
            return; // should manually update later.
        }
        List<HotAreaDelegate> areas = findOverlappedAreas(left, top, right, bottom);
        Set<AssetNode> affectedNodes = new HashSet<>();
        for (HotAreaDelegate area : areas) {
            area.refresh(alignLeft, alignTop, alignRight, alignBottom);
            affectedNodes.addAll(area.getDependents());
        }

        // scan for cell references
        if (right == null) {
            right = Integer.MAX_VALUE;
        }
        if (bottom == null) {
            bottom = Integer.MAX_VALUE;
        }

        DefaultTable table = getTable();

        for (int rowIndex = top; rowIndex <= bottom && rowIndex < table.getRowCount(); rowIndex++) {
            DefaultRow row = table.getRow(rowIndex);
            if (row == null) {
                continue;
            }
            for (int columnIndex = left; columnIndex <= right && columnIndex < table.getColumnCount(); columnIndex++) {
                DefaultCell cell = row.getCell(columnIndex);
                if (cell == null) {
                    continue;
                }
                for (AssetNode dependent : cell.getDependents()) {
                    if (!(dependent instanceof HotAreaDelegate)) {
                        affectedNodes.add(dependent);
                    }
                }
            }
        }

        for (AssetNode node : affectedNodes) {
            fixFormulaAfterAreaChanged((ValueNode) node);
        }
    }

    private void fixFormulaAfterAreaChanged(ValueNode dependentNode) {
        // this loop check and update elements for construct new formula
        for (FormulaElement elem : dependentNode.getFormula()) {
            if (!(elem instanceof ReferenceElement)) {
                continue;
            }
            if (elem instanceof CellReferenceElement) {
                CellReference cellReference = ((CellReferenceElement) elem).getCellReference();
                fixCellRef(dependentNode, cellReference);

            } else if (elem instanceof RangeReferenceElement) {
                RangeReference rangeReference = ((RangeReferenceElement) elem).getRangeReference();
                fixRangeRef(dependentNode, rangeReference);

            } else {
                throw new RuntimeException("What? I don't recognize the reference: " + elem.getClass());
            }
        }

//        if (!modified) {
//            return; // FIXME 如果采用 table!A:D这样的公式，行数变化时公式并无变化，但引用的数据实际需要刷新的。
//        }

        DefaultTable table = getTable();

        if (table.getAutomaton() != null) {
            return;
        }

        // now lets construct new formula
        String newFormula = FormulaParser.assemble(dependentNode.getFormula(), 0, 0);
        if (dependentNode instanceof DefaultCell) {
            DefaultCell cell = (DefaultCell) dependentNode;
            cell.getRow().getTable().setCellFormula(cell.getRowIndex(), cell.getColumnIndex(), newFormula); // this will trigger onCellUpdate, some other businesses will be done there.

        } else if (dependentNode.getParent() instanceof Chart
                || dependentNode.getParent() instanceof DataSeries
                || dependentNode.getParent() instanceof ChartBinder) {
            // chart property update is not as easy as a cell, property of chart may be a little
            // difficult to trace as it doesn't has row/column index. a chart property can be
            // a categories formula, or one property of any series.
            // Of cause it's not hard to update chart property itself, however, what changes
            // should we notify the listeners (especially the revise collector)?
            // currently just tell listeners the chart has changed, either model or data.
            DefaultChart chart = (DefaultChart)
                    (((dependentNode.getParent() instanceof DataSeries)
                            || dependentNode.getParent() instanceof ChartBinder) ?
                            dependentNode.getParent().getParent() : dependentNode.getParent());
            UpdateChart action = new UpdateChart(chart.getSheet().getName(), chart.getName(), null);
            table.publicly(action, () -> {
                dependentNode.setFormula(new Formula(newFormula));
//                onValueNodeUpdate(dependentNode);
                return chart;
            });

        } else if (dependentNode.getParent() instanceof Text) {
            DefaultText text = (DefaultText) dependentNode.getParent();
            text.getSheet().updateText(text.getName(), new TextData(text.getName(), new DynamicValue(newFormula), null));

        } else if (dependentNode.getParent() instanceof DefaultQueryAutomaton
                || dependentNode.getParent() instanceof DefaultPivotAutomaton
                || dependentNode.getParent() instanceof DefaultQueryTemplate) {
            AbstractAutomaton auto = dependentNode.parent(AbstractAutomaton.class);
            AutomateTable automateTable = new AutomateTable(auto.getTable().getSheet().getName(),
                    auto.getTable().getName(), null);
            table.publicly(automateTable, () -> {
                dependentNode.setDynamicVariant(new DynamicValue(newFormula));
//                onValueNodeUpdate(dependentNode);
                automateTable.setSolution(auto instanceof DefaultQueryAutomaton ?
                        ((DefaultQueryAutomaton) auto).getQueryAutomatonInfo() :
                        ((DefaultPivotAutomaton) auto).getPivotAutomatonInfo());
            });

        } else if (dependentNode.getParent() instanceof DefaultFormField
                || dependentNode.getParent() instanceof DefaultFormFieldBinding) {
            dependentNode.setDynamicVariant(new DynamicValue(newFormula));
            DefaultForm form = dependentNode.parent(DefaultForm.class);
            UpdateForm action = new UpdateForm(form.getSheet().getName(), form.getName(), form);
            table.publicly(action, () -> {
                dependentNode.setFormula(new Formula(newFormula));
                return form;
            });

        } else {
            throw new RuntimeException("Unsupported value node: " + dependentNode);
        }
    }

    private void fixRangeRef(ValueNode sourceNode, RangeReference rangeReference) {
        HotAreaDelegate hotArea = (HotAreaDelegate) getAssetManager().get(rangeReference.getHotAreaId());
        if (hotArea == null || !hotArea.isValid()) {
            rangeReference.setHotAreaId(Asset.UNSPECIFIED_ASSET_ID);
            rangeReference.setAlive(false);
            if (hotArea != null) {
                sourceNode.removeDependency(hotArea);
            }
            return;
        }

        if (!rangeReference.isPhantom()) {
            // TODO this looks redundant, may be share range object in the future.
            SimpleTableRange range = hotArea.getRange();
            // null index and null anchor should matches exactly.
            // may be do some check is better.
            if (rangeReference.getLeftAnchor() != null) {
                rangeReference.getLeftAnchor().setIndex(range.getLeft());
            }
            if (rangeReference.getTopAnchor() != null) {
                rangeReference.getTopAnchor().setIndex(range.getTop());
            }
            if (rangeReference.getRightAnchor() != null) {
                rangeReference.getRightAnchor().setIndex(range.getRight());
            }
            if (rangeReference.getBottomAnchor() != null) {
                rangeReference.getBottomAnchor().setIndex(range.getBottom());
            }
        }

        updateQualifiersIfNeeded(sourceNode, rangeReference, hotArea);
    }

    /**
     * Update row/column index of the reference by the cell's runtime ID. When a cell has moved,
     * use this method to keep the row/column index up to date.
     *
     * @param sourceNode    Source node that the <code>cellReference</code> comes from.
     * @param cellReference
     * @return true if reference has been modified, false otherwise.
     */
    private void fixCellRef(ValueNode sourceNode, CellReference cellReference) {
        if (!cellReference.isAlive()) {
            return;
        }
        HotAreaDelegate hotArea = (HotAreaDelegate) getAssetManager().get(cellReference.getHotAreaId());
        if (hotArea == null || !hotArea.isValid()) {
            cellReference.setHotAreaId(Asset.UNSPECIFIED_ASSET_ID);
            cellReference.setAlive(false);
            if (hotArea != null) {
                sourceNode.removeDependency(hotArea);
            }
            return;
        }

        PositionRef pos = cellReference.getPositionRef();
        pos.setRowIndex(hotArea.getRange().getTop());
        pos.setColumnIndex(hotArea.getRange().getLeft());

        updateQualifiersIfNeeded(sourceNode, cellReference, hotArea);
    }

    private void updateQualifiersIfNeeded(ValueNode sourceNode, AbstractReference reference,
                                          HotAreaDelegate hotArea) {
        DefaultTable table = hotArea.parent(DefaultTable.class);

        if (reference.getSheetName() != null ||
                sourceNode.parent(DefaultSheet.class) != table.getSheet()) {
            reference.setSheetName(table.getSheet().getName());
            reference.setAssetName(table.getName());

        } else if (reference.getAssetName() != null ||
                sourceNode.parent(SheetAssetNode.class) != table) {
            reference.setAssetName(table.getName());
        }
    }

    private DefaultWorkbook getWorkbook() {
        DefaultTable table = getTable();
        if (table == null) {
            return null;
        }
        return getTable().getWorkbook();
    }

    private DefaultTable getTable() {
        return (DefaultTable) getParent();
    }
}
