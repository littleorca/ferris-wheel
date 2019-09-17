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

import com.ctrip.ferriswheel.common.Sheet;
import com.ctrip.ferriswheel.common.table.Cell;
import com.ctrip.ferriswheel.common.table.Table;
import com.ctrip.ferriswheel.common.variant.ErrorCodes;
import com.ctrip.ferriswheel.common.variant.Parameter;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.core.formula.*;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;
import com.ctrip.ferriswheel.core.ref.*;

public class DefaultReferenceMaintainer implements ReferenceMaintainer {
    private final DefaultWorkbook workbook;

    public DefaultReferenceMaintainer(DefaultWorkbook workbook) {
        this.workbook = workbook;
    }

    @Override
    public void resolveFormulas(AssetNode asset) {
        if (asset instanceof ValueNode) {
            resolveFormula((ValueNode) asset);
        }
        for (AssetNode child : asset.getChildren()) {
            resolveFormulas(child);
        }
    }

    /**
     * This method resolve formula and set reference anchor (target asset ID),
     * and also updates dependencies. MAYBE split those two step for convenience
     * to do ops like resettle without update dependencies.
     *
     * @param valueNode
     */
    @Override
    public void resolveFormula(ValueNode valueNode) {
        if (workbook.isSkipWelding()) {
            return; // should manually resolve later
        }
        valueNode.clearDependencies();
//        dependencyTracer.clearRangeDependencies(valueNode.getAssetId());

        // TODO review if it is needed
//        if (table != null) {
//            valueNode.addDependency(table);
//        } else if (valueNode.getParentAsset() instanceof DefaultChart) {
//            valueNode.addDependency(valueNode.getParentAsset());
//        }

        if (valueNode.isFormula()) {
            Formula f = valueNode.getFormula();
            for (FormulaElement e : f.getElements()) {
                if (e instanceof CellReferenceElement) {
                    CellReference cellReference = ((CellReferenceElement) e).getCellReference();
                    hookCellRef(valueNode, cellReference);
                    traceRange(valueNode, cellReference);

                } else if (e instanceof NameReferenceElement) {
                    NameReference nameReference = ((NameReferenceElement) e).getNameReference();
                    hookNameRef(valueNode, nameReference);

                } else if (e instanceof RangeReferenceElement) {
                    RangeReference rangeReference = ((RangeReferenceElement) e).getRangeReference();
                    hookRangeRef(valueNode, rangeReference);
                    traceRange(valueNode, rangeReference);
                }
            }
        }
    }

    @Override
    public void hookCellRef(ValueNode fromNode, CellReference cellReference) {
        if (!cellReference.isAlive()) {
            return;
        }
        DefaultTable table = (DefaultTable) getReferredSheetAsset(cellReference,
                fromNode.parent(DefaultSheet.class),
                fromNode.parent(DefaultTable.class));
        if (table == null) {
            cellReference.setAlive(false);
            return;
        }
        if (table.getAutomaton() != null) {
            cellReference.setPhantom(true); // actually this only need to be done once
        }
        final int rowIndex = cellReference.getPositionRef().getRowIndex();
        final int columnIndex = cellReference.getPositionRef().getColumnIndex();
        if (rowIndex < 0 || rowIndex >= table.getRowCount() ||
                columnIndex < 0 || columnIndex >= table.getColumnCount()) {
            if (cellReference.isPhantom()) {
                cellReference.setCellId(DefaultCell.UNSPECIFIED_ASSET_ID);
                fromNode.addDependency(table);
            } else {
                cellReference.setAlive(false);
            }
            return;
        }
        DefaultCell depCell = table.getCell(rowIndex, columnIndex);
        cellReference.setCellId(depCell.getAssetId()); // fill runtime id for convenience.
        if (cellReference.isPhantom()) {
            fromNode.addDependency(table);
        } else {
            fromNode.addDependency(depCell);
        }
    }

    private void hookNameRef(ValueNode fromNode, NameReference nameReference) {
        if (!nameReference.isValid()) {
            return;
        }
        SheetAssetNode asset = getReferredSheetAsset(nameReference, fromNode.parent(DefaultSheet.class), fromNode.parent(SheetAssetNode.class));
        // Currently only parameter of the query table supports name reference
        if (asset == null || !(asset instanceof DefaultTable)) {
            return;
        }
        DefaultTable table = (DefaultTable) asset;
        if (table.getAutomaton() == null || !(table.getAutomaton() instanceof DefaultQueryAutomaton)) {
            return;
        }
        DefaultQueryTemplate template = ((DefaultQueryAutomaton) table.getAutomaton()).getTemplate();
        Parameter param = template.getBuiltinParam(nameReference.getTargetName());
        if (param == null) {
            return;
        }
        ValueNode targetNode = (ValueNode) param.getValue();
        nameReference.setTargetId(targetNode.getAssetId());
        fromNode.addDependency(targetNode);
    }

    private void hookRangeRef(ValueNode fromNode, RangeReference rangeReference) {
        if (!rangeReference.isAlive()) {
            return;
        }
        DefaultTable table = (DefaultTable) getReferredSheetAsset(rangeReference,
                fromNode.parent(DefaultSheet.class),
                fromNode.parent(DefaultTable.class));
        if (table == null) {
            rangeReference.setAlive(false);
            return;
//            throw new IllegalArgumentException();
        }
        if (table.getAutomaton() != null) {
            rangeReference.setPhantom(true); // actually this only need to be done once
        }
        // fill runtime id for convenience.
        final int left = rangeReference.getLeft() == -1 ? 0 : rangeReference.getLeft();
        final int top = rangeReference.getTop() == -1 ? 0 : rangeReference.getTop();
        final int right = rangeReference.getRight() != -1 ? rangeReference.getRight() :
                table.getColumnCount() > 0 ? table.getColumnCount() - 1 : 0;
        final int bottom = rangeReference.getBottom() != -1 ? rangeReference.getBottom() :
                table.getRowCount() > 0 ? table.getRowCount() - 1 : 0;
        if (top >= 0 && top < table.getRowCount() && left >= 0 && left < table.getColumnCount()) {
            DefaultCell upperLeft = table.getCell(top, left);
            rangeReference.setUpperLeftTargetId(upperLeft.getAssetId());
        }
        if (bottom >= 0 && bottom < table.getRowCount() && right >= 0 && right < table.getColumnCount()) {
            DefaultCell lowerRight = table.getCell(bottom, right);
            rangeReference.setLowerRightTargetId(lowerRight.getAssetId());
        }

        if (rangeReference.isPhantom()) {
            fromNode.addDependency(table);

        } else {
            // scan dependencies
            for (int row = Math.max(top, 0); row <= bottom && row < table.getRowCount(); row++) {
                for (int col = Math.max(left, 0); col <= right && col < table.getColumnCount(); col++) {
                    DefaultCell depCell = table.getCell(row, col);
                    if (depCell != null) {
                        fromNode.addDependency(depCell);
                    }
                }
            }
        }
    }

    private void traceRange(ValueNode fromNode, CellReference cellReference) {
        if (!cellReference.isAlive()) {
            return;
        }
        DefaultTable referredTable = (DefaultTable) getReferredSheetAsset(cellReference,
                fromNode.parent(DefaultSheet.class),
                fromNode.parent(DefaultTable.class));
        PositionRef positionRef = cellReference.getPositionRef();
        fromNode.watchRange(referredTable,
                positionRef.getColumnIndex(),
                positionRef.getRowIndex(),
                positionRef.getColumnIndex(),
                positionRef.getRowIndex());
    }

    private void traceRange(ValueNode fromNode, RangeReference rangeReference) {
        DefaultTable targetTable = (DefaultTable) getReferredSheetAsset(rangeReference,
                fromNode.parent(DefaultSheet.class),
                fromNode.parent(DefaultTable.class));

        if (targetTable == null) {
            return; // TODO is that ok?
        }

        if (rangeReference.isAlive()) {
            fromNode.watchRange(targetTable,
                    rangeReference.getLeft(),
                    rangeReference.getTop(),
                    rangeReference.getRight(),
                    rangeReference.getBottom());

//        } else if (rangeReference.getUpperLeft().isAlive()) {
//            fromNode.watchRange(targetTable,
//                    rangeReference.getUpperLeft().getRowIndex(),
//                    rangeReference.getUpperLeft().getColumnIndex());
//
//        } else if (rangeReference.getLowerRight().isAlive()) {
//            fromNode.watchRange(targetTable,
//                    rangeReference.getLowerRight().getRowIndex(),
//                    rangeReference.getLowerRight().getColumnIndex());
        }
    }

    private SheetAssetNode getReferredSheetAsset(AbstractReference ref, DefaultSheet currentSheet, SheetAssetNode currentAsset) {
        if (ref.getAssetName() == null) {
            return currentAsset;
        }
        DefaultSheet sheet = getReferredSheet(ref, currentSheet);
        return sheet == null ? null : sheet.getAsset(ref.getAssetName());
    }

    private DefaultSheet getReferredSheet(AbstractReference ref, DefaultSheet currentSheet) {
        if (ref.getSheetName() == null) {
            return currentSheet;
        } else {
            return workbook.getSheet(ref.getSheetName());
        }
    }

    @Override
    public Variant resolve(CellReferenceElement referenceElement, FormulaEvaluationContext context) {
        CellReference cellReference = referenceElement.getCellReference();
        if (!cellReference.isAlive()) {
            return Value.err(ErrorCodes.REF);
        }
        if (cellReference.getCellId() != Asset.UNSPECIFIED_ASSET_ID) {
            Asset asset = workbook.getAssetManager().get(cellReference.getCellId());
            if (asset == null || !(asset instanceof Cell)) {
                return Value.err(ErrorCodes.REF);
            }
            return ((DefaultCell) asset).getData();
        }
        return Value.err(ErrorCodes.REF); // cell not found
    }

    // TODO compare to another resolve method, this violates DRY principle!
    // Types of references may change in the future, review this by that time.
    @Override
    public Variant resolve(NameReferenceElement referenceElement, FormulaEvaluationContext context) {
        NameReference nameReference = referenceElement.getNameReference();
        if (!nameReference.isValid()) {
            return Value.err(ErrorCodes.REF);
        }
        if (nameReference.getTargetId() != Asset.UNSPECIFIED_ASSET_ID) {
            Asset asset = workbook.getAssetManager().get(nameReference.getTargetId());
            if (asset == null || !(asset instanceof ValueNode)) {
                return Value.err(ErrorCodes.REF);
            }
            return ((ValueNode) asset).getData();
        }
        return Value.err(ErrorCodes.REF);
    }

    @Override
    public Table resolveTable(String sheetName, String tableName, FormulaEvaluationContext context) {
        if (sheetName == null && tableName == null) {
            return (Table) context.getCurrentAsset();
        }
        Sheet sheet = (sheetName != null) ? workbook.getSheet(sheetName) : context.getCurrentSheet();
        if (sheet == null) {
            return null; // or throw new RuntimeException();
        }
        return sheet.getAsset(tableName);
    }

    @Override
    public Asset getAssetById(long assetId) {
        return workbook.get(assetId);
    }

}
