package com.ctrip.ferriswheel.proto.util;

import com.ctrip.ferriswheel.common.Sheet;
import com.ctrip.ferriswheel.common.Workbook;
import com.ctrip.ferriswheel.common.chart.Chart;
import com.ctrip.ferriswheel.common.chart.DataSeries;
import com.ctrip.ferriswheel.common.query.*;
import com.ctrip.ferriswheel.common.table.Table;
import com.ctrip.ferriswheel.common.util.DataSet;
import com.ctrip.ferriswheel.common.util.DataSetBuilder;
import com.ctrip.ferriswheel.common.variant.*;
import com.ctrip.ferriswheel.core.asset.DefaultQueryAutomaton;
import com.ctrip.ferriswheel.core.asset.DefaultWorkbook;
import com.ctrip.ferriswheel.core.asset.FilingClerk;
import com.ctrip.ferriswheel.core.bean.ChartData;
import com.ctrip.ferriswheel.core.bean.DefaultEnvironment;
import com.ctrip.ferriswheel.core.bean.TableAutomatonInfo;
import com.ctrip.ferriswheel.core.loader.DefaultProviderManager;
import com.google.protobuf.InvalidProtocolBufferException;
import junit.framework.TestCase;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

public class TestPbHelper extends TestCase {
    public void testWorkbookToProto() {
        //
    }

    public void testSheetToProto() {
        //
    }

    public void testTableToProto() {
        final String scheme = UUID.randomUUID().toString();

        DefaultProviderManager pm = new DefaultProviderManager();
        pm.register(new DataProvider() {
            @Override
            public boolean acceptsQuery(DataQuery query) {
                return scheme.equalsIgnoreCase(query.getScheme());
            }

            @Override
            public QueryResult execute(DataQuery query, boolean forceRefresh) throws IOException {
                DataSet dataSet = DataSetBuilder.withColumnCount(1)
                        .newRecord()
                        .set(0, Value.str("hello world"))
                        .commit()
                        .build();
                return new ImmutableQueryResult(ErrorCodes.OK, "Ok",
                        ImmutableCacheHint.newBuilder().build(), dataSet);
            }
        });
        DefaultEnvironment env = new DefaultEnvironment.Builder()
                .setProviderManager(pm)
                .build();

        DefaultWorkbook workbook = new FilingClerk(env).createWorkbook("test-workbook");
        Table table = workbook.addSheet("sheet1").addAsset(Table.class, "table1");
        table.addRows(3);
        table.addColumns(1);
        table.setCellFormula(0, 0, "2^10");
        table.setCellFormula(2, 0, "A1*4");

        workbook.refresh();

        com.ctrip.ferriswheel.proto.v1.SheetAsset asset = PbHelper.pb(table);
        assertEquals(com.ctrip.ferriswheel.proto.v1.SheetAsset.AssetCase.TABLE, asset.getAssetCase());
        com.ctrip.ferriswheel.proto.v1.Table t = asset.getTable();
        assertEquals("table1", t.getName());
        assertFalse(t.hasAutomaton());
        assertEquals(2, t.getRowsCount());
        assertEquals(0, t.getRows(0).getRowIndex());
        assertEquals(2, t.getRows(1).getRowIndex());

        Map<String, Parameter> builtinParams = new LinkedHashMap<>();
        builtinParams.put("Greeting", new DefaultParameter("Greeting", new DynamicValue("\"hello world\"")));
        builtinParams.put("Goodbye", new DefaultParameter("Goodbye", new DynamicValue("\"bye!\"")));
        table.automate(new TableAutomatonInfo.QueryAutomatonInfo(new TableAutomatonInfo.QueryTemplateInfo(scheme, builtinParams)));

        workbook.refresh();

        asset = PbHelper.pb(table);
        t = asset.getTable();
        assertTrue(t.hasAutomaton());
        com.ctrip.ferriswheel.proto.v1.TableAutomaton auto = t.getAutomaton();
        assertEquals(com.ctrip.ferriswheel.proto.v1.TableAutomaton.AutomatonCase.QUERY_AUTOMATON, auto.getAutomatonCase());
        com.ctrip.ferriswheel.proto.v1.QueryTemplate template = auto.getQueryAutomaton().getTemplate();
        assertEquals(scheme, template.getScheme());
        assertEquals(2, template.getBuiltinParamsCount());
//        com.ctrip.ferriswheel.proto.v1.UnionValue v = template.getBuiltinParamList().get("Greeting");
//        assertEquals("\"hello world\"", v.getFormulaString());
//        v = template.getBuiltinParamMap().get("Goodbye");
//        assertEquals("\"bye!\"", v.getFormulaString());
//        assertEquals(1, template.getUserParamRuleCount());
//        com.ctrip.ferriswheel.proto.v1.ValueRule r = template.getUserParamRuleMap().get("Name");
//        assertEquals(com.ctrip.ferriswheel.proto.v1.VariantType.STRING, r.getType());
//        assertTrue(r.getNullable());
//        assertEquals(2, r.getAllowedValueCount());
//        assertEquals("Sand", r.getAllowedValueList().get(0).getString());
//        assertEquals("Snow", r.getAllowedValueList().get(1).getString());
    }

    public void testChartAndSeriesToProto() {
        DefaultWorkbook workbook = new FilingClerk(new DefaultEnvironment.Builder().build())
                .createWorkbook("test-workbook");
        Sheet sheet1 = workbook.addSheet("sheet1");
        Table table1 = sheet1.addAsset(Table.class, "table1");
        table1.addRows(1);
        table1.addColumns(3);
        table1.setCellValue(0, 0, Value.str("foobar"));
        table1.setCellValue(0, 1, Value.dec(2));
        table1.setCellValue(0, 2, Value.dec(3));
        Chart chart1 = sheet1.addAsset(Chart.class,
                new ChartData("chart1", "Line",
                        new DynamicValue("\"Chart 1 (Line)\""),
                        null,
                        Arrays.asList(new ChartData.SeriesImpl(
                                new DynamicValue("table1!A1"),
                                null,
                                new DynamicValue("table1!B1:C1")))));

        workbook.refresh();

        com.ctrip.ferriswheel.proto.v1.SheetAsset asset = PbHelper.pb(chart1);
        assertEquals(com.ctrip.ferriswheel.proto.v1.SheetAsset.AssetCase.CHART, asset.getAssetCase());
        com.ctrip.ferriswheel.proto.v1.Chart c1 = asset.getChart();
        assertEquals("chart1", c1.getName());
        assertEquals("Line", c1.getType());
        assertEquals(com.ctrip.ferriswheel.proto.v1.UnionValue.ValueCase.VALUE_NOT_SET, c1.getCategories().getValueCase());
        assertEquals(1, c1.getSeriesCount());
        com.ctrip.ferriswheel.proto.v1.Series s = c1.getSeries(0);
        assertEquals("table1!A1", s.getName().getFormulaString());
        assertEquals("foobar", s.getName().getString());
        assertEquals(com.ctrip.ferriswheel.proto.v1.UnionValue.ValueCase.VALUE_NOT_SET, s.getXValues().getValueCase());
        assertEquals("table1!B1:C1", s.getYValues().getFormulaString());
        assertEquals(com.ctrip.ferriswheel.proto.v1.UnionValue.ValueCase.LIST, s.getYValues().getValueCase());
        assertEquals(2, s.getYValues().getList().getItemsCount());
        assertEquals("2", s.getYValues().getList().getItems(0).getDecimal());
        assertEquals("3", s.getYValues().getList().getItems(1).getDecimal());
    }

    public void testRowToProto() {
        DefaultWorkbook workbook = new FilingClerk(new DefaultEnvironment.Builder().build())
                .createWorkbook("test-workbook");
        Table table = workbook.addSheet("sheet1").addAsset(Table.class, "table1");
        table.addRows(3);
        table.addColumns(3);
        table.setCellFormula(2, 0, "2^10");
        table.setCellFormula(2, 2, "A3*4");

        workbook.refresh();

        com.ctrip.ferriswheel.proto.v1.Row row = PbHelper.pb(table.getRow(2), 2);
        assertEquals(2, row.getRowIndex());
        assertEquals(2, row.getCellsCount());
        assertEquals(0, row.getCells(0).getColumnIndex());
        assertEquals("1024", row.getCells(0).getValue().getDecimal());
        assertEquals(2, row.getCells(1).getColumnIndex());
        assertEquals("4096", row.getCells(1).getValue().getDecimal());
    }

    public void testCellToProto() {
        DefaultWorkbook workbook = new FilingClerk(new DefaultEnvironment.Builder().build())
                .createWorkbook("test-workbook");
        Table table = workbook.addSheet("sheet1").addAsset(Table.class, "table1");
        table.addRows(3);
        table.addColumns(3);
        table.setCellFormula(2, 2, "2^10");

        workbook.refresh();

        com.ctrip.ferriswheel.proto.v1.Cell cell = PbHelper.pb(table.getCell(2, 2), 2);
        assertEquals(2, cell.getColumnIndex());
        com.ctrip.ferriswheel.proto.v1.UnionValue v = cell.getValue();
        assertEquals("2^10", v.getFormulaString());
        assertEquals(com.ctrip.ferriswheel.proto.v1.UnionValue.ValueCase.DECIMAL, v.getValueCase());
        assertEquals("1024", v.getDecimal());
    }

    public void testVariantToProto() {
        com.ctrip.ferriswheel.proto.v1.UnionValue v = PbHelper.pb(Value.err(ErrorCodes.VALUE));
        assertEquals("", v.getFormulaString());
        assertEquals(com.ctrip.ferriswheel.proto.v1.UnionValue.ValueCase.ERROR, v.getValueCase());
        assertEquals(com.ctrip.ferriswheel.proto.v1.ErrorCode.EC_VALUE.getNumber(), v.getError().getNumber());

        v = PbHelper.pb(Value.BLANK);
        assertEquals("", v.getFormulaString());
        assertEquals(com.ctrip.ferriswheel.proto.v1.UnionValue.ValueCase.VALUE_NOT_SET, v.getValueCase());

        v = PbHelper.pb(Value.dec(new BigDecimal("3.14")));
        assertEquals("", v.getFormulaString());
        assertEquals(com.ctrip.ferriswheel.proto.v1.UnionValue.ValueCase.DECIMAL, v.getValueCase());
        assertEquals("3.14", v.getDecimal());

        v = PbHelper.pb(Value.bool(true));
        assertEquals("", v.getFormulaString());
        assertEquals(com.ctrip.ferriswheel.proto.v1.UnionValue.ValueCase.BOOLEAN, v.getValueCase());
        assertTrue(v.getBoolean());

        Instant instant = Instant.now();
        v = PbHelper.pb(Value.date(instant));
        assertEquals("", v.getFormulaString());
        assertEquals(com.ctrip.ferriswheel.proto.v1.UnionValue.ValueCase.DATE, v.getValueCase());
        assertEquals(instant.getEpochSecond(), v.getDate().getSeconds());
        assertEquals(instant.getNano(), v.getDate().getNanos());

        v = PbHelper.pb(Value.str("hello"));
        assertEquals("", v.getFormulaString());
        assertEquals(com.ctrip.ferriswheel.proto.v1.UnionValue.ValueCase.STRING, v.getValueCase());
        assertEquals("hello", v.getString());

        DynamicValue dv = new DynamicValue("2^10");
        v = PbHelper.pb(dv);
        assertEquals("2^10", v.getFormulaString());
        assertEquals(com.ctrip.ferriswheel.proto.v1.UnionValue.ValueCase.VALUE_NOT_SET, v.getValueCase());
//        assertEquals("1024", v.getDecimal());
    }

    public void testProtoToWorkbook() {
        final String scheme = UUID.randomUUID().toString();
        DefaultProviderManager pm = new DefaultProviderManager();
        pm.register(new DataProvider() {
            @Override
            public boolean acceptsQuery(DataQuery query) {
                return scheme.equalsIgnoreCase(query.getScheme());
            }

            @Override
            public QueryResult execute(DataQuery query, boolean forceRefresh) throws IOException {
                System.out.println("Filling table by fake provider.");
                DataSet dataSet = DataSetBuilder.withColumnCount(2)
                        .newRecord()
                        .set(0, Value.dec(13))
                        .set(1, Value.dec(23))
                        .commit()
                        .build();
                return new ImmutableQueryResult(ErrorCodes.OK, "Ok",
                        ImmutableCacheHint.newBuilder().build(), dataSet);
            }
        });

        DefaultEnvironment env = new DefaultEnvironment.Builder().setProviderManager(pm).build();

        Workbook wb = new FilingClerk(env).createWorkbook("test-workbook");
        Sheet s1 = wb.addSheet("s1");
        Table t0 = s1.addAsset(Table.class, "t0");
        Table t1 = s1.addAsset(Table.class, "t1");
        Table t2 = s1.addAsset(Table.class, "t2");

        Map<String, Parameter> builtinParams = new HashMap<>();
        builtinParams.put("Greeting", new DefaultParameter("Greeting", new DynamicValue("\"hello world\"")));
        t0.automate(new TableAutomatonInfo.QueryAutomatonInfo(new TableAutomatonInfo.QueryTemplateInfo(scheme, builtinParams)));

        t1.addRows(3);
        t1.addColumns(4);

        t1.setCellFormula(0, 0, "t2!A1^2");
        t1.setCellValue(0, 1, Value.str("foo"));
        t1.setCellValue(0, 2, Value.str("bar"));
        t1.setCellValue(0, 3, Value.str("foobar"));
        t1.setCellValue(1, 0, Value.str("A"));
        t1.setCellValue(1, 1, Value.dec(11));
        t1.setCellValue(1, 2, Value.dec(12));
        t1.setCellFormula(1, 3, "t0!A1");
        t1.setCellValue(2, 0, Value.str("B"));
        t1.setCellValue(2, 1, Value.dec(21));
        t1.setCellValue(2, 2, Value.dec(22));
        t1.setCellFormula(2, 3, "t0!B1");
        s1.addAsset(Chart.class, new ChartData("c1", "Line",
                new DynamicValue("\"Line Chart 1\""),
                new DynamicValue("t1!B1:D1"),
                Arrays.asList(
                        new ChartData.SeriesImpl(
                                new DynamicValue("t1!A2"),
                                null,
                                new DynamicValue("t1!B2:D2")),
                        new ChartData.SeriesImpl(
                                new DynamicValue("t1!A3"),
                                null,
                                new DynamicValue("t1!B3:D3"))
                )));

        t2.addRows(1);
        t2.addColumns(1);
        t2.setCellValue(0, 0, Value.dec(2));

        wb.refresh();

        com.ctrip.ferriswheel.proto.v1.Workbook proto = PbHelper.pb(wb);
        wb = PbHelper.bean(env, proto);

        wb.refresh();

        // assertions
        assertEquals(1, wb.getSheetCount());
        s1 = wb.getSheet(0);
        assertEquals("s1", s1.getName());
        assertEquals(4, s1.getAssetCount());
        t0 = s1.getAsset("t0");
        t1 = s1.getAsset("t1");
        t2 = s1.getAsset("t2");
        Chart c1 = s1.getAsset("c1");

        assertEquals(1, t0.getRowCount());
        assertEquals(2, t0.getColumnCount());
        assertEquals(13, t0.getCell(0, 0).getData().intValue());
        assertEquals(23, t0.getCell(0, 1).getData().intValue());
        DefaultQueryAutomaton auto = (DefaultQueryAutomaton) t0.getAutomaton();
        TableAutomatonInfo.QueryTemplateInfo template = auto.getQueryAutomatonInfo().getTemplate();
        assertEquals(scheme, template.getScheme());
        assertEquals(1, template.getAllBuiltinParams().size());
        Parameter param = template.getAllBuiltinParams().get("Greeting");
        assertEquals("\"hello world\"", param.getValue().getFormulaString());

        assertEquals(3, t1.getRowCount());
        assertEquals(4, t1.getColumnCount());
        assertEquals(4, t1.getCell(0, 0).getData().intValue());
        assertEquals("t2!A1^2", t1.getCell(0, 0).getData().getFormulaString());
        assertEquals("foo", t1.getCell(0, 1).getData().strValue());
        assertEquals("bar", t1.getCell(0, 2).getData().strValue());
        assertEquals("foobar", t1.getCell(0, 3).getData().strValue());
        assertEquals("A", t1.getCell(1, 0).getData().strValue());
        assertEquals(11, t1.getCell(1, 1).getData().intValue());
        assertEquals(12, t1.getCell(1, 2).getData().intValue());
        assertEquals(13, t1.getCell(1, 3).getData().intValue());
        assertEquals("t0!A1", t1.getCell(1, 3).getData().getFormulaString());
        assertEquals("B", t1.getCell(2, 0).getData().strValue());
        assertEquals(21, t1.getCell(2, 1).getData().intValue());
        assertEquals(22, t1.getCell(2, 2).getData().intValue());
        assertEquals(23, t1.getCell(2, 3).getData().intValue());
        assertEquals("t0!B1", t1.getCell(2, 3).getData().getFormulaString());

        assertEquals(1, t2.getRowCount());
        assertEquals(1, t2.getColumnCount());
        assertEquals(2, t2.getCell(0, 0).getData().intValue());

        assertEquals("c1", c1.getName());
        assertEquals("Line", c1.getType());
        assertEquals("\"Line Chart 1\"", c1.getTitle().getFormulaString());
        assertEquals("t1!B1:D1", c1.getCategories().getFormulaString());
        assertEquals("foo", c1.getCategories().item(0).strValue());
        assertEquals("bar", c1.getCategories().item(1).strValue());
        assertEquals("foobar", c1.getCategories().item(2).strValue());
        assertEquals(2, c1.getSeriesCount());
        DataSeries series1 = c1.getSeries(0);
        assertEquals("t1!A2", series1.getName().getFormulaString());
        assertNull(series1.getxValues());
        assertEquals("t1!B2:D2", series1.getyValues().getFormulaString());
        assertEquals(11, series1.getyValues().item(0).intValue());
        assertEquals(12, series1.getyValues().item(1).intValue());
        assertEquals(13, series1.getyValues().item(2).intValue());
        DataSeries series2 = c1.getSeries(1);
        assertEquals("t1!A3", series2.getName().getFormulaString());
        assertNull(series2.getxValues());
        assertEquals("t1!B3:D3", series2.getyValues().getFormulaString());
        assertEquals(21, series2.getyValues().item(0).intValue());
        assertEquals(22, series2.getyValues().item(1).intValue());
        assertEquals(23, series2.getyValues().item(2).intValue());
    }

    public void testJSON() throws InvalidProtocolBufferException {
        Workbook wb = new FilingClerk(new DefaultEnvironment.Builder().build()).createWorkbook("test-workbook");
        Sheet s1 = wb.addSheet("sheet1");
        Table t1 = s1.addAsset(Table.class, "table1");
        t1.addRows(3);
        t1.addColumns(3);
        t1.setCellValue(0, 1, Value.str("foo"));
        t1.setCellValue(0, 2, Value.str("bar"));
        t1.setCellValue(1, 0, Value.str("a"));
        t1.setCellValue(1, 1, Value.dec(11));
        t1.setCellValue(1, 2, Value.dec(12));
        t1.setCellValue(2, 0, Value.str("b"));
        t1.setCellValue(2, 1, Value.dec(21));
        t1.setCellValue(2, 2, Value.dec(22));

        s1.addAsset(Chart.class, new ChartData("chart1", "Line",
                new DynamicValue("\"Hello Line Chart1\""),
                new DynamicValue("table1!B1:C1"),
                Arrays.asList(new ChartData.SeriesImpl(
                        new DynamicValue("table1!A2"),
                        null,
                        new DynamicValue("table1!B2:C2")))));

//        String json = JsonFormat.printer().print(SpreadsheetProtoHelper.pb(wb));
//        System.out.println(json);
    }
}
