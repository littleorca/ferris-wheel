package com.ctrip.ferriswheel.proto.util;

import com.ctrip.ferriswheel.common.Environment;
import com.ctrip.ferriswheel.common.Sheet;
import com.ctrip.ferriswheel.common.SheetAsset;
import com.ctrip.ferriswheel.common.Workbook;
import com.ctrip.ferriswheel.common.aggregate.AggregateType;
import com.ctrip.ferriswheel.common.automaton.*;
import com.ctrip.ferriswheel.common.chart.*;
import com.ctrip.ferriswheel.common.query.DataQuery;
import com.ctrip.ferriswheel.common.query.QueryTemplate;
import com.ctrip.ferriswheel.common.table.Cell;
import com.ctrip.ferriswheel.common.table.Row;
import com.ctrip.ferriswheel.common.table.Table;
import com.ctrip.ferriswheel.common.text.Text;
import com.ctrip.ferriswheel.common.variant.*;
import com.ctrip.ferriswheel.common.view.*;
import com.ctrip.ferriswheel.core.asset.DefaultPivotAutomaton;
import com.ctrip.ferriswheel.core.asset.DefaultQueryAutomaton;
import com.ctrip.ferriswheel.core.asset.FilingClerk;
import com.ctrip.ferriswheel.core.bean.*;
import com.ctrip.ferriswheel.core.util.TreeSparseArray;
import com.ctrip.ferriswheel.core.view.LayoutImpl;
import com.google.protobuf.Timestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

public class PbHelper {

    public static com.ctrip.ferriswheel.proto.v1.Workbook pb(Workbook workbook) {
        com.ctrip.ferriswheel.proto.v1.Workbook.Builder builder = com.ctrip.ferriswheel.proto.v1.Workbook.newBuilder();
        if (workbook.getName() != null) {
            builder.setName(workbook.getName());
        }
        for (Sheet sheet : workbook) {
            builder.addSheets(pb(sheet));
        }
        return builder.build();
    }

    public static Workbook bean(Environment environment, com.ctrip.ferriswheel.proto.v1.Workbook workbookProto) {
        String name = workbookProto.getName();
        return new FilingClerk(environment).createWorkbook(name, (wb) -> {
            for (int i = 0; i < workbookProto.getSheetsCount(); i++) {
                com.ctrip.ferriswheel.proto.v1.Sheet sheetProto = workbookProto.getSheets(i);
                Sheet sheet = wb.addSheet(sheetProto.getName());
                bean(sheet, sheetProto);
            }
        });
    }

    public static com.ctrip.ferriswheel.proto.v1.Sheet pb(Sheet sheet) {
        com.ctrip.ferriswheel.proto.v1.Sheet.Builder builder = com.ctrip.ferriswheel.proto.v1.Sheet.newBuilder()
                .setName(sheet.getName())
                .setLayout(pb(sheet.getLayout()));

        for (SheetAsset asset : sheet) {
            if (asset instanceof Table) {
                builder.addAssets(pb((Table) asset));
            } else if (asset instanceof Chart) {
                builder.addAssets(pb((Chart) asset));
            } else if (asset instanceof Text) {
                builder.addAssets(pb((Text) asset));
            } else {
                throw new RuntimeException("Unsupported asset: " + asset.getClass());
            }
        }
        return builder.build();
    }

    static Sheet bean(Sheet sheet, com.ctrip.ferriswheel.proto.v1.Sheet sheetProto) {
        if (sheetProto.hasLayout()) {
            fillBeanFromProto((LayoutImpl) sheet.getLayout(), sheetProto.getLayout());
        }
        for (int i = 0; i < sheetProto.getAssetsCount(); i++) {
            com.ctrip.ferriswheel.proto.v1.SheetAsset asset = sheetProto.getAssets(i);
            switch (asset.getAssetCase()) {
                case TABLE:
                    Table table = sheet.addAsset(Table.class, asset.getTable().getName());
                    bean(table, asset.getTable());
                    break;
                case CHART:
                    sheet.addAsset(Chart.class, bean(asset.getChart()));
                    break;
                case TEXT:
                    sheet.addAsset(Text.class, bean(asset.getText()));
                    break;
                case ASSET_NOT_SET:
                default:
                    throw new RuntimeException("Illegal asset case: " + asset.getAssetCase());
            }
        }
        return sheet;
    }

    public static com.ctrip.ferriswheel.proto.v1.SheetAsset pb(String tableName, TableDataImpl tableData) {
        com.ctrip.ferriswheel.proto.v1.Table.Builder builder = com.ctrip.ferriswheel.proto.v1.Table.newBuilder();
        builder.setName(tableName);
        for (Map.Entry<Integer, Row> rowEntry : tableData) {
            builder.addRows(pb(rowEntry.getValue(), rowEntry.getKey()));
        }
        if (tableData.getAutomateConfiguration() != null) {
            com.ctrip.ferriswheel.proto.v1.TableAutomaton.Builder auto = com.ctrip.ferriswheel.proto.v1.TableAutomaton.newBuilder();
            if (tableData.getAutomateConfiguration() instanceof TableAutomatonInfo.QueryAutomatonInfo) {
                auto.setQueryAutomaton(pb((TableAutomatonInfo.QueryAutomatonInfo) tableData.getAutomateConfiguration()));
            } else if (tableData.getAutomateConfiguration() instanceof TableAutomatonInfo.PivotAutomatonInfo) {
                auto.setPivotAutomaton(pb((TableAutomatonInfo.PivotAutomatonInfo) tableData.getAutomateConfiguration()));
            } else {
                throw new RuntimeException();
            }
            builder.setAutomaton(auto);
        }
        builder.setLayout(pb(tableData.getLayout()));
        return com.ctrip.ferriswheel.proto.v1.SheetAsset.newBuilder().setTable(builder).build();
    }

    public static TableDataImpl bean(com.ctrip.ferriswheel.proto.v1.Table proto) {
        TableDataImpl table = new TableDataImpl();
        TreeSparseArray<Row> rows = new TreeSparseArray<>();
        for (int i = 0; i < proto.getRowsCount(); i++) {
            com.ctrip.ferriswheel.proto.v1.Row rowProto = proto.getRows(i);
            rows.set(rowProto.getRowIndex(), bean(rowProto));
        }
        table.setRows(rows);
        if (proto.hasAutomaton()) {
            com.ctrip.ferriswheel.proto.v1.TableAutomaton automatonProto = proto.getAutomaton();
            if (automatonProto.getAutomatonCase() != com.ctrip.ferriswheel.proto.v1.TableAutomaton.AutomatonCase.AUTOMATON_NOT_SET) {
                TableAutomatonInfo automaton = bean(automatonProto);
                table.setAutomateConfiguration(automaton);
            }
        }
        if (proto.hasLayout()) {
            if (table.getLayout() == null) {
                table.setLayout(new LayoutImpl());
            }
            fillBeanFromProto((LayoutImpl) table.getLayout(), proto.getLayout());
        }
        return table;
    }

    public static RowData bean(com.ctrip.ferriswheel.proto.v1.Row proto) {
        RowData bean = new RowData();
        // bean.setRowIndex(proto.getRowIndex());
        TreeSparseArray<Cell> cells = new TreeSparseArray<>();
        for (int i = 0; i < proto.getCellsCount(); i++) {
            com.ctrip.ferriswheel.proto.v1.Cell c = proto.getCells(i);
            cells.set(c.getColumnIndex(), bean(c));
        }
        bean.setCells(cells);
        return bean;
    }

    public static com.ctrip.ferriswheel.proto.v1.Cell pb(Cell bean, int columnIndex) {
        com.ctrip.ferriswheel.proto.v1.Cell.Builder builder = com.ctrip.ferriswheel.proto.v1.Cell.newBuilder()
                .setColumnIndex(columnIndex)
                .setValue(pb(bean.getData()));
        if (bean.getFormat() != null) {
            builder.setFormat(bean.getFormat());
        }
        return builder.build();
    }

    public static CellData bean(com.ctrip.ferriswheel.proto.v1.Cell proto) {
        CellData bean = new CellData();
        // bean.setColumnIndex(proto.getColumnIndex());
        bean.setData(toDynamicValue(proto.getValue()));
        bean.setFormat(proto.getFormat());
        return bean;
    }

    public static com.ctrip.ferriswheel.proto.v1.SheetAsset pb(Table bean) {
        com.ctrip.ferriswheel.proto.v1.Table.Builder builder = com.ctrip.ferriswheel.proto.v1.Table.newBuilder();
        builder.setName(bean.getName());
        for (Map.Entry<Integer, Row> rowEntry : bean) {
            builder.addRows(pb(rowEntry.getValue(), rowEntry.getKey()));
        }
        if (bean.getAutomaton() != null) {
            builder.setAutomaton(pb(bean.getAutomaton()));
        }
        builder.setLayout(pb(bean.getLayout()));
        return com.ctrip.ferriswheel.proto.v1.SheetAsset.newBuilder().setTable(builder).build();
    }

    static Table bean(Table table, com.ctrip.ferriswheel.proto.v1.Table proto) {
        for (int i = 0; i < proto.getRowsCount(); i++) {
            com.ctrip.ferriswheel.proto.v1.Row rowProto = proto.getRows(i);
            int rowIndex = rowProto.getRowIndex();
            for (int j = 0; j < rowProto.getCellsCount(); j++) {
                com.ctrip.ferriswheel.proto.v1.Cell cellProto = rowProto.getCells(j);
                int columnIndex = cellProto.getColumnIndex();
                if (cellProto.hasValue()) {
                    Variant value = toDynamicValue(cellProto.getValue()).getVariant();
                    table.setCellValue(rowIndex, columnIndex, value);
                    if (!"".equals(cellProto.getValue().getFormulaString())) {
                        table.setCellFormula(rowIndex, columnIndex,
                                cellProto.getValue().getFormulaString());
                    }
                    if (!"".equals(cellProto.getFormat())) {
                        table.setCellsFormat(rowIndex, columnIndex, 1, 1, cellProto.getFormat());
                    }
                }
            }
        }
        if (proto.hasAutomaton()) {
            com.ctrip.ferriswheel.proto.v1.TableAutomaton automatonProto = proto.getAutomaton();
            TableAutomatonInfo automaton = bean(automatonProto);
            table.automate(automaton);
        }
        if (proto.hasLayout()) {
            fillBeanFromProto((LayoutImpl) table.getLayout(), proto.getLayout());
        }
        return table;
    }

    public static com.ctrip.ferriswheel.proto.v1.Row pb(Row row, int index) {
        com.ctrip.ferriswheel.proto.v1.Row.Builder builder = com.ctrip.ferriswheel.proto.v1.Row.newBuilder();
        builder.setRowIndex(index);
        for (Map.Entry<Integer, Cell> cellEntry : row) {
            builder.addCells(pb(cellEntry.getValue(), cellEntry.getKey()));
        }
        return builder.build();
    }

    public static com.ctrip.ferriswheel.proto.v1.TableAutomaton pb(Automaton automaton) {
        com.ctrip.ferriswheel.proto.v1.TableAutomaton.Builder builder = com.ctrip.ferriswheel.proto.v1.TableAutomaton.newBuilder();
        if (automaton instanceof DefaultQueryAutomaton) {
            com.ctrip.ferriswheel.proto.v1.QueryAutomaton queryAutomaton = pb(((DefaultQueryAutomaton) automaton).getQueryAutomatonInfo());
            return builder.setQueryAutomaton(queryAutomaton).build();
        } else if (automaton instanceof DefaultPivotAutomaton) {
            com.ctrip.ferriswheel.proto.v1.PivotAutomaton pivotAutomaton = pb(((DefaultPivotAutomaton) automaton).getPivotAutomatonInfo());
            return builder.setPivotAutomaton(pivotAutomaton).build();
        } else {
            throw new RuntimeException("Not supported yet!");
        }
    }

    static TableAutomatonInfo bean(com.ctrip.ferriswheel.proto.v1.TableAutomaton automatonProto) {
        switch (automatonProto.getAutomatonCase()) {
            case QUERY_AUTOMATON:
                return bean(automatonProto.getQueryAutomaton());
            case PIVOT_AUTOMATON:
                return bean(automatonProto.getPivotAutomaton());
            case AUTOMATON_NOT_SET:
            default:
                throw new RuntimeException("Unsupported automaton case: "
                        + automatonProto.getAutomatonCase());
        }
    }

    public static com.ctrip.ferriswheel.proto.v1.QueryAutomaton pb(QueryConfiguration automatonInfo) {
        com.ctrip.ferriswheel.proto.v1.QueryAutomaton.Builder builder = com.ctrip.ferriswheel.proto.v1.QueryAutomaton.newBuilder()
                .setTemplate(pb(automatonInfo.getTemplate()));
        if (automatonInfo.getParameters() != null) {
            for (Map.Entry<String, Variant> entry : automatonInfo.getParameters().entrySet()) {
                builder.addParams(com.ctrip.ferriswheel.proto.v1.NamedValue.newBuilder()
                        .setName(entry.getKey())
                        .setValue(pb(entry.getValue())));
            }
        }
        if (automatonInfo.getQuery() != null) {
            builder.setQuery(pb(automatonInfo.getQuery()));
        }
        return builder.build();
    }

    public static TableAutomatonInfo.QueryAutomatonInfo bean(com.ctrip.ferriswheel.proto.v1.QueryAutomaton queryAutomaton) {
        TableAutomatonInfo.QueryTemplateInfo template = queryAutomaton.hasTemplate() ? bean(queryAutomaton.getTemplate()) : null;
        Map<String, Variant> parameters = new LinkedHashMap<>(queryAutomaton.getParamsCount());
        for (com.ctrip.ferriswheel.proto.v1.NamedValue item : queryAutomaton.getParamsList()) {
            parameters.put(item.getName(), toValue(item.getValue()));
        }
        TableAutomatonInfo.QueryInfo query = queryAutomaton.hasQuery() ? bean(queryAutomaton.getQuery()) : null;
        return new TableAutomatonInfo.QueryAutomatonInfo(template, parameters, query);
    }

    public static com.ctrip.ferriswheel.proto.v1.QueryTemplate pb(QueryTemplate templateInfo) {
        com.ctrip.ferriswheel.proto.v1.QueryTemplate.Builder builder = com.ctrip.ferriswheel.proto.v1.QueryTemplate.newBuilder();
        if (templateInfo.getScheme() != null) {
            builder.setScheme(templateInfo.getScheme());
        }
        for (Map.Entry<String, DynamicVariant> entry : templateInfo.getAllBuiltinParams().entrySet()) {
            DynamicVariant param = entry.getValue();
            builder.addBuiltinParams(com.ctrip.ferriswheel.proto.v1.NamedValue.newBuilder()
                    .setName(entry.getKey())
                    .setValue(pb(param)));
        }
        for (Map.Entry<String, VariantRule> entry : templateInfo.getAllUserParamRules().entrySet()) {
            VariantRule rule = entry.getValue();
            com.ctrip.ferriswheel.proto.v1.ParamRule.Builder ruleBuilder = com.ctrip.ferriswheel.proto.v1.ParamRule.newBuilder()
                    .setName(entry.getKey())
                    .setType(pb(rule.getType()))
                    .setNullable(rule.isNullable());
            if (rule.getAllowedValues() != null) {
                for (Variant value : rule.getAllowedValues()) {
                    ruleBuilder.addAllowedValues(pb(value));
                }
            }
            builder.addUserParamRules(ruleBuilder.build());
        }
        return builder.build();
    }

    public static TableAutomatonInfo.QueryTemplateInfo bean(com.ctrip.ferriswheel.proto.v1.QueryTemplate queryTemplate) {
        String scheme = queryTemplate.getScheme();
        Map<String, DynamicVariant> builtinParams = new LinkedHashMap<>(queryTemplate.getBuiltinParamsCount());
        Map<String, VariantRule> userParamRules = new LinkedHashMap<>(queryTemplate.getUserParamRulesCount());

        for (com.ctrip.ferriswheel.proto.v1.NamedValue item : queryTemplate.getBuiltinParamsList()) {
            builtinParams.put(item.getName(), toDynamicValue(item.getValue()));
        }

        for (com.ctrip.ferriswheel.proto.v1.ParamRule item : queryTemplate.getUserParamRulesList()) {
            userParamRules.put(item.getName(), bean(item));
        }
        return new TableAutomatonInfo.QueryTemplateInfo(scheme, builtinParams, userParamRules);
    }

    public static com.ctrip.ferriswheel.proto.v1.DataQuery pb(DataQuery queryInfo) {
        com.ctrip.ferriswheel.proto.v1.DataQuery.Builder builder = com.ctrip.ferriswheel.proto.v1.DataQuery.newBuilder();
        if (queryInfo.getScheme() != null) {
            builder.setScheme(queryInfo.getScheme());
        }
        if (queryInfo.getAllParams() != null) {
            for (Map.Entry<String, Variant> entry : queryInfo.getAllParams().entrySet()) {
                builder.addParams(com.ctrip.ferriswheel.proto.v1.NamedValue.newBuilder()
                        .setName(entry.getKey()).setValue(pb(entry.getValue())));
            }
        }
        return builder.build();
    }

    public static TableAutomatonInfo.QueryInfo bean(com.ctrip.ferriswheel.proto.v1.DataQuery queryProto) {
        Map<String, Variant> parameters = new LinkedHashMap<>(queryProto.getParamsCount());
        for (com.ctrip.ferriswheel.proto.v1.NamedValue item : queryProto.getParamsList()) {
            parameters.put(item.getName(), toDynamicValue(item.getValue()));
        }
        return new TableAutomatonInfo.QueryInfo(queryProto.getScheme(), parameters);
    }

    public static com.ctrip.ferriswheel.proto.v1.PivotAutomaton pb(PivotConfiguration pivot) {
        com.ctrip.ferriswheel.proto.v1.PivotAutomaton.Builder pb = com.ctrip.ferriswheel.proto.v1.PivotAutomaton.newBuilder()
                .setData(pb(pivot.getData()));
        if (pivot.getFilters() != null) {
            for (PivotFilter filter : pivot.getFilters()) {
                pb.addFilters(pb(filter));
            }
        }
        if (pivot.getRows() != null) {
            for (PivotField field : pivot.getRows()) {
                pb.addRows(pb(field));
            }
        }
        if (pivot.getColumns() != null) {
            for (PivotField field : pivot.getColumns()) {
                pb.addColumns(pb(field));
            }
        }
        for (PivotValue value : pivot.getValues()) {
            pb.addValues(pb(value));
        }
        return pb.build();
    }

    public static TableAutomatonInfo.PivotAutomatonInfo bean(com.ctrip.ferriswheel.proto.v1.PivotAutomaton pivotSolution) {
        DynamicValue data = toDynamicValue(pivotSolution.getData());
        List<PivotFilter> filters = new ArrayList<>(pivotSolution.getFiltersCount());
        for (com.ctrip.ferriswheel.proto.v1.PivotFilter f : pivotSolution.getFiltersList()) {
            filters.add(bean(f));
        }
        List<PivotField> rows = new ArrayList<>(pivotSolution.getRowsCount());
        for (com.ctrip.ferriswheel.proto.v1.PivotField field : pivotSolution.getRowsList()) {
            rows.add(bean(field));
        }
        List<PivotField> columns = new ArrayList<>(pivotSolution.getColumnsCount());
        for (com.ctrip.ferriswheel.proto.v1.PivotField field : pivotSolution.getColumnsList()) {
            columns.add(bean(field));
        }
        List<PivotValue> values = new ArrayList<>(pivotSolution.getValuesCount());
        for (com.ctrip.ferriswheel.proto.v1.PivotValue value : pivotSolution.getValuesList()) {
            values.add(bean(value));
        }
        return new TableAutomatonInfo.PivotAutomatonInfo(data, filters, rows, columns, values);
    }

    public static com.ctrip.ferriswheel.proto.v1.PivotFilter pb(PivotFilter bean) {
        return com.ctrip.ferriswheel.proto.v1.PivotFilter.newBuilder()
                .setField(bean.getField())
                .build();
    }

    public static PivotFilterImpl bean(com.ctrip.ferriswheel.proto.v1.PivotFilter pb) {
        return new PivotFilterImpl(pb.getField());
    }

    public static com.ctrip.ferriswheel.proto.v1.PivotField pb(PivotField bean) {
        com.ctrip.ferriswheel.proto.v1.PivotField.Builder builder = com.ctrip.ferriswheel.proto.v1.PivotField.newBuilder()
                .setField(bean.getField());
        if (bean.getFormat() != null) {
            builder.setFormat(bean.getFormat());
        }
        return builder.build();
    }

    public static PivotFieldImpl bean(com.ctrip.ferriswheel.proto.v1.PivotField pb) {
        return new PivotFieldImpl(pb.getField(), pb.getFormat());
    }

    public static com.ctrip.ferriswheel.proto.v1.PivotValue pb(PivotValue bean) {
        com.ctrip.ferriswheel.proto.v1.PivotValue.Builder builder = com.ctrip.ferriswheel.proto.v1.PivotValue.newBuilder()
                .setField(bean.getField())
                .setAggregateType(pb(bean.getAggregateType()))
                .setLabel(bean.getLabel());
        if (bean.getFormat() != null) {
            builder.setFormat(bean.getFormat());
        }
        return builder.build();
    }

    public static PivotValueImpl bean(com.ctrip.ferriswheel.proto.v1.PivotValue pb) {
        return new PivotValueImpl(pb.getField(), bean(pb.getAggregateType()), pb.getLabel(), pb.getFormat());
    }

    public static com.ctrip.ferriswheel.proto.v1.AggregateType pb(AggregateType bean) {
        if (bean == null) {
            return com.ctrip.ferriswheel.proto.v1.AggregateType.AT_UNSET;
        }
        switch (bean) {
            case SUMMARY:
                return com.ctrip.ferriswheel.proto.v1.AggregateType.AT_SUMMARY;
            case COUNT:
                return com.ctrip.ferriswheel.proto.v1.AggregateType.AT_COUNT;
            case AVERAGE:
                return com.ctrip.ferriswheel.proto.v1.AggregateType.AT_AVERAGE;
            case MAXIMUM:
                return com.ctrip.ferriswheel.proto.v1.AggregateType.AT_MAXIMUM;
            case MINIMUM:
                return com.ctrip.ferriswheel.proto.v1.AggregateType.AT_MINIMUM;
            case PRODUCT:
                return com.ctrip.ferriswheel.proto.v1.AggregateType.AT_PRODUCT;
            case DECIMAL_ONLY_COUNT:
                return com.ctrip.ferriswheel.proto.v1.AggregateType.AT_DECIMAL_ONLY_COUNT;
            case STANDARD_DEVIATION:
                return com.ctrip.ferriswheel.proto.v1.AggregateType.AT_STANDARD_DEVIATION;
            case STANDARD_DEVIATION_POPULATION:
                return com.ctrip.ferriswheel.proto.v1.AggregateType.AT_STANDARD_DEVIATION_POPULATION;
            case VARIANCE:
                return com.ctrip.ferriswheel.proto.v1.AggregateType.AT_VARIANCE;
            case VARIANCE_POPULATION:
                return com.ctrip.ferriswheel.proto.v1.AggregateType.AT_VARIANCE_POPULATION;
            case CUSTOM:
                return com.ctrip.ferriswheel.proto.v1.AggregateType.AT_CUSTOM;
            default:
                return com.ctrip.ferriswheel.proto.v1.AggregateType.UNRECOGNIZED;
        }
    }

    public static AggregateType bean(com.ctrip.ferriswheel.proto.v1.AggregateType pb) {
        switch (pb) {
            case AT_UNSET:
                return null;
            case AT_SUMMARY:
                return AggregateType.SUMMARY;
            case AT_COUNT:
                return AggregateType.COUNT;
            case AT_AVERAGE:
                return AggregateType.AVERAGE;
            case AT_MAXIMUM:
                return AggregateType.MAXIMUM;
            case AT_MINIMUM:
                return AggregateType.MINIMUM;
            case AT_PRODUCT:
                return AggregateType.PRODUCT;
            case AT_DECIMAL_ONLY_COUNT:
                return AggregateType.DECIMAL_ONLY_COUNT;
            case AT_STANDARD_DEVIATION:
                return AggregateType.STANDARD_DEVIATION;
            case AT_STANDARD_DEVIATION_POPULATION:
                return AggregateType.STANDARD_DEVIATION_POPULATION;
            case AT_VARIANCE:
                return AggregateType.VARIANCE;
            case AT_VARIANCE_POPULATION:
                return AggregateType.VARIANCE_POPULATION;
            case AT_CUSTOM:
                return AggregateType.CUSTOM;
            case UNRECOGNIZED:
            default:
                throw new IllegalArgumentException();
        }
    }

    public static com.ctrip.ferriswheel.proto.v1.ParamRule pb(VariantRule rule) {
        com.ctrip.ferriswheel.proto.v1.ParamRule.Builder builder = com.ctrip.ferriswheel.proto.v1.ParamRule.newBuilder()
                .setType(pb(rule.getType()))
                .setNullable(rule.isNullable());
        for (Variant var : rule.getAllowedValues()) {
            builder.addAllowedValues(pb(var));
        }
        return builder.build();
    }

    static ValueRule bean(com.ctrip.ferriswheel.proto.v1.ParamRule ruleProto) {
        ValueRule rule = new ValueRule(
                bean(ruleProto.getType()),
                ruleProto.getNullable(),
                new LinkedHashSet<>(ruleProto.getAllowedValuesCount()));
        for (int i = 0; i < ruleProto.getAllowedValuesCount(); i++) {
            rule.getAllowedValues().add(toValue(ruleProto.getAllowedValues(i)));
        }
        return rule;
    }

    public static com.ctrip.ferriswheel.proto.v1.UnionValue pb(Variant var) {
        com.ctrip.ferriswheel.proto.v1.UnionValue.Builder builder = com.ctrip.ferriswheel.proto.v1.UnionValue.newBuilder();
        if (var instanceof DynamicVariant) {
            if (((DynamicVariant) var).getFormulaString() != null) {
                builder.setFormulaString(((DynamicVariant) var).getFormulaString());
            }
        }
        switch (var.valueType()) {
            case ERROR:
                builder.setError(pb(var.errorValue()));
                break;
            case BLANK:
                break;
            case DECIMAL:
                builder.setDecimal(var.decimalValue().toString());
                break;
            case BOOL:
                builder.setBoolean(var.booleanValue());
                break;
            case DATE:
                Instant instant = var.dateValue().toInstant();
                builder.setDate(Timestamp.newBuilder()
                        .setSeconds(instant.getEpochSecond())
                        .setNanos(instant.getNano()));
                break;
            case STRING:
                builder.setString(var.strValue());
                break;
            case LIST:
                builder.setList(pb(var.listValue()));
                break;
            default:
                throw new RuntimeException("Unsupported value type: " + var.valueType());
        }
        return builder.build();
    }

    public static DynamicValue toDynamicValue(com.ctrip.ferriswheel.proto.v1.UnionValue valueProto) {
        String formulaString = valueProto.getFormulaString();
        if (formulaString.isEmpty()) {
            formulaString = null;
        }
        Value value = toValue(valueProto);
        return new DynamicValue(formulaString, value);
    }

    public static Value toValue(com.ctrip.ferriswheel.proto.v1.UnionValue valueProto) {
        switch (valueProto.getValueCase()) {
            case ERROR:
                return Value.err(bean(valueProto.getError()));
            case DECIMAL:
                return Value.dec(new BigDecimal(valueProto.getDecimal()));
            case BOOLEAN:
                return Value.bool(valueProto.getBoolean());
            case DATE:
                Timestamp ts = valueProto.getDate();
                return Value.date(Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos()));
            case STRING:
                return Value.str(valueProto.getString());
            case LIST:
                return bean(valueProto.getList());
            case VALUE_NOT_SET:
                return Value.BLANK;
            default:
                throw new RuntimeException("Unknown value case: " + valueProto.getValueCase());
        }
    }

    public static com.ctrip.ferriswheel.proto.v1.ListValue pb(List<Variant> list) {
        com.ctrip.ferriswheel.proto.v1.ListValue.Builder builder = com.ctrip.ferriswheel.proto.v1.ListValue.newBuilder();
        for (Variant item : list) {
            builder.addItems(pb(item));
        }
        return builder.build();
    }

    static Value bean(com.ctrip.ferriswheel.proto.v1.ListValue list) {
        List<Variant> variantList = new ArrayList<>(list.getItemsCount());
        for (int i = 0; i < list.getItemsCount(); i++) {
            variantList.add(toDynamicValue(list.getItems(i)));
        }
        return Value.list(variantList);
    }

    public static com.ctrip.ferriswheel.proto.v1.VariantType pb(VariantType variantType) {
        if (variantType == null) {
            return com.ctrip.ferriswheel.proto.v1.VariantType.VT_UNSET;
        }
        switch (variantType) {
            case ERROR:
                return com.ctrip.ferriswheel.proto.v1.VariantType.VT_ERROR;
            case BLANK:
                return com.ctrip.ferriswheel.proto.v1.VariantType.VT_BLANK;
            case DECIMAL:
                return com.ctrip.ferriswheel.proto.v1.VariantType.VT_DECIMAL;
            case BOOL:
                return com.ctrip.ferriswheel.proto.v1.VariantType.VT_BOOL;
            case DATE:
                return com.ctrip.ferriswheel.proto.v1.VariantType.VT_DATE;
            case STRING:
                return com.ctrip.ferriswheel.proto.v1.VariantType.VT_STRING;
            case LIST:
                return com.ctrip.ferriswheel.proto.v1.VariantType.VT_LIST;
            default:
                throw new RuntimeException(); // should never happen
        }
    }

    public static VariantType bean(com.ctrip.ferriswheel.proto.v1.VariantType variantType) {
        switch (variantType) {
            case VT_UNSET:
                return null;
            case VT_ERROR:
                return VariantType.ERROR;
            case VT_BLANK:
                return VariantType.BLANK;
            case VT_DECIMAL:
                return VariantType.DECIMAL;
            case VT_BOOL:
                return VariantType.BOOL;
            case VT_DATE:
                return VariantType.DATE;
            case VT_STRING:
                return VariantType.STRING;
            case VT_LIST:
                return VariantType.LIST;
            case UNRECOGNIZED:
            default:
                throw new RuntimeException("Invalid variant type.");
        }
    }

    public static com.ctrip.ferriswheel.proto.v1.ErrorCode pb(ErrorCode error) {
        ErrorCodes errorCode = (ErrorCodes) error;
        switch (errorCode) {
            case OK:
                return com.ctrip.ferriswheel.proto.v1.ErrorCode.EC_OK;
            case UNKNOWN:
                return com.ctrip.ferriswheel.proto.v1.ErrorCode.EC_UNKNOWN;
            case ILLEGAL_REF:
                return com.ctrip.ferriswheel.proto.v1.ErrorCode.EC_ILLEGAL_REF;
            case ILLEGAL_VALUE:
                return com.ctrip.ferriswheel.proto.v1.ErrorCode.EC_ILLEGAL_VALUE;
            case DIV_0:
                return com.ctrip.ferriswheel.proto.v1.ErrorCode.EC_DIV_0;
            default:
                throw new RuntimeException("Unrecognized error code: " + errorCode);
        }
    }

    public static ErrorCodes bean(com.ctrip.ferriswheel.proto.v1.ErrorCode errorCodeProto) {
        switch (errorCodeProto) {
            case EC_UNSET:
                return null;
            case EC_OK:
                return ErrorCodes.OK;
            case EC_UNKNOWN:
                return ErrorCodes.UNKNOWN;
            case EC_ILLEGAL_REF:
                return ErrorCodes.ILLEGAL_REF;
            case EC_ILLEGAL_VALUE:
                return ErrorCodes.ILLEGAL_VALUE;
            case EC_DIV_0:
                return ErrorCodes.DIV_0;
            case UNRECOGNIZED:
            default:
                throw new RuntimeException("Invalid error code(pb): " + errorCodeProto);
        }
    }

    public static com.ctrip.ferriswheel.proto.v1.SheetAsset pb(Chart chart) {
        com.ctrip.ferriswheel.proto.v1.Chart.Builder builder = com.ctrip.ferriswheel.proto.v1.Chart.newBuilder();
        builder.setName(chart.getName());
        builder.setType(chart.getType());
        builder.setTitle(pb(chart.getTitle()));
        builder.setCategories(pb(chart.getCategories()));
        for (int i = 0; i < chart.getSeriesCount(); i++) {
            DataSeries series = chart.getSeries(i);
            builder.addSeries(pb(series));
        }
        builder.setLayout(pb(chart.getLayout()));
        if (chart.getBinder() != null) {
            builder.setBinder(pb(new ChartData.BinderImpl(chart.getBinder())));
        }
        if (chart.getxAxis() != null) {
            builder.setXAxis(pb(chart.getxAxis()));
        }
        if (chart.getyAxis() != null) {
            builder.setYAxis(pb(chart.getyAxis()));
        }
        if (chart.getzAxis() != null) {
            builder.setZAxis(pb(chart.getzAxis()));
        }
        return com.ctrip.ferriswheel.proto.v1.SheetAsset.newBuilder().setChart(builder).build();
    }

    public static ChartData bean(com.ctrip.ferriswheel.proto.v1.Chart pbChart) {
        ChartData chart = new ChartData();
        chart.setName(pbChart.getName());
        chart.setType(pbChart.getType());
        if (pbChart.hasTitle()) {
            chart.setTitle(toDynamicValue(pbChart.getTitle()));
        }
        if (pbChart.hasCategories()) {
            chart.setCategories(toDynamicValue(pbChart.getCategories()));
        }
        List<DataSeries> seriesList = new ArrayList<>(pbChart.getSeriesCount());
        for (int i = 0; i < pbChart.getSeriesCount(); i++) {
            com.ctrip.ferriswheel.proto.v1.Series seriesProto = pbChart.getSeries(i);
            seriesList.add(bean(seriesProto));
        }
        chart.setSeriesList(seriesList);
        if (pbChart.hasLayout()) {
            if (chart.getLayout() == null) {
                chart.setLayout(new LayoutImpl());
            }
            fillBeanFromProto(chart.getLayout(), pbChart.getLayout());
        }
        if (pbChart.hasBinder()) {
            chart.setBinder(bean(pbChart.getBinder()));
        }
        if (pbChart.hasXAxis()) {
            chart.setxAxis(bean(pbChart.getXAxis()));
        }
        if (pbChart.hasYAxis()) {
            chart.setyAxis(bean(pbChart.getYAxis()));
        }
        if (pbChart.hasZAxis()) {
            chart.setzAxis(bean(pbChart.getZAxis()));
        }
        return chart;
    }

    public static com.ctrip.ferriswheel.proto.v1.Series pb(DataSeries series) {
        com.ctrip.ferriswheel.proto.v1.Series.Builder builder = com.ctrip.ferriswheel.proto.v1.Series.newBuilder();
        if (series.getName() != null) {
            builder.setName(pb(series.getName()));
        }
        if (series.getxValues() != null) {
            builder.setXValues(pb(series.getxValues()));
        }
        if (series.getyValues() != null) {
            builder.setYValues(pb(series.getyValues()));
        }
        return builder.build();
    }

    static ChartData.SeriesImpl bean(com.ctrip.ferriswheel.proto.v1.Series seriesProto) {
        ChartData.SeriesImpl series = new ChartData.SeriesImpl();
        if (seriesProto.hasName()) {
            series.setName(toDynamicValue(seriesProto.getName()));
        }
        if (seriesProto.hasXValues()) {
            series.setxValues(toDynamicValue(seriesProto.getXValues()));
        }
        if (seriesProto.hasYValues()) {
            series.setyValues(toDynamicValue(seriesProto.getYValues()));
        }
        return series;
    }

    public static com.ctrip.ferriswheel.proto.v1.Chart pb(String chartName, Chart chartData) {
        com.ctrip.ferriswheel.proto.v1.Chart.Builder builder = com.ctrip.ferriswheel.proto.v1.Chart.newBuilder()
                .setName(chartName)
                .setType(chartData.getType())
                .setTitle(pb(chartData.getTitle()));
        if (chartData.getCategories() != null) {
            builder.setCategories(pb(chartData.getCategories()));
        }
        for (DataSeries series : chartData.getSeriesList()) {
            builder.addSeries(pb(series));
        }
        if (chartData.getLayout() != null) {
            builder.setLayout(pb(chartData.getLayout()));
        }
        if (chartData.getBinder() != null) {
            builder.setBinder(pb(chartData.getBinder()));
        }
        if (chartData.getxAxis() != null) {
            builder.setXAxis(pb(chartData.getxAxis()));
        }
        if (chartData.getyAxis() != null) {
            builder.setYAxis(pb(chartData.getyAxis()));
        }
        if (chartData.getzAxis() != null) {
            builder.setZAxis(pb(chartData.getzAxis()));
        }
        return builder.build();
    }

    public static com.ctrip.ferriswheel.proto.v1.ChartBinder pb(ChartBinder binder) {
        return com.ctrip.ferriswheel.proto.v1.ChartBinder.newBuilder()
                .setData(pb(binder.getData()))
                .setOrientation(pb(binder.getOrientation()))
                .setCategoriesPlacement(pb(binder.getCategoriesPlacement()))
                .setSeriesNamePlacement(pb(binder.getSeriesNamePlacement()))
                .build();
    }

    public static ChartData.BinderImpl bean(com.ctrip.ferriswheel.proto.v1.ChartBinder pbChartBinder) {
        return new ChartData.BinderImpl(toDynamicValue(pbChartBinder.getData()),
                bean(pbChartBinder.getOrientation()),
                bean(pbChartBinder.getCategoriesPlacement()),
                bean(pbChartBinder.getSeriesNamePlacement()));
    }

    public static com.ctrip.ferriswheel.proto.v1.Axis pb(Axis bean) {
        com.ctrip.ferriswheel.proto.v1.Axis.Builder builder = com.ctrip.ferriswheel.proto.v1.Axis.newBuilder();
        if (bean.getTitle() != null) {
            builder.setTitle(bean.getTitle());
        }
        if (bean.getLabel() != null) {
            builder.setLabel(bean.getLabel());
        }
        if (bean.getPlacement() != null) {
            builder.setPlacement(pb(bean.getPlacement()));
        }
        builder.setReversed(bean.isReversed());
        if (bean.getInterval() != null) {
            builder.setInterval(pb(bean.getInterval()));
        }
        if (bean.getBands() != null) {
            for (AxisBand band : bean.getBands()) {
                builder.addBands(pb(band));
            }
        }
        builder.setStacking(pb(bean.getStacking()));
        if (bean.getFormat() != null) {
            builder.setFormat(bean.getFormat());
        }
        return builder.build();
    }

    public static AxisImpl bean(com.ctrip.ferriswheel.proto.v1.Axis pb) {
        AxisImpl bean = new AxisImpl();
        if (!pb.getTitle().isEmpty()) {
            bean.setTitle(pb.getTitle());
        }
        if (!pb.getLabel().isEmpty()) {
            bean.setLabel(pb.getLabel());
        }
        bean.setPlacement(bean(pb.getPlacement()));
        bean.setReversed(pb.getReversed());
        if (pb.hasInterval()) {
            bean.setInterval(bean(pb.getInterval()));
        }
        List<AxisBand> bands = new ArrayList<>(pb.getBandsCount());
        for (int i = 0; i < pb.getBandsCount(); i++) {
            bands.add(bean(pb.getBands(i)));
        }
        bean.setBands(bands);
        bean.setStacking(bean(pb.getStacking()));
        if (!pb.getFormat().isEmpty()) {
            bean.setFormat(pb.getFormat());
        }
        return bean;
    }

    public static com.ctrip.ferriswheel.proto.v1.Orientation pb(Orientation orientation) {
        if (orientation == null) {
            return com.ctrip.ferriswheel.proto.v1.Orientation.ORIENT_UNSET;
        }
        switch (orientation) {
            case HORIZONTAL:
                return com.ctrip.ferriswheel.proto.v1.Orientation.ORIENT_HORIZONTAL;
            case VERTICAL:
                return com.ctrip.ferriswheel.proto.v1.Orientation.ORIENT_VERTICAL;
            default:
                return com.ctrip.ferriswheel.proto.v1.Orientation.UNRECOGNIZED;
        }
    }

    public static Orientation bean(com.ctrip.ferriswheel.proto.v1.Orientation pbOrientation) {
        if (pbOrientation == null) {
            return null;
        }
        switch (pbOrientation) {
            case ORIENT_UNSET:
                return null;
            case ORIENT_HORIZONTAL:
                return Orientation.HORIZONTAL;
            case ORIENT_VERTICAL:
                return Orientation.VERTICAL;
            case UNRECOGNIZED:
            default:
                throw new RuntimeException("Unrecognized orientation(pb): " + pbOrientation);
        }
    }

    public static com.ctrip.ferriswheel.proto.v1.Placement pb(Placement placement) {
        if (placement == null) {
            return com.ctrip.ferriswheel.proto.v1.Placement.PLCMT_UNSET;
        }
        switch (placement) {
            case LEFT:
                return com.ctrip.ferriswheel.proto.v1.Placement.PLCMT_LEFT;
            case TOP:
                return com.ctrip.ferriswheel.proto.v1.Placement.PLCMT_TOP;
            case RIGHT:
                return com.ctrip.ferriswheel.proto.v1.Placement.PLCMT_RIGHT;
            case BOTTOM:
                return com.ctrip.ferriswheel.proto.v1.Placement.PLCMT_BOTTOM;
            case CENTER:
                return com.ctrip.ferriswheel.proto.v1.Placement.PLCMT_CENTER;
            default:
                return com.ctrip.ferriswheel.proto.v1.Placement.UNRECOGNIZED;
        }
    }

    public static Placement bean(com.ctrip.ferriswheel.proto.v1.Placement pbPlacement) {
        if (pbPlacement == null) {
            return null;
        }
        switch (pbPlacement) {
            case PLCMT_UNSET:
                return null;
            case PLCMT_LEFT:
                return Placement.LEFT;
            case PLCMT_TOP:
                return Placement.TOP;
            case PLCMT_RIGHT:
                return Placement.RIGHT;
            case PLCMT_BOTTOM:
                return Placement.BOTTOM;
            case PLCMT_CENTER:
                return Placement.CENTER;
            case UNRECOGNIZED:
            default:
                return null; // FIXME should throw Exception in the future, return null for compatibility temporarily.
            // throw new RuntimeException("Unrecognized placement(pb): " + pbPlacement);
        }
    }

    public static com.ctrip.ferriswheel.proto.v1.Interval pb(Interval bean) {
        return com.ctrip.ferriswheel.proto.v1.Interval.newBuilder()
                .setFrom(bean.getFrom())
                .setTo(bean.getTo())
                .build();
    }

    public static IntervalImpl bean(com.ctrip.ferriswheel.proto.v1.Interval pb) {
        return new IntervalImpl(pb.getFrom(), pb.getTo());
    }

    public static com.ctrip.ferriswheel.proto.v1.AxisBand pb(AxisBand bean) {
        com.ctrip.ferriswheel.proto.v1.AxisBand.Builder builder = com.ctrip.ferriswheel.proto.v1.AxisBand.newBuilder();
        if (bean.getLabel() != null) {
            builder.setLabel(bean.getLabel());
        }
        if (bean.getInterval() != null) {
            builder.setInterval(pb(bean.getInterval()));
        }
        if (bean.getColor() != null) {
            builder.setColor(pb(bean.getColor()));
        }
        return builder.build();
    }

    public static AxisBandImpl bean(com.ctrip.ferriswheel.proto.v1.AxisBand pb) {
        AxisBandImpl bean = new AxisBandImpl();
        if (!pb.getLabel().isEmpty()) {
            bean.setLabel(pb.getLabel());
        }
        if (pb.hasInterval()) {
            bean.setInterval(bean(pb.getInterval()));
        }
        if (pb.hasColor()) {
            bean.setColor(bean(pb.getColor()));
        }
        return bean;
    }

    public static com.ctrip.ferriswheel.proto.v1.Stacking pb(Stacking stacking) {
        if (stacking == null) {
            return com.ctrip.ferriswheel.proto.v1.Stacking.STACKING_UNSET;
        }
        switch (stacking) {
            case ABSOLUTE:
                return com.ctrip.ferriswheel.proto.v1.Stacking.STACKING_ABSOLUTE;
            case PERCENT:
                return com.ctrip.ferriswheel.proto.v1.Stacking.STACKING_PERCENT;
            default:
                return com.ctrip.ferriswheel.proto.v1.Stacking.UNRECOGNIZED;
        }
    }

    public static Stacking bean(com.ctrip.ferriswheel.proto.v1.Stacking pb) {
        switch (pb) {
            case STACKING_UNSET:
                return null;
            case STACKING_ABSOLUTE:
                return Stacking.ABSOLUTE;
            case STACKING_PERCENT:
                return Stacking.PERCENT;
            case UNRECOGNIZED:
            default:
                throw new RuntimeException("Stacking case unrecognized(pb): " + pb);
        }
    }

    public static com.ctrip.ferriswheel.proto.v1.Color pb(Color color) {
        return com.ctrip.ferriswheel.proto.v1.Color.newBuilder()
                .setRed(color.getRed())
                .setGreen(color.getGreen())
                .setBlue(color.getBlue())
                .setAlpha(color.getAlpha())
                .build();
    }

    public static ColorImpl bean(com.ctrip.ferriswheel.proto.v1.Color pb) {
        return new ColorImpl(pb.getRed(), pb.getGreen(), pb.getBlue(), pb.getAlpha());
    }

    public static com.ctrip.ferriswheel.proto.v1.SheetAsset pb(Text text) {
        return com.ctrip.ferriswheel.proto.v1.SheetAsset.newBuilder()
                .setText(
                        com.ctrip.ferriswheel.proto.v1.Text.newBuilder()
                                .setName(text.getName())
                                .setContent(pb(text.getContent()))
                                .setLayout(pb(text.getLayout())))
                .build();
    }

    public static com.ctrip.ferriswheel.proto.v1.Layout pb(Layout bean) {
        com.ctrip.ferriswheel.proto.v1.Layout.Builder builder = com.ctrip.ferriswheel.proto.v1.Layout.newBuilder()
                .setDisplay(pb(bean.getDisplay()))
                .setWidth(bean.getWidth())
                .setHeight(bean.getHeight())
                .setAlign(pb(bean.getAlign()))
                .setVerticalAlign(pb(bean.getVerticalAlign()));
        if (bean.getGrid() != null) {
            builder.setGrid(pb(bean.getGrid()));
        }
        return builder.build();
    }

    public static LayoutImpl bean(com.ctrip.ferriswheel.proto.v1.Layout proto) {
        return new LayoutImpl(
                bean(proto.getDisplay()),
                proto.getWidth(),
                proto.getHeight(),
                bean(proto.getAlign()),
                bean(proto.getVerticalAlign()),
                proto.hasGrid() ? bean(proto.getGrid()) : null
        );
    }

    public static void fillBeanFromProto(LayoutImpl bean, com.ctrip.ferriswheel.proto.v1.Layout proto) {
        bean.copy(bean(proto));
    }

    public static com.ctrip.ferriswheel.proto.v1.Grid pb(Grid bean) {
        com.ctrip.ferriswheel.proto.v1.Grid.Builder builder = com.ctrip.ferriswheel.proto.v1.Grid.newBuilder()
                .setColumns(bean.getColumns())
                .setRows(bean.getRows());
        if (bean.getColumn() != null) {
            builder.setColumn(pb(bean.getColumn()));
        }
        if (bean.getRow() != null) {
            builder.setRow(pb(bean.getRow()));
        }
        return builder.build();
    }

    public static LayoutImpl.GridImpl bean(com.ctrip.ferriswheel.proto.v1.Grid pb) {
        LayoutImpl.GridImpl bean = new LayoutImpl.GridImpl(
                pb.getColumns(),
                pb.getRows(),
                pb.hasColumn() ? bean(pb.getColumn()) : null,
                pb.hasRow() ? bean(pb.getRow()) : null
        );
        return bean;
    }

    public static com.ctrip.ferriswheel.proto.v1.Span pb(Span bean) {
        return com.ctrip.ferriswheel.proto.v1.Span.newBuilder()
                .setStart(bean.getStart())
                .setEnd(bean.getEnd())
                .build();
    }

    public static LayoutImpl.SpanImpl bean(com.ctrip.ferriswheel.proto.v1.Span pb) {
        return new LayoutImpl.SpanImpl(pb.getStart(), pb.getEnd());
    }

    public static com.ctrip.ferriswheel.proto.v1.Text pb(String name, Text bean) {
        com.ctrip.ferriswheel.proto.v1.Text.Builder builder = com.ctrip.ferriswheel.proto.v1.Text.newBuilder()
                .setName(name)
                .setContent(pb(bean.getContent()));
        if (bean.getLayout() != null) {
            builder.setLayout(pb(bean.getLayout()));
        }
        return builder.build();
    }

    public static TextData bean(com.ctrip.ferriswheel.proto.v1.Text proto) {
        return new TextData(proto.getName(), toDynamicValue(proto.getContent()),
                proto.hasLayout() ? bean(proto.getLayout()) : null);
    }

    public static com.ctrip.ferriswheel.proto.v1.Display pb(Display bean) {
        if (bean == null) {
            return com.ctrip.ferriswheel.proto.v1.Display.DISP_UNSET;
        }
        switch (bean) {
            case NONE:
                return com.ctrip.ferriswheel.proto.v1.Display.DISP_NONE;
            case BLOCK:
                return com.ctrip.ferriswheel.proto.v1.Display.DISP_BLOCK;
            case GRID:
                return com.ctrip.ferriswheel.proto.v1.Display.DISP_GRID;
            default:
                return com.ctrip.ferriswheel.proto.v1.Display.UNRECOGNIZED;
        }
    }

    public static Display bean(com.ctrip.ferriswheel.proto.v1.Display pb) {
        if (pb == null) {
            return null;
        }
        switch (pb) {
            case DISP_UNSET:
                return null;
            case DISP_NONE:
                return Display.NONE;
            case DISP_BLOCK:
                return Display.BLOCK;
            case DISP_GRID:
                return Display.GRID;
            case UNRECOGNIZED:
            default:
                throw new IllegalArgumentException("Display type unrecognized: " + pb);
        }
    }
}
