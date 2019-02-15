import * as React from 'react';
import classnames from 'classnames';
import './PlacementSelector.css';

enum PlacementItem {
    TOP, RIGHT, BOTTOM, LEFT,
    TOP_LEFT, TOP_RIGHT,
    BOTTOM_LEFT, BOTTOM_RIGHT,
    CENTER,
}

interface PlacementSelectorProps extends React.ClassAttributes<PlacementSelector> {
    availableItems?: Set<PlacementItem>,
    checkedItems?: Set<PlacementItem>,
    onToggle?: (item: PlacementItem, selected: boolean) => void,
}

interface PlacementSelectorState {
    availableItems: Set<PlacementItem>,
    checkedItems: Set<PlacementItem>,
}

class PlacementSelector extends React.Component<PlacementSelectorProps, PlacementSelectorState> {
    protected static defaultProps: Partial<PlacementSelectorProps> = {
        availableItems: new Set([
            PlacementItem.LEFT,
            PlacementItem.TOP,
            PlacementItem.RIGHT,
            PlacementItem.BOTTOM,
        ]),
        checkedItems: new Set([]),
    };

    constructor(props: PlacementSelectorProps) {
        super(props);

        this.state = this.createInitialState(props);

        this.handleToggle = this.handleToggle.bind(this);
    }

    public componentDidUpdate(prevProps: PlacementSelectorProps) {
        if (this.props.availableItems !== prevProps.availableItems ||
            this.props.checkedItems !== prevProps.checkedItems) {
            this.setState(this.createInitialState(this.props));
        }
    }

    protected createInitialState(props: PlacementSelectorProps) {
        return {
            availableItems: props.availableItems || new Set([
                PlacementItem.LEFT,
                PlacementItem.TOP,
                PlacementItem.RIGHT,
                PlacementItem.BOTTOM,
            ]),
            checkedItems: props.checkedItems || new Set()
        };
    }

    public toggleSelection(item: PlacementItem) {
        if (!this.state.availableItems.has(item)) {
            return;
        }
        const checkedItems = this.state.checkedItems;
        let checked;
        if (checkedItems.has(item)) {
            checkedItems.delete(item);
            checked = false;
        } else {
            checkedItems.add(item);
            checked = true;
        }
        this.setState({
            checkedItems
        })
        if (typeof this.props.onToggle !== 'undefined') {
            this.props.onToggle(item, checked);
        }
    }

    protected handleToggle(event: React.MouseEvent) {
        const plcmt = event.currentTarget.getAttribute("data-placement");
        switch (plcmt) {
            case 'top':
                this.toggleSelection(PlacementItem.TOP);
                return;
            case 'right':
                this.toggleSelection(PlacementItem.RIGHT);
                return;
            case 'bottom':
                this.toggleSelection(PlacementItem.BOTTOM);
                return;
            case 'left':
                this.toggleSelection(PlacementItem.LEFT);
                return;
            case 'top-left':
                this.toggleSelection(PlacementItem.TOP_LEFT);
                return;
            case 'top-right':
                this.toggleSelection(PlacementItem.TOP_RIGHT);
                return;
            case 'bottom-left':
                this.toggleSelection(PlacementItem.BOTTOM_LEFT);
                return;
            case 'bottom-right':
                this.toggleSelection(PlacementItem.BOTTOM_RIGHT);
                return;
            case 'center':
                this.toggleSelection(PlacementItem.CENTER);
                return;
            default:
            // throw new Error();
        }
    }

    public isChecked(item: PlacementItem) {
        return this.state.checkedItems.has(item);
    }

    public isSelectable(item: PlacementItem) {
        return this.state.availableItems.has(item);
    }

    protected classNameForState(item: PlacementItem) {
        return classnames(
            this.isChecked(item) ? "checked" : "unchecked",
            this.isSelectable(item) ? "enabled" : "disabled");
    }

    public render() {
        return (
            <div className="placement-selector">
                <div className="row-top">
                    <label className={"col-left top-left "
                        + this.classNameForState(PlacementItem.TOP_LEFT)}
                        data-placement="top-left"
                        onClick={this.handleToggle}>
                        左上
                    </label>
                    <label className={"col-middle top "
                        + this.classNameForState(PlacementItem.TOP)}
                        data-placement="top"
                        onClick={this.handleToggle}>
                        上
                    </label>
                    <label className={"col-right top-right "
                        + this.classNameForState(PlacementItem.TOP_RIGHT)}
                        data-placement="top-right"
                        onClick={this.handleToggle}>
                        右上
                    </label>
                </div>
                <div className="row-middle">
                    <label className={"col-left left "
                        + this.classNameForState(PlacementItem.LEFT)}
                        data-placement="left"
                        onClick={this.handleToggle}>
                        左
                    </label>
                    <label className={"col-middle center "
                        + this.classNameForState(PlacementItem.CENTER)}
                        data-placement="center"
                        onClick={this.handleToggle}>
                        中
                    </label>
                    <label className={"col-right right "
                        + this.classNameForState(PlacementItem.RIGHT)}
                        data-placement="right"
                        onClick={this.handleToggle}>
                        右
                    </label>
                </div>
                <div className="row-bottom">
                    <label className={"col-left bottom-left "
                        + this.classNameForState(PlacementItem.BOTTOM_LEFT)}
                        data-placement="bottom-left"
                        onClick={this.handleToggle}>
                        左下
                    </label>
                    <label className={"col-middle bottom "
                        + this.classNameForState(PlacementItem.BOTTOM)}
                        data-placement="bottom"
                        onClick={this.handleToggle}>
                        下
                    </label>
                    <label className={"col-right bottom-right "
                        + this.classNameForState(PlacementItem.BOTTOM_RIGHT)}
                        data-placement="bottom-right"
                        onClick={this.handleToggle}>
                        右下
                    </label>
                </div>
            </div>
        );
    }
}

export default PlacementSelector;
export { PlacementItem, PlacementSelectorProps };
