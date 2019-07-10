package com.ctrip.ferriswheel.example.web;

import com.ctrip.ferriswheel.common.Environment;
import com.ctrip.ferriswheel.common.Sheet;
import com.ctrip.ferriswheel.common.Workbook;
import com.ctrip.ferriswheel.common.action.Action;
import com.ctrip.ferriswheel.common.chart.Chart;
import com.ctrip.ferriswheel.common.form.Form;
import com.ctrip.ferriswheel.common.table.Table;
import com.ctrip.ferriswheel.common.text.Text;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.core.action.*;
import com.ctrip.ferriswheel.core.asset.DefaultForm;
import com.ctrip.ferriswheel.core.asset.DefaultQueryAutomaton;
import com.ctrip.ferriswheel.core.asset.DefaultSheet;
import com.ctrip.ferriswheel.core.asset.ReviseCollector;
import com.ctrip.ferriswheel.core.bean.ChartData;
import com.ctrip.ferriswheel.core.bean.DefaultEnvironment;
import com.ctrip.ferriswheel.core.loader.DefaultProviderManager;
import com.ctrip.ferriswheel.core.util.ChartConsultantHelper;
import com.ctrip.ferriswheel.proto.util.PbActionHelper;
import com.ctrip.ferriswheel.proto.util.PbHelper;
import com.ctrip.ferriswheel.proto.v1.EditResponse;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.ctrip.ferriswheel.proto.util.PbHelper.toValue;

public class WorkbookRequestHandler extends TextWebSocketHandler implements RequestHandler {
    private static final Logger LOG = LoggerFactory.getLogger(WorkbookRequestHandler.class);
    private static final String WORK_CONTEXT = "WORK_CONTEXT";
    private static final Environment ENV;

    static {
        DefaultProviderManager pm = new DefaultProviderManager();
        ENV = new DefaultEnvironment.Builder()
                .setProviderManager(pm)
                .build();
    }

    // CAUTION: must keep these handlers in strict order to match protobuf
    private final RequestActionHandler[] handlers = new RequestActionHandler[]{
            // NOT_SET(0):
            new PingHandler(),
            // ADDCHART(1):
            new AddChartHandler(),
            // ADDSHEET(2):
            new AddSheetHandler(),
            // ADDTABLE(3):
            new AddTableHandler(),
            // AUTOMATETABLE(4):
            new AutomateTableHandler(),
            // SETCELLVALUE(5):
            new SetCellValueHandler(),
            // SETCELLFORMULA(6):
            new SetCellFormulaHandler(),
            // REFRESHCELLVALUE(7):
            new RefreshCellValueHandler(),
            // CHARTCONSULT(8):
            new ChartConsultHandler(),
            // ERASECOLUMNS(9):
            new EraseCellsHandler(),
            // ERASEROWS(10):
            new DummyHandler(),
            // FILLUP(11):
            new FillUpHandler(),
            // FILLRIGHT(12):
            new FillRightHandler(),
            // FILLDOWN(13):
            new FillDownHandler(),
            // FILLLEFT(14):
            new FillLeftHandler(),
            // INSERTCOLUMNS(15):
            new InsertColumnsHandler(),
            // INSERTROWS(16):
            new InsertRowsHandler(),
            // MOVESHEET(17):
            new MoveSheetHandler(),
            // REMOVEASSET(18):
            new RemoveAssetHandler(),
            // REMOVECOLUMNS(19):
            new RemoveColumnsHandler(),
            // REMOVEROWS(20):
            new RemoveRowsHandler(),
            // REMOVESHEET(21):
            new RemoveSheetHandler(),
            // RENAMEASSET(22):
            new RenameAssetHandler(),
            // RENAMESHEET(23):
            new RenameSheetHandler(),
            // TRANSFERASSET(24):
            new TransferAssetHandler(),
            // UPDATEAUTOMATON(25):
            new UpdateAutomatonHandler(),
            // UPDATECHART(26):
            new UpdateChartHandler(),
            // CREATEWORKBOOK(27):
            new CreateWorkbookHandler(),
            // OPENWORKBOOK(28):
            new OpenWorkbookHandler(),
            // SAVEWORKBOOK(29):
            new SaveWorkbookHandler(),
            // CLOSEWORKBOOK(30):
            new CloseWorkbookHandler(),
            // LayoutAsset(31)
            new LayoutAssetHandler(),
            // AddText(32)
            new AddTextHandler(),
            // UpdateText(33)
            new UpdateTextHandler(),
            // ExecuteQuery(34)
            new ExecuteQueryHandler(),
            // ResetTable(35)
            new DummyHandler(),
            // SetCellsFormat(36)
            new SetCellsFormatHandler(),
            // AddForm(37)
            new AddFormHandler(),
            // UpdateForm(38)
            new UpdateFormHandler(),
            // SubmitForm(39)
            new SubmitFormHandler(),
    };

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        com.ctrip.ferriswheel.proto.v1.EditRequest request;
        com.ctrip.ferriswheel.proto.v1.EditResponse response = null;

        try { // parse request
            com.ctrip.ferriswheel.proto.v1.EditRequest.Builder requestBuilder = com.ctrip.ferriswheel.proto.v1.EditRequest.newBuilder();
            JsonFormat.parser().ignoringUnknownFields().merge(message.getPayload(), requestBuilder);
            request = requestBuilder.build();
        } catch (Throwable t) {
            LOG.warn("Failed to parse request.", t);
            throw t; // just throw up
        }

        try { // handle request
            WorkContext workContext = getOrCreateContext(session);
            response = handle(request, workContext);
        } catch (Throwable t) {
            LOG.error("Exception occurred while handling message.", t);
            response = com.ctrip.ferriswheel.proto.v1.EditResponse.newBuilder()
                    .setTxId(request.getTxId())
                    .setStatusCode(SpreadsheetResponse.STATUS_UNKNOWN_ERROR)
                    .setMessage("发生异常了：" + t.getMessage())
                    .build();
        } finally {
            session.sendMessage(new TextMessage(JsonFormat.printer()
                    .includingDefaultValueFields()
                    .print(response)));
        }
    }

    protected WorkContext getOrCreateContext(WebSocketSession session) {
        WorkContext workContext = (WorkContext) session.getAttributes().get(WORK_CONTEXT);
        if (workContext == null) {
            workContext = new WorkContext();
            session.getAttributes().put(WORK_CONTEXT, workContext);
        }
        return workContext;
    }

    @Override
    public com.ctrip.ferriswheel.proto.v1.EditResponse handle(com.ctrip.ferriswheel.proto.v1.EditRequest request,
                                                              WorkContext workContext) {
        if (request.getTxId() < -1 || workContext == null) {
            throw new IllegalArgumentException();
        }
        com.ctrip.ferriswheel.proto.v1.Action action = request.getAction();
        int actionCase = action.getActionCase().getNumber();
        if (actionCase < 0 || actionCase > handlers.length) {
            throw new RuntimeException("Unrecognized action case: " + actionCase);
        }
        RequestActionHandler handler = handlers[actionCase];
        return handler.handle(request.getTxId(), action, workContext);
    }

    Workbook loadExampleWorkbook() throws IOException {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("workbook-example.json");
        Reader rd = new InputStreamReader(is);
        com.ctrip.ferriswheel.proto.v1.Workbook.Builder builder = com.ctrip.ferriswheel.proto.v1.Workbook.newBuilder();
        JsonFormat.parser().merge(rd, builder);
        com.ctrip.ferriswheel.proto.v1.Workbook pb = builder.build();
        return PbHelper.bean(ENV, pb);
    }

    class PingHandler implements RequestActionHandler {

        @Override
        public EditResponse handle(long txId, com.ctrip.ferriswheel.proto.v1.Action action, WorkContext workContext) {
            return com.ctrip.ferriswheel.proto.v1.EditResponse.newBuilder()
                    .setTxId(txId)
                    .setStatusCode(0)
                    .setMessage("OK")
                    .build();
        }
    }

    abstract class WorkbookActionHandler implements RequestActionHandler {
        @Override
        public com.ctrip.ferriswheel.proto.v1.EditResponse handle(long txId,
                                                                  com.ctrip.ferriswheel.proto.v1.Action action,
                                                                  WorkContext workContext) {
            Workbook workbook = workContext.getWorkbook();
            if (workbook == null) {
                throw new IllegalArgumentException();
            }
            handle(txId, action, workbook);


            List<Action> actions = drainRevises(workContext);
            com.ctrip.ferriswheel.proto.v1.EditResponse.Builder respBuilder = com.ctrip.ferriswheel.proto.v1.EditResponse.newBuilder();
            respBuilder.setTxId(txId)
                    .setStatusCode(SpreadsheetResponse.STATUS_OK)
                    .setMessage("操作成功。");
            com.ctrip.ferriswheel.proto.v1.ChangeList.Builder cl = com.ctrip.ferriswheel.proto.v1.ChangeList.newBuilder();
            for (Action act : actions) {
                cl.addActions(PbActionHelper.pb(act));
            }
            respBuilder.setChanges(cl);
            return respBuilder.build();
        }

        abstract void handle(long txId, com.ctrip.ferriswheel.proto.v1.Action action, Workbook workbook);


        private List<Action> drainRevises(WorkContext context) {
            List<Action> actions = context.getCollector().drainRevises();
            Workbook workbook = context.getWorkbook();
            for (Action action : actions) {
                if (action instanceof AddChart) {
                    AddChart addChart = (AddChart) action;
                    Sheet s = workbook.getSheet(addChart.getSheetName());
                    Chart c = s.getAsset(addChart.getChartData().getName());
                    addChart.setChartData(c);

                } else if (action instanceof UpdateChart) {
                    UpdateChart updateChart = (UpdateChart) action;
                    Sheet s = workbook.getSheet(updateChart.getSheetName());
                    Chart c = s.getAsset(updateChart.getChartName());
                    updateChart.setChartData(c);

                } else if (action instanceof AddTable) {
                    AddTable addTable = (AddTable) action;
                    Sheet s = workbook.getSheet(addTable.getSheetName());
                    Table t = s.getAsset(addTable.getTableData().getName());
                    addTable.setTableData(t);
                }
            }
            return actions;
        }

    }

    class AddChartHandler extends WorkbookActionHandler {

        @Override
        public void handle(long txId, com.ctrip.ferriswheel.proto.v1.Action action, Workbook workbook) {
            com.ctrip.ferriswheel.proto.v1.AddChart addChart = action.getAddChart();
            workbook.getSheet(addChart.getSheetName()).addAsset(Chart.class,
                    PbHelper.bean(addChart.getChart()));
        }
    }

    class AddSheetHandler extends WorkbookActionHandler {

        @Override
        public void handle(long txId, com.ctrip.ferriswheel.proto.v1.Action action, Workbook workbook) {
            com.ctrip.ferriswheel.proto.v1.AddSheet addSheet = action.getAddSheet();
            workbook.addSheet(addSheet.getSheetName());
        }
    }

    class AddTableHandler extends WorkbookActionHandler {

        @Override
        public void handle(long txId, com.ctrip.ferriswheel.proto.v1.Action action, Workbook workbook) {
            com.ctrip.ferriswheel.proto.v1.AddTable addTable = action.getAddTable();
            workbook.getSheet(addTable.getSheetName()).addAsset(Table.class, PbHelper.bean(addTable.getTable()));
        }
    }

    class AutomateTableHandler extends WorkbookActionHandler {

        @Override
        public void handle(long txId, com.ctrip.ferriswheel.proto.v1.Action action, Workbook workbook) {
            com.ctrip.ferriswheel.proto.v1.AutomateTable automateTable = action.getAutomateTable();
            Table table = workbook.getSheet(automateTable.getSheetName())
                    .getAsset(automateTable.getTableName());
            com.ctrip.ferriswheel.proto.v1.TableAutomaton automaton = automateTable.getAutomaton();
            switch (automaton.getAutomatonCase()) {
                case QUERY_AUTOMATON:
                    table.automate(PbHelper.bean(automaton.getQueryAutomaton()));
                    break;
                case PIVOT_AUTOMATON:
                    table.automate(PbHelper.bean(automaton.getPivotAutomaton()));
                    break;
                case AUTOMATON_NOT_SET:
                default:
                    throw new RuntimeException("Unrecognized solution case: "
                            + automaton.getAutomatonCase());
            }
        }
    }

    class SetCellValueHandler extends WorkbookActionHandler {

        @Override
        public void handle(long txId, com.ctrip.ferriswheel.proto.v1.Action action, Workbook workbook) {
            com.ctrip.ferriswheel.proto.v1.SetCellValue setCellValue = action.getSetCellValue();
            ((Table) workbook.getSheet(setCellValue.getSheetName())
                    .getAsset(setCellValue.getTableName()))
                    .setCellValue(setCellValue.getRowIndex(),
                            setCellValue.getColumnIndex(),
                            PbHelper.toDynamicValue(setCellValue.getValue()));
        }
    }

    class SetCellFormulaHandler extends WorkbookActionHandler {

        @Override
        public void handle(long txId, com.ctrip.ferriswheel.proto.v1.Action action, Workbook workbook) {
            com.ctrip.ferriswheel.proto.v1.SetCellFormula setCellFormula = action.getSetCellFormula();
            ((Table) workbook.getSheet(setCellFormula.getSheetName())
                    .getAsset(setCellFormula.getTableName()))
                    .setCellFormula(setCellFormula.getRowIndex(),
                            setCellFormula.getColumnIndex(),
                            setCellFormula.getFormulaString());
        }
    }

    class RefreshCellValueHandler extends WorkbookActionHandler {

        @Override
        public void handle(long txId, com.ctrip.ferriswheel.proto.v1.Action action, Workbook workbook) {
            throw new RuntimeException("This action should only be triggered internally.");
        }
    }

    class ChartConsultHandler implements RequestActionHandler {

        /**
         * TODO chart consultant lacks of abstract layer
         *
         * @param txId
         * @param action
         * @param workContext
         * @return
         */
        @Override
        public com.ctrip.ferriswheel.proto.v1.EditResponse handle(long txId, com.ctrip.ferriswheel.proto.v1.Action action, WorkContext workContext) {
            Workbook workbook = workContext.getWorkbook();
            if (workbook == null) {
                throw new IllegalArgumentException();
            }
            com.ctrip.ferriswheel.proto.v1.ChartConsult ccProto = action.getChartConsult();
            ChartConsult cc = PbActionHelper.bean(ccProto);
            Sheet sheet = workbook.getSheet(cc.getSheetName());
            Table table = sheet.getAsset(cc.getTableName());
            ChartData c = ChartConsultantHelper.getSuggestedChartModel(table,
                    cc.getLeft(),
                    cc.getTop(),
                    cc.getRight(),
                    cc.getBottom());
            c.setType(cc.getType());
            com.ctrip.ferriswheel.proto.v1.EditResponse.Builder respBuilder = com.ctrip.ferriswheel.proto.v1.EditResponse.newBuilder();
            respBuilder.setTxId(txId)
                    .setStatusCode(SpreadsheetResponse.STATUS_OK)
                    .setMessage("操作成功。");
            String chartName = c.getType() + "-chart-";
            int i = 1;
            while (true) {
                if (sheet.getAsset(chartName + i) == null) {
                    break;
                } else {
                    i++;
                }
            }
            chartName = chartName + i;
            respBuilder.setSuggestedChart(PbHelper.pb(chartName, c));
            return respBuilder.build();
        }
    }

    class EraseCellsHandler extends WorkbookActionHandler {

        @Override
        public void handle(long txId, com.ctrip.ferriswheel.proto.v1.Action action, Workbook workbook) {
            com.ctrip.ferriswheel.proto.v1.EraseCells eraseCells = action.getEraseCells();
            ((Table) workbook.getSheet(eraseCells.getSheetName())
                    .getAsset(eraseCells.getTableName()))
                    .eraseCells(eraseCells.getTop(),
                            eraseCells.getRight(),
                            eraseCells.getBottom(),
                            eraseCells.getLeft());
        }
    }

    class FillUpHandler extends WorkbookActionHandler {

        @Override
        public void handle(long txId, com.ctrip.ferriswheel.proto.v1.Action action, Workbook workbook) {
            com.ctrip.ferriswheel.proto.v1.FillUp fillUp = action.getFillUp();
            ((Table) workbook.getSheet(fillUp.getSheetName())
                    .getAsset(fillUp.getTableName()))
                    .fillUp(fillUp.getRowIndex(),
                            fillUp.getFirstColumn(),
                            fillUp.getLastColumn(),
                            fillUp.getNRows());
        }
    }

    class FillRightHandler extends WorkbookActionHandler {

        @Override
        public void handle(long txId, com.ctrip.ferriswheel.proto.v1.Action action, Workbook workbook) {
            com.ctrip.ferriswheel.proto.v1.FillRight fillRight = action.getFillRight();
            ((Table) workbook.getSheet(fillRight.getSheetName())
                    .getAsset(fillRight.getTableName()))
                    .fillRight(fillRight.getColumnIndex(),
                            fillRight.getFirstRow(),
                            fillRight.getLastRow(),
                            fillRight.getNColumns());
        }
    }

    class FillDownHandler extends WorkbookActionHandler {

        @Override
        public void handle(long txId, com.ctrip.ferriswheel.proto.v1.Action action, Workbook workbook) {
            com.ctrip.ferriswheel.proto.v1.FillDown fillDown = action.getFillDown();
            ((Table) workbook.getSheet(fillDown.getSheetName())
                    .getAsset(fillDown.getTableName()))
                    .fillDown(fillDown.getRowIndex(),
                            fillDown.getFirstColumn(),
                            fillDown.getLastColumn(),
                            fillDown.getNRows());
        }
    }

    class FillLeftHandler extends WorkbookActionHandler {

        @Override
        public void handle(long txId, com.ctrip.ferriswheel.proto.v1.Action action, Workbook workbook) {
            com.ctrip.ferriswheel.proto.v1.FillLeft fillLeft = action.getFillLeft();
            ((Table) workbook.getSheet(fillLeft.getSheetName())
                    .getAsset(fillLeft.getTableName()))
                    .fillLeft(fillLeft.getColumnIndex(),
                            fillLeft.getFirstRow(),
                            fillLeft.getLastRow(),
                            fillLeft.getNColumns());
        }
    }

    class InsertColumnsHandler extends WorkbookActionHandler {

        @Override
        public void handle(long txId, com.ctrip.ferriswheel.proto.v1.Action action, Workbook workbook) {
            com.ctrip.ferriswheel.proto.v1.InsertColumns insertColumns = action.getInsertColumns();
            ((Table) workbook.getSheet(insertColumns.getSheetName())
                    .getAsset(insertColumns.getTableName()))
                    .addColumns(insertColumns.getColumnIndex(), insertColumns.getNColumns());
        }
    }

    class InsertRowsHandler extends WorkbookActionHandler {

        @Override
        public void handle(long txId, com.ctrip.ferriswheel.proto.v1.Action action, Workbook workbook) {
            com.ctrip.ferriswheel.proto.v1.InsertRows insertRows = action.getInsertRows();
            ((Table) workbook.getSheet(insertRows.getSheetName())
                    .getAsset(insertRows.getTableName()))
                    .addRows(insertRows.getRowIndex(), insertRows.getNRows());
        }
    }

    class MoveSheetHandler extends WorkbookActionHandler {

        @Override
        public void handle(long txId, com.ctrip.ferriswheel.proto.v1.Action action, Workbook workbook) {
            com.ctrip.ferriswheel.proto.v1.MoveSheet moveSheet = action.getMoveSheet();
            workbook.moveSheet(moveSheet.getSheetName(), moveSheet.getTargetIndex());
        }
    }

    class RemoveAssetHandler extends WorkbookActionHandler {

        @Override
        public void handle(long txId, com.ctrip.ferriswheel.proto.v1.Action action, Workbook workbook) {
            com.ctrip.ferriswheel.proto.v1.RemoveAsset removeAsset = action.getRemoveAsset();
            workbook.getSheet(removeAsset.getSheetName())
                    .removeAsset(removeAsset.getAssetName());
        }
    }

    class RemoveColumnsHandler extends WorkbookActionHandler {

        @Override
        public void handle(long txId, com.ctrip.ferriswheel.proto.v1.Action action, Workbook workbook) {
            com.ctrip.ferriswheel.proto.v1.RemoveColumns removeColumns = action.getRemoveColumns();
            ((Table) workbook.getSheet(removeColumns.getSheetName())
                    .getAsset(removeColumns.getTableName()))
                    .removeColumns(removeColumns.getColumnIndex(), removeColumns.getNColumns());
        }
    }

    class RemoveRowsHandler extends WorkbookActionHandler {

        @Override
        public void handle(long txId, com.ctrip.ferriswheel.proto.v1.Action action, Workbook workbook) {
            com.ctrip.ferriswheel.proto.v1.RemoveRows removeRows = action.getRemoveRows();
            ((Table) workbook.getSheet(removeRows.getSheetName())
                    .getAsset(removeRows.getTableName()))
                    .removeRows(removeRows.getRowIndex(), removeRows.getNRows());
        }
    }

    class RemoveSheetHandler extends WorkbookActionHandler {

        @Override
        public void handle(long txId, com.ctrip.ferriswheel.proto.v1.Action action, Workbook workbook) {
            com.ctrip.ferriswheel.proto.v1.RemoveSheet removeSheet = action.getRemoveSheet();
            workbook.removeSheet(removeSheet.getSheetName());
        }
    }

    class RenameAssetHandler extends WorkbookActionHandler {

        @Override
        public void handle(long txId, com.ctrip.ferriswheel.proto.v1.Action action, Workbook workbook) {
            com.ctrip.ferriswheel.proto.v1.RenameAsset renameAsset = action.getRenameAsset();
            workbook.getSheet(renameAsset.getSheetName())
                    .renameAsset(renameAsset.getOldAssetName(), renameAsset.getNewAssetName());
        }
    }

    class RenameSheetHandler extends WorkbookActionHandler {

        @Override
        public void handle(long txId, com.ctrip.ferriswheel.proto.v1.Action action, Workbook workbook) {
            com.ctrip.ferriswheel.proto.v1.RenameSheet renameSheet = action.getRenameSheet();
            workbook.renameSheet(renameSheet.getOldSheetName(), renameSheet.getNewSheetName());
        }
    }

    class TransferAssetHandler extends WorkbookActionHandler {
        @Override
        void handle(long txId, com.ctrip.ferriswheel.proto.v1.Action action, Workbook workbook) {
            com.ctrip.ferriswheel.proto.v1.TransferAsset transferAsset = action.getTransferAsset();
            // TODO
        }
    }

    class UpdateAutomatonHandler extends WorkbookActionHandler {

        @Override
        public void handle(long txId, com.ctrip.ferriswheel.proto.v1.Action action, Workbook workbook) {
            com.ctrip.ferriswheel.proto.v1.UpdateAutomaton updateAutomaton = action.getUpdateAutomaton();
            Table table = workbook.getSheet(updateAutomaton.getSheetName()).getAsset(updateAutomaton.getTableName());
            com.ctrip.ferriswheel.proto.v1.TableAutomaton automaton = updateAutomaton.getAutomaton();
            switch (automaton.getAutomatonCase()) {
                case QUERY_AUTOMATON:
                    table.automate(PbHelper.bean(automaton.getQueryAutomaton()));
                    break;
                case PIVOT_AUTOMATON:
                    table.automate(PbHelper.bean(automaton.getPivotAutomaton()));
                    break;
                case AUTOMATON_NOT_SET:
                default:
                    throw new RuntimeException("Unrecognized solution case: "
                            + automaton.getAutomatonCase());
            }
        }
    }

    class UpdateChartHandler extends WorkbookActionHandler {
        @Override
        public void handle(long txId, com.ctrip.ferriswheel.proto.v1.Action action, Workbook workbook) {
            com.ctrip.ferriswheel.proto.v1.UpdateChart updateChart = action.getUpdateChart();
            workbook.getSheet(updateChart.getSheetName())
                    .updateChart(updateChart.getChart().getName(),
                            PbHelper.bean(updateChart.getChart()));
        }
    }

    class CreateWorkbookHandler implements RequestActionHandler {

        @Override
        public com.ctrip.ferriswheel.proto.v1.EditResponse handle(long txId,
                                                                  com.ctrip.ferriswheel.proto.v1.Action action,
                                                                  WorkContext context) {
            throw new RuntimeException("Deprecated and not supported any more!");
        }
    }

    class OpenWorkbookHandler implements RequestActionHandler {

        @Override
        public com.ctrip.ferriswheel.proto.v1.EditResponse handle(long txId,
                                                                  com.ctrip.ferriswheel.proto.v1.Action action,
                                                                  WorkContext workContext) {
            com.ctrip.ferriswheel.proto.v1.WorkbookOperation openWorkbook = action.getOpenWorkbook();

            Workbook workbook;
            try {
                workbook = loadExampleWorkbook();
                // workbook.refresh(); // TODO is this necessary?
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            //workContext.setWorkDir(openWorkbook.getPathname());
            workContext.setWorkbook(workbook);
            ReviseCollector reviseCollector = new ReviseCollector();
            workbook.addListener(reviseCollector);
            workContext.setCollector(reviseCollector);

            return com.ctrip.ferriswheel.proto.v1.EditResponse.newBuilder()
                    .setTxId(txId)
                    .setStatusCode(0)
                    .setMessage("OK")
                    .setWorkbook(PbHelper.pb(workbook))
                    .build();
        }
    }

    class SaveWorkbookHandler implements RequestActionHandler {

        @Override
        public com.ctrip.ferriswheel.proto.v1.EditResponse handle(long txId,
                                                                  com.ctrip.ferriswheel.proto.v1.Action action,
                                                                  WorkContext workContext) {
            com.ctrip.ferriswheel.proto.v1.WorkbookOperation saveWorkbook = action.getSaveWorkbook();

            // TODO persist workbook

            return com.ctrip.ferriswheel.proto.v1.EditResponse.newBuilder()
                    .setTxId(txId)
                    .setStatusCode(0)
                    .setMessage("OK")
                    .build();
        }
    }

    class CloseWorkbookHandler implements RequestActionHandler {

        @Override
        public com.ctrip.ferriswheel.proto.v1.EditResponse handle(long txId,
                                                                  com.ctrip.ferriswheel.proto.v1.Action action,
                                                                  WorkContext workContext) {
            com.ctrip.ferriswheel.proto.v1.WorkbookOperation createWorkbook = action.getCreateWorkbook();
            workContext.setWorkbook(null);
            workContext.setCollector(null);
            // workContext.setWorkDir(null);
            return com.ctrip.ferriswheel.proto.v1.EditResponse.newBuilder()
                    .setTxId(txId)
                    .setStatusCode(0)
                    .setMessage("OK")
                    .build();
        }
    }

    class LayoutAssetHandler extends WorkbookActionHandler {

        @Override
        void handle(long txId, com.ctrip.ferriswheel.proto.v1.Action action, Workbook workbook) {
            LayoutAsset layoutAsset = PbActionHelper.bean(action.getLayoutAsset());
            Sheet sheet = workbook.getSheet(layoutAsset.getSheetName());
            sheet.layoutAsset(layoutAsset.getAssetName(), layoutAsset.getLayout());
        }
    }

    class AddTextHandler extends WorkbookActionHandler {

        @Override
        void handle(long txId, com.ctrip.ferriswheel.proto.v1.Action action, Workbook workbook) {
            AddText addText = PbActionHelper.bean(action.getAddText());
            Sheet sheet = workbook.getSheet(addText.getSheetName());
            sheet.addAsset(Text.class, addText.getTextData());
        }
    }

    class UpdateTextHandler extends WorkbookActionHandler {

        @Override
        void handle(long txId, com.ctrip.ferriswheel.proto.v1.Action action, Workbook workbook) {
            UpdateText updateText = PbActionHelper.bean(action.getUpdateText());
            Sheet sheet = workbook.getSheet(updateText.getSheetName());
            sheet.updateText(updateText.getTextName(), updateText.getTextData());
        }
    }

    class ExecuteQueryHandler extends WorkbookActionHandler {
        @Override
        void handle(long txId, com.ctrip.ferriswheel.proto.v1.Action action, Workbook workbook) {
            ExecuteQuery execQuery = PbActionHelper.bean(action.getExecuteQuery());
            Sheet sheet = workbook.getSheet(execQuery.getSheetName());
            Table table = sheet.getAsset(execQuery.getTableName());
            DefaultQueryAutomaton auto = (DefaultQueryAutomaton) table.getAutomaton();
            auto.handleAction(execQuery);
        }
    }

    class SetCellsFormatHandler extends WorkbookActionHandler {
        @Override
        void handle(long txId, com.ctrip.ferriswheel.proto.v1.Action action, Workbook workbook) {
            com.ctrip.ferriswheel.proto.v1.SetCellsFormat setCellsFormat = action.getSetCellsFormat();
            ((Table) workbook.getSheet(setCellsFormat.getSheetName())
                    .getAsset(setCellsFormat.getTableName()))
                    .setCellsFormat(setCellsFormat.getRowIndex(),
                            setCellsFormat.getColumnIndex(),
                            setCellsFormat.getNRows(),
                            setCellsFormat.getNColumns(),
                            setCellsFormat.getFormat());
        }
    }

    class AddFormHandler extends WorkbookActionHandler {
        @Override
        void handle(long txId, com.ctrip.ferriswheel.proto.v1.Action action, Workbook workbook) {
            com.ctrip.ferriswheel.proto.v1.AddForm addForm = action.getAddForm();
            workbook.getSheet(addForm.getSheetName())
                    .addAsset(Form.class, PbHelper.bean(addForm.getForm()));
        }
    }

    class UpdateFormHandler extends WorkbookActionHandler {
        @Override
        void handle(long txId, com.ctrip.ferriswheel.proto.v1.Action action, Workbook workbook) {
            com.ctrip.ferriswheel.proto.v1.UpdateForm updateForm = action.getUpdateForm();
            ((DefaultSheet) workbook.getSheet(updateForm.getSheetName()))
                    .updateForm(PbHelper.bean(updateForm.getForm()));
        }
    }

    class SubmitFormHandler extends WorkbookActionHandler {
        @Override
        void handle(long txId, com.ctrip.ferriswheel.proto.v1.Action action, Workbook workbook) {
            com.ctrip.ferriswheel.proto.v1.SubmitForm submitForm = action.getSubmitForm();
            Map<String, Variant> params = new LinkedHashMap<>(submitForm.getParamsCount());
            for (com.ctrip.ferriswheel.proto.v1.Parameter item : submitForm.getParamsList()) {
                params.put(item.getName(), toValue(item.getValue()));
            }
            ((DefaultForm) workbook.getSheet(submitForm.getSheetName())
                    .getAsset(submitForm.getFormName()))
                    .submit(params);
        }
    }

    class DummyHandler extends WorkbookActionHandler {
        @Override
        void handle(long txId, com.ctrip.ferriswheel.proto.v1.Action action, Workbook workbook) {
            throw new UnsupportedOperationException();
        }
    }
}
