import * as React from 'react';
import SheetAsset from '../model/SheetAsset';
import TableView from './TableView';
import ChartView from './ChartView';
import TextView from './TextView';
import FormView from './FormView';
import SharedViewProps from './SharedViewProps';
import Action from '../action/Action';
import ActionHandler from '../action/ActionHandler';
import ActionHerald from '../action/ActionHerald';
import classnames from "classnames";

interface AssetViewProps extends SharedViewProps<AssetView> {
    asset: SheetAsset;
    className?: string;
}

class AssetView extends React.Component<AssetViewProps> implements ActionHerald {
    private listeners: Set<ActionHandler> = new Set();

    constructor(props: AssetViewProps) {
        super(props);

        this.onApplyAction = this.onApplyAction.bind(this);
        this.onRequestAction = this.onRequestAction.bind(this);

        if (typeof this.props.herald !== 'undefined') {
            this.props.herald.subscribe(this.onApplyAction);
        }
    }

    public componentWillUnmount() {
        if (typeof this.props.herald !== 'undefined') {
            this.props.herald.unsubscribe(this.onApplyAction);
        }
    }

    protected onApplyAction(action: Action) {
        this.listeners.forEach(handler => handler(action));
    }

    protected onRequestAction(action: Action) {
        if (typeof this.props.onAction !== 'undefined') {
            this.props.onAction(action);
        }
    }

    public subscribe(handler: ActionHandler) {
        this.listeners.add(handler);
    }

    public unsubscribe(handler: ActionHandler) {
        this.listeners.delete(handler);
    }

    public render() {
        const className = classnames("asset-view", this.props.className);
        const asset = this.props.asset;
        const commonProps = {
            className,
            asset,
            editable: this.props.editable,
            onAction: this.onRequestAction,
            herald: this,
        }

        let specificAsset;

        if (typeof asset.table !== 'undefined') {
            specificAsset = (
                <TableView
                    {...commonProps}
                    table={asset.table}
                    controlPortal={this.props.controlPortal} />
            );
        } else if (typeof asset.chart !== 'undefined') {
            specificAsset = (
                <ChartView
                    {...commonProps}
                    chart={asset.chart}
                    controlPortal={this.props.controlPortal} />
            );
        } else if (typeof asset.text !== 'undefined') {
            specificAsset = (
                <TextView
                    {...commonProps}
                    text={asset.text}
                    controlPortal={this.props.controlPortal} />
            );
        } else if (typeof asset.form !== 'undefined') {
            specificAsset = (
                <FormView
                    {...commonProps}
                    form={asset.form}
                    controlPortal={this.props.controlPortal} />
            );
        } else { // maybe should just throw error
            specificAsset = (
                <div {...commonProps}>
                    <p className="alert">Invalid asset!</p>
                </div>
            );
        }

        return specificAsset;
    }
}

export default AssetView;
export { AssetViewProps };
