package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.common.query.DataSet;
import com.ctrip.ferriswheel.common.table.*;
import com.ctrip.ferriswheel.common.variant.impl.DynamicVariantImpl;
import com.ctrip.ferriswheel.common.variant.impl.Value;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.core.analysis.DimensionalAggregator;
import com.ctrip.ferriswheel.core.bean.*;
import com.ctrip.ferriswheel.core.formula.RangeReferenceElement;
import com.ctrip.ferriswheel.core.loader.DataSetBuilder;
import com.ctrip.ferriswheel.core.ref.RangeRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DefaultPivotAutomaton extends AbstractTableAutomaton implements PivotAutomaton {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultPivotAutomaton.class);
    private final ValueNode data;
    private List<PivotFilter> filters; // TODO
    private List<FieldMeta> rows;
    private List<FieldMeta> columns;
    private List<ValueMeta> values;

    DefaultPivotAutomaton(AssetManager assetManager, PivotConfiguration pivot) {
        super(assetManager);

        this.data = new ValueNode(getAssetManager(), Value.BLANK, null);
        this.filters = Collections.emptyList();
        this.rows = Collections.emptyList();
        this.columns = Collections.emptyList();
        this.values = Collections.emptyList();

        bindChild(this.data);

        try {
            updatePivot(pivot);
        } catch (RuntimeException e) {
            LOG.warn("Failed to update pivot table.", e);
            // TODO mark error
            clearTable();
        }
    }

    private void updatePivot(PivotConfiguration pivot) {
        if (pivot.getData() == null || pivot.getRows() == null || pivot.getValues() == null) {
            throw new IllegalArgumentException("Missing at least one of the required fields: [data, rows, values].");
        }
        if (!pivot.getData().isFormula()) {
            throw new IllegalArgumentException("Data area must be specified by a range reference formula.");
        }
        this.data.setDynamicVariant(pivot.getData());
        if (this.data.getFormulaElements() == null
                || this.data.getFormulaElements().length != 1
                || !(this.data.getFormulaElements()[0] instanceof RangeReferenceElement)) {
            this.data.setFormula(null);
            this.data.setValue(Value.BLANK);
            this.data.clearDependencies();
            throw new IllegalArgumentException("Data area formula must be a simple range reference.");
        }

        if (pivot.getFilters() != null) {
//            pivot.getFilters(); // TODO
        }

        this.rows = new ArrayList<>(pivot.getRows().size());
        for (PivotField row : pivot.getRows()) {
            this.rows.add(new FieldMeta(row, -1)); // make copy to avoid being modified outside.
        }

        if (pivot.getColumns() != null) {
            this.columns = new ArrayList<>(pivot.getColumns().size());
            for (PivotField column : pivot.getColumns()) {
                this.columns.add(new FieldMeta(column, -1));
            }
        }

        this.values = new ArrayList<>(pivot.getValues().size());
        for (PivotValue value : pivot.getValues()) {
            this.values.add(new ValueMeta(value, -1));
        }
    }

    /**
     * Do not call this method manually. this method exists for workbook
     */
    @Override
    public void execute(boolean forceUpdate) {
        if (!forceUpdate && getLastUpdateSequenceNumber() > data.getLastUpdateSequenceNumber()) {
            return;
        }
        // TODO remove this debugging code.
        LOG.info("Executing pivot automaton with forceUpdate=" + forceUpdate);
        try {
            doExecute();
        } catch (RuntimeException e) {
            LOG.warn("Failed to execute pivot automaton.", e);
            // TODO mark error.
            clearTable();
        }
    }

    protected void doExecute() {
        RangeReferenceElement rangeElement = (RangeReferenceElement) data.getFormulaElements()[0];
        RangeRef rangeRef = rangeElement.getRangeRef();

        DefaultSheet sourceSheet = rangeRef.sheetName() == null ?
                getTable().getSheet() : getTable().getWorkbook().getSheet(rangeRef.sheetName());
        DefaultTable sourceTable = sourceSheet.getAsset(rangeRef.tableName());
        final int left = rangeRef.getLeft() >= 0 ? rangeRef.getLeft() : 0;
        final int top = rangeRef.getTop() >= 0 ? rangeRef.getTop() : 0;
        final int right = rangeRef.getRight() >= 0 ? rangeRef.getRight() : sourceTable.getColumnCount() - 1;
        final int bottom = rangeRef.getBottom() >= 0 ? rangeRef.getBottom() : sourceTable.getRowCount() - 1;

        DataSet dataSet;

        if (left <= right && top < bottom) {
            dataSet = analyse(sourceTable, left, top, right, bottom);
        } else {
            dataSet = DataSetBuilder.emptyDataSet();
        }

        // fill table
        DefaultTable table = getTable();
        fillTable(table, dataSet);
        setLastUpdateSequenceNumber(parent(DefaultWorkbook.class).nextSequenceNumber());
        getTable().getWorkbook().onAutomatonExecuted(getTable());
    }

    private DataSet analyse(final DefaultTable table,
                            final int left,
                            final int top,
                            final int right,
                            final int bottom) {
//        if (columns.size() > 1 || rows.size() > 1 || values.size() != 1) {
//            throw new RuntimeException("Multiple fields/values pivot table is not supported yet!");
//        }

        // map field -> column index
        analyseHeader(table, top, left, right);

        // aggregate
        DimensionalAggregator aggregator = aggregate(table, left, top + 1, right, bottom);

        // expand column/row dimensions

        List<Dimension[]> allColumnDimensions = combineDimensions(aggregator, columns);
        List<Dimension[]> allRowDimensions = combineDimensions(aggregator, rows);

        // prepare data set

        DataSetBuilder dataSetBuilder = new DataSetBuilder()
                .setColumnCount(rows.size() + allColumnDimensions.size() * values.size())
                .setHasRowMeta(false);

        // add headers

        for (int i = 0; i < columns.size(); i++) {
            DataSetBuilder.DataSetRecordBuilder recordBuilder = dataSetBuilder.newRecord();
            int fieldOffset = rows.size();
            for (Dimension[] columnDimensions : allColumnDimensions) {
                for (ValueMeta ignored : values) {
                    recordBuilder.set(fieldOffset++, columnDimensions[i].value);
                }
            }
            recordBuilder.commit();
        }
        if (values.size() > 1) {
            DataSetBuilder.DataSetRecordBuilder valueNameRow = dataSetBuilder.newRecord();
            int fieldOffset = rows.size();
            for (int i = 0; i < allColumnDimensions.size(); i++) {
                for (ValueMeta valueMeta : values) {
                    valueNameRow.set(fieldOffset++, Value.str(valueMeta.getLabel()));
                }
            }
            valueNameRow.commit();
        }

        // extract data

        for (Dimension[] rowDimensions : allRowDimensions) {
            DataSetBuilder.DataSetRecordBuilder recordBuilder = dataSetBuilder.newRecord();
            int fieldIdx = 0;
            Map<String, Variant> rowDimMap = new HashMap<>();
            for (Dimension rowDimension : rowDimensions) {
                rowDimMap.put(rowDimension.getName(), rowDimension.getValue());
                recordBuilder.set(fieldIdx++, rowDimension.getValue());
            }

            for (Dimension[] columnDimensions : allColumnDimensions) {
                Map<String, Variant> allDimMap = new HashMap<>(rowDimMap);
                for (Dimension columnDimension : columnDimensions) {
                    allDimMap.put(columnDimension.getName(), columnDimension.getValue());
                }
                Variant[] aggValues = aggregator.getValues(allDimMap);
                for (int i = 0; i < values.size(); i++) {
                    Variant val = null;
                    if (aggValues != null) {
                        val = aggValues[i];
                    }
                    if (val == null) {
                        val = Value.BLANK;
                    }
                    recordBuilder.set(fieldIdx++, val);
                }
            }
            recordBuilder.commit();
        }

        return dataSetBuilder.build();
    }

    private Map<String, Integer> analyseHeader(DefaultTable table, int row, int left, int right) {
        HashMap<String, Integer> fieldMap = new HashMap<>(right - left + 1);
        for (int i = left; i <= right; i++) {
            DefaultCell cell = table.getCell(row, i);
            if (cell.isValid() && !cell.isBlank()) {
                fieldMap.put(cell.strValue(), i);
            }
        }

        for (FieldMeta meta : rows) {
            if (!fieldMap.containsKey(meta.getField())) {
                throw new RuntimeException("Field \"" + meta.getField() + "\" not found.");
            }
            meta.setColumnIndex(fieldMap.get(meta.getField()));
        }
        for (FieldMeta meta : columns) {
            if (!fieldMap.containsKey(meta.getField())) {
                throw new RuntimeException("Field \"" + meta.getField() + "\" not found.");
            }
            meta.setColumnIndex(fieldMap.get(meta.getField()));
        }
        for (ValueMeta meta : values) {
            if (!fieldMap.containsKey(meta.getField())) {
                throw new RuntimeException("Field \"" + meta.getField() + "\" not found.");
            }
            meta.setColumnIndex(fieldMap.get(meta.getField()));
        }

        return fieldMap;
    }

    private DimensionalAggregator aggregate(DefaultTable table, int left, int top, int right, int bottom) {
        AggregateType[] types = new AggregateType[values.size()];
        for (int i = 0; i < values.size(); i++) {
            types[i] = values.get(i).getAggregateType();
        }
        DimensionalAggregator aggregator = new DimensionalAggregator(types);

        for (int rowIndex = top; rowIndex <= bottom; rowIndex++) {
            DimensionalAggregator.Record record = aggregator.newRecord();
            for (FieldMeta field : columns) {
                DefaultCell cell = table.getCell(rowIndex, field.getColumnIndex());
                record.dim(field.getField(), cell.getData());
            }
            for (FieldMeta field : rows) {
                DefaultCell cell = table.getCell(rowIndex, field.getColumnIndex());
                record.dim(field.getField(), cell.getData());
            }
            for (int i = 0; i < values.size(); i++) {
                DefaultCell cell = table.getCell(rowIndex, values.get(i).getColumnIndex());
                record.val(i, cell.getData());
            }
            record.commit();
        }
        return aggregator;
    }

    private List<Dimension[]> combineDimensions(DimensionalAggregator aggregator, List<FieldMeta> fields) {
        Dimension[][] rowDimensions = new Dimension[fields.size()][];
        for (int i = 0; i < fields.size(); i++) {
            FieldMeta meta = fields.get(i);
            Set<Variant> dimensions = aggregator.getAllDimensions().get(meta.getField());
            dimensions = new TreeSet<>(dimensions);
            List<Dimension> dimList = new ArrayList<>(dimensions.size());
            for (Variant dim : dimensions) {
                dimList.add(new Dimension(meta.getField(), dim));
            }
            rowDimensions[i] = dimList.toArray(new Dimension[dimList.size()]);
        }

        List<Dimension[]> combinations = new ArrayList<>();
        Dimension[] pendingCombination = new Dimension[rowDimensions.length];
        combineDimensions(combinations, pendingCombination, rowDimensions, 0);
        return combinations;
    }

    private void combineDimensions(final List<Dimension[]> combinations,
                                   final Dimension[] pendingCombination,
                                   final Dimension[][] allDimensions,
                                   final int dimensionsIndex) {
        if (dimensionsIndex >= allDimensions.length) {
            combinations.add(Arrays.copyOf(pendingCombination, pendingCombination.length));
            return;
        }
        final Dimension[] currentDimensions = allDimensions[dimensionsIndex];
        for (int i = 0; i < currentDimensions.length; i++) {
            pendingCombination[dimensionsIndex] = currentDimensions[i];
            combineDimensions(combinations,
                    pendingCombination,
                    allDimensions,
                    dimensionsIndex + 1);
        }
    }

    public TableAutomatonInfo.PivotAutomatonInfo getPivotAutomatonInfo() {
        List<PivotFilter> filterList = new ArrayList<>(filters.size());
        List<PivotField> rowList = new ArrayList<>(rows.size());
        List<PivotField> columnList = new ArrayList<>(columns.size());
        List<PivotValue> valueList = new ArrayList<>(values.size());

        for (FieldMeta fm : rows) {
            rowList.add(new PivotFieldImpl(fm));
        }
        for (FieldMeta fm : columns) {
            columnList.add(new PivotFieldImpl(fm));
        }
        for (ValueMeta vm : values) {
            valueList.add(new PivotValueImpl(vm));
        }

        return new TableAutomatonInfo.PivotAutomatonInfo(
                new DynamicVariantImpl(data.getFormulaString()), filterList, rowList, columnList, valueList);
    }

    public ValueNode getData() {
        return data;
    }

    public List<PivotFilter> getFilters() {
        return filters;
    }

    public List<PivotField> getRows() {
        return Collections.unmodifiableList(rows);
    }

    public List<PivotField> getColumns() {
        return Collections.unmodifiableList(columns);
    }

    public List<PivotValue> getValues() {
        return Collections.unmodifiableList(values);
    }

    class FieldMeta extends PivotFieldImpl {
        private int columnIndex;

        public FieldMeta(PivotField field, int columnIndex) {
            super(field);
            this.columnIndex = columnIndex;
        }

        public int getColumnIndex() {
            return columnIndex;
        }

        public void setColumnIndex(int columnIndex) {
            this.columnIndex = columnIndex;
        }
    }

    class ValueMeta extends PivotValueImpl {
        private int columnIndex;

        public ValueMeta(PivotValue value, int columnIndex) {
            super(value);
            this.columnIndex = columnIndex;
        }

        public int getColumnIndex() {
            return columnIndex;
        }

        public void setColumnIndex(int columnIndex) {
            this.columnIndex = columnIndex;
        }
    }

    class Dimension {
        private String name;
        private Variant value;

        public Dimension(String name, Variant value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Variant getValue() {
            return value;
        }

        public void setValue(Variant value) {
            this.value = value;
        }
    }
}
