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
import com.ctrip.ferriswheel.common.chart.DataSeries;
import com.ctrip.ferriswheel.common.table.Table;
import com.ctrip.ferriswheel.common.variant.DynamicValue;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.core.bean.ChartData;
import com.ctrip.ferriswheel.core.bean.DefaultEnvironment;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

public class TestDefaultSheet extends TestCase {
    protected ReviseCollector reviseCollector = new ReviseCollector();
    protected DefaultWorkbook workbook = new DefaultWorkbook(new DefaultEnvironment.Builder().build());

    @Override
    protected void setUp() throws Exception {
        workbook.addListener(reviseCollector);
    }

    public void testAddChart() {
        DefaultTable table = createTable33();
        table.addRows(0, 1);
        table.setCellValue(0, 0, new Value.StrValue("Q1"));
        table.setCellValue(0, 1, new Value.StrValue("Q2"));
        table.setCellValue(0, 2, new Value.StrValue("Q3"));
        table.addColumns(0, 1);
        table.setCellValue(1, 0, new Value.StrValue("apple"));
        table.setCellValue(2, 0, new Value.StrValue("boy"));
        table.setCellValue(3, 0, new Value.StrValue("cat"));

        List<DataSeries> seriesList = new ArrayList<>(3);
        seriesList.add(new ChartData.SeriesImpl(
                new DynamicValue(table.getSheet().getName() + "!" + table.getName() + "!$A$2"),
                null,
                new DynamicValue(table.getSheet().getName() + "!" + table.getName() + "!$B$2:$D$2")));
        seriesList.add(new ChartData.SeriesImpl(
                new DynamicValue(table.getSheet().getName() + "!" + table.getName() + "!$A$3"),
                null,
                new DynamicValue(table.getSheet().getName() + "!" + table.getName() + "!$B$3:$D$3")));
        seriesList.add(new ChartData.SeriesImpl(
                new DynamicValue(table.getSheet().getName() + "!" + table.getName() + "!$A$4"),
                null,
                new DynamicValue(table.getSheet().getName() + "!" + table.getName() + "!$B$4:$D$4")));

        ChartData chartSettings = new ChartData("testChart", "Line",
                new DynamicValue("\"Hello Line Chart!\""),
                new DynamicValue(table.getSheet().getName() + "!" + table.getName() + "!$B$1:$D$1"),
                seriesList);

        DefaultChart chart = (DefaultChart) table.getSheet().addAsset(Chart.class, chartSettings);

        table.getWorkbook().refresh();

        assertEquals("Line", chart.getType());
        assertEquals("testChart", chart.getName());
        assertEquals("Hello Line Chart!", chart.getTitle().strValue());
        assertEquals(table.getSheet().getName() + "!" + table.getName() + "!$B$1:$D$1", chart.getCategories().getFormulaString());
        assertEquals(3, chart.getCategories().itemCount());
        assertEquals("Q1", chart.getCategories().item(0).strValue());
        assertEquals("Q2", chart.getCategories().item(1).strValue());
        assertEquals("Q3", chart.getCategories().item(2).strValue());

        assertEquals(3, chart.getSeriesCount());

        DefaultDataSeries series = chart.getSeries(0);
        assertEquals(table.getSheet().getName() + "!" + table.getName() + "!$A$2", series.getName().getFormulaString());
        assertEquals("apple", series.getName().strValue());
        assertNull(series.getxValues());
        assertEquals(table.getSheet().getName() + "!" + table.getName() + "!$B$2:$D$2", series.getyValues().getFormulaString());
        assertEquals(3, series.getyValues().itemCount());
        assertEquals(11, series.getyValues().item(0).intValue());
        assertEquals(12, series.getyValues().item(1).intValue());
        assertEquals(13, series.getyValues().item(2).intValue());

        series = chart.getSeries(1);
        assertEquals(table.getSheet().getName() + "!" + table.getName() + "!$A$3", series.getName().getFormulaString());
        assertEquals("boy", series.getName().strValue());
        assertNull(series.getxValues());
        assertEquals(table.getSheet().getName() + "!" + table.getName() + "!$B$3:$D$3", series.getyValues().getFormulaString());
        assertEquals(3, series.getyValues().itemCount());
        assertEquals(21, series.getyValues().item(0).intValue());
        assertEquals(22, series.getyValues().item(1).intValue());
        assertEquals(23, series.getyValues().item(2).intValue());

        series = chart.getSeries(2);
        assertEquals(table.getSheet().getName() + "!" + table.getName() + "!$A$4", series.getName().getFormulaString());
        assertEquals("cat", series.getName().strValue());
        assertNull(series.getxValues());
        assertEquals(table.getSheet().getName() + "!" + table.getName() + "!$B$4:$D$4", series.getyValues().getFormulaString());
        assertEquals(3, series.getyValues().itemCount());
        assertEquals(31, series.getyValues().item(0).intValue());
        assertEquals(32, series.getyValues().item(1).intValue());
        assertEquals(33, series.getyValues().item(2).intValue());

        // let's do something
        table.setCellValue(1, 1, new Value.DecimalValue("1024"));
        table.setCellValue(3, 3, new Value.DecimalValue("3072"));
        table.addRows(2, 1);
        table.addColumns(3, 1);

        table.getWorkbook().refresh();

        chart = table.getSheet().getAsset("testChart");

        assertEquals("Line", chart.getType());
        assertEquals("testChart", chart.getName());
        assertEquals("Hello Line Chart!", chart.getTitle().strValue());
        assertEquals(table.getSheet().getName() + "!" + table.getName() + "!$B$1:$E$1", chart.getCategories().getFormulaString());
        assertEquals(4, chart.getCategories().itemCount());
        assertEquals("Q1", chart.getCategories().item(0).strValue());
        assertEquals("Q2", chart.getCategories().item(1).strValue());
        assertTrue(chart.getCategories().item(2).isBlank());
        assertEquals("Q3", chart.getCategories().item(3).strValue());

        assertEquals(3, chart.getSeriesCount());

        series = chart.getSeries(0);
        assertEquals(table.getSheet().getName() + "!" + table.getName() + "!$A$2", series.getName().getFormulaString());
        assertEquals("apple", series.getName().strValue());
        assertNull(series.getxValues());
        assertEquals(table.getSheet().getName() + "!" + table.getName() + "!$B$2:$E$2", series.getyValues().getFormulaString());
        assertEquals(4, series.getyValues().itemCount());
        assertEquals(1024, series.getyValues().item(0).intValue());
        assertEquals(12, series.getyValues().item(1).intValue());
        assertTrue(series.getyValues().item(2).isBlank());
        assertEquals(13, series.getyValues().item(3).intValue());

        series = chart.getSeries(1);
        assertEquals(table.getSheet().getName() + "!" + table.getName() + "!$A$4", series.getName().getFormulaString());
        assertEquals("boy", series.getName().strValue());
        assertNull(series.getxValues());
        assertEquals(table.getSheet().getName() + "!" + table.getName() + "!$B$4:$E$4", series.getyValues().getFormulaString());
        assertEquals(4, series.getyValues().itemCount());
        assertEquals(21, series.getyValues().item(0).intValue());
        assertEquals(22, series.getyValues().item(1).intValue());
        assertTrue(series.getyValues().item(2).isBlank());
        assertEquals(23, series.getyValues().item(3).intValue());

        series = chart.getSeries(2);
        assertEquals(table.getSheet().getName() + "!" + table.getName() + "!$A$5", series.getName().getFormulaString());
        assertEquals("cat", series.getName().strValue());
        assertNull(series.getxValues());
        assertEquals(table.getSheet().getName() + "!" + table.getName() + "!$B$5:$E$5", series.getyValues().getFormulaString());
        assertEquals(4, series.getyValues().itemCount());
        assertEquals(31, series.getyValues().item(0).intValue());
        assertEquals(32, series.getyValues().item(1).intValue());
        assertTrue(series.getyValues().item(2).isBlank());
        assertEquals(3072, series.getyValues().item(3).intValue());
    }

    public void testUpdateChart() {
        DefaultTable table = createTable33();
        table.addRows(0, 1);
        table.setCellValue(0, 0, new Value.StrValue("Q1"));
        table.setCellValue(0, 1, new Value.StrValue("Q2"));
        table.setCellValue(0, 2, new Value.StrValue("Q3"));
        table.addColumns(0, 1);
        table.setCellValue(1, 0, new Value.StrValue("apple"));
        table.setCellValue(2, 0, new Value.StrValue("boy"));
        table.setCellValue(3, 0, new Value.StrValue("cat"));

        List<DataSeries> seriesList = new ArrayList<>(3);
        seriesList.add(new ChartData.SeriesImpl(
                new DynamicValue(table.getSheet().getName() + "!" + table.getName() + "!$A$2"),
                null,
                new DynamicValue(table.getSheet().getName() + "!" + table.getName() + "!$B$2:$D$2")));
        seriesList.add(new ChartData.SeriesImpl(
                new DynamicValue(table.getSheet().getName() + "!" + table.getName() + "!$A$3"),
                null,
                new DynamicValue(table.getSheet().getName() + "!" + table.getName() + "!$B$3:$D$3")));
        seriesList.add(new ChartData.SeriesImpl(
                new DynamicValue(table.getSheet().getName() + "!" + table.getName() + "!$A$4"),
                null,
                new DynamicValue(table.getSheet().getName() + "!" + table.getName() + "!$B$4:$D$4")));

        ChartData chartSettings = new ChartData("testChart", "Line",
                new DynamicValue("\"Hello Line Chart!\""),
                new DynamicValue(table.getSheet().getName() + "!" + table.getName() + "!$B$1:$D$1"),
                seriesList);

        DefaultChart chart = (DefaultChart) table.getSheet().addAsset(Chart.class, chartSettings);

        // update
        ((ChartData.SeriesImpl) seriesList.get(0)).setyValues(new DynamicValue(table.getSheet().getName() + "!" + table.getName() + "!$B$2:$C$2"));
        ((ChartData.SeriesImpl) seriesList.get(1)).setyValues(new DynamicValue(table.getSheet().getName() + "!" + table.getName() + "!$B$3:$C$3"));
        seriesList.remove(2);
        seriesList.add(new ChartData.SeriesImpl(new DynamicValue(table.getSheet().getName() + "!" + table.getName() + "!$A$4"),
                null,
                new DynamicValue(table.getSheet().getName() + "!" + table.getName() + "!$B$4:$C$4")));
        table.getSheet().updateChart("testChart", new ChartData("testChart", "Line",
                new DynamicValue("\"Hello New Line Chart!\""),
                new DynamicValue(table.getSheet().getName() + "!" + table.getName() + "!$B$1:$C$1"),
                seriesList
        ));

        workbook.refresh();
        System.out.println(chart);

        assertEquals("Line", chart.getType());
        assertEquals("testChart", chart.getName());
        assertEquals("Hello New Line Chart!", chart.getTitle().strValue());
        assertEquals(table.getSheet().getName() + "!" + table.getName() + "!$B$1:$C$1", chart.getCategories().getFormulaString());
        assertEquals(2, chart.getCategories().itemCount());
        assertEquals("Q1", chart.getCategories().item(0).strValue());
        assertEquals("Q2", chart.getCategories().item(1).strValue());

        assertEquals(3, chart.getSeriesCount());

        DefaultDataSeries series = chart.getSeries(0);
        assertEquals(table.getSheet().getName() + "!" + table.getName() + "!$A$2", series.getName().getFormulaString());
        assertEquals("apple", series.getName().strValue());
        assertNull(series.getxValues());
        assertEquals(table.getSheet().getName() + "!" + table.getName() + "!$B$2:$C$2", series.getyValues().getFormulaString());
        assertEquals(2, series.getyValues().itemCount());
        assertEquals(11, series.getyValues().item(0).intValue());
        assertEquals(12, series.getyValues().item(1).intValue());

        series = chart.getSeries(1);
        assertEquals(table.getSheet().getName() + "!" + table.getName() + "!$A$3", series.getName().getFormulaString());
        assertEquals("boy", series.getName().strValue());
        assertNull(series.getxValues());
        assertEquals(table.getSheet().getName() + "!" + table.getName() + "!$B$3:$C$3", series.getyValues().getFormulaString());
        assertEquals(2, series.getyValues().itemCount());
        assertEquals(21, series.getyValues().item(0).intValue());
        assertEquals(22, series.getyValues().item(1).intValue());

        series = chart.getSeries(2);
        assertEquals(table.getSheet().getName() + "!" + table.getName() + "!$A$4", series.getName().getFormulaString());
        assertEquals("cat", series.getName().strValue());
        assertNull(series.getxValues());
        assertEquals(table.getSheet().getName() + "!" + table.getName() + "!$B$4:$C$4", series.getyValues().getFormulaString());
        assertEquals(2, series.getyValues().itemCount());
        assertEquals(31, series.getyValues().item(0).intValue());
        assertEquals(32, series.getyValues().item(1).intValue());
    }

    public void testRemoveChart() {
    }

    protected DefaultTable createTable33() {
        DefaultTable table = (DefaultTable) workbook.addSheet("sheet1").addAsset(Table.class, "table1");
        table.addColumns(0, 3);
        table.addRows(0, 3);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                table.setCellValue(i, j, new Value.DecimalValue((i + 1) * 10 + j + 1));
            }
        }
        assertEquals(3, table.getRowCount());
        assertEquals(3, table.getColumnCount());
        return table;
    }

}
