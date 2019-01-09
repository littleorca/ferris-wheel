package com.ctrip.ferriswheel.core.util;

import com.ctrip.ferriswheel.common.Sheet;
import com.ctrip.ferriswheel.common.SheetAsset;
import com.ctrip.ferriswheel.common.chart.Chart;
import com.ctrip.ferriswheel.common.chart.DataSeries;
import com.ctrip.ferriswheel.common.table.Cell;
import com.ctrip.ferriswheel.common.table.Row;
import com.ctrip.ferriswheel.common.table.Table;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.core.asset.DefaultSheet;
import com.ctrip.ferriswheel.core.asset.DefaultWorkbook;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class HtmlHelper {
    public enum Option {
        FULL_HTML,
        WITH_STYLE
    }

    private Function<Chart, String> chartRenderer;

    public String workbookToHtml(DefaultWorkbook workbook, Option... options) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html>\n<head>\n" +
                "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n" +
                "    <title>")/*.append(escapeHtml(workbook.getName()))*/.append("</title>\n");
        appendTableStyle(html);
        html.append("</head>\n\n<body>\n<div id=\"workbook-container\">\n");

        // title bar
        html.append("<h1>工作簿：")/*.append(workbook.getName())*/.append("</h1>\n");

        // sheet list
        html.append("\n<div id=\"sheet-list\">\n");
        for (Sheet sheet : workbook) {
            html.append("\n<section id=\"sheet-")
                    .append(((DefaultSheet) sheet).getAssetId())
                    .append("\" class=\"sheet-section\">\n<h2><a href=\"#sheet-")
                    .append(((DefaultSheet) sheet).getAssetId())
                    .append("\">")
                    .append(escapeHtml(sheet.getName()))
                    .append("</a></h2>\n<div class=\"sheet-content\">\n");
            sheetToHtml(html, sheet);
            html.append("</div><!-- end of sheet-content -->\n").append("</section>\n\n");
        }

        html.append("\n</div><!-- end of sheet-list -->\n\n</div><!-- end of workbook-container -->\n</body>\n</html>\n");
        return html.toString();
    }

    public String sheetToHtml(Sheet sheet, Option... options) {
        return sheetToHtml(new StringBuilder(), sheet, options).toString();
    }

    public StringBuilder sheetToHtml(StringBuilder html, Sheet sheet, Option... options) {
        for (SheetAsset asset : sheet) {
            if (asset instanceof Table) {
                tableToHtml(html, (Table) asset);
            } else if (asset instanceof Chart) {
                chartToHtml(html, (Chart) asset);
            }
        }
        return html;
    }

    public String tableToHtml(Table table, Option... options) {
        return tableToHtml(new StringBuilder(), table, options).toString();
    }

    public StringBuilder tableToHtml(StringBuilder html, Table table, Option... options) {
        List<Option> optionList = Arrays.asList(options);

        if (optionList.contains(Option.FULL_HTML)) {
            html.append("<!DOCTYPE html>\n<html>\n<head>\n" +
                    "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n" +
                    "    <title>").append(escapeHtml(table.getName())).append("</title>\n");
            appendTableStyle(html);
            html.append("</head>\n\n<body>\n");

        } else if (optionList.contains(Option.WITH_STYLE)) {
            appendTableStyle(html);
        }

        html.append("\n    <table class=\"spreadsheet-table\">\n        <thead>\n            <tr><th></th>");
        for (int colIndex = 0; colIndex < table.getColumnCount(); colIndex++) {
            html.append("<th><span>").append(References.toColumnCode(colIndex)).append("</span></th>");
        }
        html.append("</tr>\n        </thead>\n        <tbody>\n");
        for (int rowIndex = 0; rowIndex < table.getRowCount(); rowIndex++) {
            Row row = table.getRow(rowIndex);
            html.append("            <tr><th><span>").append(References.toRowCode(rowIndex)).append("</span></th>");
            for (int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
                String type = Value.BlankValue.class.getSimpleName();
                String value = "";
                String info = "";
                Cell cell = columnIndex < row.getCellCount() ? row.getCell(columnIndex) : null;
                if (cell != null) {
                    type = cell.getData().valueType().toString();
                    value = escapeHtml(cell.getData().strValue());
                    if (cell.getData().isFormula()) {
                        info = cell.getData().getFormulaString();
                    }
                }
                html.append("<td class=\"")
                        .append(type)
                        .append("\"><span title=\"")
                        .append(info)
                        .append("\">")
                        .append(value)
                        .append("</span></td>");
            }
            html.append("</tr>\n");
        }
        html.append("        </tbody>\n    </table>\n\n");

        if (optionList.contains(Option.FULL_HTML)) {
            html.append("</body>\n</html>\n");
        }

        return html;
    }

    public String chartToHtml(Chart chart) {
        return chartToHtml(new StringBuilder(), chart).toString();
    }

    public StringBuilder chartToHtml(StringBuilder html, Chart chart) {
        if (chartRenderer != null) {
            html.append(chartRenderer.apply(chart));
        } else {
            chartInfoToHtml(html, chart);
        }
        return html;
    }

    private StringBuilder chartInfoToHtml(StringBuilder html, Chart chart) {
        html.append("\n<div id=\"")
                .append(escapeHtml(chart.getName()))
                .append("\" class=\"chart-container chart-")
                .append(escapeHtml(chart.getType()))
                .append("\">\n")
                .append("    <dl>\n        <dt>")
                .append(escapeHtml(chart.getTitle().strValue()))
                .append("</dt>\n")
                .append("        <dd><label>categories: </label> <em>")
                .append(escapeHtml(chart.getCategories().getFormulaString()))
                .append("</em> <span>").append(escapeHtml(chart.getCategories().strValue()))
                .append("</span></dd>\n")
                .append("    <dd>\n        <strong>series:</strong>\n        <dl>\n");

        for (int i = 0; i < chart.getSeriesCount(); i++) {
            DataSeries series = chart.getSeries(i);
            if (series.getName() != null) {
                html.append("            <dt><em>")
                        .append(escapeHtml(series.getName().getFormulaString()))
                        .append("</em> <span>")
                        .append(escapeHtml(series.getName().strValue()))
                        .append("</span></dt>\n");
            } else {
                html.append("            <dt><span>&lt;")
                        .append(i)
                        .append("&gt;<span></span></dt>\n");
            }
            if (series.getxValues() != null) {
                html.append("            <dd><label>xValues: </label> <em>")
                        .append(series.getxValues().getFormulaString())
                        .append("</em> <span>").append(series.getxValues().strValue())
                        .append("</span></dd>\n");
            }
            if (series.getyValues() != null) {
                html.append("            <dd><label>yValues: </label> <em>")
                        .append(series.getyValues().getFormulaString())
                        .append("</em> <span>").append(series.getyValues().strValue())
                        .append("</span></dd>\n");
            }
        }

        return html.append("    </dl>\n</div>\n\n");
    }

    public static String escapeHtml(String s) {
        return s == null ? "" : s.replaceAll("&", "&amp;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;");
    }

    private StringBuilder appendTableStyle(StringBuilder stringBuilder) {
        return stringBuilder.append("    <style>\n" +
                "        .spreadsheet-table{border-collapse:separate;border-spacing:1px;background:#ddd;}\n" +
                "        .spreadsheet-table th, .spreadsheet-table td{padding:.2em .4em;}\n" +
                "        .spreadsheet-table th{background:#eee;}\n" +
                "        .spreadsheet-table tbody tr:nth-child(odd){background:#fff;}\n" +
                "        .spreadsheet-table tbody tr:nth-child(even){background:#f9f9f9;}\n" +
                "        .spreadsheet-table tbody th{text-align:right;}\n" +
                "        .spreadsheet-table .StrValue{text-align:left;}\n" +
                "        .spreadsheet-table .DecimalValue{text-align:right;}\n" +
                "    </style>\n");
    }

    public Function<Chart, String> getChartRenderer() {
        return chartRenderer;
    }

    public void setChartRenderer(Function<Chart, String> chartRenderer) {
        this.chartRenderer = chartRenderer;
    }
}
