import * as React from 'react';
import WorkbookView from './WorkbookView';
import Workbook from '../model/Workbook';
import Sheet from '../model/Sheet';
import SheetAsset from '../model/SheetAsset';
import Chart from '../model/Chart';
import Table from '../model/Table';
import Row from '../model/Row';
import Cell from '../model/Cell';
import Text from '../model/Text';
import Values from '../model/Values';
import Layout from '../model/Layout';
import TableAutomaton from '../model/TableAutomaton';
import PivotAutomaton from '../model/PivotAutomaton';
import QueryAutomaton from '../model/QueryAutomaton';
import QueryTemplate from '../model/QueryTemplate';
import Stacking from '../model/Stacking';
import Toolbar, { Group } from '../ctrl/Toolbar';
import DropdownButton from '../ctrl/DropdownButton';
import Button, { ButtonProps } from '../ctrl/Button'; 
import LayoutForm from '../form/LayoutForm';
import QueryTemplateForm from '../form/QueryTemplateForm';
import ChartForm from '../form/ChartForm';
import PivotForm from '../form/PivotForm';
import GroupView, { GroupItem } from './GroupView';
import FormatFormDialog from './FormatFormDialog';
import Action from '../action/Action';
import ActionHandler from '../action/ActionHandler';
import ActionHerald from '../action/ActionHerald';
import Service from '../action/Service';
import EditRequest from '../action/EditRequest';
import EditResponse from '../action/EditResponse';
import RemoveAsset from '../action/RemoveAsset';
import RemoveSheet from '../action/RemoveSheet';
import AutomateTable from '../action/AutomateTable';
import LayoutAsset from '../action/LayoutAsset';
import UpdateChart from '../action/UpdateChart';
import AddTable from '../action/AddTable';
import AddChart from '../action/AddChart';
import AddText from '../action/AddText';
import InsertRows from '../action/InsertRows';
import InsertColumns from '../action/InsertColumns';
import RemoveColumns from '../action/RemoveColumns';
import RemoveRows from '../action/RemoveRows';
import SetCellsFormat from '../action/SetCellsFormat';
import WorkbookOperation from '../action/WorkbookOperation';
import Extension, { QueryWizard } from '../extension/Extension';
import Dialog from './Dialog';
import Loading from 'react-loading';
import * as ReactModal from 'react-modal';
import classnames from "classnames";
import './WorkbookEditor.css';

interface WorkbookEditorProps extends React.ClassAttributes<WorkbookEditor> {
    workbook: Workbook;
    service: Service;
    className?: string;
    extensions?: Extension[];
}

interface WorkbookEditorState {
    txId: number;
    currentSheet?: Sheet;
    currentAsset?: SheetAsset;
    clipBoard?: Sheet | SheetAsset;
    showSidebar: boolean;
    message: string;
    serviceStatus: string;
    showMask: boolean;
    dialog?: React.ReactNode;
}

class WorkbookEditor extends React.Component<WorkbookEditorProps, WorkbookEditorState> implements ActionHerald {
    private listeners: Set<ActionHandler> = new Set();
    private queryWizardButtons = new Array<ButtonProps>();
    private queryWizardMap: Map<string, QueryWizard> = new Map();

    constructor(props: WorkbookEditorProps) {
        super(props);

        this.state = this.createInitialState(props);

        this.handleSave = this.handleSave.bind(this);

        this.handleCopy = this.handleCopy.bind(this);
        this.handlePaste = this.handlePaste.bind(this);
        this.handleRemove = this.handleRemove.bind(this);

        this.handleAddTable = this.handleAddTable.bind(this);
        this.handleOpenQueryWizard = this.handleOpenQueryWizard.bind(this);
        this.handleQueryWizardOk = this.handleQueryWizardOk.bind(this);
        this.handleQueryWizardCancelled = this.handleQueryWizardCancelled.bind(this);
        this.handleAddQueryTable = this.handleAddQueryTable.bind(this);
        this.handleAddPivotTable = this.handleAddPivotTable.bind(this);
        this.handleAlterTable = this.handleAlterTable.bind(this);
        this.handleFormatCell = this.handleFormatCell.bind(this);

        this.handleAddLineChart = this.handleAddLineChart.bind(this);
        this.handleAddStackedLineChart = this.handleAddStackedLineChart.bind(this);
        this.handleAddBarChart = this.handleAddBarChart.bind(this);
        this.handleAddStackedBarChart = this.handleAddStackedBarChart.bind(this);
        this.handleAddPieChart = this.handleAddPieChart.bind(this);
        this.handleAddDoughnutChart = this.handleAddDoughnutChart.bind(this);
        this.handleAddGaugeChart = this.handleAddGaugeChart.bind(this);
        this.handleAddRadarChart = this.handleAddRadarChart.bind(this);
        this.handleAddScatterChart = this.handleAddScatterChart.bind(this);
        this.handleAddBubbleChart = this.handleAddBubbleChart.bind(this);

        this.handleAddText = this.handleAddText.bind(this);

        this.handleToggleSidebar = this.handleToggleSidebar.bind(this);

        this.handleAction = this.handleAction.bind(this);
        this.onServiceOk = this.onServiceOk.bind(this);
        this.onServiceError = this.onServiceError.bind(this);
        this.preventSubmit = this.preventSubmit.bind(this);

        if(typeof this.props.extensions !== 'undefined') {
            this.props.extensions.forEach((value, index) => {
                if (typeof value.queryWizard !== 'undefined') {
                    const name = 'add-query-table--' + value.queryWizard.name;
                    this.queryWizardButtons.push({
                        name,
                        label: value.queryWizard.title,
                        tips: value.queryWizard.description,
                        onClick: this.handleOpenQueryWizard
                    });
                    this.queryWizardMap.set(name, value.queryWizard);
                }
            });
        }

        if (this.queryWizardButtons.length > 0) {
            this.queryWizardButtons.push({
                name: DropdownButton.SEPARATOR,
            });
        }
    }

    protected createInitialState(props: WorkbookEditorProps): WorkbookEditorState {
        return {
            txId: 0,
            showSidebar: true,
            message: "编辑文档",
            serviceStatus: "就绪",
            showMask: false,
        };
    }

    public subscribe(handler: ActionHandler) {
        this.listeners.add(handler);
    }

    public unsubscribe(handler: ActionHandler) {
        this.listeners.delete(handler);
    }

    protected handleSave() {
        const action = new WorkbookOperation('').wrapAsSaveWorkbook();
        this.handleAction(action);
    }

    protected handleCopy() {
        if (typeof this.state.currentSheet === 'undefined') {
            return; // not possible
        }
        if (typeof this.state.currentAsset !== 'undefined') {
            // copy asset
            this.setState({
                clipBoard: SheetAsset.deserialize(this.state.currentAsset)
            });

        } else {
            // copy sheet
            this.setState({
                clipBoard: Sheet.deserialize(this.state.currentSheet)
            });
        }
    }

    protected handlePaste() {
        if (this.state.clipBoard instanceof Sheet) {
            // console.log('will paste sheet');
            // const addSheet=new AddSheet();

        } else if (this.state.clipBoard instanceof SheetAsset) {
            const sheet = this.state.currentSheet;
            if (typeof sheet === 'undefined') {
                return; // unable to paste
            }

            const specificAsset = this.state.clipBoard.specific();
            let action: Action | null = null;

            if (specificAsset instanceof Chart) {
                const chart = Chart.deserialize(specificAsset); // copy
                const addChart = new AddChart(sheet.name, chart);
                chart.name = this.newAssetName(sheet, chart.name + ' (copy)');
                action = addChart.wrapper();

            } else if (specificAsset instanceof Table) {
                const table = Table.deserialize(specificAsset); // copy
                const addTable = new AddTable(sheet.name, table);
                table.name = this.newAssetName(sheet, table.name + ' (copy)');
                action = addTable.wrapper();

            } else if (specificAsset instanceof Text) {
                const text = Text.deserialize(specificAsset); // copy
                const addText = new AddText(sheet.name, text);
                text.name = this.newAssetName(sheet, text.name + ' (copy)');
                action = addText.wrapper();
            }

            if (action !== null) {
                this.handleAction(action);
            }
        }
    }

    protected handleRemove() {
        if (typeof this.state.currentSheet !== 'undefined' &&
            typeof this.state.currentAsset !== 'undefined') {
            const removeAsset = new RemoveAsset(
                this.state.currentSheet.name,
                this.state.currentAsset.specific().name);
            this.handleAction(removeAsset.wrapper());

        } else if (typeof this.state.currentSheet !== 'undefined') {
            const removeSheet = new RemoveSheet(this.state.currentSheet.name);
            this.handleAction(removeSheet.wrapper());
        }
    }

    protected handleAddTable() {
        if (typeof this.state.currentSheet === 'undefined') {
            throw new Error('Illegal state, current sheet is not defined!');
        }
        const tableName = this.newAssetName(this.state.currentSheet, 'table');
        const table = new Table(tableName, [new Row(0, [new Cell(0, Values.blank())])]);
        const addTable = new AddTable(this.state.currentSheet.name, table);
        this.handleAction(addTable.wrapper());
    }

    protected handleOpenQueryWizard(name: string, event: React.MouseEvent<HTMLButtonElement>) {
        const wizard = this.queryWizardMap.get(name);
        if (typeof wizard === 'undefined') {
            throw new Error('Wizard component not found: ' + name); // TODO review
        }
        this.showDialog(
            <wizard.component
                onOk={this.handleQueryWizardOk}
                onCancel={this.handleQueryWizardCancelled} />
        );
    }

    protected handleQueryWizardOk(queryTemplate: QueryTemplate) {
        this.doAddQueryTable(queryTemplate);
        this.closeDialog();
    }

    protected handleQueryWizardCancelled() {
        this.closeDialog();
    }

    protected handleAddQueryTable() {
        this.doAddQueryTable();
    }

    protected doAddQueryTable(queryTemplate?: QueryTemplate) {
        if (typeof this.state.currentSheet === 'undefined') {
            throw new Error('Illegal state, current sheet is not defined!');
        }
        const tableName = this.newAssetName(this.state.currentSheet, 'table');
        const queryAutomaton = new QueryAutomaton(queryTemplate);
        const table = new Table(tableName, [], new TableAutomaton(queryAutomaton));
        const addTable = new AddTable(this.state.currentSheet.name, table);
        this.handleAction(addTable.wrapper());
    }

    protected handleAddPivotTable() {
        if (typeof this.state.currentSheet === 'undefined') {
            throw new Error('Illegal state, current sheet is not defined!');
        }
        const tableName = this.newAssetName(this.state.currentSheet, 'table');
        const pivotAutomaton = new PivotAutomaton();
        const table = new Table(tableName, [], new TableAutomaton(undefined, pivotAutomaton));
        const addTable = new AddTable(this.state.currentSheet.name, table);
        this.handleAction(addTable.wrapper());
    }

    protected handleAlterTable(buttonName: string) {
        if (typeof this.state.currentSheet === 'undefined') {
            return;
        }
        const sheet = this.state.currentSheet;
        if (typeof this.state.currentAsset === 'undefined' ||
            this.state.currentAsset.assetType() !== 'table') {
            return;
        }
        const table = this.state.currentAsset.specific() as Table;
        const selection = table.getSelectedRange();
        if (selection === null) {
            return;
        }

        let action: Action | null = null;
        switch (buttonName) {
            case 'insert-row':
                action = new InsertRows(sheet.name, table.name, selection.top, 1).wrapper();
                break;
            case 'insert-column':
                action = new InsertColumns(sheet.name, table.name, selection.left, 1).wrapper();
                break;
            case 'append-row':
                action = new InsertRows(sheet.name, table.name, selection.bottom + 1, 1).wrapper();
                break;
            case 'append-column':
                action = new InsertColumns(sheet.name, table.name, selection.right + 1, 1).wrapper();
                break;
            case 'remove-row':
                action = new RemoveRows(sheet.name, table.name, selection.top, selection.bottom - selection.top + 1).wrapper();
                break;
            case 'remove-column':
                action = new RemoveColumns(sheet.name, table.name, selection.left, selection.right - selection.left + 1).wrapper();
                break;
        }

        if (action !== null) {
            this.handleAction(action);
        }
    }

    protected handleFormatCell(format: string) {
        if (typeof this.state.currentSheet === 'undefined') {
            return;
        }
        const sheet = this.state.currentSheet;
        if (typeof this.state.currentAsset === 'undefined' ||
            this.state.currentAsset.assetType() !== 'table') {
            return;
        }
        const table = this.state.currentAsset.specific() as Table;
        const selection = table.getSelectedRange();
        if (selection === null) {
            return;
        }
        this.handleAction(new SetCellsFormat(
            sheet.name,
            table.name,
            selection.top,
            selection.left,
            selection.bottom - selection.top + 1,
            selection.right - selection.left + 1,
            format).wrapper());
    }

    protected handleAddLineChart() {
        const addChart = this.createAddChartAction('Line');
        this.handleAction(addChart.wrapper());
    }

    protected handleAddStackedLineChart() {
        const addChart = this.createAddChartAction('Line');
        addChart.chart.yAxis.stacking = Stacking.ABSOLUTE;
        this.handleAction(addChart.wrapper());
    }

    protected handleAddBarChart() {
        const addChart = this.createAddChartAction('Bar');
        this.handleAction(addChart.wrapper());
    }

    protected handleAddStackedBarChart() {
        const addChart = this.createAddChartAction('Bar');
        addChart.chart.yAxis.stacking = Stacking.ABSOLUTE;
        this.handleAction(addChart.wrapper());
    }

    protected handleAddPieChart() {
        const addChart = this.createAddChartAction('Pie');
        this.handleAction(addChart.wrapper());
    }

    protected handleAddDoughnutChart() {
        const addChart = this.createAddChartAction('Doughnut');
        this.handleAction(addChart.wrapper());
    }

    protected handleAddGaugeChart() {
        const addChart = this.createAddChartAction('Gauge');
        this.handleAction(addChart.wrapper());
    }

    protected handleAddRadarChart() {
        const addChart = this.createAddChartAction('Radar');
        this.handleAction(addChart.wrapper());
    }

    protected handleAddScatterChart() {
        const addChart = this.createAddChartAction('Scatter');
        this.handleAction(addChart.wrapper());
    }

    protected handleAddBubbleChart() {
        const addChart = this.createAddChartAction('Bubble');
        this.handleAction(addChart.wrapper());
    }

    protected createAddChartAction(type: string): AddChart {
        if (typeof this.state.currentSheet === 'undefined') {
            throw new Error('Unable to create chart as current sheet is not defined!');
        }
        const chartName = this.newAssetName(this.state.currentSheet, 'chart');
        const chart = new Chart(chartName, type, Values.str(chartName));
        return new AddChart(this.state.currentSheet.name, chart);
    }

    protected handleAddText() {
        if (typeof this.state.currentSheet === 'undefined') {
            throw new Error('Unable to create text as current sheet is not defined!');
        }
        const textName = this.newAssetName(this.state.currentSheet, 'text');
        const text = new Text(textName, Values.blank());
        const addText = new AddText(this.state.currentSheet.name, text);
        this.handleAction(addText.wrapper());
    }

    protected handleToggleSidebar() {
        this.setState({
            showSidebar: !this.state.showSidebar,
        });
    }

    protected handleAction(action: Action) {
        // console.log('handleAction', action);
        if (action.isLocalAction()) {
            this.handleLocalAction(action);
        } else {
            this.handleRemoteAction(action);
        }
    }

    protected handleLocalAction(action: Action) {
        if (typeof action.selectAsset !== 'undefined') {
            this.setState({
                currentSheet: action.selectAsset.sheet,
                currentAsset: action.selectAsset.asset,
            });
        }
    }

    protected handleRemoteAction(action: Action) {
        const request = new EditRequest(this.state.txId + 1, action);
        this.setState({
            txId: request.txId,
            message: `服务请求中，txId=${request.txId}…`,
            serviceStatus: "忙碌…",
            showMask: true,
        });
        this.props.service.call(
            request,
            this.onServiceOk,
            this.onServiceError,
        );
    }

    protected onServiceOk(resp: EditResponse) {
        // make sure resp is an EditResponse instance.
        resp = EditResponse.deserialize(resp);
        if (resp.statusCode === 0) {
            if (typeof resp.changes !== 'undefined') {
                this.applyChanges(resp.changes.actions);
            }
            this.setState({
                txId: resp.txId,
                message: `服务成功，txId=${resp.txId}, ${resp.message}`,
                serviceStatus: "就绪",
                showMask: false,
            });

        } else {
            this.setState({
                txId: resp.txId,
                message: `服务失败，txId=${resp.txId}, status=${resp.statusCode}, ${resp.message}`,
                serviceStatus: "就绪",
                showMask: false,
            });
        }
    }

    protected applyChanges(actions: Action[]) {
        for (const action of actions) {
            this.listeners.forEach(handler => handler(action));
        }
    }

    protected onServiceError() {
        this.setState({
            message: "服务请求出现异常！",
            serviceStatus: "出错！",
            showMask: false,
        });
    }

    protected newAssetName(sheet: Sheet, prefix: string) {
        let i = 0;
        while (true) {
            const name = prefix + (i === 0 ? "" : " " + i);
            let unique = true;
            for (const asset of sheet.assets) {
                if (asset.specific().name === name) {
                    unique = false;
                    break;
                }
            }
            if (unique) {
                return name;
            } else {
                i++;
            }
        }
    }

    protected preventSubmit(event: React.FormEvent<HTMLFormElement>) {
        event.preventDefault();
    }

    public render() {
        const className = classnames("workbook-editor", this.props.className);

        let contextInfo = "工作簿";
        if (typeof this.state.currentSheet !== 'undefined') {
            contextInfo += `：${this.state.currentSheet.name}`;
        }
        if (typeof this.state.currentAsset !== 'undefined') {
            contextInfo += `：${this.state.currentAsset.specific().name}`;
        }

        return (
            <div
                className={className}>
                <div className="editor-header">
                    {this.renderToolbar()}
                </div>
                <div className="editor-body">
                    <WorkbookView
                        workbook={this.props.workbook}
                        editable={true}
                        onAction={this.handleAction}
                        herald={this} />
                    {this.state.showSidebar && (
                        <div className="sidebar">
                            {this.renderSideContent()}
                        </div>
                    )}
                </div>
                <div className="editor-footer">
                    <span className="editor-footer-item message">
                        {this.state.message}
                    </span>
                    <span
                        className="editor-footer-item context"
                        title={contextInfo}>
                        {contextInfo}
                    </span>
                    <span className="editor-footer-item status">
                        {this.state.serviceStatus}
                    </span>
                </div>
                {this.state.showMask && (
                    <div className="editor-mask">
                        <Loading
                            className="workbook-loading"
                            type="spinningBubbles"
                            color="rgba(153, 153, 153, .9)"
                            width="8rem"
                            height="8rem" />
                    </div>
                )}
                {this.state.dialog && (
                    <ReactModal
                        isOpen={true} 
                        ariaHideApp={false}
                        style={{
                            content: {
                                top: "50%",
                                left: "50%",
                                right: "auto",
                                bottom: "auto",
                                marginRight: "-50%",
                                transform: "translate(-50%, -50%)"
                            },
                            overlay: { zIndex: 9999 }
                        }}>
                        {this.state.dialog}
                    </ReactModal>
                )}
            </div>
        );
    }

    protected renderToolbar() {
        return (
            <Toolbar>
                <Group>
                    <Button
                        name="save"
                        label="保存"
                        className="primary"
                        onClick={this.handleSave} />
                </Group>
                <Group>
                    <Button
                        name="copy"
                        label="复制"
                        onClick={this.handleCopy} />
                    <Button
                        name="paste"
                        label="粘贴"
                        disabled={typeof this.state.clipBoard === 'undefined'}
                        onClick={this.handlePaste} />
                    <Button
                        name="remove"
                        label="删除"
                        onClick={this.handleRemove} />
                </Group>
                <Group>
                    <Button
                        name="add-table"
                        label="表格"
                        tips="新建空白表格"
                        onClick={this.handleAddTable} />
                    <DropdownButton
                        primary={{
                            name: "add-query-table",
                            label: "查询",
                            tips: "新建基于查询器的自动化表格"
                        }}
                        items={[...this.queryWizardButtons, {
                            name: "add-query-table-manually",
                            label: "手工配置查询",
                            tips: "新建基于手工配置查询器的自动化表格",
                            onClick: this.handleAddQueryTable
                        }]} />
                    <Button
                        name="add-pivot-table"
                        label="透视"
                        tips="新建透视表格"
                        onClick={this.handleAddPivotTable} />
                </Group>
                <Group>
                    <DropdownButton
                        primary={{
                            name: 'add-line-chart',
                            label: "线图",
                            tips: "新建折线图",
                            onClick: this.handleAddLineChart
                        }}
                        items={[{
                            name: "add-stacked-line-chart",
                            label: "堆叠折线图",
                            tips: "新建堆叠折线图",
                            onClick: this.handleAddStackedLineChart
                        }]} />
                    <DropdownButton
                        primary={{
                            name: 'add-bar-chart',
                            label: "柱图",
                            tips: "新建柱图",
                            onClick: this.handleAddBarChart
                        }}
                        items={[{
                            name: "add-stacked-bar-chart",
                            label: "堆叠柱图",
                            tips: "新建堆叠柱图",
                            onClick: this.handleAddStackedBarChart
                        }]} />
                    <DropdownButton
                        primary={{
                            name: 'add-pie-chart',
                            label: "饼图",
                            tips: "新建饼图",
                            onClick: this.handleAddPieChart
                        }}
                        items={[{
                            name: "add-doughnut-chart",
                            label: "环形饼图",
                            tips: "新建环形饼图",
                            onClick: this.handleAddDoughnutChart
                        }]} />
                    <DropdownButton
                        primary={{
                            name: 'add-other-chart',
                            label: "更多",
                            tips: "更多其它图表类型",
                        }}
                        items={[{
                            name: 'add-gauge-chart',
                            label: "计量表",
                            tips: "新建计量表",
                            onClick: this.handleAddGaugeChart
                        }, {
                            name: 'add-radar-chart',
                            label: "雷达图",
                            tips: "新建雷达图",
                            onClick: this.handleAddRadarChart
                        }, {
                            name: 'add-scatter-chart',
                            label: "散点图",
                            tips: "新建散点图",
                            onClick: this.handleAddScatterChart
                        }, {
                            name: "add-bubble-chart",
                            label: "气泡图",
                            tips: "新建气泡图",
                            onClick: this.handleAddBubbleChart
                        }]} />
                </Group>
                <Group>
                    <Button
                        name="add-text"
                        label="文本"
                        tips="添加文本块"
                        onClick={this.handleAddText} />
                </Group>
                <Group>
                    <Button
                        name="toggle-sidebar"
                        label="边栏"
                        tips="显示/隐藏边栏"
                        onClick={this.handleToggleSidebar} />
                </Group>
            </Toolbar>
        );
    }

    protected renderSideContent() {
        if (typeof this.state.currentSheet !== 'undefined') {
            if (typeof this.state.currentAsset !== 'undefined') {
                const assetType = this.state.currentAsset.assetType();
                const asset = this.state.currentAsset.specific();
                switch (assetType) {
                    case 'table':
                        return this.renderTableOption(asset as Table);
                    case 'chart':
                        return this.renderChartOption(asset as Chart);
                    case 'text':
                        return this.renderTextOption(asset as Text);
                    default:
                        throw new Error('Invalid asset type!');
                }

            } else {
                return this.renderSheetOption(this.state.currentSheet);
            }

        } else {
            return (
                <div className="realtime-edit" />
            );
        }
    }

    protected renderSheetOption(sheet: Sheet) {
        const handleLayoutChange = (layout: Layout) => {
            // TODO update sheet layout
        };
        return (
            <GroupView className="realtime-edit sheet-option">
                <GroupItem
                    name="layout"
                    title="布局">
                    <form onSubmit={this.preventSubmit}>
                        <LayoutForm
                            layout={sheet.layout}
                            afterChange={handleLayoutChange} />
                    </form>
                </GroupItem>
            </GroupView>
        );
    }

    protected renderTableOption(table: Table) {
        const sheet = this.state.currentSheet;
        if (typeof sheet === 'undefined') {
            throw new Error('Current sheet is undefined.');
        }

        const handleAutomatonChange = () => {
            const automateTable = new AutomateTable(sheet.name, table.name, table.automaton);
            this.handleAction(automateTable.wrapper());
        };

        const handleLayoutChange = (layout: Layout) => {
            const layoutAsset = new LayoutAsset(sheet.name, table.name, layout);
            this.handleAction(layoutAsset.wrapper());
        };

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
                        {this.showQueryWizardButtonIfPossible(table.automaton.queryAutomaton.template, handleAutomatonChange)}
                        <form onSubmit={this.preventSubmit}>
                            <QueryTemplateForm
                                queryTemplate={table.automaton.queryAutomaton.template}
                                afterChange={handleAutomatonChange} />
                        </form>
                    </GroupItem>
                )}
                {table.automaton.pivotAutomaton && (
                    <GroupItem
                        name="pivot"
                        title="透视表">
                        <form onSubmit={this.preventSubmit}>
                            <PivotForm
                                pivot={table.automaton.pivotAutomaton}
                                afterChange={handleAutomatonChange} />
                        </form>
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
                                    tips="在当前行之前插入新行"
                                    disabled={!isTableSelected}
                                    onClick={this.handleAlterTable} />
                                <Button
                                    name="insert-column"
                                    label="插入列"
                                    tips="在当前列之前插入新的列"
                                    disabled={!isTableSelected}
                                    onClick={this.handleAlterTable} />
                            </div>
                            <div>
                                <Button
                                    name="append-row"
                                    label="追加行"
                                    tips="在当前行后追加新行"
                                    disabled={!isTableSelected}
                                    onClick={this.handleAlterTable} />
                                <Button
                                    name="append-column"
                                    label="追加列"
                                    tips="在当前列后追加新列"
                                    disabled={!isTableSelected}
                                    onClick={this.handleAlterTable} />
                            </div>
                            <div>
                                <Button
                                    name="remove-row"
                                    label="删除行"
                                    tips="删除当前行"
                                    disabled={!isTableSelected}
                                    className="danger"
                                    onClick={this.handleAlterTable} />
                                <Button
                                    name="remove-column"
                                    label="删除列"
                                    tips="删除当前列"
                                    disabled={!isTableSelected}
                                    className="danger"
                                    onClick={this.handleAlterTable} />
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
                    <form onSubmit={this.preventSubmit}>
                        <LayoutForm
                            layout={table.layout}
                            afterChange={handleLayoutChange} />
                    </form>
                </GroupItem>
            </GroupView>
        );
    }

    protected showQueryWizardButtonIfPossible(queryTemplate: QueryTemplate, handleAutomatonChange: () => void) {
        if (typeof this.props.extensions === "undefined") {
            return;
        }
        let wizard: QueryWizard | undefined = undefined;
        for (let i = 0; i < this.props.extensions.length; i++) {
            const extension = this.props.extensions[i];
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
        const openWizard = () => {
            this.setState({
                dialog: <nonNullWizard.component
                    initialQueryTemplate={queryTemplate}
                    onOk={(result) => {
                        this.closeDialog();
                        Object.assign(queryTemplate, result);
                        handleAutomatonChange();
                    }}
                    onCancel={this.handleQueryWizardCancelled} />
            });
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
            let formatSet = new Set<string>();
            if (typeof this.state.currentAsset !== 'undefined' &&
                this.state.currentAsset.assetType() === 'table') {
                const table = this.state.currentAsset.specific() as Table;
                const selection = table.getSelectedRange();
                if (selection !== null) {
                    for (let r = selection.top; r <= selection.bottom; r++) {
                        for (let c = selection.left; c <= selection.right; c++) {
                            const cell = selection.cellMatrix[r][c];
                            if (typeof cell === "undefined") {
                                continue;
                            }
                            formatSet.add(cell.format);
                        }
                    }
                }
            }
            const showFormatDialog = (format: string)=> {
                this.setState({
                    dialog: <FormatFormDialog
                        format={format}
                        onCancel={() => this.closeDialog()}
                        onOk={format => {
                            this.closeDialog();
                            submitCallback(format);
                        }} />
                });    
            }
            if (formatSet.size > 1) {
                this.confirm(
                        "所选区域存在多种格式，是否继续以统一设置新的格式？",
                        () => {
                            showFormatDialog("");
                        });
            } else {
                showFormatDialog(formatSet.values().next().value);
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

    protected renderChartOption(chart: Chart) {
        const sheet = this.state.currentSheet;
        if (typeof sheet === 'undefined') {
            throw new Error('Current sheet is undefined.');
        }

        const handleChartChange = () => {
            const updateChart = new UpdateChart(sheet.name, chart);
            this.handleAction(updateChart.wrapper());
        };

        const handleLayoutChange = (layout: Layout) => {
            const layoutAsset = new LayoutAsset(sheet.name, chart.name, layout);
            this.handleAction(layoutAsset.wrapper());
        };

        return (
            <GroupView
                className="realtime-edit chart-option">
                <GroupItem
                    name="chart"
                    title="图表">
                    <form onSubmit={this.preventSubmit}>
                        <ChartForm
                            chart={chart}
                            afterChange={handleChartChange} />
                    </form>
                </GroupItem>
                <GroupItem
                    name="layout"
                    title="布局">
                    <form onSubmit={this.preventSubmit}>
                        <LayoutForm
                            layout={chart.layout}
                            afterChange={handleLayoutChange} />
                    </form>
                </GroupItem>
            </GroupView>
        );
    }

    protected renderTextOption(text: Text) {
        const sheet = this.state.currentSheet;
        if (typeof sheet === 'undefined') {
            throw new Error('Current sheet is undefined.');
        }

        const handleLayoutChange = (layout: Layout) => {
            const layoutAsset = new LayoutAsset(sheet.name, text.name, layout);
            this.handleAction(layoutAsset.wrapper());
        };

        return (
            <GroupView
                className="realtime-edit text-option">
                <GroupItem
                    name="layout"
                    title="布局">
                    <form onSubmit={this.preventSubmit}>
                        <LayoutForm
                            layout={text.layout}
                            afterChange={handleLayoutChange} />
                    </form>
                </GroupItem>
            </GroupView>
        );
    }

    protected confirm(message: string, onOk: () => void, onCancel?: () => void) {
        const actions = [{
                name: "format-form-cancel",
                label: "取消",
                callback: () => {
                    this.closeDialog();
                    if (typeof onCancel !== "undefined") {
                        onCancel();
                    }
                }
            }, {
                name: "format-form-ok",
                label: "确定",
                callback: () => {
                    this.closeDialog();
                    onOk();
                }
            }];
        this.showDialog(
            <Dialog className="confirm-dialog" actions={actions}>
                {message}
            </Dialog>
        );
    }
    
    protected showDialog(dialog: React.ReactNode) {
        this.setState({ dialog });
    }

    protected closeDialog() {
        this.setState({ dialog: undefined });
    }
}

export default WorkbookEditor;
export { WorkbookEditorProps };
