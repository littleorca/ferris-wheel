package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.common.Environment;
import com.ctrip.ferriswheel.common.chart.Chart;
import com.ctrip.ferriswheel.common.table.Table;
import com.ctrip.ferriswheel.common.variant.DynamicValue;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.common.view.Orientation;
import com.ctrip.ferriswheel.common.view.Placement;
import com.ctrip.ferriswheel.core.bean.ChartData;
import com.ctrip.ferriswheel.core.bean.DefaultEnvironment;
import com.ctrip.ferriswheel.core.view.LayoutImpl;
import junit.framework.TestCase;

import java.util.Arrays;

public class TestDefaultChartWithBinder extends TestCase {
    private DefaultTable table;
    private Environment environment;

    @Override
    protected void setUp() throws Exception {
        environment = new DefaultEnvironment.Builder().build();
        DefaultWorkbook wb = new DefaultWorkbook(environment);
        DefaultSheet s1 = wb.addSheet("s1");
        table = (DefaultTable) s1.addAsset(Table.class, "t1");

        table.addColumns(0,3);
        table.addRows(0, 3);

        table.setCellValue(0, 1, Value.str("foo"));
        table.setCellValue(0, 2, Value.str("bar"));
        table.setCellValue(1, 0, Value.str("alpha"));
        table.setCellValue(1, 1, Value.dec(1));
        table.setCellValue(1, 2, Value.dec(2));
        table.setCellValue(2, 0, Value.str("beta"));
        table.setCellValue(2, 1, Value.dec(3));
        table.setCellValue(2, 2, Value.dec(4));
        System.out.println(table);
    }

    public void testRebindColumns() {
        DefaultSheet s1 = table.getSheet();
        DefaultTable t1 = table;

        DefaultChart c1 = (DefaultChart) s1.addAsset(Chart.class, new ChartData(
                "c1", "Line",
                new DynamicValue(Value.str("Chart 1")),
                new DynamicValue("t1!B1:C1"),
                Arrays.asList(
                        new ChartData.SeriesImpl(new DynamicValue("t1!A2"),
                                null,
                                new DynamicValue("t1!B2:C2")),
                        new ChartData.SeriesImpl(new DynamicValue("t1!A3"),
                                null,
                                new DynamicValue("t1!B3:C3"))
                ),
                new LayoutImpl(),
                new ChartData.BinderImpl(
                        new DynamicValue("t1!A:C"),
                        Orientation.HORIZONTAL,
                        Placement.TOP,
                        Placement.LEFT
                ),
                null,
                null,
                null
        ));

        DefaultChart c2 = (DefaultChart) s1.addAsset(Chart.class, new ChartData(
                "c2", "Line",
                new DynamicValue(Value.str("Chart 2")),
                new DynamicValue(Value.BLANK),
                Arrays.asList(),
                new LayoutImpl(),
                new ChartData.BinderImpl(
                        new DynamicValue("t1!A:C"),
                        Orientation.VERTICAL,
                        Placement.LEFT,
                        Placement.TOP
                ),
                null,
                null,
                null
        ));

        t1.addRows(3, 1);
        t1.addColumns(3, 1);

        t1.setCellValue(0, 3, Value.str("foobar"));
        t1.setCellValue(1, 3, Value.dec(5));
        t1.setCellValue(2, 3, Value.dec(6));
        t1.setCellValue(3, 0, Value.str("alpha-beta"));
        t1.setCellValue(3, 1, Value.dec(7));
        t1.setCellValue(3, 2, Value.dec(8));
        t1.setCellValue(3, 3, Value.dec(9));

        System.out.println(t1);

        // c1
        assertEquals(2, c1.getCategories().itemCount());
        assertEquals("foo", c1.getCategories().item(0).strValue());
        assertEquals("bar", c1.getCategories().item(1).strValue());
        assertEquals(3, c1.getSeriesCount());
        DefaultDataSeries series = c1.getSeries(0);
        assertEquals("alpha", series.getName().strValue());
        assertEquals(2, series.getyValues().itemCount());
        assertEquals(1, series.getyValues().item(0).intValue());
        assertEquals(2, series.getyValues().item(1).intValue());
        series = c1.getSeries(1);
        assertEquals("beta", series.getName().strValue());
        assertEquals(2, series.getyValues().itemCount());
        assertEquals(3, series.getyValues().item(0).intValue());
        assertEquals(4, series.getyValues().item(1).intValue());
        series = c1.getSeries(2);
        assertEquals("alpha-beta", series.getName().strValue());
        assertEquals(2, series.getyValues().itemCount());
        assertEquals(7, series.getyValues().item(0).intValue());
        assertEquals(8, series.getyValues().item(1).intValue());

        // c2
        assertEquals(3, c2.getCategories().itemCount());
        assertEquals("alpha", c2.getCategories().item(0).strValue());
        assertEquals("beta", c2.getCategories().item(1).strValue());
        assertEquals("alpha-beta", c2.getCategories().item(2).strValue());
        assertEquals(2, c2.getSeriesCount());
        series = c2.getSeries(0);
        assertEquals("foo", series.getName().strValue());
        assertEquals(3, series.getyValues().itemCount());
        assertEquals(1, series.getyValues().item(0).intValue());
        assertEquals(3, series.getyValues().item(1).intValue());
        assertEquals(7, series.getyValues().item(2).intValue());
        series = c2.getSeries(1);
        assertEquals("bar", series.getName().strValue());
        assertEquals(3, series.getyValues().itemCount());
        assertEquals(2, series.getyValues().item(0).intValue());
        assertEquals(4, series.getyValues().item(1).intValue());
        assertEquals(8, series.getyValues().item(2).intValue());
    }

    public void testRebindRows() {
        DefaultSheet s1 = table.getSheet();
        DefaultTable t1 = table;

        DefaultChart c1 = (DefaultChart) s1.addAsset(Chart.class, new ChartData(
                "c1", "Line",
                new DynamicValue(Value.str("Chart 1")),
                new DynamicValue("t1!B1:C1"),
                Arrays.asList(
                        new ChartData.SeriesImpl(new DynamicValue("t1!A2"),
                                null,
                                new DynamicValue("t1!B2:C2")),
                        new ChartData.SeriesImpl(new DynamicValue("t1!A3"),
                                null,
                                new DynamicValue("t1!B3:C3"))
                ),
                new LayoutImpl(),
                new ChartData.BinderImpl(
                        new DynamicValue("t1!1:3"),
                        Orientation.HORIZONTAL,
                        Placement.TOP,
                        Placement.LEFT
                ),
                null,
                null,
                null
        ));

        DefaultChart c2 = (DefaultChart) s1.addAsset(Chart.class, new ChartData(
                "c2", "Line",
                new DynamicValue(Value.str("Chart 2")),
                new DynamicValue(Value.BLANK),
                Arrays.asList(),
                new LayoutImpl(),
                new ChartData.BinderImpl(
                        new DynamicValue("t1!1:3"),
                        Orientation.VERTICAL,
                        Placement.LEFT,
                        Placement.TOP
                ),
                null,
                null,
                null
        ));

        t1.addRows(3, 1);
        t1.addColumns(3, 1);

        t1.setCellValue(0, 3, Value.str("foobar"));
        t1.setCellValue(1, 3, Value.dec(5));
        t1.setCellValue(2, 3, Value.dec(6));
        t1.setCellValue(3, 0, Value.str("alpha-beta"));
        t1.setCellValue(3, 1, Value.dec(7));
        t1.setCellValue(3, 2, Value.dec(8));
        t1.setCellValue(3, 3, Value.dec(9));

        System.out.println(t1);

        // c1
        assertEquals(3, c1.getCategories().itemCount());
        assertEquals("foo", c1.getCategories().item(0).strValue());
        assertEquals("bar", c1.getCategories().item(1).strValue());
        assertEquals("foobar", c1.getCategories().item(2).strValue());
        assertEquals(2, c1.getSeriesCount());
        DefaultDataSeries series = c1.getSeries(0);
        assertEquals("alpha", series.getName().strValue());
        assertEquals(3, series.getyValues().itemCount());
        assertEquals(1, series.getyValues().item(0).intValue());
        assertEquals(2, series.getyValues().item(1).intValue());
        assertEquals(5, series.getyValues().item(2).intValue());
        series = c1.getSeries(1);
        assertEquals("beta", series.getName().strValue());
        assertEquals(3, series.getyValues().itemCount());
        assertEquals(3, series.getyValues().item(0).intValue());
        assertEquals(4, series.getyValues().item(1).intValue());
        assertEquals(6, series.getyValues().item(2).intValue());

        // c2
        assertEquals(2, c2.getCategories().itemCount());
        assertEquals("alpha", c2.getCategories().item(0).strValue());
        assertEquals("beta", c2.getCategories().item(1).strValue());
        assertEquals(3, c2.getSeriesCount());
        series = c2.getSeries(0);
        assertEquals("foo", series.getName().strValue());
        assertEquals(2, series.getyValues().itemCount());
        assertEquals(1, series.getyValues().item(0).intValue());
        assertEquals(3, series.getyValues().item(1).intValue());
        series = c2.getSeries(1);
        assertEquals("bar", series.getName().strValue());
        assertEquals(2, series.getyValues().itemCount());
        assertEquals(2, series.getyValues().item(0).intValue());
        assertEquals(4, series.getyValues().item(1).intValue());
        series = c2.getSeries(2);
        assertEquals("foobar", series.getName().strValue());
        assertEquals(2, series.getyValues().itemCount());
        assertEquals(5, series.getyValues().item(0).intValue());
        assertEquals(6, series.getyValues().item(1).intValue());
    }
}
