import * as React from 'react';
import * as ReactDOM from "react-dom";
import Table from '../model/Table';
import SharedViewProps from './SharedViewProps';
import { VariantType } from '../model/Variant';
import UnionValue from '../model/UnionValue';
import Parameter from '../model/Parameter';
import Values from '../model/Values';
import QueryAutomaton from '../model/QueryAutomaton';
import UnionValueEdit, { fromEditableString, UnionValueEditMode } from '../ctrl/UnionValueEdit';
import EditableText from '../ctrl/EditableText';
import GridTable, { CustomEditorProps } from '../table/GridTable';
import Cell from '../model/Cell';
import { QueryWizard } from '../extension/Extension';
import Formatter from '../util/Formatter';
import RenameAsset from '../action/RenameAsset';
import InsertRows from '../action/InsertRows';
import RemoveRows from '../action/RemoveRows';
import InsertColumns from '../action/InsertColumns';
import RemoveColumns from '../action/RemoveColumns';
import EraseCells from '../action/EraseCells';
import GridCell from '../model/GridCell';
import SetCellFormula from '../action/SetCellFormula';
import SetCellValue from '../action/SetCellValue';
import ExecuteQuery from '../action/ExecuteQuery';
import Action from '../action/Action';
import AutomateTable from '../action/AutomateTable';
import RefreshCellValue from '../action/RefreshCellValue';
import ResetTable from '../action/ResetTable';
import SetCellsFormat from '../action/SetCellsFormat';
import Layout from '../model/Layout';
import LayoutAsset from '../action/LayoutAsset';
import ValueChange from '../ctrl/ValueChange';
import GroupView, { GroupItem } from './GroupView';
import QueryTemplateForm from '../form/QueryTemplateForm';
import PivotForm from '../form/PivotForm';
import Button from '../ctrl/Button';
import LayoutForm from '../form/LayoutForm';
import QueryTemplate from '../model/QueryTemplate';
import Dialog from './Dialog';
import FormatFormDialog from './FormatFormDialog';
import { ModalProps } from './Modal';
import StillFormEnclosure from '../form/StillFormEnclosure';
import classnames from "classnames";
import './TableView.css';

interface TableViewProps extends SharedViewProps<TableView> {
    table: Table;
    className?: string;
    style?: React.CSSProperties;
}

class TableView extends React.Component<TableViewProps>{
    private readonly VALUE_MODES: UnionValueEditMode[] = ["formula", "decimal", "boolean", "date", "string"];

    private tableRef: React.RefObject<GridTable<Cell>> = React.createRef();
    private queryWizardMap: Map<string, QueryWizard> = new Map();
    private automatonChanged: boolean = false;

    constructor(props: TableViewProps) {
        super(props);

        this.handleNameChange = this.handleNameChange.bind(this);

        this.afterAddRows = this.afterAddRows.bind(this);
        this.afterRemoveRows = this.afterRemoveRows.bind(this);
        this.afterAddColumns = this.afterAddColumns.bind(this);
        this.afterRemoveColumns = this.afterRemoveColumns.bind(this);
        this.afterEraseCells = this.afterEraseCells.bind(this);
        this.afterSetCellValue = this.afterSetCellValue.bind(this);

        // this.shouldDeselectOnOutsideClick = this.shouldDeselectOnOutsideClick.bind(this);

        this.handleAddRowsAbove = this.handleAddRowsAbove.bind(this);
        this.handleAddRowsBelow = this.handleAddRowsBelow.bind(this);
        this.handleRemoveRows = this.handleRemoveRows.bind(this);
        this.handleAddColumnsBefore = this.handleAddColumnsBefore.bind(this);
        this.handleAddColumnsAfter = this.handleAddColumnsAfter.bind(this);
        this.handleRemoveColumns = this.handleRemoveColumns.bind(this);

        this.handleFormatCell = this.handleFormatCell.bind(this);
        this.handleAutomatonChange = this.handleAutomatonChange.bind(this);
        this.handleSubmitAutomatonIfChanged = this.handleSubmitAutomatonIfChanged.bind(this);
        this.handleSubmitAutomatonChange = this.handleSubmitAutomatonChange.bind(this);

        this.handleQuery = this.handleQuery.bind(this);
        this.handleLayoutChange = this.handleLayoutChange.bind(this);

        this.applyAction = this.applyAction.bind(this);

        this.renderUnionValueEditor = this.renderUnionValueEditor.bind(this);

        if (typeof this.props.extensions !== 'undefined') {
            this.props.extensions.forEach((value, index) => {
                if (typeof value.queryWizard !== 'undefined') {
                    const name = 'add-query-table--' + value.queryWizard.name;
                    this.queryWizardMap.set(name, value.queryWizard);
                }
            });
        }

        if (typeof this.props.herald !== 'undefined') {
            this.props.herald.subscribe(this.applyAction);
        }
    }

    public componentWillUnmount() {
        if (typeof this.props.herald !== 'undefined') {
            this.props.herald.unsubscribe(this.applyAction);
        }
    }

    protected getCell(row: number, column: number): Cell | undefined {
        return this.props.table.getCellData(row, column).getData();
    }

    protected getCellValue(row: number, column: number) {
        const cell = this.getCell(row, column);
        if (cell === undefined) {
            return Values.blank();
        }
        return cell.value;
    }

    protected cellValueToDisplayString(cell?: Cell) {
        if (!cell) {
            return "";
        } else {
            return Formatter.format(cell.value, cell.format);
        }
    }

    protected setCell(row: number, column: number, cell: Cell) {
        // const table = this.assertAndGetTable();
        const table = this.props.table;
        const gridCell = table.getCellData(row, column);
        gridCell.setData(cell);
        if (cell.value.valueType() === VariantType.DECIMAL) {
            gridCell.setAlign("right");
        } else {
            gridCell.setAlign("left");
        }
        this.forceUpdate();
    }

    protected setCellValue(row: number, column: number, value: UnionValue) {
        let cell = this.getCell(row, column);
        if (typeof cell !== "undefined") {
            cell.value = value;
        } else {
            cell = new Cell(column, value);
        }
        this.setCell(row, column, cell);

        // TODO action callback
    }

    protected getSelection() {
        const table = this.tableRef.current;
        if (!table) {
            return null;
        }
        return table.getSelection();
    }

    protected handleNameChange(value: string) {
        if (typeof this.props.onAction !== 'undefined') {
            const renameAsset = new RenameAsset('', this.props.table.name, value);
            this.props.onAction(renameAsset.wrapper());
        }
    }

    protected afterAddRows(rowIndex: number, rowCount: number) {
        const insertRows = new InsertRows("", this.props.table.name, rowIndex, rowCount);
        this.handleAction(insertRows.wrapper());
    }

    protected afterRemoveRows(rowIndex: number, rowCount: number) {
        const removeRows = new RemoveRows("", this.props.table.name, rowIndex, rowCount);
        this.handleAction(removeRows.wrapper());
    }

    protected afterAddColumns(columnIndex: number, columnCount: number) {
        const insertColumns = new InsertColumns("", this.props.table.name, columnIndex, columnCount);
        this.handleAction(insertColumns.wrapper());
    }

    protected afterRemoveColumns(columnIndex: number, columnCount: number) {
        const removeColumns = new RemoveColumns("", this.props.table.name, columnIndex, columnCount);
        this.handleAction(removeColumns.wrapper());
    }

    protected afterEraseCells(top: number, right: number, bottom: number, left: number) {
        const eraseCells = new EraseCells("", this.props.table.name,
            top, right, bottom, left);
        this.handleAction(eraseCells.wrapper());
    }

    protected afterSetCellValue(cellData: GridCell<Cell>, rowIndex: number, columnIndex: number) {
        const cell = cellData.getData();
        if (typeof cell === "undefined") {
            return;
        }

        if (cell.value.valueType() === VariantType.DECIMAL) {
            cellData.setAlign("right");
        } else {
            cellData.setAlign("left");
        }

        let action;

        if (cell.value.isFormula()) {
            const setCellFormula = new SetCellFormula("",
                this.props.table.name,
                rowIndex,
                columnIndex,
                cell.value.getFormulaString());
            action = setCellFormula.wrapper();

        } else {
            const setCellValue = new SetCellValue("",
                this.props.table.name,
                rowIndex,
                columnIndex,
                cell.value);
            action = setCellValue.wrapper();
        }

        this.handleAction(action);
    }

    // protected shouldDeselectOnOutsideClick(e: HTMLElement) {
    //     // FIXME this is a hack for preventing handsontable from grabbing cursor
    //     // unexpectedly. e.g.:
    //     // select a cell and then double click the table name to rename the table,
    //     // and the input will be captured by the cell instead of name input box.
    //     if (e.closest(".workbook-editor .editor-header") !== null ||
    //         e.closest(".workbook-editor .sidebar") !== null ||
    //         e.closest(".dialog") !== null) {
    //         return false;
    //     }
    //     return true;
    // }

    protected handleQuery(params: Parameter[]) {
        // console.log('query', params);
        // TODO send action
        if (typeof this.props.onAction !== 'undefined') {
            const action = new ExecuteQuery(
                '',
                this.props.table.name,
                params,
            ).wrapper();
            this.props.onAction(action);
        }
    }

    protected applyAction(action: Action) {
        if (!action.isAssetAction()) {
            return; // should not happen.
        }
        if (action.targetAsset() !== this.props.table.name) {
            return;
        }
        if (typeof action.renameAsset !== 'undefined') {
            this.applyRenameAsset(action.renameAsset);
        } else if (typeof action.automateTable !== 'undefined') {
            this.applyAutomateTable(action.automateTable);
        } else if (typeof action.executeQuery !== 'undefined') {
            this.applyExecuteQuery(action.executeQuery);
        } else if (typeof action.setCellValue !== 'undefined') {
            this.applySetCellValue(action.setCellValue);
        } else if (typeof action.setCellFormula !== 'undefined') {
            this.applySetCellFormula(action.setCellFormula);
        } else if (typeof action.refreshCellValue !== 'undefined') {
            this.applyRefreshCellValue(action.refreshCellValue);
        } else if (typeof action.insertRows !== 'undefined') {
            // this.applyInsertRows(action.insertRows);
        } else if (typeof action.insertColumns !== 'undefined') {
            // this.applyInsertColumns(action.insertColumns);
        } else if (typeof action.removeRows !== 'undefined') {
            // this.applyRemoveRows(action.removeRows);
        } else if (typeof action.removeColumns !== 'undefined') {
            // this.applyRemoveColumns(action.removeColumns);
        } else if (typeof action.eraseCells !== "undefined") {
            // nothing to do
        } else if (typeof action.resetTable !== 'undefined') {
            this.applyResetTable(action.resetTable);
        } else if (typeof action.setCellsFormat !== 'undefined') {
            this.applySetCellsFormat(action.setCellsFormat);
        } else {
            throw new Error('Unrecognized table action: ' + action);
        }
    }

    protected applyRenameAsset(renameAsset: RenameAsset) {
        this.props.table.name = renameAsset.newAssetName;
        this.forceUpdate();
    }

    protected applyAutomateTable(automateTable: AutomateTable) {
        this.props.table.automaton = automateTable.automaton;
        this.forceUpdate();
    }

    protected applyExecuteQuery(executeQuery: ExecuteQuery) {
        if (typeof this.props.table.automaton.queryAutomaton !== 'undefined') {
            this.props.table.automaton.queryAutomaton.params = executeQuery.params;
        }
        this.forceUpdate();
    }

    protected applySetCellValue(setCellValue: SetCellValue) {
        const { rowIndex, columnIndex, value } = setCellValue;
        const oldValue = this.getCellValue(rowIndex, columnIndex);
        if (oldValue.isFormula()) {
            value.formulaString = oldValue.formulaString;
        }
        this.doApplySetCellValue(rowIndex, columnIndex, value);
    }

    protected applySetCellFormula(setCellFormula: SetCellFormula) {
        const { rowIndex, columnIndex, formulaString } = setCellFormula;
        const value = this.getCellValue(rowIndex, columnIndex);
        value.formulaString = formulaString;
        this.doApplySetCellValue(rowIndex, columnIndex, value);
    }

    protected applyRefreshCellValue(refreshCellValue: RefreshCellValue) {
        const { rowIndex, columnIndex, value } = refreshCellValue;
        const oldValue = this.getCellValue(rowIndex, columnIndex);
        if (oldValue.isFormula()) {
            value.formulaString = oldValue.formulaString;
        }
        this.doApplySetCellValue(rowIndex, columnIndex, value);
    }

    protected doApplySetCellValue(rowIndex: number, columnIndex: number, value: UnionValue) {
        this.setCellValue(rowIndex, columnIndex, value);
    }

    protected applyResetTable(resetTable: ResetTable) {
        Object.assign(this.props.table, resetTable.table);
        if (this.tableRef.current) {
            this.tableRef.current.validSelection();
        }
        this.forceUpdate();
    }

    protected applySetCellsFormat(setCellsFormat: SetCellsFormat) {
        const left = setCellsFormat.columnIndex;
        const top = setCellsFormat.rowIndex;
        const right = left + setCellsFormat.nColumns - 1;
        const bottom = top + setCellsFormat.nRows - 1;
        const format = setCellsFormat.format;
        for (let r = top; r <= bottom; r++) {
            for (let c = left; c <= right; c++) {
                let cell = this.getCell(r, c);
                if (typeof cell !== "undefined") {
                    cell.format = format;
                } else {
                    cell = new Cell(c, Values.blank(), format);
                    this.setCell(r, c, cell);
                }
            }
        }
    }

    protected mergeParams(out: Parameter[], queryAutomaton: QueryAutomaton) {
        const paramMap = new Map<string, Parameter>();
        if (typeof queryAutomaton.template !== 'undefined' &&
            typeof queryAutomaton.template.builtinParams !== 'undefined') {
            for (const p of queryAutomaton.template.builtinParams) {
                paramMap.set(p.name, p);
            }
        }
        if (typeof queryAutomaton.params !== 'undefined') {
            for (const p of queryAutomaton.params) {
                paramMap.set(p.name, p);
            }
        }
        paramMap.forEach((value: Parameter, key: string, map: Map<string, Parameter>) => {
            out.push(value);
        });
    }

    protected handleAddRowsAbove() {
        const table = this.tableRef.current;
        if (table) {
            table.addRowsAbove();
        }
    }

    protected handleAddRowsBelow() {
        const table = this.tableRef.current;
        if (table) {
            table.addRowsBelow();
        }
    }

    protected handleRemoveRows() {
        const table = this.tableRef.current;
        if (table) {
            table.removeRows();
        }
    }

    protected handleAddColumnsBefore() {
        const table = this.tableRef.current;
        if (table) {
            table.addColumnsBefore();
        }
    }

    protected handleAddColumnsAfter() {
        const table = this.tableRef.current;
        if (table) {
            table.addColumnsAfter();
        }
    }

    protected handleRemoveColumns() {
        const table = this.tableRef.current;
        if (table) {
            table.removeColumns();
        }
    }

    protected handleFormatCell(format: string) {
        const table = this.props.table;
        const selection = this.getSelection();
        if (selection === null) {
            return;
        }
        this.handleAction(new SetCellsFormat(
            "", // will be filled by SheetView when this action pass through it.
            table.name,
            selection.topRow(),
            selection.leftColumn(),
            selection.rowCount(),
            selection.columnCount(),
            format).wrapper());
    }

    protected handleAutomatonChange() {
        this.automatonChanged = true;
    }

    protected handleSubmitAutomatonIfChanged() {
        if (!this.automatonChanged) {
            return;
        }
        this.automatonChanged = false;
        this.handleSubmitAutomatonChange();
    }

    protected handleSubmitAutomatonChange() {
        const table = this.props.table;
        const automateTable = new AutomateTable("", table.name, table.automaton);
        this.handleAction(automateTable.wrapper());
    }

    protected handleLayoutChange(layout: Layout) {
        const table = this.props.table;
        const layoutAsset = new LayoutAsset("", table.name, layout);
        this.handleAction(layoutAsset.wrapper());
    }

    protected handleAction(action: Action) {
        if (this.props.onAction) {
            this.props.onAction(action);
        }
    }

    public render() {
        const className = classnames(
            "table-view",
            { "editable": this.props.editable },
            this.props.className);

        const auto = this.props.table.automaton;
        const params: Parameter[] = [];
        if (typeof this.props.table.automaton.queryAutomaton !== 'undefined') {
            this.mergeParams(params, this.props.table.automaton.queryAutomaton);
        }

        const readOnly = !this.props.editable ||
            typeof auto.queryAutomaton !== 'undefined' ||
            typeof auto.pivotAutomaton !== 'undefined';

        return (
            <div className={className} style={this.props.style}>
                <div className="title">
                    <h3>
                        <EditableText
                            value={this.props.table.name}
                            readOnly={!this.props.editable}
                            afterChange={this.handleNameChange} />
                    </h3>
                </div>
                {/* <div className="action automaton">
                    {auto && auto.queryAutomaton && (
                        <AutoForm
                            rules={auto.queryAutomaton.template.userParamRules}
                            params={params}
                            onSumbit={this.handleQuery} />
                    )}
                </div> */}
                <div className="content">
                    {(
                        <GridTable
                            ref={this.tableRef}
                            data={this.props.table}
                            editable={!readOnly}
                            forDisplay={this.cellValueToDisplayString}
                            customEditor={this.renderUnionValueEditor}
                            afterAddRows={this.afterAddRows}
                            afterRemoveRows={this.afterRemoveRows}
                            afterAddColumns={this.afterAddColumns}
                            afterRemoveColumns={this.afterRemoveColumns}
                            afterEraseCells={this.afterEraseCells}
                            afterSetCellValue={this.afterSetCellValue} />
                    )}
                </div>
                <>
                    {this.props.controlPortal &&
                        ReactDOM.createPortal(this.renderControl(), this.props.controlPortal)}
                </>
            </div>
        );
    }

    protected renderUnionValueEditor(props: CustomEditorProps<Cell>) {
        const cell = props.data;
        const value = typeof cell !== "undefined" ? cell.value : Values.blank();
        const initialUpdate = typeof props.initialInput === "string" ?
            fromEditableString(props.initialInput) : undefined;

        const afterChange = (change: ValueChange<UnionValue>) => {
            if (change.type === "commit") {
                props.onOk(new Cell(props.columnIndex, change.toValue, cell && cell.format));
            } else if (change.type === "rollback") {
                props.onCancel();
            }
        };

        return (
            <UnionValueEdit
                className="cell-editor"
                style={{
                    boxSizing: "border-box",
                    width: "100%",
                    height: "100%",
                }}
                modes={this.VALUE_MODES}
                value={value}
                initialUpdate={initialUpdate}
                focusByDefault={true}
                afterChange={afterChange}
                afterEndEdit={props.onCancel} />
        );
    }

    protected renderControl() {
        const table = this.props.table;

        const isTableSelected = true; // maybe just spare this, as this pane only be renderred when a table is selected.

        const isTableEditable = typeof table.automaton.queryAutomaton === "undefined" &&
            typeof table.automaton.pivotAutomaton === "undefined";

        return (
            <GroupView
                className="realtime-edit table-option">
                {table.automaton.queryAutomaton && (
                    <GroupItem
                        name="query"
                        title="查询模板">
                        {this.showQueryWizardButtonIfPossible(table.automaton.queryAutomaton.template,
                            this.handleSubmitAutomatonChange)}
                        <StillFormEnclosure
                            noResetButton={true}
                            submitLabel={"立即应用"}
                            onSubmit={this.handleSubmitAutomatonIfChanged}
                            onDestroy={this.handleSubmitAutomatonIfChanged}>
                            <QueryTemplateForm
                                queryTemplate={table.automaton.queryAutomaton.template}
                                afterChange={this.handleAutomatonChange} />
                        </StillFormEnclosure>
                    </GroupItem>
                )}
                {table.automaton.pivotAutomaton && (
                    <GroupItem
                        name="pivot"
                        title="透视表">
                        <StillFormEnclosure
                            noResetButton={true}
                            submitLabel={"立即应用"}
                            onSubmit={this.handleSubmitAutomatonIfChanged}
                            onDestroy={this.handleSubmitAutomatonIfChanged}>
                            <PivotForm
                                pivot={table.automaton.pivotAutomaton}
                                afterChange={this.handleAutomatonChange} />
                        </StillFormEnclosure>
                    </GroupItem>
                )}
                {isTableEditable && (
                    <GroupItem
                        name="alter-table"
                        title="修改表">
                        <div className="alter-table-actions">
                            <div>
                                <Button
                                    name="insert-row"
                                    label="插入行"
                                    tips="在上面插入新行"
                                    disabled={!isTableSelected}
                                    onClick={this.handleAddRowsAbove} />
                                <Button
                                    name="insert-column"
                                    label="插入列"
                                    tips="在前面插入新列"
                                    disabled={!isTableSelected}
                                    onClick={this.handleAddColumnsBefore} />
                            </div>
                            <div>
                                <Button
                                    name="append-row"
                                    label="追加行"
                                    tips="在下面添加行"
                                    disabled={!isTableSelected}
                                    onClick={this.handleAddRowsBelow} />
                                <Button
                                    name="append-column"
                                    label="追加列"
                                    tips="在后面添加列"
                                    disabled={!isTableSelected}
                                    onClick={this.handleAddColumnsAfter} />
                            </div>
                            <div>
                                <Button
                                    name="remove-row"
                                    label="删除行"
                                    tips="删除当前行"
                                    disabled={!isTableSelected}
                                    className="danger"
                                    onClick={this.handleRemoveRows} />
                                <Button
                                    name="remove-column"
                                    label="删除列"
                                    tips="删除当前列"
                                    disabled={!isTableSelected}
                                    className="danger"
                                    onClick={this.handleRemoveColumns} />
                            </div>
                        </div>
                        <div className="alter-table-actions">
                            {this.showFormatCellButtonIfPossible(this.handleFormatCell)}
                        </div>
                    </GroupItem>
                )}
                <GroupItem
                    name="layout"
                    title="布局">
                    <StillFormEnclosure
                        noSubmitButton={true}
                        noResetButton={true}>
                        <LayoutForm
                            layout={table.layout}
                            afterChange={this.handleLayoutChange} />
                    </StillFormEnclosure>
                </GroupItem>
            </GroupView>
        );
    }

    protected showQueryWizardButtonIfPossible(queryTemplate: QueryTemplate, handleAutomatonChange: () => void) {
        if (typeof this.props.extensions === "undefined") {
            return;
        }
        let wizard: QueryWizard | undefined/* = undefined*/;
        for (const extension of this.props.extensions) {
            if (typeof extension.queryWizard === "undefined") {
                continue;
            }
            if (extension.queryWizard.accepts(queryTemplate)) {
                wizard = extension.queryWizard;
                break;
            }
        }
        if (wizard === undefined) {
            return;
        }
        const nonNullWizard = wizard;
        const wizardRenderer = (props: ModalProps) => {
            const handleOk = (result: QueryTemplate) => {
                props.close();
                Object.assign(queryTemplate, result);
                handleAutomatonChange();
            };
            return <nonNullWizard.component
                initialQueryTemplate={queryTemplate}
                onOk={handleOk}
                onCancel={props.close} />;
        };
        const openWizard = () => {
            Dialog.show(wizardRenderer);
        }
        return (
            <div className="query-wizard-actions">
                <Button
                    name="open-query-wizard"
                    label="使用向导编辑"
                    tips="使用查询器向导编辑该查询"
                    onClick={openWizard} />
            </div>
        );
    }

    protected showFormatCellButtonIfPossible(submitCallback: (format: string) => void) {
        // TODO check if cell(s) selected
        const openFormatForm = () => {
            // if (typeof this.state.currentSheet === 'undefined') {
            //     return;
            // }
            // const sheet = this.state.currentSheet;
            const formatSet = new Set<string>();
            const table = this.tableRef.current;
            const selection = table ? table.getSelection() : null;
            if (selection !== null) {
                for (let r = selection.topRow(); r <= selection.bottomRow(); r++) {
                    for (let c = selection.leftColumn(); c <= selection.rightColumn(); c++) {
                        const cell = this.getCell(r, c);
                        if (typeof cell === "undefined") {
                            continue;
                        }
                        formatSet.add(cell.format);
                    }
                }
            }
            if (formatSet.size > 1) {
                Dialog.confirm(
                    "所选区域存在多种格式，是否继续以统一设置新的格式？",
                    () => {
                        FormatFormDialog.show("", submitCallback);
                    });
            } else {
                FormatFormDialog.show(formatSet.values().next().value, submitCallback);
            }
        }
        return (
            <div className="cell-format-actions">
                <Button
                    name="format-cell"
                    label="单元格格式"
                    tips="设置单元格格式"
                    onClick={openFormatForm} />
            </div>
        );
    }

    // protected assertAndGetTable() {
    //     const table = this.tableRef.current;
    //     if (!table) {
    //         throw new Error("Table is not available.");
    //     }
    //     return table;
    // }
}

export default TableView;
export { TableViewProps };
