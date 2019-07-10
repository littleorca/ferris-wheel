import * as React from 'react';
import * as ReactDOM from 'react-dom';
import SharedViewProps from './SharedViewProps';
import Sheet from '../model/Sheet';
import Layout from '../model/Layout';
import * as ReactGridLayout from 'react-grid-layout';
import SheetAsset from '../model/SheetAsset';
import AssetView from './AssetView';
import SelectAsset from '../action/SelectAsset';
import Grid from '../model/Grid';
import Span from '../model/Span';
import LayoutAsset from '../action/LayoutAsset';
import Action from '../action/Action';
import ActionHandler from '../action/ActionHandler';
import ActionHerald from '../action/ActionHerald';
import ChartConsult from '../action/ChartConsult';
import AddChart from '../action/AddChart';
import AddTable from '../action/AddTable';
import RemoveAsset from '../action/RemoveAsset';
import AddForm from '../action/AddForm';
import AddText from '../action/AddText';
import RenameAsset from '../action/RenameAsset';
import GroupView, { GroupItem } from './GroupView';
import LayoutForm from '../form/LayoutForm';
import classnames from "classnames";
import 'react-grid-layout/css/styles.css';
import 'react-resizable/css/styles.css';
import './SheetView.css';

interface SheetViewProps extends SharedViewProps<SheetView> {
    sheet: Sheet;
    className?: string;
}

interface SheetViewState {
    selected?: SheetAsset;
}

class SheetView extends React.Component<SheetViewProps, SheetViewState> implements ActionHerald {
    private sheetElement: React.RefObject<HTMLDivElement> = React.createRef();
    private layoutElement: React.RefObject<ReactGridLayout> = React.createRef();
    private listeners: Set<ActionHandler> = new Set();

    constructor(props: SheetViewProps) {
        super(props);

        this.applyAction = this.applyAction.bind(this);
        this.handleContainerClick = this.handleContainerClick.bind(this);
        this.handleContainerLayoutChange = this.handleContainerLayoutChange.bind(this);
        this.handleAssetLayoutChange = this.handleAssetLayoutChange.bind(this);
        this.handleAssetAction = this.handleAssetAction.bind(this);

        this.state = {
            selected: undefined
        };

        if (typeof this.props.herald !== 'undefined') {
            this.props.herald.subscribe(this.applyAction);
        }
    }

    public componentDidUpdate(prevProps: SheetViewProps) {
        if (this.props.sheet !== prevProps.sheet) {
            this.selectAsset(undefined);
        }
    }

    public componentWillUnmount() {
        if (typeof this.props.herald !== 'undefined') {
            this.props.herald.unsubscribe(this.applyAction);
        }
    }

    public subscribe(handler: ActionHandler) {
        this.listeners.add(handler);
    }

    public unsubscribe(handler: ActionHandler) {
        this.listeners.delete(handler);
    }

    protected applyAction(action: Action) {
        if (!action.isSheetAction() ||
            action.targetSheet() !== this.props.sheet.name) {
            return;
        }
        if (typeof action.addChart !== 'undefined') {
            this.applyAddChart(action.addChart);
        } else if (typeof action.addTable !== 'undefined') {
            this.applyAddTable(action.addTable);
        } else if (typeof action.addText !== 'undefined') {
            this.applyAddText(action.addText);
        } else if (typeof action.chartConsult !== 'undefined') {
            this.applyChartConsult(action.chartConsult);
        } else if (typeof action.addForm !== 'undefined') {
            this.applyAddForm(action.addForm);
        } else if (typeof action.removeAsset !== 'undefined') {
            this.applyRemoveAsset(action.removeAsset);
        } else if (typeof action.renameAsset !== 'undefined') {
            this.listeners.forEach(handler => handler(action));
            this.applyRenameAsset(action.renameAsset);
        } else if (typeof action.layoutAsset !== 'undefined') {
            this.applyLayoutAsset(action.layoutAsset);
        } else {
            this.listeners.forEach(handler => handler(action));
        }
    }

    protected applyAddChart(addChart: AddChart) {
        this.doApplyAddAsset(new SheetAsset(undefined, addChart.chart));
    }

    protected applyAddTable(addTable: AddTable) {
        this.doApplyAddAsset(new SheetAsset(addTable.table));
    }

    protected applyAddText(addText: AddText) {
        this.doApplyAddAsset(new SheetAsset(undefined, undefined, addText.text));
    }

    protected applyAddForm(addForm: AddForm) {
        this.doApplyAddAsset(new SheetAsset(undefined, undefined, undefined, addForm.form));
    }

    protected doApplyAddAsset(asset: SheetAsset) {
        this.props.sheet.assets.push(asset);
        this.selectAsset(asset);
    }

    protected applyChartConsult(chartConsult: ChartConsult) {
        // TODO
    }

    protected applyRemoveAsset(removeAsset: RemoveAsset) {
        const assets = this.props.sheet.assets;
        for (let i = 0; i < assets.length; i++) {
            if (removeAsset.assetName === assets[i].specific().name) {
                const removed = assets[i];
                assets.splice(i, 1);
                if (typeof this.state.selected !== 'undefined' &&
                    this.state.selected === removed) {
                    this.selectAsset(undefined);
                } else {
                    this.forceUpdate();
                }
                break;
            }
        }
    }

    protected applyRenameAsset(renameAsset: RenameAsset) {
        this.forceUpdate();
    }

    protected applyLayoutAsset(layoutAsset: LayoutAsset) {
        for (const sheetAsset of this.props.sheet.assets) {
            const specificAsset = sheetAsset.specific();
            if (specificAsset.name === layoutAsset.assetName) {
                specificAsset.layout = layoutAsset.layout;
                break;
            }
        }
        this.forceUpdate();
    }

    protected selectAsset(asset?: SheetAsset) {
        this.setState({
            selected: asset,
        });
        if (typeof this.props.onAction !== 'undefined') {
            const selectAsset = new SelectAsset(this.props.sheet, asset);
            this.props.onAction(selectAsset.wrapper());
        }
    }

    public getSelectedAsset() {
        return this.state.selected;
    }

    protected handleContainerClick(event: React.MouseEvent<HTMLDivElement>) {
        if (!this.props.editable) {
            return;
        }
        const target = event.target;
        if (target === this.sheetElement.current ||
            (this.layoutElement.current &&
                target === ReactDOM.findDOMNode(this.layoutElement.current))) {

            if (this.state.selected !== null) {
                this.selectAsset(undefined);
            }
        }
    }

    protected handleAssetClick(arbitraryAsset: SheetAsset) {
        if (!this.props.editable) {
            return;
        }
        if (this.state.selected !== arbitraryAsset) {
            this.selectAsset(arbitraryAsset);
        }
    }

    protected handleAssetLayoutChange(gridLayouts: ReactGridLayout.Layout[]) {
        if (typeof this.props.onAction === 'undefined') {
            return;
        }

        const layoutChanges: Action[] = [];
        for (const gridLayout of gridLayouts) {
            const gl = this.makeGridFromGridLayout(gridLayout);
            const arbitraryAsset = this.props.sheet.getAssetByName(gridLayout.i || '');

            if (arbitraryAsset === null) {
                continue;
            }

            const asset = arbitraryAsset.specific();
            if (!this.hasGridChanged(asset.layout.grid, gl)) {
                continue;
            }

            const newLayout = asset.layout.clone();
            if (typeof newLayout.grid === 'undefined' || newLayout.grid === null) {
                newLayout.grid = new Grid();
            }
            newLayout.grid.column = gl.column;
            newLayout.grid.row = gl.row;

            const layoutAsset = new LayoutAsset(
                this.props.sheet.name,
                asset.name,
                newLayout,
            );

            layoutChanges.push(layoutAsset.wrapper());
        }

        for (const action of layoutChanges) {
            this.props.onAction(action);
        }
    }

    protected makeGridLayout(layout: Layout): ReactGridLayout.Layout {
        const gl = {
            x: 0,
            y: 0,
            w: 6,
            h: 4,
        };
        const layoutGrid = layout.grid;
        gl.x = layoutGrid.column.start - 1;
        gl.y = layoutGrid.row.start - 1;
        gl.w = layoutGrid.column.end - layoutGrid.column.start;
        gl.h = layoutGrid.row.end - layoutGrid.row.start;
        if (gl.x < 0) {
            gl.x = 0;
        }
        if (gl.y < 0) {
            gl.y = 0;
        }
        if (gl.w < 1) {
            gl.w = 4;
        }
        if (gl.h === 0) {
            gl.h = 6; // FIXME
        }
        return gl;
    }

    protected makeGridFromGridLayout(gridData: ReactGridLayout.Layout): Grid {
        return new Grid(
            0,
            0,
            new Span(gridData.x + 1, gridData.x + 1 + gridData.w),
            new Span(gridData.y + 1, gridData.y + 1 + gridData.h)
        );
    }

    protected hasGridChanged(g1: Grid, g2: Grid): boolean {
        return g1.column.start !== g2.column.start ||
            g1.column.end !== g2.column.end ||
            g1.row.start !== g2.row.start ||
            g1.row.end !== g2.row.end;
    }

    protected handleContainerLayoutChange(layout: Layout) {
        const action = new LayoutAsset(this.props.sheet.name, "", layout).wrapper();
        this.handleAction(action);
    }

    protected handleAssetAction(action: Action) {
        const meta = action.specific();
        if (meta['sheetName'] === '') {
            meta['sheetName'] = this.props.sheet.name;
        }
        this.handleAction(action);
    }

    protected handleAction(action: Action) {
        if (typeof this.props.onAction !== 'undefined') {
            this.props.onAction(action);
        }
    }

    public render() {
        const sheet = this.props.sheet;
        const assets = sheet.assets;
        const layout = sheet.layout;

        const className = classnames(
            "sheet-view",
            { "editable": this.props.editable },
            this.props.className);

        return (
            <div
                ref={this.sheetElement}
                className={className}
                onClick={this.handleContainerClick}>

                <ReactGridLayout
                    ref={this.layoutElement}
                    className="layout-container"
                    cols={layout.grid.columns || 12}
                    rowHeight={20}
                    width={layout.width || 1200}
                    margin={[10, 10]}
                    useCSSTransforms={false} /* handsontable's column/row resize handler has a problem when work with css transform, refer: https://github.com/handsontable/handsontable/issues/2937 */
                    isDraggable={this.props.editable}
                    isResizable={this.props.editable}
                    draggableHandle=".draggable-handle"
                    onLayoutChange={this.handleAssetLayoutChange}>

                    {assets.map((arbitraryAsset, i) => {
                        const asset = arbitraryAsset.specific();
                        const gridLayout = this.makeGridLayout(asset.layout);
                        const onClick = (event: React.MouseEvent<HTMLDivElement>) => {
                            this.handleAssetClick(arbitraryAsset);
                        }

                        const isSelected = (this.state.selected === arbitraryAsset);
                        const assetContainerClassName = classnames(
                            "asset-container",
                            arbitraryAsset.assetType() + "-asset-container",
                            {
                                "editable": this.props.editable,
                                "selected": isSelected
                            });

                        return (
                            <div
                                key={asset.name}
                                className={assetContainerClassName}
                                data-grid={gridLayout}
                                onClick={onClick}>

                                <AssetView
                                    asset={arbitraryAsset}
                                    editable={this.props.editable}
                                    onAction={this.handleAssetAction}
                                    herald={this}
                                    controlPortal={isSelected ?
                                        this.props.controlPortal : undefined} />

                                {isSelected && (
                                    <div className="asset-container-action">
                                        <span className="draggable-handle">&#10021;</span>
                                    </div>
                                )}
                            </div>
                        );
                    })}

                </ReactGridLayout>

                {typeof this.state.selected === "undefined" &&
                    typeof this.props.controlPortal !== "undefined" &&
                    ReactDOM.createPortal(this.renderControl(), this.props.controlPortal)}
            </div>
        );
    }

    protected renderControl() {
        return (
            <GroupView className="realtime-edit sheet-option">
                <GroupItem
                    name="layout"
                    title="布局">
                    <form onSubmit={e => e.preventDefault()}>
                        <LayoutForm
                            layout={this.props.sheet.layout}
                            afterChange={this.handleContainerLayoutChange} />
                    </form>
                </GroupItem>
            </GroupView>
        );
    }
}

export default SheetView;
export { SheetViewProps };
