import * as React from 'react';
import WorkbookView from './WorkbookView';
import Workbook from '../model/Workbook';
import Sheet from '../model/Sheet';
import SheetAsset from '../model/SheetAsset';
import Chart from '../model/Chart';
import GridCellImpl from '../model/GridCellImpl';
import Table from '../model/Table';
import Form from "../model/Form";
import Cell from '../model/Cell';
import Text from '../model/Text';
import Values from '../model/Values';
import TableAutomaton from '../model/TableAutomaton';
import PivotAutomaton from '../model/PivotAutomaton';
import QueryAutomaton from '../model/QueryAutomaton';
import QueryTemplate from '../model/QueryTemplate';
import Stacking from '../model/Stacking';
import Toolbar, { Group } from '../ctrl/Toolbar';
import DropdownButton from '../ctrl/DropdownButton';
import Button, { ButtonProps } from '../ctrl/Button';
import Action from '../action/Action';
import ActionHandler from '../action/ActionHandler';
import ActionHerald from '../action/ActionHerald';
import Service from '../action/Service';
import EditRequest from '../action/EditRequest';
import EditResponse from '../action/EditResponse';
import RemoveAsset from '../action/RemoveAsset';
import RemoveSheet from '../action/RemoveSheet';
import AddForm from '../action/AddForm';
import AddTable from '../action/AddTable';
import AddChart from '../action/AddChart';
import AddText from '../action/AddText';
import WorkbookOperation from '../action/WorkbookOperation';
import { PendingField } from '../form/AddFormForm';
import AddFormDialog from './AddFormDialog';
import Extension, { QueryWizard } from '../extension/Extension';
import Dialog from './Dialog';
import { ModalProps } from './Modal';
import Loading from 'react-loading';
import classnames from "classnames";
import './WorkbookEditor.css';

interface WorkbookEditorProps extends React.ClassAttributes<WorkbookEditor> {
    workbook: Workbook;
    service: Service;
    defaultSheet?: string;
    className?: string;
    extensions?: Extension[];
    beforeAction?: (action: Action) => boolean;
    afterAction?: (action: Action) => void;
}

interface WorkbookEditorState {
    respTxId: number;
    currentSheet?: Sheet;
    currentAsset?: SheetAsset;
    clipBoard?: Sheet | SheetAsset;
    showSidebar: boolean;
    message: string;
    serviceStatus: string;
    showMask: boolean;
}

class WorkbookEditor extends React.Component<WorkbookEditorProps, WorkbookEditorState> implements ActionHerald {
    private sideBarRef: React.RefObject<HTMLDivElement> = React.createRef();
    private sideBarPortalNode: HTMLDivElement;
    private reqTxId = 0;

    private listeners: Set<ActionHandler> = new Set();
    private queryWizardButtons = new Array<ButtonProps>();
    private queryWizardMap: Map<string, QueryWizard> = new Map();

    constructor(props: WorkbookEditorProps) {
        super(props);

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
        this.handleAddForm = this.handleAddForm.bind(this);

        this.handleToggleSidebar = this.handleToggleSidebar.bind(this);

        this.handleAction = this.handleAction.bind(this);
        this.onServiceOk = this.onServiceOk.bind(this);
        this.onServiceError = this.onServiceError.bind(this);
        this.preventSubmit = this.preventSubmit.bind(this);

        this.state = this.createInitialState(props);

        this.sideBarPortalNode = document.createElement("div");
        this.sideBarPortalNode.className = "realtime-edit";

        if (typeof this.props.extensions !== 'undefined') {
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
            respTxId: 0,
            showSidebar: true,
            message: "编辑文档",
            serviceStatus: "就绪",
            showMask: false,
        };
    }

    public componentDidMount() {
        const sideBar = this.sideBarRef.current;
        if (!sideBar) {
            throw Error("Missing sidebar!");
        }
        sideBar.appendChild(this.sideBarPortalNode);
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
                clipBoard: this.state.currentAsset.clone()
            });

        } else {
            // copy sheet
            this.setState({
                clipBoard: this.state.currentSheet.clone()
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
                const chart = specificAsset.clone(); // copy
                const addChart = new AddChart(sheet.name, chart);
                chart.name = this.newAssetName(sheet, chart.name + ' (copy)');
                action = addChart.wrapper();

            } else if (specificAsset instanceof Table) {
                const table = specificAsset.clone(); // copy
                const addTable = new AddTable(sheet.name, table);
                table.name = this.newAssetName(sheet, table.name + ' (copy)');
                action = addTable.wrapper();

            } else if (specificAsset instanceof Text) {
                const text = specificAsset.clone(); // copy
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
        const table = new Table(tableName,
            [[
                new GridCellImpl<Cell>(new Cell(0, Values.blank()))
            ]]);
        const addTable = new AddTable(this.state.currentSheet.name, table);
        this.handleAction(addTable.wrapper());
    }

    protected handleOpenQueryWizard(name: string, event: React.MouseEvent<HTMLButtonElement>) {
        const wizard = this.queryWizardMap.get(name);
        if (typeof wizard === 'undefined') {
            throw new Error('Wizard component not found: ' + name); // TODO review
        }
        const wizardRender = (props: ModalProps) => {
            const onOk = (queryTemplate: QueryTemplate) => {
                props.close();
                this.handleQueryWizardOk(queryTemplate);
            };
            const onCancel = () => {
                props.close();
                this.handleQueryWizardCancelled();
            };
            return (<wizard.component
                onOk={onOk}
                onCancel={onCancel} />);
        };
        Dialog.show(wizardRender);
    }

    protected handleQueryWizardOk(queryTemplate: QueryTemplate) {
        this.doAddQueryTable(queryTemplate);
    }

    protected handleQueryWizardCancelled() {
        // TBD
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
        const table = new Table(tableName, [], [], [], new TableAutomaton(queryAutomaton));
        const addTable = new AddTable(this.state.currentSheet.name, table);
        this.handleAction(addTable.wrapper());
    }

    protected handleAddPivotTable() {
        if (typeof this.state.currentSheet === 'undefined') {
            throw new Error('Illegal state, current sheet is not defined!');
        }
        const tableName = this.newAssetName(this.state.currentSheet, 'table');
        const pivotAutomaton = new PivotAutomaton();
        const table = new Table(tableName, [], [], [], new TableAutomaton(undefined, pivotAutomaton));
        const addTable = new AddTable(this.state.currentSheet.name, table);
        this.handleAction(addTable.wrapper());
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

    // TODO consider initialize the form with all possible fields for convenience.
    protected handleAddForm() {
        if (typeof this.state.currentSheet === 'undefined') {
            throw new Error('Unable to create form as current sheet is not defined!');
        }

        const pendingFields: PendingField[] = [];
        this.props.workbook.sheets.forEach(s => {
            s.assets.forEach(a => {
                if (a.assetType() === "table") {
                    const table = a.specific() as Table;
                    if (typeof table.automaton.queryAutomaton !== "undefined") {
                        const params = table.automaton.queryAutomaton.template.builtinParams;
                        params.forEach(p => {
                            pendingFields.push({
                                sheetName: s.name,
                                assetName: table.name,
                                paramName: p.name,
                                paramType: p.type,
                                mandatory: p.mandatory,
                                multiple: p.multiple,
                            });
                        })
                    }
                }
            })
        });

        const doAddForm = (form: Form) => {
            if (typeof this.state.currentSheet === 'undefined') {
                throw new Error('Unable to create form as current sheet is not defined!!');
            }
            const addForm = new AddForm(this.state.currentSheet.name, form);
            this.handleAction(addForm.wrapper());
        }

        AddFormDialog.show(pendingFields, doAddForm);
    }

    protected handleToggleSidebar() {
        this.setState({
            showSidebar: !this.state.showSidebar,
        });
    }

    protected handleAction(action: Action) {
        // console.log('handleAction', action);
        if (typeof this.props.beforeAction !== "undefined") {
            if (!this.props.beforeAction(action)) {
                return;
            }
        }

        if (action.isLocalAction()) {
            this.handleLocalAction(action);
        } else {
            this.handleRemoteAction(action);
        }

        if (typeof this.props.afterAction !== "undefined") {
            this.props.afterAction(action);
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

    // FIXME setState is not synchronize, txId may duplicate
    protected handleRemoteAction(action: Action) {
        const request = new EditRequest(++this.reqTxId, action);
        this.setState({
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
        if (resp.statusCode === 0) {
            if (typeof resp.changes !== 'undefined') {
                this.applyChanges(resp.changes.actions);
            }
            this.setState({
                respTxId: resp.txId,
                message: `服务成功，txId=${resp.txId}, ${resp.message}`,
                serviceStatus: "就绪",
                showMask: false,
            });

        } else {
            this.setState({
                respTxId: resp.txId,
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
        return false;
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
                        defaultSheet={this.props.defaultSheet}
                        editable={true}
                        extensions={this.props.extensions}
                        onAction={this.handleAction}
                        herald={this}
                        controlPortal={this.sideBarPortalNode} />
                    <div
                        ref={this.sideBarRef}
                        className="sidebar"
                        style={{
                            display: this.state.showSidebar ? undefined : "none"
                        }} />
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
                    {/* <DropdownButton
                        className="primary"
                        primary={{
                            name: "save",
                            label: "保存",
                            className: "primary",
                            tips: "保存编辑内容使之生效。",
                            onClick: this.handleSave
                        }}
                        items={[{
                            name: "save-and-close",
                            label: "保存并关闭",
                            tips: "保存编辑内容并关闭。",
                            onClick: this.handleSaveAndClose
                        }, {
                            name: "close-without-save",
                            label: "放弃编辑并关闭",
                            tips: "放弃编辑内容直接关闭。",
                            onClick: this.handleCloseWithoutSave
                        }]} /> */}
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
                    <Button
                        name="add-form"
                        label="表单"
                        tips="添加表单"
                        onClick={this.handleAddForm} />
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
}

export default WorkbookEditor;
export { WorkbookEditorProps };
