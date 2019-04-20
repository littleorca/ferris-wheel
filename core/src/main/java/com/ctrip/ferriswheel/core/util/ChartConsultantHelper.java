package com.ctrip.ferriswheel.core.util;

import com.ctrip.ferriswheel.common.table.Table;
import com.ctrip.ferriswheel.common.variant.DynamicValue;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.core.asset.DefaultCell;
import com.ctrip.ferriswheel.core.bean.ChartData;
import com.ctrip.ferriswheel.core.ref.CellReference;
import com.ctrip.ferriswheel.core.ref.RangeReference;
import com.ctrip.ferriswheel.core.view.Rectangle;

import java.util.ArrayList;

public class ChartConsultantHelper {

    public static ChartData getSuggestedChartModel(Table table, int left, int top, int right, int bottom) {
        Rectangle rc = TableScout.getBiggestDecimalRectangleFromBottomRight(table, left, top, right, bottom);
        if (rc == null) {
            // force regard the bottom right corner cell as decimal compatible.
            // this may not make sense, however, MS Excel do this logic.
            rc = new Rectangle(right, bottom, right, bottom);
        }

        ChartData chartData = new ChartData();
        chartData.setTitle(new DynamicValue(Value.str("Chart-" + System.currentTimeMillis())));

        if (rc.getRight() - rc.getLeft() >= rc.getBottom() - rc.getTop()) {
            // horizontal data series
            // get categories
            if (rc.getTop() > top) {
                int row = rc.getTop() - 1;
                chartData.setCategories(new DynamicValue(formula(table, rc.getLeft(), row, rc.getRight(), row)));
            }
            // scan series
            chartData.setSeriesList(new ArrayList<>(rc.getBottom() - rc.getTop() + 1));
            for (int row = rc.getTop(); row <= rc.getBottom(); row++) {
                ChartData.SeriesImpl s = new ChartData.SeriesImpl();
                if (rc.getLeft() - 1 >= left) {
                    s.setName(new DynamicValue(formula(table, row, rc.getLeft() - 1)));
                }
                s.setyValues(new DynamicValue(formula(table, rc.getLeft(), row, rc.getRight(), row)));
                chartData.getSeriesList().add(s);
            }

        } else {
            // vertical data series
            // get categories
            if (rc.getLeft() > left) {
                int col = rc.getLeft() - 1;
                chartData.setCategories(new DynamicValue(formula(table, col, rc.getTop(), col, rc.getBottom())));
            }
            // scan series
            chartData.setSeriesList(new ArrayList(rc.getBottom() - rc.getTop() + 1));
            for (int col = rc.getLeft(); col <= rc.getRight(); col++) {
                ChartData.SeriesImpl s = new ChartData.SeriesImpl();
                if (rc.getTop() - 1 >= top) {
                    s.setName(new DynamicValue(formula(table, rc.getTop() - 1, col)));
                }
                s.setyValues(new DynamicValue(formula(table, col, rc.getTop(), col, rc.getBottom())));
                chartData.getSeriesList().add(s);
            }
        }

        // TODO generate binder

        // refreshChartModel(chartModel, sheet.getWorkbook());

        return chartData;
    }

    static private String formula(Table table, int row, int column) {
        return References.toFormula(new CellReference((DefaultCell) table.getCell(row, column), true, true));
    }

    static private String formula(Table table, int left, int top, int right, int bottom) {
        return References.toFormula(new RangeReference(
                new CellReference((DefaultCell) table.getCell(top, left), true, true),
                new CellReference((DefaultCell) table.getCell(bottom, right), true, true)));
    }


//    private static void refreshChartModel(DefaultChartModel chartModel, DefaultWorkbook workbook) {
//        FormulaEvaluator evaluator = new FormulaEvaluator(workbook);
//        refreshChartProperty(evaluator, chartModel.getTitle());
//        refreshChartProperty(evaluator, chartModel.getCategories());
//        for (int i = 0; i < chartModel.getSeriesCount(); i++) {
//            DefaultDataSeries series = chartModel.getSeries(i);
//            refreshChartProperty(evaluator, series.getName());
//            refreshChartProperty(evaluator, series.getxValues());
//            refreshChartProperty(evaluator, series.getyValues());
//        }
//    }
//
//    private static void refreshChartProperty(FormulaEvaluator evaluator, DefaultChartProperty property) {
//        if (property != null && property.isFormula()) {
//            property.setValue(evaluator.evaluate(property.getFormulaElements()));
//        }
//    }

}
