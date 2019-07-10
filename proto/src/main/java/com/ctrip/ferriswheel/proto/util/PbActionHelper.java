package com.ctrip.ferriswheel.proto.util;

import com.ctrip.ferriswheel.common.action.Action;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.core.action.*;
import com.ctrip.ferriswheel.core.bean.TableAutomatonInfo;

import java.util.LinkedHashMap;
import java.util.Map;

public class PbActionHelper {

    public static AddChart bean(com.ctrip.ferriswheel.proto.v1.AddChart proto) {
        if (proto == null) {
            return null;
        }
        AddChart bean = new AddChart();
        bean.setSheetName(proto.getSheetName());
        bean.setChartData(PbHelper.bean(proto.getChart()));
        return bean;
    }

    public static com.ctrip.ferriswheel.proto.v1.AddChart pb(AddChart bean) {
        return com.ctrip.ferriswheel.proto.v1.AddChart.newBuilder()
                .setSheetName(bean.getSheetName())
                .setChart(PbHelper.pb(bean.getChartData().getName(), bean.getChartData()))
                .build();
    }

    public static AddSheet bean(com.ctrip.ferriswheel.proto.v1.AddSheet proto) {
        return new AddSheet(proto.getSheetName(), proto.getIndex());
    }

    public static com.ctrip.ferriswheel.proto.v1.AddSheet pb(AddSheet bean) {
        return com.ctrip.ferriswheel.proto.v1.AddSheet.newBuilder()
                .setSheetName(bean.getSheetName())
                .setIndex(bean.getIndex())
                .build();
    }

    public static AddTable bean(com.ctrip.ferriswheel.proto.v1.AddTable proto) {
        // here ignores some of proto's table data such as layout.
        return new AddTable(proto.getSheetName(), PbHelper.bean(proto.getTable()));
    }

    public static com.ctrip.ferriswheel.proto.v1.AddTable pb(AddTable bean) {
        com.ctrip.ferriswheel.proto.v1.AddTable.Builder builder = com.ctrip.ferriswheel.proto.v1.AddTable.newBuilder()
                .setSheetName(bean.getSheetName());
        if (bean.getTableData() != null) {
            builder.setTable(PbHelper.pb(bean.getTableData()).getTable());
        }
        return builder.build();
    }

    public static AutomateTable bean(com.ctrip.ferriswheel.proto.v1.AutomateTable proto) {
        TableAutomatonInfo solution;
        switch (proto.getAutomaton().getAutomatonCase()) {
            case QUERY_AUTOMATON:
                solution = PbHelper.bean(proto.getAutomaton().getQueryAutomaton());
                break;
            case PIVOT_AUTOMATON:
                solution = PbHelper.bean(proto.getAutomaton().getPivotAutomaton());
                break;
            case AUTOMATON_NOT_SET:
            default:
                throw new IllegalArgumentException();
        }
        return new AutomateTable(proto.getSheetName(), proto.getTableName(), solution);
    }

    public static com.ctrip.ferriswheel.proto.v1.AutomateTable pb(AutomateTable bean) {
        com.ctrip.ferriswheel.proto.v1.TableAutomaton.Builder builder = com.ctrip.ferriswheel.proto.v1.TableAutomaton.newBuilder();
        if (bean.getSolution() instanceof TableAutomatonInfo.QueryAutomatonInfo) {
            builder.setQueryAutomaton(PbHelper.pb((TableAutomatonInfo.QueryAutomatonInfo) bean.getSolution()));
        } else if (bean.getSolution() instanceof TableAutomatonInfo.PivotAutomatonInfo) {
            builder.setPivotAutomaton(PbHelper.pb((TableAutomatonInfo.PivotAutomatonInfo) bean.getSolution()));
        }
        return com.ctrip.ferriswheel.proto.v1.AutomateTable.newBuilder()
                .setSheetName(bean.getSheetName())
                .setTableName(bean.getTableName())
                .setAutomaton(builder)
                .build();
    }

    public static CellAction.SetCellValue bean(com.ctrip.ferriswheel.proto.v1.SetCellValue proto) {
        return new CellAction.SetCellValue(proto.getSheetName(),
                proto.getTableName(),
                proto.getRowIndex(),
                proto.getColumnIndex(),
                PbHelper.toValue(proto.getValue()));
    }

    public static com.ctrip.ferriswheel.proto.v1.SetCellValue pb(CellAction.SetCellValue bean) {
        return com.ctrip.ferriswheel.proto.v1.SetCellValue.newBuilder()
                .setSheetName(bean.getSheetName())
                .setTableName(bean.getTableName())
                .setRowIndex(bean.getRowIndex())
                .setColumnIndex(bean.getColumnIndex())
                .setValue(PbHelper.pb(bean.getValue()))
                .build();
    }

    public static CellAction.SetCellFormula bean(com.ctrip.ferriswheel.proto.v1.SetCellFormula proto) {
        return new CellAction.SetCellFormula(proto.getSheetName(),
                proto.getTableName(),
                proto.getRowIndex(),
                proto.getColumnIndex(),
                proto.getFormulaString());
    }

    public static com.ctrip.ferriswheel.proto.v1.SetCellFormula pb(CellAction.SetCellFormula bean) {
        return com.ctrip.ferriswheel.proto.v1.SetCellFormula.newBuilder()
                .setSheetName(bean.getSheetName())
                .setTableName(bean.getTableName())
                .setRowIndex(bean.getRowIndex())
                .setColumnIndex(bean.getColumnIndex())
                .setFormulaString(bean.getFormulaString())
                .build();
    }

    public static CellAction.RefreshCellValue bean(com.ctrip.ferriswheel.proto.v1.RefreshCellValue proto) {
        return new CellAction.RefreshCellValue(proto.getSheetName(),
                proto.getTableName(),
                proto.getRowIndex(),
                proto.getColumnIndex(),
                PbHelper.toValue(proto.getValue()));
    }

    public static com.ctrip.ferriswheel.proto.v1.RefreshCellValue pb(CellAction.RefreshCellValue bean) {
        return com.ctrip.ferriswheel.proto.v1.RefreshCellValue.newBuilder()
                .setSheetName(bean.getSheetName())
                .setTableName(bean.getTableName())
                .setRowIndex(bean.getRowIndex())
                .setColumnIndex(bean.getColumnIndex())
                .setValue(PbHelper.pb(bean.getValue()))
                .build();
    }

    public static ChartConsult bean(com.ctrip.ferriswheel.proto.v1.ChartConsult proto) {
        return new ChartConsult(proto.getSheetName(),
                proto.getTableName(),
                proto.getType(),
                proto.getLeft(),
                proto.getTop(),
                proto.getRight(),
                proto.getBottom());
    }

    public static com.ctrip.ferriswheel.proto.v1.ChartConsult pb(ChartConsult bean) {
        return com.ctrip.ferriswheel.proto.v1.ChartConsult.newBuilder()
                .setSheetName(bean.getSheetName())
                .setTableName(bean.getTableName())
                .setType(bean.getType())
                .setLeft(bean.getLeft())
                .setTop(bean.getTop())
                .setRight(bean.getRight())
                .setBottom(bean.getBottom())
                .build();
    }

    public static EraseCells bean(com.ctrip.ferriswheel.proto.v1.EraseCells proto) {
        return new EraseCells(proto.getSheetName(),
                proto.getTableName(),
                proto.getTop(),
                proto.getRight(),
                proto.getBottom(),
                proto.getLeft());
    }

    public static com.ctrip.ferriswheel.proto.v1.EraseCells pb(EraseCells bean) {
        return com.ctrip.ferriswheel.proto.v1.EraseCells.newBuilder()
                .setSheetName(bean.getSheetName())
                .setTableName(bean.getTableName())
                .setTop(bean.getTop())
                .setRight(bean.getRight())
                .setBottom(bean.getBottom())
                .setLeft(bean.getLeft())
                .build();
    }

    public static FillCells.FillUp bean(com.ctrip.ferriswheel.proto.v1.FillUp proto) {
        return new FillCells.FillUp(proto.getSheetName(),
                proto.getTableName(),
                proto.getRowIndex(),
                proto.getFirstColumn(),
                proto.getLastColumn(),
                proto.getNRows());
    }

    public static com.ctrip.ferriswheel.proto.v1.FillUp pb(FillCells.FillUp bean) {
        return com.ctrip.ferriswheel.proto.v1.FillUp.newBuilder()
                .setSheetName(bean.getSheetName())
                .setTableName(bean.getTableName())
                .setRowIndex(bean.getRowIndex())
                .setFirstColumn(bean.getFirstColumn())
                .setLastColumn(bean.getLastColumn())
                .setNRows(bean.getnRows())
                .build();
    }

    public static FillCells.FillRight bean(com.ctrip.ferriswheel.proto.v1.FillRight proto) {
        return new FillCells.FillRight(proto.getSheetName(),
                proto.getTableName(),
                proto.getColumnIndex(),
                proto.getFirstRow(),
                proto.getLastRow(),
                proto.getNColumns());
    }

    public static com.ctrip.ferriswheel.proto.v1.FillRight pb(FillCells.FillRight bean) {
        return com.ctrip.ferriswheel.proto.v1.FillRight.newBuilder()
                .setSheetName(bean.getSheetName())
                .setTableName(bean.getTableName())
                .setColumnIndex(bean.getColumnIndex())
                .setFirstRow(bean.getFirstRow())
                .setLastRow(bean.getLastRow())
                .setNColumns(bean.getnColumns())
                .build();
    }

    public static FillCells.FillDown bean(com.ctrip.ferriswheel.proto.v1.FillDown proto) {
        return new FillCells.FillDown(proto.getSheetName(),
                proto.getTableName(),
                proto.getRowIndex(),
                proto.getFirstColumn(),
                proto.getLastColumn(),
                proto.getNRows());
    }

    public static com.ctrip.ferriswheel.proto.v1.FillDown pb(FillCells.FillDown bean) {
        return com.ctrip.ferriswheel.proto.v1.FillDown.newBuilder()
                .setSheetName(bean.getSheetName())
                .setTableName(bean.getTableName())
                .setRowIndex(bean.getRowIndex())
                .setFirstColumn(bean.getFirstColumn())
                .setLastColumn(bean.getLastColumn())
                .setNRows(bean.getnRows())
                .build();
    }

    public static FillCells.FillLeft bean(com.ctrip.ferriswheel.proto.v1.FillLeft proto) {
        return new FillCells.FillLeft(proto.getSheetName(),
                proto.getTableName(),
                proto.getColumnIndex(),
                proto.getFirstRow(),
                proto.getLastRow(),
                proto.getNColumns());
    }

    public static com.ctrip.ferriswheel.proto.v1.FillLeft pb(FillCells.FillLeft bean) {
        return com.ctrip.ferriswheel.proto.v1.FillLeft.newBuilder()
                .setSheetName(bean.getSheetName())
                .setTableName(bean.getTableName())
                .setColumnIndex(bean.getColumnIndex())
                .setFirstRow(bean.getFirstRow())
                .setLastRow(bean.getLastRow())
                .setNColumns(bean.getnColumns())
                .build();
    }

    public static InsertColumns bean(com.ctrip.ferriswheel.proto.v1.InsertColumns proto) {
        return new InsertColumns(proto.getSheetName(),
                proto.getTableName(),
                proto.getColumnIndex(),
                proto.getNColumns());
    }

    public static com.ctrip.ferriswheel.proto.v1.InsertColumns pb(InsertColumns bean) {
        return com.ctrip.ferriswheel.proto.v1.InsertColumns.newBuilder()
                .setSheetName(bean.getSheetName())
                .setTableName(bean.getTableName())
                .setColumnIndex(bean.getColumnIndex())
                .setNColumns(bean.getnColumns())
                .build();
    }

    public static InsertRows bean(com.ctrip.ferriswheel.proto.v1.InsertRows proto) {
        return new InsertRows(proto.getSheetName(),
                proto.getTableName(),
                proto.getRowIndex(),
                proto.getNRows());
    }

    public static com.ctrip.ferriswheel.proto.v1.InsertRows pb(InsertRows bean) {
        return com.ctrip.ferriswheel.proto.v1.InsertRows.newBuilder()
                .setSheetName(bean.getSheetName())
                .setTableName(bean.getTableName())
                .setRowIndex(bean.getRowIndex())
                .setNRows(bean.getnRows())
                .build();
    }

    public static MoveSheet bean(com.ctrip.ferriswheel.proto.v1.MoveSheet proto) {
        return new MoveSheet(proto.getSheetName(), proto.getTargetIndex());
    }

    public static com.ctrip.ferriswheel.proto.v1.MoveSheet pb(MoveSheet bean) {
        return com.ctrip.ferriswheel.proto.v1.MoveSheet.newBuilder()
                .setSheetName(bean.getSheetName())
                .setTargetIndex(bean.getTargetIndex())
                .build();
    }

    public static RemoveAsset bean(com.ctrip.ferriswheel.proto.v1.RemoveAsset proto) {
        return new RemoveAsset(proto.getSheetName(), proto.getAssetName());
    }

    public static com.ctrip.ferriswheel.proto.v1.RemoveAsset pb(RemoveAsset bean) {
        return com.ctrip.ferriswheel.proto.v1.RemoveAsset.newBuilder()
                .setSheetName(bean.getSheetName())
                .setAssetName(bean.getAssetName())
                .build();
    }

    public static RemoveColumns bean(com.ctrip.ferriswheel.proto.v1.RemoveColumns proto) {
        return new RemoveColumns(proto.getSheetName(),
                proto.getTableName(),
                proto.getColumnIndex(),
                proto.getNColumns());
    }

    public static com.ctrip.ferriswheel.proto.v1.RemoveColumns pb(RemoveColumns bean) {
        return com.ctrip.ferriswheel.proto.v1.RemoveColumns.newBuilder()
                .setSheetName(bean.getSheetName())
                .setTableName(bean.getTableName())
                .setColumnIndex(bean.getColumnIndex())
                .setNColumns(bean.getnColumns())
                .build();
    }

    public static RemoveRows bean(com.ctrip.ferriswheel.proto.v1.RemoveRows proto) {
        return new RemoveRows(proto.getSheetName(),
                proto.getTableName(),
                proto.getRowIndex(),
                proto.getNRows());
    }

    public static com.ctrip.ferriswheel.proto.v1.RemoveRows pb(RemoveRows bean) {
        return com.ctrip.ferriswheel.proto.v1.RemoveRows.newBuilder()
                .setSheetName(bean.getSheetName())
                .setTableName(bean.getTableName())
                .setRowIndex(bean.getRowIndex())
                .setNRows(bean.getnRows())
                .build();
    }

    public static RemoveSheet bean(com.ctrip.ferriswheel.proto.v1.RemoveSheet proto) {
        return new RemoveSheet(proto.getSheetName());
    }

    public static com.ctrip.ferriswheel.proto.v1.RemoveSheet pb(RemoveSheet bean) {
        return com.ctrip.ferriswheel.proto.v1.RemoveSheet.newBuilder()
                .setSheetName(bean.getSheetName())
                .build();
    }

    public static RenameAsset bean(com.ctrip.ferriswheel.proto.v1.RenameAsset proto) {
        return new RenameAsset(proto.getSheetName(), proto.getOldAssetName(), proto.getNewAssetName());
    }

    public static com.ctrip.ferriswheel.proto.v1.RenameAsset pb(RenameAsset bean) {
        return com.ctrip.ferriswheel.proto.v1.RenameAsset.newBuilder()
                .setSheetName(bean.getSheetName())
                .setOldAssetName(bean.getOldAssetName())
                .setNewAssetName(bean.getNewAssetName())
                .build();
    }

    public static RenameSheet bean(com.ctrip.ferriswheel.proto.v1.RenameSheet proto) {
        return new RenameSheet(proto.getOldSheetName(), proto.getNewSheetName());
    }

    public static com.ctrip.ferriswheel.proto.v1.RenameSheet pb(RenameSheet bean) {
        return com.ctrip.ferriswheel.proto.v1.RenameSheet.newBuilder()
                .setOldSheetName(bean.getOldSheetName())
                .setNewSheetName(bean.getNewSheetName())
                .build();
    }

    // TODO transfer asset

    public static UpdateAutomaton bean(com.ctrip.ferriswheel.proto.v1.UpdateAutomaton updateAutomaton) {
        return null; // TODO not implemented yet
    }

    public static com.ctrip.ferriswheel.proto.v1.UpdateAutomaton pb(UpdateAutomaton updateAutomaton) {
        return null; // TODO not implemented yet
    }

    public static UpdateChart bean(com.ctrip.ferriswheel.proto.v1.UpdateChart proto) {
        return new UpdateChart(proto.getSheetName(),
                proto.getChart().getName(),
                PbHelper.bean(proto.getChart()));
    }

    public static com.ctrip.ferriswheel.proto.v1.UpdateChart pb(UpdateChart bean) {
        return com.ctrip.ferriswheel.proto.v1.UpdateChart.newBuilder()
                .setSheetName(bean.getSheetName())
                .setChart(PbHelper.pb(bean.getChartName(), bean.getChartData()))
                .build();
    }

    public static LayoutAsset bean(com.ctrip.ferriswheel.proto.v1.LayoutAsset proto) {
        return new LayoutAsset(
                proto.getSheetName(),
                proto.getAssetName(),
                PbHelper.bean(proto.getLayout())
        );
    }

    public static com.ctrip.ferriswheel.proto.v1.LayoutAsset pb(LayoutAsset layoutAsset) {
        return com.ctrip.ferriswheel.proto.v1.LayoutAsset.newBuilder()
                .setSheetName(layoutAsset.getSheetName())
                .setAssetName(layoutAsset.getAssetName())
                .setLayout(PbHelper.pb(layoutAsset.getLayout()))
                .build();
    }

    public static AddText bean(com.ctrip.ferriswheel.proto.v1.AddText proto) {
        return new AddText(proto.getSheetName(), PbHelper.bean(proto.getText()));
    }

    public static com.ctrip.ferriswheel.proto.v1.AddText pb(AddText bean) {
        return com.ctrip.ferriswheel.proto.v1.AddText.newBuilder()
                .setSheetName(bean.getSheetName())
                .setText(PbHelper.pb(bean.getTextData().getName(), bean.getTextData()))
                .build();
    }

    public static UpdateText bean(com.ctrip.ferriswheel.proto.v1.UpdateText proto) {
        return new UpdateText(proto.getSheetName(),
                proto.getText().getName(),
                PbHelper.bean(proto.getText())
        );
    }

    public static com.ctrip.ferriswheel.proto.v1.UpdateText pb(UpdateText bean) {
        return com.ctrip.ferriswheel.proto.v1.UpdateText.newBuilder()
                .setSheetName(bean.getSheetName())
                .setText(PbHelper.pb(bean.getTextName(), bean.getTextData()))
                .build();
    }

    public static ExecuteQuery bean(com.ctrip.ferriswheel.proto.v1.ExecuteQuery proto) {
        ExecuteQuery q = new ExecuteQuery(proto.getSheetName(),
                proto.getTableName(),
                new LinkedHashMap<>(proto.getParamsCount()));
        for (com.ctrip.ferriswheel.proto.v1.Parameter item : proto.getParamsList()) {
            q.getParams().put(item.getName(), PbHelper.toValue(item.getValue()));
        }
        return q;
    }

    public static com.ctrip.ferriswheel.proto.v1.ExecuteQuery pb(ExecuteQuery bean) {
        com.ctrip.ferriswheel.proto.v1.ExecuteQuery.Builder builder = com.ctrip.ferriswheel.proto.v1.ExecuteQuery.newBuilder()
                .setSheetName(bean.getSheetName())
                .setTableName(bean.getTableName());
        if (bean.getParams() != null) {
            for (Map.Entry<String, Variant> entry : bean.getParams().entrySet()) {
                builder.addParams(com.ctrip.ferriswheel.proto.v1.Parameter.newBuilder()
                        .setName(entry.getKey())
                        .setValue(PbHelper.pb(entry.getValue())));
            }
        }
        return builder.build();
    }

    public static com.ctrip.ferriswheel.proto.v1.ResetTable pb(ResetTable bean) {
        com.ctrip.ferriswheel.proto.v1.ResetTable.Builder builder = com.ctrip.ferriswheel.proto.v1.ResetTable.newBuilder()
                .setSheetName(bean.getSheetName())
                .setTable(PbHelper.pb(bean.getTable()).getTable());
        return builder.build();
    }

    public static com.ctrip.ferriswheel.proto.v1.SetCellsFormat pb(SetCellsFormat bean) {
        com.ctrip.ferriswheel.proto.v1.SetCellsFormat.Builder builder = com.ctrip.ferriswheel.proto.v1.SetCellsFormat.newBuilder()
                .setSheetName(bean.getSheetName())
                .setTableName(bean.getTableName())
                .setRowIndex(bean.getRowIndex())
                .setColumnIndex(bean.getColumnIndex())
                .setNRows(bean.getnRows())
                .setNColumns(bean.getnColumns())
                .setFormat(bean.getFormat());
        return builder.build();
    }

    public static com.ctrip.ferriswheel.proto.v1.AddForm pb(AddForm addForm) {
        com.ctrip.ferriswheel.proto.v1.AddForm.Builder builder = com.ctrip.ferriswheel.proto.v1.AddForm.newBuilder()
                .setSheetName(addForm.getSheetName())
                .setForm(PbHelper.pbForm(addForm.getFormData()));
        return builder.build();
    }

    public static com.ctrip.ferriswheel.proto.v1.UpdateForm pb(UpdateForm updateForm) {
        com.ctrip.ferriswheel.proto.v1.UpdateForm.Builder builder = com.ctrip.ferriswheel.proto.v1.UpdateForm.newBuilder()
                .setSheetName(updateForm.getSheetName())
                .setForm(PbHelper.pbForm(updateForm.getFormData()));
        return builder.build();
    }

    public static com.ctrip.ferriswheel.proto.v1.SubmitForm pb(SubmitForm submitForm) {
        com.ctrip.ferriswheel.proto.v1.SubmitForm.Builder builder = com.ctrip.ferriswheel.proto.v1.SubmitForm.newBuilder()
                .setSheetName(submitForm.getSheetName());
        for (Map.Entry<String, Variant> entry : submitForm.getFormData().entrySet()) {
            builder.addParams(com.ctrip.ferriswheel.proto.v1.Parameter.newBuilder()
                    .setName(entry.getKey())
                    .setValue(PbHelper.pb(entry.getValue())));
        }
        return builder.build();
    }

    public static com.ctrip.ferriswheel.proto.v1.Action pb(Action action) {
        if (action instanceof AddChart) {
            return com.ctrip.ferriswheel.proto.v1.Action.newBuilder()
                    .setAddChart(pb((AddChart) action))
                    .build();

        } else if (action instanceof AddSheet) {
            return com.ctrip.ferriswheel.proto.v1.Action.newBuilder()
                    .setAddSheet(pb((AddSheet) action))
                    .build();

        } else if (action instanceof AddTable) {
            return com.ctrip.ferriswheel.proto.v1.Action.newBuilder()
                    .setAddTable(pb((AddTable) action))
                    .build();

        } else if (action instanceof AutomateTable) {
            return com.ctrip.ferriswheel.proto.v1.Action.newBuilder()
                    .setAutomateTable(pb((AutomateTable) action))
                    .build();

        } else if (action instanceof CellAction.SetCellValue) {
            return com.ctrip.ferriswheel.proto.v1.Action.newBuilder()
                    .setSetCellValue(pb((CellAction.SetCellValue) action))
                    .build();

        } else if (action instanceof CellAction.SetCellFormula) {
            return com.ctrip.ferriswheel.proto.v1.Action.newBuilder()
                    .setSetCellFormula(pb((CellAction.SetCellFormula) action))
                    .build();

        } else if (action instanceof CellAction.RefreshCellValue) {
            return com.ctrip.ferriswheel.proto.v1.Action.newBuilder()
                    .setRefreshCellValue(pb((CellAction.RefreshCellValue) action))
                    .build();

        } else if (action instanceof ChartConsult) {
            return com.ctrip.ferriswheel.proto.v1.Action.newBuilder()
                    .setChartConsult(pb((ChartConsult) action))
                    .build();

        } else if (action instanceof EraseCells) {
            return com.ctrip.ferriswheel.proto.v1.Action.newBuilder()
                    .setEraseCells(pb((EraseCells) action))
                    .build();

//        } else if (action instanceof EraseRows) {
//            return com.ctrip.ferriswheel.proto.v1.Action.newBuilder()
//                    .setEraseRows(pb((EraseRows) action))
//                    .build();

        } else if (action instanceof FillCells.FillUp) {
            return com.ctrip.ferriswheel.proto.v1.Action.newBuilder()
                    .setFillUp(pb((FillCells.FillUp) action))
                    .build();

        } else if (action instanceof FillCells.FillRight) {
            return com.ctrip.ferriswheel.proto.v1.Action.newBuilder()
                    .setFillRight(pb((FillCells.FillRight) action))
                    .build();

        } else if (action instanceof FillCells.FillDown) {
            return com.ctrip.ferriswheel.proto.v1.Action.newBuilder()
                    .setFillDown(pb((FillCells.FillDown) action))
                    .build();

        } else if (action instanceof FillCells.FillLeft) {
            return com.ctrip.ferriswheel.proto.v1.Action.newBuilder()
                    .setFillLeft(pb((FillCells.FillLeft) action))
                    .build();

        } else if (action instanceof InsertColumns) {
            return com.ctrip.ferriswheel.proto.v1.Action.newBuilder()
                    .setInsertColumns(pb((InsertColumns) action))
                    .build();

        } else if (action instanceof InsertRows) {
            return com.ctrip.ferriswheel.proto.v1.Action.newBuilder()
                    .setInsertRows(pb((InsertRows) action))
                    .build();

        } else if (action instanceof MoveSheet) {
            return com.ctrip.ferriswheel.proto.v1.Action.newBuilder()
                    .setMoveSheet(pb((MoveSheet) action))
                    .build();

        } else if (action instanceof RemoveAsset) {
            return com.ctrip.ferriswheel.proto.v1.Action.newBuilder()
                    .setRemoveAsset(pb((RemoveAsset) action))
                    .build();

        } else if (action instanceof RemoveColumns) {
            return com.ctrip.ferriswheel.proto.v1.Action.newBuilder()
                    .setRemoveColumns(pb((RemoveColumns) action))
                    .build();

        } else if (action instanceof RemoveRows) {
            return com.ctrip.ferriswheel.proto.v1.Action.newBuilder()
                    .setRemoveRows(pb((RemoveRows) action))
                    .build();

        } else if (action instanceof RemoveSheet) {
            return com.ctrip.ferriswheel.proto.v1.Action.newBuilder()
                    .setRemoveSheet(pb((RemoveSheet) action))
                    .build();

        } else if (action instanceof RenameAsset) {
            return com.ctrip.ferriswheel.proto.v1.Action.newBuilder()
                    .setRenameAsset(pb((RenameAsset) action))
                    .build();

        } else if (action instanceof RenameSheet) {
            return com.ctrip.ferriswheel.proto.v1.Action.newBuilder()
                    .setRenameSheet(pb((RenameSheet) action))
                    .build();

            //} else if (action instanceof TransferAsset) {

        } else if (action instanceof UpdateAutomaton) {
            return com.ctrip.ferriswheel.proto.v1.Action.newBuilder()
                    .setUpdateAutomaton(pb((UpdateAutomaton) action))
                    .build();

        } else if (action instanceof UpdateChart) {
            return com.ctrip.ferriswheel.proto.v1.Action.newBuilder()
                    .setUpdateChart(pb((UpdateChart) action))
                    .build();

        } else if (action instanceof LayoutAsset) {
            return com.ctrip.ferriswheel.proto.v1.Action.newBuilder()
                    .setLayoutAsset(pb((LayoutAsset) action))
                    .build();

        } else if (action instanceof AddText) {
            return com.ctrip.ferriswheel.proto.v1.Action.newBuilder()
                    .setAddText(pb((AddText) action))
                    .build();

        } else if (action instanceof UpdateText) {
            return com.ctrip.ferriswheel.proto.v1.Action.newBuilder()
                    .setUpdateText(pb((UpdateText) action))
                    .build();

        } else if (action instanceof ExecuteQuery) {
            return com.ctrip.ferriswheel.proto.v1.Action.newBuilder()
                    .setExecuteQuery(pb((ExecuteQuery) action))
                    .build();

        } else if (action instanceof ResetTable) {
            return com.ctrip.ferriswheel.proto.v1.Action.newBuilder()
                    .setResetTable(pb((ResetTable) action))
                    .build();

        } else if (action instanceof SetCellsFormat) {
            return com.ctrip.ferriswheel.proto.v1.Action.newBuilder()
                    .setSetCellsFormat(pb((SetCellsFormat) action))
                    .build();

        } else if (action instanceof AddForm) {
            return com.ctrip.ferriswheel.proto.v1.Action.newBuilder()
                    .setAddForm(pb((AddForm) action))
                    .build();

        } else if (action instanceof UpdateForm) {
            return com.ctrip.ferriswheel.proto.v1.Action.newBuilder()
                    .setUpdateForm(pb((UpdateForm) action))
                    .build();

        } else if (action instanceof SubmitForm) {
            return com.ctrip.ferriswheel.proto.v1.Action.newBuilder()
                    .setSubmitForm(pb((SubmitForm) action))
                    .build();

        } else {
            throw new RuntimeException();
        }
    }

}
