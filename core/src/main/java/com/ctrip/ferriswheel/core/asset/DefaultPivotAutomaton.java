package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.common.aggregate.AggregateType;
import com.ctrip.ferriswheel.common.aggregate.NamedValuesSample;
import com.ctrip.ferriswheel.common.automaton.*;
import com.ctrip.ferriswheel.common.table.Row;
import com.ctrip.ferriswheel.common.util.DataSet;
import com.ctrip.ferriswheel.common.util.ListDataSet;
import com.ctrip.ferriswheel.common.util.StylizedValue;
import com.ctrip.ferriswheel.common.variant.DynamicValue;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.core.analysis.AggregateMeta;
import com.ctrip.ferriswheel.core.analysis.DimensionalAggregateMaster;
import com.ctrip.ferriswheel.core.bean.PivotFieldImpl;
import com.ctrip.ferriswheel.core.bean.PivotValueImpl;
import com.ctrip.ferriswheel.core.bean.TableAutomatonInfo;
import com.ctrip.ferriswheel.core.formula.RangeReferenceElement;
import com.ctrip.ferriswheel.core.ref.RangeReference;
import com.ctrip.ferriswheel.core.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DefaultPivotAutomaton extends AbstractAutomaton implements PivotAutomaton {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultPivotAutomaton.class);
    private final ValueNode data;
    private List<PivotFilter> filters; // TODO
    private List<PivotField> rows;
    private List<PivotField> columns;
    private List<com.ctrip.ferriswheel.common.automaton.PivotValue> values;
    private DataSet dataSet;

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
            // TODO clearTable();
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
            this.filters = pivot.getFilters();
        }
        if (pivot.getRows() != null) {
            this.rows = pivot.getRows();
        }
        if (pivot.getColumns() != null) {
            this.columns = pivot.getColumns();
        }
        if (pivot.getValues() != null) {
            this.values = pivot.getValues();
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
            this.dataSet = doExecute();
            setLastUpdateSequenceNumber(parent(DefaultWorkbook.class).nextSequenceNumber());
        } catch (RuntimeException e) {
            LOG.warn("Failed to execute pivot automaton.", e);
            // TODO mark error.
        }
    }

    protected DataSet doExecute() {
        RangeReferenceElement rangeElement = (RangeReferenceElement) data.getFormulaElements()[0];
        RangeReference rangeReference = rangeElement.getRangeReference();

        DefaultSheet sourceSheet = rangeReference.getSheetName() == null ?
                getTable().getSheet() : getTable().getWorkbook().getSheet(rangeReference.getSheetName());
        DefaultTable sourceTable = sourceSheet.getAsset(rangeReference.getAssetName());
        final int left = rangeReference.getLeft() >= 0 ? rangeReference.getLeft() : 0;
        final int top = rangeReference.getTop() >= 0 ? rangeReference.getTop() : 0;
        final int right = rangeReference.getRight() >= 0 ? rangeReference.getRight() : sourceTable.getColumnCount() - 1;
        final int bottom = rangeReference.getBottom() >= 0 ? rangeReference.getBottom() : sourceTable.getRowCount() - 1;

        DataSet dataSet;

        if (left <= right && top < bottom) {
            dataSet = analyse(sourceTable, left, top, right, bottom);
        } else {
            dataSet = ListDataSet.Builder.emptyDataSet();
        }

        return dataSet;
    }

    @Override
    public DataSet getDataSet() {
        return dataSet;
    }

    /**
     * TODO values should be able to present as either pivot column or row, and users should
     * be allowed to change values' level among other pivot fields. For now value is hardcoded
     * as the last level of the column fields.
     *
     * @param table
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @return
     */
    private DataSet analyse(final DefaultTable table,
                            final int left,
                            final int top,
                            final int right,
                            final int bottom) {
//        if (columns.size() > 1 || rows.size() > 1 || values.size() != 1) {
//            throw new RuntimeException("Multiple fields/values pivot table is not supported yet!");
//        }

        // map field -> column index
        Map<String, Integer> nameMapper = analyseHeader(table, top, left, right);

        // aggregate
        DimensionalAggregateMaster aggregator = aggregate(nameMapper, table, left, top + 1, right, bottom);

        // expand column/row dimensions

        List<Dimension[]> allColumnDimensions = combineDimensions(aggregator, columns);
        List<Dimension[]> allRowDimensions = combineDimensions(aggregator, rows);

        if (!values.isEmpty()) { // make sure both column and row are not empty
            if (allColumnDimensions.isEmpty()) {
                allColumnDimensions.add(new Dimension[0]); // default column
            }
            if (allRowDimensions.isEmpty()) {
                allRowDimensions.add(new Dimension[0]); // default row
            }
        }

        if (values.isEmpty() && allColumnDimensions.isEmpty() && allRowDimensions.isEmpty()) {
            return ListDataSet.Builder.emptyDataSet();
        }

        // prepare data set

        ListDataSet.Builder dataSetBuilder = ListDataSet.newBuilder()
                .setColumnCount((rows.isEmpty() ? 0 : 1) + allColumnDimensions.size() * values.size());

        // add headers

        ListDataSet.RecordBuilder headerBuilder = dataSetBuilder.newRecordBuilder();

        if (!rows.isEmpty()) {
            StringBuilder name = new StringBuilder();
            for (PivotField row : rows) {
                if (name.length() > 0) {
                    name.append("/");
                }
                name.append(row.getField());
            }
            headerBuilder.set(0, Value.str(name.toString()));
        }

        int fieldOffset = rows.isEmpty() ? 0 : 1;
        for (Dimension[] columnDimensions : allColumnDimensions) {
            if (columnDimensions.length == 1 && values.size() <= 1) {
                // For single variant just keep the variant.
                headerBuilder.set(fieldOffset++, columnDimensions[0].getValue());

            } else { // For multiple variant we join there string values.
                StringBuilder namePrefix = new StringBuilder();
                for (Dimension columnDimension : columnDimensions) {
                    if (namePrefix.length() > 0) {
                        namePrefix.append("\n");
                    }
                    namePrefix.append(columnDimension.getValue().strValue());
                }
                for (com.ctrip.ferriswheel.common.automaton.PivotValue value : values) {
                    String name = namePrefix.toString();
                    if (values.size() > 1 || name.isEmpty()) {
                        if (!name.isEmpty()) {
                            name += "\n";
                        }
                        name += StringUtils.isNullOrEmpty(value.getLabel()) ? value.getField() : value.getLabel();
                    }
                    headerBuilder.set(fieldOffset++, Value.str(name));
                }
            }
        }
        headerBuilder.commit();

        // extract data

        for (Dimension[] rowDimensions : allRowDimensions) {
            ListDataSet.RecordBuilder recordBuilder = dataSetBuilder.newRecordBuilder();
            int fieldIdx = 0;
            Map<String, Variant> rowDimMap = new HashMap<>();

            if (rowDimensions.length == 1) { // For single variant just keep the variant.
                rowDimMap.put(rowDimensions[0].getName(), rowDimensions[0].getValue());
                recordBuilder.set(fieldIdx++, rowDimensions[0].getValue());

            } else if (rowDimensions.length > 1) { // For multiple variant join there string values.
                StringBuilder name = new StringBuilder();
                for (Dimension rowDimension : rowDimensions) {
                    rowDimMap.put(rowDimension.getName(), rowDimension.getValue());
                    if (name.length() > 0) {
                        name.append("/");
                    }
                    name.append(rowDimension.getValue().strValue());
                }
                recordBuilder.set(fieldIdx++, Value.str(name.toString()));
            }

            for (Dimension[] columnDimensions : allColumnDimensions) {
                Map<String, Variant> allDimMap = new HashMap<>(rowDimMap);
                for (Dimension columnDimension : columnDimensions) {
                    if (allDimMap.containsKey(columnDimension.getName())) {
                        throw new RuntimeException("Illegal pivot settings.");
                    }
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
                    recordBuilder.set(fieldIdx++, new StylizedValue(val, values.get(i).getFormat()));
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

        for (PivotField field : rows) {
            if (!fieldMap.containsKey(field.getField())) {
                throw new RuntimeException("Field \"" + field.getField() + "\" not found.");
            }
        }
        for (PivotField field : columns) {
            if (!fieldMap.containsKey(field.getField())) {
                throw new RuntimeException("Field \"" + field.getField() + "\" not found.");
            }
        }
        for (com.ctrip.ferriswheel.common.automaton.PivotValue value : values) {
            if (AggregateType.CUSTOM.equals(value.getAggregateType())) {
                // TODO check formula references
            } else if (!fieldMap.containsKey(value.getField())) {
                throw new RuntimeException("Field \"" + value.getField() + "\" not found.");
            }
        }

        return fieldMap;
    }

    private DimensionalAggregateMaster aggregate(Map<String, Integer> nameMapper, DefaultTable table, int left, int top, int right, int bottom) {
        List<String> dimensionNameList = new ArrayList<>(rows.size() + columns.size());
        for (PivotField row : rows) {
            dimensionNameList.add(row.getField());
        }
        for (PivotField col : columns) {
            dimensionNameList.add(col.getField());
        }
        String[] dimensionNames = dimensionNameList.toArray(new String[dimensionNameList.size()]);

        AggregateMeta[] aggregateMetas = new AggregateMeta[values.size()];
        for (int i = 0; i < values.size(); i++) {
            com.ctrip.ferriswheel.common.automaton.PivotValue value = values.get(i);
            aggregateMetas[i] = new AggregateMeta(value.getAggregateType(), value.getField());
        }

        DimensionalAggregateMaster aggregator = new DimensionalAggregateMaster(dimensionNames, aggregateMetas);
        SampleWrapper sampleWrapper = new SampleWrapper(nameMapper);

        for (int rowIndex = top; rowIndex <= bottom; rowIndex++) {
            sampleWrapper.wrap(table.getRow(rowIndex));
            aggregator.feed(sampleWrapper);
        }

        return aggregator;
    }

    private List<Dimension[]> combineDimensions(DimensionalAggregateMaster aggregator, List<PivotField> fields) {
        if (fields.isEmpty()) {
            return new ArrayList<>(0);
        }
        Dimension[][] allFieldDimensions = new Dimension[fields.size()][];
        for (int i = 0; i < fields.size(); i++) {
            PivotField meta = fields.get(i);
            Set<Variant> dimensions = aggregator.getDimensions(meta.getField());
            dimensions = new TreeSet<>(dimensions);
            List<Dimension> dimList = new ArrayList<>(dimensions.size());
            for (Variant dim : dimensions) {
                dimList.add(new Dimension(meta.getField(), dim));
            }
            allFieldDimensions[i] = dimList.toArray(new Dimension[dimList.size()]);
        }

        List<Dimension[]> combinations = new ArrayList<>();
        Dimension[] pendingCombination = new Dimension[allFieldDimensions.length];
        combineDimensionsRecursively(combinations, pendingCombination, allFieldDimensions, 0);
        return combinations;
    }

    private void combineDimensionsRecursively(final List<Dimension[]> combinations,
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
            combineDimensionsRecursively(combinations,
                    pendingCombination,
                    allDimensions,
                    dimensionsIndex + 1);
        }
    }

    public TableAutomatonInfo.PivotAutomatonInfo getPivotAutomatonInfo() {
        List<PivotFilter> filterList = new ArrayList<>(filters.size());
        List<PivotField> rowList = new ArrayList<>(rows.size());
        List<PivotField> columnList = new ArrayList<>(columns.size());
        List<com.ctrip.ferriswheel.common.automaton.PivotValue> valueList = new ArrayList<>(values.size());

        for (PivotField field : rows) {
            rowList.add(new PivotFieldImpl(field));
        }
        for (PivotField field : columns) {
            columnList.add(new PivotFieldImpl(field));
        }
        for (PivotValue value : values) {
            valueList.add(new PivotValueImpl(value));
        }

        return new TableAutomatonInfo.PivotAutomatonInfo(
                new DynamicValue(data.getFormulaString()), filterList, rowList, columnList, valueList);
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

    public List<com.ctrip.ferriswheel.common.automaton.PivotValue> getValues() {
        return Collections.unmodifiableList(values);
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

    class SampleWrapper implements NamedValuesSample {
        private final Map<String, Integer> nameMapper;
        private Row row;

        public SampleWrapper(Map<String, Integer> nameMapper) {
            this.nameMapper = Collections.unmodifiableMap(nameMapper);
        }

        public SampleWrapper wrap(Row row) {
            this.row = row;
            return this;
        }

        @Override
        public int size() {
            return nameMapper.size();
        }

        @Override
        public Variant getValue(String name) {
            Integer index = nameMapper.get(name);
            return row.getCell(index).getData();
        }

        @Override
        public Iterator<String> iterator() {
            return nameMapper.keySet().iterator();
        }
    }
}
