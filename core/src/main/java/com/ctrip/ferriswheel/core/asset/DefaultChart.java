package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.common.chart.Chart;
import com.ctrip.ferriswheel.common.chart.DataSeries;
import com.ctrip.ferriswheel.common.variant.DynamicValue;
import com.ctrip.ferriswheel.common.variant.DynamicVariant;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.common.view.Orientation;
import com.ctrip.ferriswheel.common.view.Placement;
import com.ctrip.ferriswheel.core.action.UpdateChart;
import com.ctrip.ferriswheel.core.bean.AxisImpl;
import com.ctrip.ferriswheel.core.bean.ChartData;
import com.ctrip.ferriswheel.core.formula.CellReferenceElement;
import com.ctrip.ferriswheel.core.formula.FormulaElement;
import com.ctrip.ferriswheel.core.formula.RangeReferenceElement;
import com.ctrip.ferriswheel.core.formula.ReferenceElement;
import com.ctrip.ferriswheel.core.ref.CellReference;
import com.ctrip.ferriswheel.core.ref.RangeReference;
import com.ctrip.ferriswheel.core.ref.TableRange;
import com.ctrip.ferriswheel.core.view.ChartLayout;
import com.ctrip.ferriswheel.core.view.LayoutImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultChart extends SheetAssetNode implements Chart {
    private String type;
    private final ValueNode title;
    private final ValueNode categories;
    private final AssetList<DefaultDataSeries> seriesList;
    private AxisImpl xAxis;
    private AxisImpl yAxis;
    private AxisImpl zAxis;
    private final ChartLayout layout = new ChartLayout();
    private DefaultChartBinder binder;

    DefaultChart(String name, String type, AssetManager assetManager) {
        super(name, assetManager);
        this.type = type;
        this.title = new ValueNode(getAssetManager(), Value.BLANK, null);
        this.categories = new ValueNode(getAssetManager(), Value.BLANK, null);
        this.seriesList = new AssetList<>(this);

        bindChild(title);
        bindChild(categories);
    }

    @Override
    public String toString() {
        return "DefaultChart{" +
                "type='" + type + '\'' +
                ", title=" + title +
                ", categories=" + categories +
                ", seriesList=" + seriesList +
                '}';
    }

    public void toString(StringBuilder sb) {
        sb.append(toString());
    }

    @Override
    public int getSeriesCount() {
        return seriesList == null ? 0 : seriesList.size();
    }

    @Override
    public DefaultDataSeries getSeries(int i) {
        return seriesList.get(i);
    }

    @Override
    public String getType() {
        return type;
    }

    void setType(String type) {
        this.type = type;
        markDirty();
    }

    @Override
    public ValueNode getTitle() {
        return title;
    }

    public void setTitle(DynamicVariant title) {
        this.title.setDynamicVariant(title);
    }

    @Override
    public ValueNode getCategories() {
        return categories;
    }

    public void setCategories(DynamicVariant categories) {
        this.categories.setDynamicVariant(categories);
    }

    @Override
    public List<DataSeries> getSeriesList() {
        return Collections.unmodifiableList(seriesList); // internal list is not allowed to modify directly.
    }

    public void addSeries(DynamicVariant name, DynamicVariant xValues, DynamicVariant yValues) {
        DefaultDataSeries series = new DefaultDataSeries(getAssetManager());
        if (name != null) {
            series.setName(new ValueNode(getAssetManager(), name));
        }
        if (xValues != null) {
            series.setxValues(new ValueNode(getAssetManager(), xValues));
        }
        if (yValues != null) {
            series.setyValues(new ValueNode(getAssetManager(), yValues));
        }
        seriesList.add(series);
    }

    void clearSeries() {
        seriesList.clear();
    }

    @Override
    public AxisImpl getxAxis() {
        return xAxis;
    }

    public void setxAxis(AxisImpl xAxis) {
        this.xAxis = xAxis;
        markDirty(); // TODO modifications via AxisImpl object itself is not tracked.
    }

    @Override
    public AxisImpl getyAxis() {
        return yAxis;
    }

    public void setyAxis(AxisImpl yAxis) {
        this.yAxis = yAxis;
        markDirty(); // TODO refer setxAxis
    }

    @Override
    public AxisImpl getzAxis() {
        return zAxis;
    }

    public void setzAxis(AxisImpl zAxis) {
        this.zAxis = zAxis;
        markDirty(); // TODO refer setxAxis
    }

    @Override
    public ChartLayout getLayout() {
        return layout;
    }

    @Override
    public DefaultChartBinder getBinder() {
        return binder;
    }

    void setBinder(DefaultChartBinder binder) {
        if (this.binder != null) {
            unbindChild(this.binder);
//            removeDependency(this.binder.getData());
        }
        this.binder = binder;
        if (this.binder != null) {
            bindChild(this.binder);
//            addDependency(this.binder.getData());
        }
    }

    void rebind() {
        if (binder == null) {
            throw new IllegalStateException("Binder is null.");
        }

        ValueNode data = binder.getData();
        if (!data.isFormula() || data.getFormula().getElementCount() != 1
                || !(data.getFormula().getElement(0) instanceof ReferenceElement)) {
            throw new RuntimeException("Data area must be a formula with single reference element.");
        }

        RangeReference rangeReference = null;
        FormulaElement elem = data.getFormula().getElement(0);
        if (elem instanceof CellReferenceElement) {
            CellReference cellReference = ((CellReferenceElement) elem).getCellReference();
            rangeReference = new RangeReference(cellReference, cellReference);
        } else if (elem instanceof RangeReferenceElement) {
            rangeReference = ((RangeReferenceElement) elem).getRangeReference();
        }
        DefaultSheet sheet = getSheet();
        if (rangeReference.getSheetName() != null) {
            sheet = sheet.getWorkbook().getSheet(rangeReference.getSheetName());
        }
        if (sheet == null) { // TODO mark illegal reference?
            clearData();
            getSheet().publicly(new UpdateChart(getSheet().getName(),
                    getName(), new ChartData(this)), () -> {
                // just publish the action
            });
            // throw new RuntimeException("Failed to get referred table.");
            return;
        }
        DefaultTable table = sheet.getAsset(rangeReference.getAssetName());
        if (table == null) { // TODO mark illegal reference?
            clearData();
            getSheet().publicly(new UpdateChart(getSheet().getName(),
                    getName(), new ChartData(this)), () -> {
                // just publish the action
            });
            // throw new RuntimeException("Failed to get referred table.");
            return;
        }

        TableRange validRange = rangeReference.getOverlappedRectangle(table);
        if (validRange == null) {
            clearData();
        } else if (binder.getOrientation() == Orientation.HORIZONTAL) {
            bindHorizontally(table, validRange.getLeft(), validRange.getTop(), validRange.getRight(), validRange.getBottom());
        } else if (binder.getOrientation() == Orientation.VERTICAL) {
            bindVertically(table, validRange.getLeft(), validRange.getTop(), validRange.getRight(), validRange.getBottom());
        }

        getSheet().publicly(new UpdateChart(getSheet().getName(),
                getName(), new ChartData(this)), () -> {
            // just publish the action
        });
    }

    private void clearData() {
        setCategories(new DynamicValue(Value.BLANK));
        clearSeries();
    }

    private void bindHorizontally(DefaultTable table, int left, int top, int right, int bottom) {
        boolean hasCategories = binder.getCategoriesPlacement() != null;
        boolean hasSeriesName = binder.getSeriesNamePlacement() != null;

        int dataStartRow = top;
        int dataEndRow = bottom;
        int dataStartColumn = left;
        int dataEndColumn = right;

        if (binder.getSeriesNamePlacement() == Placement.LEFT) {
            dataStartColumn++;
        } else if (binder.getSeriesNamePlacement() == Placement.RIGHT) {
            dataEndColumn--;
        } else if (hasSeriesName) {
            throw new RuntimeException("Illegal series name placement.");
        }

        if (binder.getCategoriesPlacement() == Placement.TOP) {
            dataStartRow++;
        } else if (binder.getCategoriesPlacement() == Placement.BOTTOM) {
            dataEndRow--;
        } else if (hasCategories) {
            throw new RuntimeException("Illegal categories placement.");
        }

        if (dataStartColumn > dataEndColumn || dataStartRow > dataEndRow) {
            clearData();
            return;
        }

        int rowIdx = -1, columnIdx;
        DynamicValue categories = null;

        if (binder.getCategoriesPlacement() == Placement.TOP) {
            rowIdx = top;
        } else if (binder.getCategoriesPlacement() == Placement.BOTTOM) {
            rowIdx = bottom;
        } else if (hasCategories) {
            throw new RuntimeException("Illegal categories placement.");
        }

        if (rowIdx != -1) {
            List<Variant> ls = new ArrayList<>(dataEndColumn - dataStartColumn + 1);
            for (int i = dataStartColumn; i <= dataEndColumn; i++) {
                ls.add(table.getCell(rowIdx, i).getData());
            }
            categories = new DynamicValue(Value.list(ls));
        }

        setCategories(categories);
        clearSeries();

        for (rowIdx = dataStartRow; rowIdx <= dataEndRow; rowIdx++) {
            columnIdx = -1;
            DynamicValue name = null;
            DynamicValue xValues = null;
            DynamicValue yValues = null;
            DynamicValue zValues = null;

            if (binder.getSeriesNamePlacement() == Placement.LEFT) {
                columnIdx = left;
            } else if (binder.getSeriesNamePlacement() == Placement.RIGHT) {
                columnIdx = right;
            }

            if (columnIdx != -1) {
                name = new DynamicValue(Value.from(table.getCell(rowIdx, columnIdx).getData()));
            }

            List<Variant> ls = new ArrayList<>(dataEndColumn - dataStartColumn + 1);
            for (int i = dataStartColumn; i <= dataEndColumn; i++) {
                ls.add(table.getCell(rowIdx, i).getData());
            }
            yValues = new DynamicValue(Value.list(ls));

            addSeries(name, xValues, yValues);
        }
    }

    private void bindVertically(DefaultTable table, int left, int top, int right, int bottom) {
        boolean hasCategories = binder.getCategoriesPlacement() != null;
        boolean hasSeriesName = binder.getSeriesNamePlacement() != null;

        int dataStartRow = top;
        int dataEndRow = bottom;
        int dataStartColumn = left;
        int dataEndColumn = right;

        if (binder.getSeriesNamePlacement() == Placement.TOP) {
            dataStartRow++;
        } else if (binder.getSeriesNamePlacement() == Placement.BOTTOM) {
            dataEndRow--;
        } else if (hasSeriesName) {
            throw new RuntimeException("Illegal series name placement.");
        }

        if (binder.getCategoriesPlacement() == Placement.LEFT) {
            dataStartColumn++;
        } else if (binder.getCategoriesPlacement() == Placement.RIGHT) {
            dataEndColumn--;
        } else if (hasCategories) {
            throw new RuntimeException("Illegal categories placement.");
        }

        if (dataStartColumn > dataEndColumn || dataStartRow > dataEndRow) {
            clearData();
            return;
        }

        int rowIdx, columnIdx = -1;
        DynamicValue categories = null;

        if (binder.getCategoriesPlacement() == Placement.LEFT) {
            columnIdx = left;
        } else if (binder.getCategoriesPlacement() == Placement.RIGHT) {
            columnIdx = right;
        } else if (hasCategories) {
            throw new RuntimeException("Illegal categories placement.");
        }

        if (columnIdx != -1) {
            List<Variant> ls = new ArrayList<>(dataEndRow - dataStartRow + 1);
            for (int i = dataStartRow; i <= dataEndRow; i++) {
                ls.add(table.getCell(i, columnIdx).getData());
            }
            categories = new DynamicValue(Value.list(ls));
        }

        setCategories(categories);
        clearSeries();

        for (columnIdx = dataStartColumn; columnIdx <= dataEndColumn; columnIdx++) {
            rowIdx = -1;
            DynamicValue name = null;
            DynamicValue xValues = null;
            DynamicValue yValues = null;
            DynamicValue zValues = null;

            if (binder.getSeriesNamePlacement() == Placement.TOP) {
                rowIdx = top;
            } else if (binder.getSeriesNamePlacement() == Placement.BOTTOM) {
                rowIdx = bottom;
            }

            if (rowIdx != -1) {
                name = new DynamicValue(Value.from(table.getCell(rowIdx, columnIdx).getData()));
            }

            List<Variant> ls = new ArrayList<>(dataEndRow - dataStartRow + 1);
            for (int i = dataStartRow; i <= dataEndRow; i++) {
                ls.add(table.getCell(i, columnIdx).getData());
            }
            yValues = new DynamicValue(Value.list(ls));

            addSeries(name, xValues, yValues);
        }
    }

    void rebindIfPossible() {
        if (binder == null) {
            return;
        }
        if (getSheet() == null
                || getSheet().getWorkbook() == null
                || !getSheet().getWorkbook().isSkipWelding()) {
            rebind();
        }
    }

    @Override
    protected EvaluationState doEvaluate(EvaluationContext context) {
        rebindIfPossible();
        return EvaluationState.DONE;
    }
}
