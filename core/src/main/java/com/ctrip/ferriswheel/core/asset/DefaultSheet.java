package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.core.action.*;
import com.ctrip.ferriswheel.core.bean.DynamicValue;
import com.ctrip.ferriswheel.core.bean.RowData;
import com.ctrip.ferriswheel.core.view.Layout;
import com.ctrip.ferriswheel.core.view.SheetLayout;
import com.ctrip.ferriswheel.core.action.*;
import com.ctrip.ferriswheel.core.bean.*;
import com.ctrip.ferriswheel.core.intf.*;
import com.ctrip.ferriswheel.core.util.UUIDGen;
import com.ctrip.ferriswheel.core.util.UnmodifiableIterator;
import com.ctrip.ferriswheel.core.view.Layout;
import com.ctrip.ferriswheel.core.view.SheetLayout;

import java.util.Iterator;
import java.util.concurrent.Callable;

class DefaultSheet extends NamedAssetNode implements Sheet {
    private final NamedAssetList<NamedAssetNode> assets;
    private final SheetLayout layout = new SheetLayout();
    private ActionNotifier notifier;

    DefaultSheet(String name, AssetManager assetManager, ActionNotifier notifier) {
        super(name, assetManager);
        this.assets = new NamedAssetList<>(this);
        this.notifier = notifier;
    }

    @Override
    public DefaultWorkbook getWorkbook() {
        return (DefaultWorkbook) getParentAsset();
    }

    @Override
    public int getAssetCount() {
        return assets.size();
    }

    @Override
    public NamedAssetNode getAsset(int index) {
        return assets.get(index);
    }

    @Override
    public NamedAssetNode getAsset(String name) {
        return assets.get(name);
    }

    @Override
    public NamedAssetNode removeAsset(int index) {
        return doRemoveAsset(getAsset(index));
    }

    @Override
    public NamedAssetNode removeAsset(String name) {
        return doRemoveAsset(getAsset(name));
    }

    private NamedAssetNode doRemoveAsset(NamedAsset asset) {
        if (asset == null) {
            return null;
        }
        return handleAction(new RemoveAsset(getName(), asset.getName()));
    }

    NamedAssetNode handleAction(RemoveAsset removeAsset) {
        NamedAssetNode asset = getAsset(removeAsset.getAssetName());
        if (asset == null) {
            return null;
        }
        return publicly(removeAsset, () -> {
            assets.remove(asset.getName());
            if (asset instanceof DefaultTable) {
                DefaultTable table = (DefaultTable) asset;
                getWorkbook().onTableRemoved(table);
            }
            return asset;
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
        publicly(renameAsset, () -> {
            if (!assets.rename(renameAsset.getOldAssetName(), renameAsset.getNewAssetName())) {
                throw new RuntimeException("Failed to rename asset.");
            }
            // if renamed asset is  a table, there could be formulas which need update.
            if (asset instanceof DefaultTable) {
                DefaultTable table = (DefaultTable) asset;
                getWorkbook().updateRangeReferences(table,
                        0,
                        0,
                        null, // null means unlimited
                        null,
                        null,
                        null,
                        null,
                        null);
            }
        });
        // technically rename a table doesn't affect any cell value or chart property value,
        // it just affect formulas.
        //refreshIfNeeded();
    }

    @Override
    public DefaultTable getTable(String name) {
        return (DefaultTable) getAsset(name);
    }

    @Override
    public DefaultTable addTable(String name) {
        return addTable(name, new TableData());
    }

    @Override
    public DefaultTable addTable(String name, TableData tableData) {
        return handleAction(new AddTable(getName(), name, tableData));
    }

    DefaultTable handleAction(AddTable addTable) {
        if (addTable.getTableName() == null || addTable.getTableName().isEmpty()) {
            addTable.setTableName(UUIDGen.generate().toString());
        }
        if (getAsset(addTable.getTableName()) != null) {
            throw new IllegalArgumentException("Duplicated name: " + addTable.getTableName());
        }
        return publicly(addTable, () -> {
            DefaultTable table = createTable(addTable.getTableName());
            assets.add(table);

            notifier.privately(() -> {
                TableData td = addTable.getTableData();
                if (td.getRows() != null) {
                    for (RowData rd : td.getRows().values()) {
                        if (rd.getCells() != null) {
                            for (CellData cd : rd.getCells().values()) {
                                if (cd.getValue().isFormula()) {
                                    table.setCellFormula(rd.getIndex(), cd.getIndex(), cd.getValue().getFormulaString());
                                } else {
                                    table.setCellValue(rd.getIndex(), cd.getIndex(), cd.getValue());
                                }
                            }
                        }
                    }
                }

                if (td.getAutomatonInfo() != null) {
                    table.automate(td.getAutomatonInfo());
                }

                // TODO use user specified layout?
            });

            layoutNewAsset(table);
            if (addTable.getTableData() == null) {
                addTable.setTableData(new TableData());
            }
            addTable.getTableData().setLayout(new Layout(table.getLayout()));
            return table;
        });
    }

    DefaultTable createTable(String name) {
        return new DefaultTable(name, getAssetManager());
    }

    @Override
    public DefaultChart getChart(String name) {
        return (DefaultChart) assets.get(name);
    }

    @Override
    public DefaultChart addChart(String name, ChartData chartData) {
        return handleAction(new AddChart(getName(), name, chartData));
    }

    DefaultChart handleAction(AddChart addChart) {
        if (addChart.getChartName() == null || addChart.getChartName().isEmpty()) {
            addChart.setChartName(UUIDGen.generate().toString());
        }
        if (getAsset(addChart.getChartName()) != null) {
            throw new IllegalArgumentException("Duplicated name: " + addChart.getChartName());
        }
        return publicly(addChart, () -> {
            ChartData chartData = addChart.getChartData();
            DefaultChart chart = new DefaultChart(addChart.getChartName(), chartData.getType(), this);
            assets.add(chart);
            // now fill data
            layoutNewAsset(chart);
            updateChart(chart, chartData);
            getWorkbook().onChartCreated(chart);
            return chart;
        });
    }

    @Override
    public DefaultChart updateChart(String name, ChartData chartData) {
        return handleAction(new UpdateChart(getName(), name, chartData));
    }

    DefaultChart handleAction(UpdateChart updateChart) {
        DefaultChart chart = getChart(updateChart.getChartName());
        ChartData chartData = updateChart.getChartData();
        if (chart == null || chartData == null) {
            throw new IllegalArgumentException();
        }
        return publicly(updateChart, () -> {
            updateChart(chart, chartData);
            getWorkbook().onChartUpdated(chart);
            return chart;
        });
    }

    private void updateChart(DefaultChart chart, ChartData chartData) {
        chart.setType(chartData.getType());
        chart.setTitle(chartData.getTitle());

        if (chartData.getLayout() != null) {
            chart.getLayout().copy(chartData.getLayout());
        }

        if (isValidBinder(chartData.getBinder())) {
            ChartData.Binder inputBinder = chartData.getBinder();
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
                for (ChartData.Series series : chartData.getSeriesList()) {
                    chart.addSeries(series.getName(), series.getxValues(), series.getyValues());
                }
            }
        }

        if (chartData.getxAxis() != null) {
            chart.setxAxis(new Axis(chartData.getxAxis()));
        }
        if (chartData.getyAxis() != null) {
            chart.setyAxis(new Axis(chartData.getyAxis()));
        }
        if (chartData.getzAxis() != null) {
            chart.setzAxis(new Axis(chartData.getzAxis()));
        }
    }

    private boolean isValidBinder(ChartData.Binder binder) {
        if (binder == null || binder.getData() == null) {
            return false;
        }
        return binder.getData().isFormula();
    }

    @Override
    public DefaultText getText(String name) {
        return (DefaultText) getAsset(name);
    }

    @Override
    public DefaultText addText(String name, TextData textData) {
        return handleAction(new AddText(getName(), name, textData));
    }

    DefaultText handleAction(AddText addText) {
        if (addText.getTextName() == null || addText.getTextName().isEmpty()) {
            addText.setTextName(UUIDGen.generate().toString());
        }
        if (getAsset(addText.getTextName()) != null) {
            throw new IllegalArgumentException("Duplicated name: " + addText.getTextName());
        }
        return publicly(addText, () -> {
            DefaultText text = createText(addText.getTextName());
            assets.add(text);

            fillTextData(text, addText.getTextData());
            if (addText.getTextData().getLayout() == null) {
                layoutNewAsset(text);
                addText.getTextData().setLayout(new Layout(text.getLayout()));
            }
            getWorkbook().onTextCreated(text);
            return text;
        });
    }

    DefaultText createText(String name) {
        return new DefaultText(name, this);
    }

    @Override
    public DefaultText updateText(String name, TextData textData) {
        return handleAction(new UpdateText(getName(), name, textData));
    }

    DefaultText handleAction(UpdateText updateText) {
        DefaultText text = getText(updateText.getTextName());
        TextData textData = updateText.getTextData();
        if (text == null || textData == null) {
            throw new IllegalArgumentException();
        }
        return publicly(updateText, () -> {
            fillTextData(text, textData);
            if (updateText.getTextData().getLayout() == null) {
                updateText.getTextData().setLayout(new Layout(text.getLayout()));
            }
            getWorkbook().onTextUpdated(text);
            return text;
        });
    }

    void fillTextData(DefaultText text, TextData data) {
        DynamicValue dv = data.getContent();
        if (dv.isFormula()) {
            text.getContent().setFormula(dv.getFormula());
        } else {
            text.getContent().setFormula(null);
            text.getContent().setValue(dv.getValue());
        }
        if (data.getLayout() != null) {
            text.getLayout().copy(data.getLayout());
        }
    }

    private void layoutNewAsset(NamedAsset namedAsset) {
        // just put the new asset to the bottom
        int rowEnd = 0;
        for (Asset asset : this) {
            if (asset instanceof Displayable) {
                Layout.Grid grid = ((Displayable) asset).getLayout().getGrid();
                if (grid == null) {
                    continue;
                }
                if (rowEnd < grid.getRow().getEnd()) {
                    rowEnd = grid.getRow().getEnd();
                }
            }
        }

        Layout l = new Layout();
        l.setDisplay(Display.BLOCK);
        // let's put new asset to the bottom.
        l.setGrid(new Layout.Grid(0, 0,
                new Layout.Span(1, 7),
                new Layout.Span(rowEnd, rowEnd + 4)));
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
            displayable.getLayout().copy(layoutAsset.getLayout());
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
        for (NamedAsset asset : this) {
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
    public Layout getLayout() {
        return layout;
    }

    @Override
    public Iterator<NamedAsset> iterator() {
        return new UnmodifiableIterator<>(assets.iterator());
    }
}
