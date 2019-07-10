package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.common.Sheet;
import com.ctrip.ferriswheel.common.SheetAsset;
import com.ctrip.ferriswheel.common.action.Action;
import com.ctrip.ferriswheel.common.action.ActionNotifier;
import com.ctrip.ferriswheel.common.chart.Chart;
import com.ctrip.ferriswheel.common.chart.ChartBinder;
import com.ctrip.ferriswheel.common.chart.DataSeries;
import com.ctrip.ferriswheel.common.form.Form;
import com.ctrip.ferriswheel.common.form.FormField;
import com.ctrip.ferriswheel.common.table.Cell;
import com.ctrip.ferriswheel.common.table.Row;
import com.ctrip.ferriswheel.common.table.Table;
import com.ctrip.ferriswheel.common.text.Text;
import com.ctrip.ferriswheel.common.variant.DynamicValue;
import com.ctrip.ferriswheel.common.view.Display;
import com.ctrip.ferriswheel.common.view.Displayable;
import com.ctrip.ferriswheel.common.view.Grid;
import com.ctrip.ferriswheel.common.view.Layout;
import com.ctrip.ferriswheel.core.action.*;
import com.ctrip.ferriswheel.core.bean.*;
import com.ctrip.ferriswheel.core.formula.Formula;
import com.ctrip.ferriswheel.core.util.UUIDGen;
import com.ctrip.ferriswheel.core.util.UnmodifiableIterator;
import com.ctrip.ferriswheel.core.view.LayoutImpl;
import com.ctrip.ferriswheel.core.view.SheetLayout;

import java.util.Iterator;
import java.util.concurrent.Callable;

public class DefaultSheet extends NamedAssetNode implements Sheet {
    private final NamedAssetList<SheetAssetNode> assets;
    private final SheetLayout layout = new SheetLayout();
    private ActionNotifier notifier;

    DefaultSheet(String name, AssetManager assetManager, ActionNotifier notifier) {
        super(name, assetManager);
        this.assets = new NamedAssetList<>(this);
        this.notifier = notifier;
    }

    @Override
    public DefaultWorkbook getWorkbook() {
        return (DefaultWorkbook) getParent();
    }

    @Override
    public int getAssetCount() {
        return assets.size();
    }

    @Override
    public <T extends SheetAsset> T addAsset(Class<T> clazz, String name) {
        if (Table.class.isAssignableFrom(clazz)) {
            TableData table = new TableData();
            table.setName(name);
            return (T) addTable(table);
        } else if (Chart.class.isAssignableFrom(clazz)) {
            ChartData chart = new ChartData();
            chart.setName(name);
            return (T) addChart(chart);
        } else if (Text.class.isAssignableFrom(clazz)) {
            TextData text = new TextData();
            text.setName(name);
            return (T) addText(text);
        } else if (Form.class.isAssignableFrom(clazz)) {
            FormData form = new FormData();
            form.setName(name);
            return (T) addForm(form);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public <T extends SheetAsset> T addAsset(Class<T> clazz, T asset) {
        if (!clazz.isInterface()) {
            throw new IllegalArgumentException();
        }
        if (Table.class.equals(clazz)) {
            return (T) addTable((Table) asset);
        } else if (Chart.class.equals(clazz)) {
            return (T) addChart((Chart) asset);
        } else if (Text.class.equals(clazz)) {
            return (T) addText((Text) asset);
        } else if (Form.class.equals(clazz)) {
            return (T) addForm((Form) asset);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public <T extends SheetAsset> T getAsset(int index) {
        return (T) assets.get(index);
    }

    @Override
    public <T extends SheetAsset> T getAsset(String name) {
        return (T) assets.get(name);
    }

    @Override
    public <T extends SheetAsset> T removeAsset(int index) {
        return doRemoveAsset(getAsset(index));
    }

    @Override
    public <T extends SheetAsset> T removeAsset(String name) {
        return doRemoveAsset(getAsset(name));
    }

    private <T extends SheetAsset> T doRemoveAsset(NamedAsset asset) {
        if (asset == null) {
            return null;
        }
        return handleAction(new RemoveAsset(getName(), asset.getName()));
    }

    <T extends SheetAsset> T handleAction(RemoveAsset removeAsset) {
        SheetAssetNode asset = getAsset(removeAsset.getAssetName());
        if (asset == null) {
            return null;
        }
        return publicly(removeAsset, () -> {
            assets.remove(asset.getName());
            if (asset instanceof DefaultTable) {
                DefaultTable table = (DefaultTable) asset;
                getWorkbook().onTableRemoved(table);
            }
            return (T) asset;
        });
    }

    @Override
    public void renameAsset(String oldName, String newName) {
        handleAction(new RenameAsset(getName(), oldName, newName));
    }

    void handleAction(RenameAsset renameAsset) {
        NamedAssetNode asset = getAsset(renameAsset.getOldAssetName());
        if (asset == null) {
            throw new IllegalArgumentException("Invalid asset name: " + renameAsset.getOldAssetName());
        }
        if (renameAsset.getNewAssetName().equals(asset.getName())) {
            return; // nothing changed
        }
        if (getAsset(renameAsset.getNewAssetName()) != null) {
            throw new IllegalArgumentException("Duplicated asset name: " + renameAsset.getNewAssetName());
        }
        getWorkbook().withoutRefresh(() ->
                publicly(renameAsset, () -> {
                    if (!assets.rename(renameAsset.getOldAssetName(), renameAsset.getNewAssetName())) {
                        throw new RuntimeException("Failed to rename asset.");
                    }
                    // if renamed asset is  a table, there could be formulas which need update.
                    if (asset instanceof DefaultTable) {
                        DefaultTable table = (DefaultTable) asset;
                        getWorkbook().onTableUpdate(table);
                    }
                }));
        // technically rename a table doesn't affect any cell value or chart property value,
        // it just affect formulas.
        getWorkbook().refreshIfNeeded();
    }

    protected DefaultTable addTable(Table tableData) {
        return handleAction(new AddTable(getName(), tableData));
    }

    DefaultTable handleAction(AddTable addTable) {
        String tableName = addTable.getTableData().getName();
        if (tableName == null || tableName.isEmpty()) {
            tableName = UUIDGen.generate().toString();
        }
        if (getAsset(tableName) != null) {
            throw new IllegalArgumentException("Duplicated name: " + tableName);
        }
        String finalTableName = tableName;
        return publicly(addTable, () -> {
            DefaultTable table = createTable(finalTableName);
            assets.add(table);

            notifier.privately(() -> {
                Table td = addTable.getTableData();
                if (td.getColumnCount() > 0) {
                    table.addColumns(td.getColumnCount());
                }
                if (td.getRowCount() > 0) {
                    table.addRows(td.getRowCount());
                }
                for (int rowIndex = 0; rowIndex < td.getRowCount(); rowIndex++) {
                    Row row = td.getRow(rowIndex);
                    // TODO skip empty rows/cells.
                    for (int colIndex = 0; colIndex < row.getCellCount(); colIndex++) {
                        Cell cd = row.getCell(colIndex);
                        if (cd.getData().isFormula()) {
                            table.setCellFormula(rowIndex, colIndex, cd.getData().getFormulaString());
                        } else {
                            table.setCellValue(rowIndex, colIndex, cd.getData());
                        }
                    }
                }

                if (td.getAutomateConfiguration() != null) {
                    table.automate(td.getAutomateConfiguration());
                }

                // TODO use user specified layout?
            });

            layoutNewAsset(table);
            addTable.setTableData(table);
            return table;
        });
    }

    DefaultTable createTable(String name) {
        return new DefaultTable(name, this);
    }

    protected DefaultChart addChart(Chart chart) {
        return handleAction(new AddChart(getName(), chart));
    }

    DefaultChart handleAction(AddChart addChart) {
        String chartName = addChart.getChartData().getName();
        if (chartName == null || chartName.isEmpty()) {
            chartName = UUIDGen.generate().toString();
        }
        if (getAsset(chartName) != null) {
            throw new IllegalArgumentException("Duplicated name: " + chartName);
        }
        final String finalChartName = chartName;
        return publicly(addChart, () -> {
            Chart chartData = addChart.getChartData();
            DefaultChart chart = new DefaultChart(finalChartName, chartData.getType(), this);
            assets.add(chart);
            // now fill data
            layoutNewAsset(chart);
            updateChart(chart, chartData);
            getWorkbook().onChartCreated(chart);
            addChart.setChartData(chart);
            return chart;
        });
    }

    @Override
    @Deprecated // move to chart itself?
    public DefaultChart updateChart(String name, Chart chartData) {
        return handleAction(new UpdateChart(getName(), name, chartData));
    }

    DefaultChart handleAction(UpdateChart updateChart) {
        DefaultChart chart = getAsset(updateChart.getChartName());
        Chart chartData = updateChart.getChartData();
        if (chart == null || chartData == null) {
            throw new IllegalArgumentException();
        }
        return publicly(updateChart, () -> {
            updateChart(chart, chartData);
            getWorkbook().onChartUpdated(chart);
            return chart;
        });
    }

    private void updateChart(DefaultChart chart, Chart chartData) {
        chart.setType(chartData.getType());
        chart.setTitle(chartData.getTitle());

        if (chartData.getLayout() != null) {
            chart.getLayout().copy(chartData.getLayout());
        }

        if (isValidBinder(chartData.getBinder())) {
            ChartBinder inputBinder = chartData.getBinder();
            DefaultChartBinder internalBinder = new DefaultChartBinder(getAssetManager());
            internalBinder.setData(inputBinder.getData());
            internalBinder.setOrientation(inputBinder.getOrientation());
            internalBinder.setCategoriesPlacement(inputBinder.getCategoriesPlacement());
            internalBinder.setSeriesNamePlacement(inputBinder.getSeriesNamePlacement());
            chart.setBinder(internalBinder);
            //chart.rebindIfPossible();

        } else {
            if (chart.getBinder() != null) {
                chart.setBinder(null);
            }

            chart.setCategories(chartData.getCategories());
            chart.clearSeries();
            if (chartData.getSeriesList() != null) {
                for (DataSeries series : chartData.getSeriesList()) {
                    chart.addSeries(series.getName(), series.getxValues(), series.getyValues());
                }
            }
        }

        if (chartData.getxAxis() != null) {
            chart.setxAxis(new AxisImpl(chartData.getxAxis()));
        }
        if (chartData.getyAxis() != null) {
            chart.setyAxis(new AxisImpl(chartData.getyAxis()));
        }
        if (chartData.getzAxis() != null) {
            chart.setzAxis(new AxisImpl(chartData.getzAxis()));
        }
    }

    private boolean isValidBinder(ChartBinder binder) {
        if (binder == null || binder.getData() == null) {
            return false;
        }
        return binder.getData().isFormula();
    }

    protected DefaultText addText(Text text) {
        return handleAction(new AddText(getName(), text));
    }

    DefaultText handleAction(AddText addText) {
        String textName = addText.getTextData().getName();
        if (textName == null || textName.isEmpty()) {
            textName = UUIDGen.generate().toString();
        }
        if (getAsset(textName) != null) {
            throw new IllegalArgumentException("Duplicated name: " + textName);
        }
        final String finalTextName = textName;
        return publicly(addText, () -> {
            DefaultText text = createText(finalTextName);
            assets.add(text);

            fillTextData(text, addText.getTextData());
            if (addText.getTextData().getLayout() == null) {
                layoutNewAsset(text);
            }
            getWorkbook().onAssetUpdate(text);
            addText.setTextData(text);
            return text;
        });
    }

    DefaultText createText(String name) {
        return new DefaultText(name, this);
    }

    @Override
    @Deprecated
    public DefaultText updateText(String name, Text textData) {
        return handleAction(new UpdateText(getName(), name, textData));
    }

    DefaultText handleAction(UpdateText updateText) {
        DefaultText text = getAsset(updateText.getTextName());
        Text textData = updateText.getTextData();
        if (text == null || textData == null) {
            throw new IllegalArgumentException();
        }
        return publicly(updateText, () -> {
            fillTextData(text, textData);
            if (updateText.getTextData().getLayout() == null) {
                ((TextData) updateText.getTextData()).setLayout(new LayoutImpl(text.getLayout()));
            }
            getWorkbook().onAssetUpdate(text);
            return text;
        });
    }

    void fillTextData(DefaultText text, Text data) {
        DynamicValue dv = (DynamicValue) data.getContent();
        if (dv.isFormula()) {
            text.getContent().setFormula(new Formula(dv.getFormulaString()));
        } else {
            text.getContent().setFormula(null);
            text.getContent().setValue(dv.getVariant());
        }
        if (data.getLayout() != null) {
            text.getLayout().copy(data.getLayout());
        }
    }

    protected DefaultForm addForm(Form form) {
        return handleAction(new AddForm(getName(), form));
    }

    DefaultForm handleAction(AddForm addForm) {
        String formName = addForm.getFormData().getName();
        if (formName == null || formName.isEmpty()) {
            formName = UUIDGen.generate().toString();
        }
        if (getAsset(formName) != null) {
            throw new IllegalArgumentException("Duplicated name: " + formName);
        }
        final String finalFormName = formName;
        return publicly(addForm, () -> {
            DefaultForm form = createForm(finalFormName);
            assets.add(form);

            fillFormData(form, addForm.getFormData());
            if (addForm.getFormData().getLayout() == null) {
                layoutNewAsset(form);
            }

            getWorkbook().onAssetUpdate(form);
            addForm.setFormData(form);
            return form;
        });
    }

    DefaultForm createForm(String name) {
        return new DefaultForm(name, this);
    }

    public DefaultForm updateForm(Form form) {
        return handleAction(new UpdateForm(getName(), form.getName(), form));
    }

    DefaultForm handleAction(UpdateForm updateForm) {
        if (getAsset(updateForm.getFormName()) == null) {
            throw new IllegalArgumentException("There is no form with name: " + updateForm.getFormName());
        }
        return publicly(updateForm, () -> {
            DefaultForm form = getAsset(updateForm.getFormName());
            fillFormData(form, updateForm.getFormData());
            getWorkbook().onAssetUpdate(form);
            updateForm.setFormData(form);
            return form;
        });
    }

    void fillFormData(DefaultForm form, Form data) {
        form.clearFields();
        for (FormField field : data) {
            DefaultFormField defaultField = new DefaultFormField(field.getName(), form);
            defaultField.fillFieldData(field);
            form.addField(defaultField);
        }
        if (data.getLayout() != null) {
            form.getLayout().copy(data.getLayout());
        }
    }

    private void layoutNewAsset(NamedAsset namedAsset) {
        // just put the new asset to the bottom
        int rowEnd = 0;
        for (SheetAsset asset : this) {
            if (asset instanceof Displayable) {
                Grid grid = ((Displayable) asset).getLayout().getGrid();
                if (grid == null) {
                    continue;
                }
                if (rowEnd < grid.getRow().getEnd()) {
                    rowEnd = grid.getRow().getEnd();
                }
            }
        }

        LayoutImpl l = new LayoutImpl();
        l.setDisplay(Display.BLOCK);
        // let's put new asset to the bottom.
        l.setGrid(new LayoutImpl.GridImpl(0, 0,
                new LayoutImpl.SpanImpl(1, 7),
                new LayoutImpl.SpanImpl(rowEnd, rowEnd + 4)));
        handleAction(new LayoutAsset(getName(), namedAsset.getName(), l));
    }

    @Override
    public void layoutAsset(String assetName, Layout layout) {
        handleAction(new LayoutAsset(getName(), assetName, layout));
    }

    void handleAction(LayoutAsset layoutAsset) {
        NamedAsset asset = getAsset(layoutAsset.getAssetName());
        if (asset == null) {
            throw new IllegalArgumentException("Asset not found, name=" + layoutAsset.getAssetName());
        } else if (!(asset instanceof Displayable)) {
            throw new IllegalArgumentException("Asset \"" + asset.getName() + "\" no displayable.");
        }
        Displayable displayable = (Displayable) asset;
        publicly(layoutAsset, () -> {
            ((LayoutImpl) displayable.getLayout()).copy(layoutAsset.getLayout());
        });
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        toString(sb);
        return sb.toString();
    }

    public void toString(StringBuilder sb) {
        sb.append("Sheet: ").append(getName()).append(", assets=").append(getAssetCount()).append("\n");
        for (SheetAsset asset : this) {
            if (asset instanceof DefaultTable) {
                ((DefaultTable) asset).toString(sb);
            } else if (asset instanceof DefaultChart) {
                ((DefaultChart) asset).toString(sb);
            }
        }
    }

    <V> V publicly(Action action, Callable<V> callable) {
        if (notifier != null) {
            return notifier.publicly(action, callable);
        } else {
            try {
                return callable.call();
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    void publicly(Action action, Runnable runnable) {
        if (notifier != null) {
            notifier.publicly(action, runnable);
        } else {
            try {
                runnable.run();
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    ActionNotifier getNotifier() {
        return notifier;
    }

    @Override
    public LayoutImpl getLayout() {
        return layout;
    }

    @Override
    public Iterator<SheetAsset> iterator() {
        return new UnmodifiableIterator<>(assets.iterator());
    }
}
