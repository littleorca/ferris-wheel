import * as React from 'react';
import SheetAsset from '../model/SheetAsset';
import TableView from './TableView';
import ChartView from './ChartView';
import TextView from './TextView';
import SharedViewProps from './SharedViewProps';
import Action from '../action/Action';
import ActionHandler from '../action/ActionHandler';
import ActionHerald from '../action/ActionHerald';
import classnames from "classnames";

interface AssetViewProps extends SharedViewProps<AssetView> {
    asset: SheetAsset;
    className?: string;
}

const AssetContentView = (props: AssetViewProps) => {
    if (typeof props.asset.table !== 'undefined') {
        return (
            <TableView
                className={props.className}
                table={props.asset.table}
                editable={props.editable}
                onAction={props.onAction}
                herald={props.herald} />
        );
    } else if (typeof props.asset.chart !== 'undefined') {
        return (
            <ChartView
                className={props.className}
                chart={props.asset.chart}
                editable={props.editable}
                onAction={props.onAction}
                herald={props.herald} />
        );
    } else if (typeof props.asset.text !== 'undefined') {
        return (
            <TextView
                className={props.className}
                text={props.asset.text}
                editable={props.editable}
                onAction={props.onAction}
                herald={props.herald} />
        );
    } else { // maybe should just throw error
        return (
            <div className={props.className}>
                <p className="alert">Invalid asset!</p>
            </div>
        );
    }
}

class AssetView extends React.Component<AssetViewProps> implements ActionHerald {
    private listeners: Set<ActionHandler> = new Set();

    constructor(props: AssetViewProps) {
        super(props);

        this.onApplyAction = this.onApplyAction.bind(this);
        this.onRequestAction = this.onRequestAction.bind(this);
    }

    public componentDidMount() {
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

        return (
            <AssetContentView
                className={className}
                asset={this.props.asset}
                editable={this.props.editable}
                onAction={this.onRequestAction}
                herald={this} />
        );
    }
}

export default AssetView;
export { AssetViewProps };
