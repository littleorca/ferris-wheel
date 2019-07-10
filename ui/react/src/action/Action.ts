import AddChart from "./AddChart";
import AddSheet from "./AddSheet";
import AddTable from "./AddTable";
import AutomateTable from "./AutomateTable";
import SetCellValue from "./SetCellValue";
import SetCellFormula from "./SetCellFormula";
import RefreshCellValue from "./RefreshCellValue";
import ChartConsult from "./ChartConsult";
import EraseCells from "./EraseCells";
import FillUp from "./FillUp";
import FillRight from "./FillRight";
import FillDown from "./FillDown";
import FillLeft from "./FillLeft";
import InsertColumns from "./InsertColumns";
import InsertRows from "./InsertRows";
import MoveSheet from "./MoveSheet";
import RemoveAsset from "./RemoveAsset";
import RemoveColumns from "./RemoveColumns";
import RemoveRows from "./RemoveRows";
import RemoveSheet from "./RemoveSheet";
import RenameAsset from "./RenameAsset";
import RenameSheet from "./RenameSheet";
import TransferAsset from "./TransferAsset";
import UpdateAutomaton from "./UpdateAutomaton";
import UpdateChart from "./UpdateChart";
import WorkbookOperation from "./WorkbookOperation";
import LayoutAsset from "./LayoutAsset";
import AddText from "./AddText";
import UpdateText from "./UpdateText";
import ExecuteQuery from "./ExecuteQuery";
import SelectAsset from "./SelectAsset";
import ActionMeta from "./ActionMeta";
import ResetTable from "./ResetTable";
import SetCellsFormat from "./SetCellsFormat";
import AddForm from "./AddForm";
import UpdateForm from "./UpdateForm";
import SubmitForm from "./SubmitForm";

class Action extends ActionMeta {
    /* actions defined in protobuf */
    public addChart?: AddChart;
    public addSheet?: AddSheet;
    public addTable?: AddTable;
    public automateTable?: AutomateTable;
    public setCellValue?: SetCellValue;
    public setCellFormula?: SetCellFormula;
    public refreshCellValue?: RefreshCellValue;
    public chartConsult?: ChartConsult;
    public eraseCells?: EraseCells;
    public fillUp?: FillUp;
    public fillRight?: FillRight;
    public fillDown?: FillDown;
    public fillLeft?: FillLeft;
    public insertColumns?: InsertColumns;
    public insertRows?: InsertRows;
    public moveSheet?: MoveSheet;
    public removeAsset?: RemoveAsset;
    public removeColumns?: RemoveColumns;
    public removeRows?: RemoveRows;
    public removeSheet?: RemoveSheet;
    public renameAsset?: RenameAsset;
    public renameSheet?: RenameSheet;
    public transferAsset?: TransferAsset;
    public updateAutomaton?: UpdateAutomaton;
    public updateChart?: UpdateChart;
    public createWorkbook?: WorkbookOperation;
    public openWorkbook?: WorkbookOperation;
    public saveWorkbook?: WorkbookOperation;
    public closeWorkbook?: WorkbookOperation;
    public layoutAsset?: LayoutAsset;
    public addText?: AddText;
    public updateText?: UpdateText;
    public executeQuery?: ExecuteQuery;
    public resetTable?: ResetTable;
    public setCellsFormat?: SetCellsFormat;
    public addForm?: AddForm;
    public updateForm?: UpdateForm;
    public submitForm?: SubmitForm;
    /* extra actions for UI only */
    public selectAsset?: SelectAsset;

    public static deserialize(input: any): Action {
        const action = new Action();
        /* actions defined in protobuf */
        if (typeof input.addChart !== 'undefined') {
            action.addChart = AddChart.deserialize(input.addChart);
        } else if (typeof input.addSheet !== 'undefined') {
            action.addSheet = AddSheet.deserialize(input.addSheet);
        } else if (typeof input.addTable !== 'undefined') {
            action.addTable = AddTable.deserialize(input.addTable);
        } else if (typeof input.automateTable !== 'undefined') {
            action.automateTable = AutomateTable.deserialize(input.automateTable);
        } else if (typeof input.setCellValue !== 'undefined') {
            action.setCellValue = SetCellValue.deserialize(input.setCellValue);
        } else if (typeof input.setCellFormula !== 'undefined') {
            action.setCellFormula = SetCellFormula.deserialize(input.setCellFormula);
        } else if (typeof input.refreshCellValue !== 'undefined') {
            action.refreshCellValue = RefreshCellValue.deserialize(input.refreshCellValue);
        } else if (typeof input.chartConsult !== 'undefined') {
            action.chartConsult = ChartConsult.deserialize(input.chartConsult);
        } else if (typeof input.eraseCells !== 'undefined') {
            action.eraseCells = EraseCells.deserialize(input.eraseCells);
        } else if (typeof input.fillUp !== 'undefined') {
            action.fillUp = FillUp.deserialize(input.fillUp);
        } else if (typeof input.fillRight !== 'undefined') {
            action.fillRight = FillRight.deserialize(input.fillRight);
        } else if (typeof input.fillDown !== 'undefined') {
            action.fillDown = FillDown.deserialize(input.fillDown);
        } else if (typeof input.fillLeft !== 'undefined') {
            action.fillLeft = FillLeft.deserialize(input.fillLeft);
        } else if (typeof input.insertColumns !== 'undefined') {
            action.insertColumns = InsertColumns.deserialize(input.insertColumns);
        } else if (typeof input.insertRows !== 'undefined') {
            action.insertRows = InsertRows.deserialize(input.insertRows);
        } else if (typeof input.moveSheet !== 'undefined') {
            action.moveSheet = MoveSheet.deserialize(input.moveSheet);
        } else if (typeof input.removeAsset !== 'undefined') {
            action.removeAsset = RemoveAsset.deserialize(input.removeAsset);
        } else if (typeof input.removeColumns !== 'undefined') {
            action.removeColumns = RemoveColumns.deserialize(input.removeColumns);
        } else if (typeof input.removeRows !== 'undefined') {
            action.removeRows = RemoveRows.deserialize(input.removeRows);
        } else if (typeof input.removeSheet !== 'undefined') {
            action.removeSheet = RemoveSheet.deserialize(input.removeSheet);
        } else if (typeof input.renameAsset !== 'undefined') {
            action.renameAsset = RenameAsset.deserialize(input.renameAsset);
        } else if (typeof input.renameSheet !== 'undefined') {
            action.renameSheet = RenameSheet.deserialize(input.renameSheet);
        } else if (typeof input.transferAsset !== 'undefined') {
            action.transferAsset = TransferAsset.deserialize(input.transferAsset);
        } else if (typeof input.updateAutomaton !== 'undefined') {
            action.updateAutomaton = UpdateAutomaton.deserialize(input.updateAutomaton);
        } else if (typeof input.updateChart !== 'undefined') {
            action.updateChart = UpdateChart.deserialize(input.updateChart);
        } else if (typeof input.createWorkbook !== 'undefined') {
            action.createWorkbook = WorkbookOperation.deserialize(input.createWorkbook);
        } else if (typeof input.openWorkbook !== 'undefined') {
            action.openWorkbook = WorkbookOperation.deserialize(input.openWorkbook);
        } else if (typeof input.saveWorkbook !== 'undefined') {
            action.saveWorkbook = WorkbookOperation.deserialize(input.saveWorkbook);
        } else if (typeof input.closeWorkbook !== 'undefined') {
            action.closeWorkbook = WorkbookOperation.deserialize(input.closeWorkbook);
        } else if (typeof input.layoutAsset !== 'undefined') {
            action.layoutAsset = LayoutAsset.deserialize(input.layoutAsset);
        } else if (typeof input.addText !== 'undefined') {
            action.addText = AddText.deserialize(input.addText);
        } else if (typeof input.updateText !== 'undefined') {
            action.updateText = UpdateText.deserialize(input.updateText);
        } else if (typeof input.executeQuery !== 'undefined') {
            action.executeQuery = ExecuteQuery.deserialize(input.executeQuery);
        } else if (typeof input.resetTable !== 'undefined') {
            action.resetTable = ResetTable.deserialize(input.resetTable);
        } else if (typeof input.setCellsFormat !== 'undefined') {
            action.setCellsFormat = SetCellsFormat.deserialize(input.setCellsFormat);
        } else if (typeof input.addForm !== 'undefined') {
            action.addForm = AddForm.deserialize(input.addForm);
        } else if (typeof input.updateForm !== 'undefined') {
            action.updateForm = UpdateForm.deserialize(input.updateForm);
        } else if (typeof input.submitForm !== 'undefined') {
            action.submitForm = SubmitForm.deserialize(input.submitForm);
            // } else if (typeof input.selectAsset !== 'undefined') {
            //     action.selectAsset = SelectAsset.deserialize(input.selectAsset);
        } else {
            throw new Error('Illegal action wrapper, no valid action inside.');
        }
        return action;
    }

    public specific(): ActionMeta {
        if (typeof this.addChart !== 'undefined') {
            return this.addChart;
        } else if (typeof this.addSheet !== 'undefined') {
            return this.addSheet;
        } else if (typeof this.addTable !== 'undefined') {
            return this.addTable;
        } else if (typeof this.automateTable !== 'undefined') {
            return this.automateTable;
        } else if (typeof this.setCellValue !== 'undefined') {
            return this.setCellValue;
        } else if (typeof this.setCellFormula !== 'undefined') {
            return this.setCellFormula;
        } else if (typeof this.refreshCellValue !== 'undefined') {
            return this.refreshCellValue;
        } else if (typeof this.chartConsult !== 'undefined') {
            return this.chartConsult;
        } else if (typeof this.eraseCells !== 'undefined') {
            return this.eraseCells;
        } else if (typeof this.fillUp !== 'undefined') {
            return this.fillUp;
        } else if (typeof this.fillRight !== 'undefined') {
            return this.fillRight;
        } else if (typeof this.fillDown !== 'undefined') {
            return this.fillDown;
        } else if (typeof this.fillLeft !== 'undefined') {
            return this.fillLeft;
        } else if (typeof this.insertColumns !== 'undefined') {
            return this.insertColumns;
        } else if (typeof this.insertRows !== 'undefined') {
            return this.insertRows;
        } else if (typeof this.moveSheet !== 'undefined') {
            return this.moveSheet;
        } else if (typeof this.removeAsset !== 'undefined') {
            return this.removeAsset;
        } else if (typeof this.removeColumns !== 'undefined') {
            return this.removeColumns;
        } else if (typeof this.removeRows !== 'undefined') {
            return this.removeRows;
        } else if (typeof this.removeSheet !== 'undefined') {
            return this.removeSheet;
        } else if (typeof this.renameAsset !== 'undefined') {
            return this.renameAsset;
        } else if (typeof this.renameSheet !== 'undefined') {
            return this.renameSheet;
        } else if (typeof this.transferAsset !== 'undefined') {
            return this.transferAsset;
        } else if (typeof this.updateAutomaton !== 'undefined') {
            return this.updateAutomaton;
        } else if (typeof this.updateChart !== 'undefined') {
            return this.updateChart;
        } else if (typeof this.createWorkbook !== 'undefined') {
            return this.createWorkbook;
        } else if (typeof this.openWorkbook !== 'undefined') {
            return this.openWorkbook;
        } else if (typeof this.saveWorkbook !== 'undefined') {
            return this.saveWorkbook;
        } else if (typeof this.closeWorkbook !== 'undefined') {
            return this.closeWorkbook;
        } else if (typeof this.layoutAsset !== 'undefined') {
            return this.layoutAsset;
        } else if (typeof this.addText !== 'undefined') {
            return this.addText;
        } else if (typeof this.updateText !== 'undefined') {
            return this.updateText;
        } else if (typeof this.executeQuery !== 'undefined') {
            return this.executeQuery;
        } else if (typeof this.resetTable !== 'undefined') {
            return this.resetTable;
        } else if (typeof this.setCellsFormat !== 'undefined') {
            return this.setCellsFormat;
        } else if (typeof this.addForm !== 'undefined') {
            return this.addForm;
        } else if (typeof this.updateForm !== 'undefined') {
            return this.updateForm;
        } else if (typeof this.submitForm !== 'undefined') {
            return this.submitForm;
        } else if (typeof this.selectAsset !== 'undefined') {
            return this.selectAsset;
        } else {
            throw new Error('Illegal action wrapper, no valid action inside.');
        }
    }

    public isLocalAction(): boolean {
        return this.specific().isLocalAction();
    }

    public isSheetAction(): boolean {
        return this.specific().isSheetAction();
    }

    public isAssetAction(): boolean {
        return this.specific().isAssetAction();
    }

    public targetSheet(): string | undefined {
        return this.specific().targetSheet();
    }

    public targetAsset(): string | undefined {
        return this.specific().targetAsset();
    }
}

export default Action;
