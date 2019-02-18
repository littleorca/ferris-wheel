import * as React from 'react';
import Table from '../model/Table';
import SharedViewProps from './SharedViewProps';
import { HotTable } from '@handsontable/react';
import UnionValue from '../model/UnionValue';
import { VariantType } from '../model/Variant';
import { ErrorCodeNames } from '../model/Variant';
import NamedValue from '../model/NamedValue';
import Values from '../model/Values';
import QueryAutomaton from '../model/QueryAutomaton';
import { toEditableString, fromEditableString } from '../ctrl/UnionValueEdit';
import EditableText from '../ctrl/EditableText';
import AutoForm from '../form/AutoForm';
import {
    ExecuteQuery, Action, AutomateTable, RenameAsset, RefreshCellValue, SetCellValue, SetCellFormula, InsertRows, RemoveRows, RemoveColumns, InsertColumns, ResetTable
} from '..';
import classnames from "classnames";
import 'handsontable/dist/handsontable.full.css';
import './TableView.css';

interface TableViewProps extends SharedViewProps<TableView> {
    table: Table;
    className?: string;
    style?: React.CSSProperties;
}

type Primitive = string | number | boolean | Date;

interface TableData {
    rows: number;
    columns: number;
    variantMatrix: UnionValue[][];
    hotTableData: Primitive[][];
}

class TableView extends React.Component<TableViewProps>{
    private hotTableRef: React.RefObject<HotTable> = React.createRef();
    private tableData: TableData;

    constructor(props: TableViewProps) {
        super(props);

        this.handleNameChange = this.handleNameChange.bind(this);

        this.getSelectedRange = this.getSelectedRange.bind(this);

        this.shouldDeselectOnOutsideClick = this.shouldDeselectOnOutsideClick.bind(this);

        // this.beforeCut = this.beforeCut.bind(this);
        this.beforePaste = this.beforePaste.bind(this);

        this.beforeCreateRow = this.beforeCreateRow.bind(this);
        this.afterCreateRow = this.afterCreateRow.bind(this);
        this.beforeRemoveRow = this.beforeRemoveRow.bind(this);
        this.afterRemoveRow = this.afterRemoveRow.bind(this);

        this.beforeColumnSort = this.beforeColumnSort.bind(this);
        this.afterColumnSort = this.afterColumnSort.bind(this);
        this.beforeCreateCol = this.beforeCreateCol.bind(this);
        this.afterCreateCol = this.afterCreateCol.bind(this);
        this.beforeRemoveCol = this.beforeRemoveCol.bind(this);
        this.afterRemoveCol = this.afterRemoveCol.bind(this);

        this.afterSetDataAtCell = this.afterSetDataAtCell.bind(this);
        this.beforeValueRender = this.beforeValueRender.bind(this);
        this.modifyData = this.modifyData.bind(this);

        this.afterDeselect = this.afterDeselect.bind(this);
        this.afterSelectionEnd = this.afterSelectionEnd.bind(this);

        this.handleQuery = this.handleQuery.bind(this);
        this.applyAction = this.applyAction.bind(this);

        this.tableData = this.prepareTableData(props.table);
    }

    public componentDidUpdate(prevProps: TableViewProps) {
        // maybe skip update to prevent performance issue.
        if (this.props.table !== prevProps.table) {
            this.tableData = this.prepareTableData(this.props.table);
            // unhook table method
            prevProps.table.getSelectedRange = function () {
                return null;
            };
        }
    }

    public componentDidMount() {
        if (this.hotTableRef.current) {
            this.hotTableRef.current.forceUpdate();
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

    protected handleNameChange(value: string) {
        if (typeof this.props.onAction !== 'undefined') {
            const renameAsset = new RenameAsset('', this.props.table.name, value);
            this.props.onAction(renameAsset.wrapper());
        }
    }

    protected prepareTableData(table: Table): TableData {
        /*
         * Bad code: the following code contains two work around.
         * getSelectedRange: for convenience of get selected cells range from workbook editor;
         *
         * __table_data__: when switch between sheets, table will be redraw, if this.props.table not reloaded, changes will disappear. so here I hacked table object and save extra data to it.
         * 
         * It is supposed to improve in the future!
         */

        // hook table method
        table.getSelectedRange = this.getSelectedRange;

        if (typeof table['__table_data__'] !== 'undefined') {
            return table['__table_data__'];
        }

        let maxRowIdx = -1;
        let maxColIdx = -1;
        const variantMatrix: UnionValue[][] = [];
        const hotTableData: Primitive[][] = [];

        const rows = table.rows;
        for (const row of rows) {
            if (maxRowIdx < row.rowIndex) {
                maxRowIdx = row.rowIndex;
            }
            const cells = row.cells;
            const matrixRow: UnionValue[] = variantMatrix[row.rowIndex] = [];
            const htRow: Primitive[] = hotTableData[row.rowIndex] = [];
            for (const cell of cells) {
                if (maxColIdx < cell.columnIndex) {
                    maxColIdx = cell.columnIndex;
                }
                const value = cell.value;
                matrixRow[cell.columnIndex] = value;
                htRow[cell.columnIndex] = toEditableString(value);
            }
        }

        // fill blank cells, if not, handsontable may not work properly.
        for (let i = 0; i <= maxRowIdx; i++) {
            if (typeof hotTableData[i] === 'undefined') {
                hotTableData[i] = [];
            }
            for (let j = 0; j <= maxColIdx; j++) {
                if (typeof hotTableData[i][j] === 'undefined') {
                    hotTableData[i][j] = '';
                }
            }
        }

        // hotTableData cannot be empty
        if (hotTableData.length === 0) {
            hotTableData.push([]);
        }

        return table['__table_data__'] = {
            rows: maxRowIdx + 1,
            columns: maxColIdx + 1,
            variantMatrix,
            hotTableData
        };
    }


    protected getCellValue(row: number, column: number) {
        const matrixRow = this.tableData.variantMatrix[row];
        if (matrixRow === undefined) {
            return Values.blank();
        }
        const val = matrixRow[column];
        if (val === undefined) {
            return Values.blank();
        }
        return val;
    }

    protected getPlainCellValue(row: number, column: number) {
        const val = this.getCellValue(row, column);
        switch (val.valueType()) {
            case VariantType.BLANK:
                return '';
            case VariantType.BOOL:
                return val.booleanValue();
            case VariantType.DATE:
                return val.toString();
            case VariantType.DECIMAL:
                return val.decimalValue();
            case VariantType.ERROR:
                return ErrorCodeNames[val.errorValue()];
            case VariantType.LIST:
                throw new Error('Table cell cannot hold list value.');
            case VariantType.STRING:
                return val.strValue();
            default:
                throw new Error('Invalid variant type: '
                    + val.valueType());
        }
    }

    protected setCellValue(row: number, column: number, value: UnionValue) {
        let matrixRow = this.tableData.variantMatrix[row];
        if (matrixRow === undefined) {
            matrixRow = this.tableData.variantMatrix[row] = [];
        }
        matrixRow[column] = value;

        if (row >= this.tableData.rows) {
            this.tableData.rows = row + 1;
        }
        if (column >= this.tableData.columns) {
            this.tableData.columns = column + 1;
        }

        // TODO action callback
    }

    protected getSelectedRange() {
        if (this.hotTableRef.current === null) {
            return null;
        }
        const hotInst = this.hotTableRef.current.hotInstance;
        const ranges: any = hotInst.getSelectedRange();
        if (typeof ranges === 'undefined') {
            return null;
        }

        let finalRange = null;
        for (const range of ranges) {
            const rangeElement = {
                left: range.getTopLeftCorner().col,
                top: range.getTopLeftCorner().row,
                right: range.getBottomRightCorner().col,
                bottom: range.getBottomRightCorner().row,
            };

            if (finalRange === null) {
                finalRange = rangeElement;

            } else {
                if (finalRange.left > rangeElement.left) {
                    finalRange.left = rangeElement.left;
                }
                if (finalRange.top > rangeElement.top) {
                    finalRange.top = rangeElement.top;
                }
                if (finalRange.right < rangeElement.right) {
                    finalRange.right = rangeElement.right;
                }
                if (finalRange.bottom < rangeElement.bottom) {
                    finalRange.bottom = rangeElement.bottom;
                }
            }
        }
        return finalRange;
    }

    protected shouldDeselectOnOutsideClick(e: HTMLElement) {
        // TODO this is a hack for preventing handsontable from grabbing cursor
        // unexpectedly. e.g.:
        // select a cell and then double click the table name to rename the table,
        // and the input will be captured by the cell instead of name input box.
        if (e.closest(".workbook-editor .editor-header") !== null ||
            e.closest(".workbook-editor .sidebar") !== null) {
            return false;
        }
        return true;
    }

    // protected beforeCut() {
    //     return false;
    // }

    protected beforePaste() {
        return false;
    }

    protected beforeCreateRow(index: number, amount: number, source?: string) {
        // console.log('beforeCreateRow', index, amount, source);
    }

    protected afterCreateRow(index: number, amount: number) {
        // console.log('afterCreateRow', index, amount);
        // alter table matrix
        const matrix = this.tableData.variantMatrix;
        const tail = matrix.splice(index);
        for (let i = 0; i < tail.length; i++) {
            if (tail[i] !== undefined) {
                matrix[index + amount + i] = tail[i];
            }
        }
        if (index < this.tableData.rows) {
            this.tableData.rows += amount;
        } else {
            this.tableData.rows = index + amount;
        }
        // console.log('altered table: ', this.tableData);
        // TODO action callback
    }

    protected beforeRemoveRow(index: number, amount: number, logicalRows?: any[]) {
        // console.log('beforeRemoveRow', index, amount, logicalRows);
    }

    protected afterRemoveRow(index: number, amount: number) {
        // console.log('afterRemoveRow', index, amount);
        // alter table matrix
        const matrix = this.tableData.variantMatrix;
        matrix.splice(index, amount);
        if (index + amount < this.tableData.rows) {
            this.tableData.rows -= amount;
        } else {
            this.tableData.rows = index;
        }
        // console.log('altered table: ', this.tableData);
        // TODO action callback
    }

    protected beforeColumnSort(currentSortConfig: object[], destinationSortConfigs: object[]) {
        // console.log('beforeColumnSort', currentSortConfig, destinationSortConfigs);
    }

    protected afterColumnSort(currentSortConfig: object[], destinationSortConfigs: object[]) {
        // console.log('afterColumnSort', currentSortConfig, destinationSortConfigs);
        // TODO action callback
    }

    protected beforeCreateCol(index: number, amount: number, source?: string) {
        // console.log('beforeCreateCol', index, amount, source);
    }

    protected afterCreateCol(index: number, amount: number) {
        // console.log('afterCreateCol', index, amount);
        // alter table matrix
        const matrix = this.tableData.variantMatrix;
        for (const row of matrix) {
            if (row === undefined) { // is that possible?
                continue;
            }
            const tail = row.splice(index);
            for (let i = 0; i < tail.length; i++) {
                if (tail[i] !== undefined) {
                    row[index + amount + i] = tail[i];
                }
            }
        }
        if (index < this.tableData.columns) {
            this.tableData.columns += amount;
        } else {
            this.tableData.columns = index + amount;
        }
        // console.log('altered table: ', this.tableData);
        // TODO action callback
    }

    protected beforeRemoveCol(index: number, amount: number, logicalCols?: any[]) {
        // console.log('beforeRemoveCol', index, amount, logicalCols);
    }

    protected afterRemoveCol(index: number, amount: number) {
        // console.log('afterRemoveCol', index, amount);
        // alter table matrix
        const matrix = this.tableData.variantMatrix;
        for (const row of matrix) {
            if (row === undefined) { // is that possible?
                continue;
            }
            row.splice(index, amount);
        }
        if (index + amount < this.tableData.columns) {
            this.tableData.columns -= amount;
        } else {
            this.tableData.columns = index;
        }
        // console.log('altered table: ', this.tableData);
        // TODO action callback
    }

    protected afterSetDataAtCell(changes: any[], source?: string) {
        // console.log('afterSetDataAtCell', changes, source);
        if (source === 'loadData' || source === 'sync') {
            return;
        }
        const hotTable = this.hotTableRef.current;
        if (!hotTable) {
            throw new Error('Table not ready.');
        }

        for (const [row, column, oldValue, newValue] of changes) {
            const physicalColumn = hotTable.hotInstance.propToCol(column);
            const physicalRow = hotTable.hotInstance.toPhysicalRow(row);

            if (oldValue !== newValue) {
                const value = fromEditableString(newValue);
                this.setCellValue(physicalRow, physicalColumn, value);

                if (typeof this.props.onAction !== 'undefined') {
                    if (value.isFormula()) {
                        const setCellFormula = new SetCellFormula('', this.props.table.name,
                            physicalRow, physicalColumn, value.formulaString || ''/* should never be undefined */);
                        this.props.onAction(setCellFormula.wrapper());
                    } else {
                        const setCellValue = new SetCellValue('', this.props.table.name,
                            physicalRow, physicalColumn, value);
                        this.props.onAction(setCellValue.wrapper());
                    }
                }
            }
        }
    }

    protected beforeValueRender(value: any, cellProperties: object) {
        // const str = toEditableString(Values.auto(value));
        // str.toString();
        // console.log('beforeValueRender', value, cellProperties);
        // return "render as what?";
        return value;
    }

    protected afterDeselect() {
        // TODO
    }

    protected afterSelectionEnd(
        r: number,
        c: number,
        r2: number,
        c2: number,
        selectionLayerLevel: number) {
        // TODO
    }

    protected modifyData(row: number, column: number, valueHolder: any, ioMode: string) {
        if (ioMode === 'get') {
            valueHolder.value = this.getPlainCellValue(row, column);
        } else if (ioMode === 'set') {
            const value = fromEditableString(valueHolder.value);
            valueHolder.value = toEditableString(value);
        }
    }

    protected handleQuery(params: NamedValue[]) {
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
            this.applyInsertRows(action.insertRows);
        } else if (typeof action.insertColumns !== 'undefined') {
            this.applyInsertColumns(action.insertColumns);
        } else if (typeof action.removeRows !== 'undefined') {
            this.applyRemoveRows(action.removeRows);
        } else if (typeof action.removeColumns !== 'undefined') {
            this.applyRemoveColumns(action.removeColumns);
        } else if (typeof action.resetTable !== 'undefined') {
            this.applyResetTable(action.resetTable);
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
        if (this.hotTableRef.current !== null) {
            const inst = this.hotTableRef.current.hotInstance;
            /* this will trigger a refresh of table cell */
            inst.setDataAtCell(rowIndex, columnIndex, toEditableString(value), 'sync');
        }
    }

    protected applyInsertRows(insertRows: InsertRows) {
        if (this.hotTableRef.current === null) {
            throw new Error('Table not available.');
        }
        const hTable = this.hotTableRef.current.hotInstance;
        const rowIndex = insertRows.rowIndex;
        const nRows = insertRows.nRows;
        hTable.alter('insert_row', rowIndex, nRows);
    }

    protected applyInsertColumns(insertColumns: InsertColumns) {
        if (this.hotTableRef.current === null) {
            throw new Error('Table not available.');
        }
        const hTable = this.hotTableRef.current.hotInstance;
        const columnIndex = insertColumns.columnIndex;
        const nColumns = insertColumns.nColumns;
        hTable.alter('insert_col', columnIndex, nColumns);
    }

    protected applyRemoveRows(removeRows: RemoveRows) {
        if (this.hotTableRef.current === null) {
            throw new Error('Table not available.');
        }
        const hTable = this.hotTableRef.current.hotInstance;
        const rowIndex = removeRows.rowIndex;
        const nRows = removeRows.nRows;
        hTable.alter('remove_row', rowIndex, nRows);
    }

    protected applyRemoveColumns(removeColumns: RemoveColumns) {
        if (this.hotTableRef.current === null) {
            throw new Error('Table not available.');
        }
        const hTable = this.hotTableRef.current.hotInstance;
        var columnIndex = removeColumns.columnIndex;
        var nColumns = removeColumns.nColumns;
        hTable.alter('remove_col', columnIndex, nColumns);
    }

    protected applyResetTable(resetTable: ResetTable) {
        Object.assign(this.props.table, resetTable.table);
        // remove this hack, or prepareTableData won't work.
        this.props.table['__table_data__'] = undefined;
        this.tableData = this.prepareTableData(this.props.table);
        this.forceUpdate();
    }

    protected mergeParams(out: NamedValue[], queryAutomaton: QueryAutomaton) {
        const paramMap = new Map<string, NamedValue>();
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
        paramMap.forEach((value: NamedValue, key: string, map: Map<string, NamedValue>) => {
            out.push(value);
        });
    }

    public render() {
        const className = classnames(
            "table-view",
            { "editable": this.props.editable },
            this.props.className);

        const data = this.tableData.hotTableData;
        const auto = this.props.table.automaton;
        const params: NamedValue[] = [];
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
                <div className="action automaton">
                    {auto && auto.queryAutomaton && (
                        <AutoForm
                            rules={auto.queryAutomaton.template.userParamRules}
                            params={params}
                            onSumbit={this.handleQuery} />
                    )}
                </div>
                <div className="content">
                    <HotTable
                        ref={this.hotTableRef}
                        data={data}
                        readOnly={readOnly}
                        colHeaders={true}
                        rowHeaders={true}
                        columnSorting={false}
                        // width="auto"
                        stretchH="all"
                        manualColumnResize={true}
                        manualRowResize={true}
                        outsideClickDeselects={this.shouldDeselectOnOutsideClick}
                        fillHandle={{ autoInsertRow: false }}
                        contextMenu={false}
                        copyable={true}
                        copyPaste={true} // paste are disabled by beforePaste hook
                        // afterBeginEdting={this.afterBeginEditing}
                        // beforeChange={this.beforeChange}
                        // afterChange={this.afterChange}
                        afterColumnSort={this.afterColumnSort}
                        afterCreateCol={this.afterCreateCol}
                        afterCreateRow={this.afterCreateRow}
                        afterDeselect={this.afterDeselect}
                        afterRemoveCol={this.afterRemoveCol}
                        afterRemoveRow={this.afterRemoveRow}
                        afterSelectionEnd={this.afterSelectionEnd}
                        afterSetDataAtCell={this.afterSetDataAtCell}
                        afterSetDataAtRowProp={this.afterSetDataAtCell}
                        beforeColumnSort={this.beforeColumnSort}
                        beforeCreateCol={this.beforeCreateCol}
                        beforeCreateRow={this.beforeCreateRow}
                        // beforeCut={this.beforeCut}
                        beforePaste={this.beforePaste}
                        beforeRemoveCol={this.beforeRemoveCol}
                        beforeRemoveRow={this.beforeRemoveRow}
                        beforeValueRender={this.beforeValueRender}
                        modifyData={this.modifyData} />
                </div>
            </div>
        );
    }
}

export default TableView;
export { TableViewProps };