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

import com.ctrip.ferriswheel.common.variant.ErrorCodes;
import com.ctrip.ferriswheel.common.variant.Parameter;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.core.formula.*;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;
import com.ctrip.ferriswheel.core.ref.*;

import java.util.LinkedList;
import java.util.List;

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
        valueNode.setValid(true);

        if (valueNode.isFormula()) {
            Formula f = valueNode.getFormula();
            for (FormulaElement e : f) {
                if (e instanceof CellReferenceElement) {
                    CellReference cellReference = ((CellReferenceElement) e).getCellReference();
                    hookCellRef(valueNode, cellReference);
//                    traceRange(valueNode, cellReference);

                } else if (e instanceof NameReferenceElement) {
                    NameReference nameReference = ((NameReferenceElement) e).getNameReference();
                    hookNameRef(valueNode, nameReference);

                } else if (e instanceof RangeReferenceElement) {
                    RangeReference rangeReference = ((RangeReferenceElement) e).getRangeReference();
                    hookRangeRef(valueNode, rangeReference);
//                    traceRange(valueNode, rangeReference);
                }
            }
        }
    }

    private void hookCellRef(ValueNode fromNode, CellReference cellReference) {
        if (!cellReference.isAlive()) {
            return;
        }
        DefaultTable table = (DefaultTable) getReferredSheetAsset(cellReference,
                fromNode.parent(DefaultSheet.class),
                fromNode.parent(DefaultTable.class));
        if (table == null) {
            cellReference.setHotAreaId(Asset.UNSPECIFIED_ASSET_ID);
            cellReference.setAlive(false);
            return;
        }
        table.hookCell(cellReference, fromNode.getAssetId());
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
            rangeReference.setHotAreaId(Asset.UNSPECIFIED_ASSET_ID);
            rangeReference.setAlive(false);
            return;
//            throw new IllegalArgumentException();
        }
        if (table.getAutomaton() != null) {
            rangeReference.setPhantom(true); // actually this only need to be done once
        }
        table.hookArea(rangeReference, fromNode.getAssetId());
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
        if (!cellReference.isValid()) {
            return Value.err(ErrorCodes.REF);
        }
        DefaultCell cell = getReferredAsset(cellReference);
        if (cell == null) {
            if (cellReference.isPhantom()) {
                return Value.err(ErrorCodes.NA);
            } else {
                return Value.err(ErrorCodes.REF);
            }
        }
        return cell.getData();
    }

    @Override
    public Variant resolve(RangeReferenceElement referenceElement, FormulaEvaluationContext context) {
        RangeReference rangeReference = referenceElement.getRangeReference();
        if (!rangeReference.isAlive()) {
            return Value.err(ErrorCodes.REF);
        }

        HotAreaDelegate hotArea = (HotAreaDelegate) getAssetById(rangeReference.getHotAreaId());

        if (hotArea == null || !hotArea.isValid()) {
            return Value.err(ErrorCodes.REF);
        }

        DefaultTable table = hotArea.parent(DefaultTable.class);

        if (table == null) {
            // maybe throw an exception.
            return Value.err(ErrorCodes.REF);
        }

        SimpleTableRange validRange = hotArea.getValidRange();
        if (validRange == null) {
            return Value.BLANK;
        }

        List<Variant> valueList = new LinkedList<>();
        for (int rowIndex = validRange.getTop(); rowIndex <= validRange.getBottom(); rowIndex++) {
            DefaultRow row = table.getRow(rowIndex);
            for (int columnIndex = validRange.getLeft(); columnIndex <= validRange.getRight(); columnIndex++) {
                DefaultCell cell = row == null ? null : row.getCell(columnIndex);
                Variant data = cell == null ? Value.BLANK : cell.getData();
                valueList.add(Value.from(data));
            }
        }
        final int actualResultColumns = validRange.width();
        return new Value.ListValue(valueList, actualResultColumns);
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
    public DefaultCell getReferredAsset(CellReference cellReference) {
        if (!cellReference.isValid()) {
            return null;
        }
        HotAreaDelegate hotArea = (HotAreaDelegate) getAssetById(cellReference.getHotAreaId());
        SimpleTableRange validRange = hotArea.getValidRange();
        if (validRange == null) {
            throw new IllegalStateException("Cell reference is marked as valid but actually invalid.");
        }
        Integer one = Integer.valueOf(1);
        if (!one.equals(validRange.width()) || !one.equals(validRange.height())) {
            throw new IllegalStateException("Cell reference is marked as valid but actually malformed.");
        }
        DefaultTable table = hotArea.parent(DefaultTable.class);
        if (table == null) {
            throw new IllegalStateException("Cell reference is marked as valid but table is not found.");
        }
        return table.getCell(validRange.getTop(), validRange.getLeft());
    }

    @Override
    public AssetNode getReferredAsset(NameReference nameReference) {
        if (!nameReference.isValid()) {
            return null;
        }
        return getAssetById(nameReference.getTargetId());
    }

    @Override
    public AssetNode getAssetById(long assetId) {
        return workbook.get(assetId);
    }

}
