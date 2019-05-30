import * as React from 'react';
import Binder from '../model/Binder';
import Orientation from '../model/Orientation';
import Placement from '../model/Placement';
import UnionValue from "../model/UnionValue";
import UnionValueEdit from '../ctrl/UnionValueEdit';
import ValueChange from "../ctrl/ValueChange";
import PlacementSelector, { PlacementItem } from '../ctrl/PlacementSelector';
import classnames from "classnames";

interface DataBinderFormProps extends React.ClassAttributes<DataBinderForm> {
    binder: Binder,
    className?: string,
    afterChange?: (binder: Binder) => void,
}

const availablePlacementItems = new Set([
    PlacementItem.LEFT,
    PlacementItem.TOP,
    PlacementItem.RIGHT,
    PlacementItem.BOTTOM
]);

class DataBinderForm extends React.Component<DataBinderFormProps> {
    constructor(props: DataBinderFormProps) {
        super(props);

        this.handleAreaChange = this.handleAreaChange.bind(this);
        this.updateOrientation = this.updateOrientation.bind(this);
        this.togglePlacementSelection = this.togglePlacementSelection.bind(this);
    }

    protected handleAreaChange(change: ValueChange<UnionValue>) {
        if (change.type !== 'commit') {
            return;
        }

        const binder = this.props.binder;
        binder.data = change.toValue;

        this.onUpdate();
    }

    protected updateOrientation(event: React.ChangeEvent<HTMLInputElement>) {
        const orientation = event.currentTarget.value;
        const binder = this.props.binder;
        binder.orientation = orientation as Orientation;

        const tempPlacement = binder.categoriesPlacement;
        binder.categoriesPlacement = binder.seriesNamePlacement;
        binder.seriesNamePlacement = tempPlacement;

        this.onUpdate();
    }

    protected togglePlacementSelection(item: PlacementItem, selected: boolean) {
        const binder = this.props.binder;
        const placement = this.toPlacement(item);
        if ((binder.orientation === Orientation.HORIZONTAL &&
            (placement === Placement.TOP || placement === Placement.BOTTOM))
            ||
            (binder.orientation === Orientation.VERTICAL &&
                (placement === Placement.LEFT || placement === Placement.RIGHT))) {

            binder.categoriesPlacement = selected ? placement : Placement.UNSET;

        } else if ((binder.orientation === Orientation.VERTICAL &&
            (placement === Placement.TOP || placement === Placement.BOTTOM))
            ||
            (binder.orientation === Orientation.HORIZONTAL &&
                (placement === Placement.LEFT || placement === Placement.RIGHT))) {

            binder.seriesNamePlacement = selected ? placement : Placement.UNSET;
        }

        this.onUpdate();
    }

    protected onUpdate() {
        if (typeof this.props.afterChange !== 'undefined') {
            this.props.afterChange(this.props.binder);
        }
        this.forceUpdate();
    }

    protected toPlacement(placementItem: PlacementItem): Placement {
        switch (placementItem) {
            // case PlacementItem.UNSET: return Placement.UNSET;
            case PlacementItem.TOP: return Placement.TOP;
            case PlacementItem.RIGHT: return Placement.RIGHT;
            case PlacementItem.BOTTOM: return Placement.BOTTOM;
            case PlacementItem.LEFT: return Placement.LEFT;
            case PlacementItem.CENTER: return Placement.CENTER;
        }
        return Placement.UNSET;
    }

    protected toPlacementItem(placement: Placement): PlacementItem {
        switch (placement) {
            // case Placement.UNSET:  'UNSET';
            case Placement.TOP: return PlacementItem.TOP;
            case Placement.RIGHT: return PlacementItem.RIGHT;
            case Placement.BOTTOM: return PlacementItem.BOTTOM;
            case Placement.LEFT: return PlacementItem.LEFT;
            case Placement.CENTER: return PlacementItem.CENTER;
        }
        throw new Error();
    }

    protected getPlacementLabel(placement: Placement): string {
        switch (placement) {
            case 'PLCMT_TOP':
                return '顶部';
            case 'PLCMT_RIGHT':
                return '右侧';
            case 'PLCMT_BOTTOM':
                return '底部';
            case 'PLCMT_LEFT':
                return '左侧';
            case 'PLCMT_UNSET':
            default:
                return '无';
        }
    }

    public render() {
        const binder = this.props.binder;

        const checkedPlacementItems = new Set<PlacementItem>();
        if (binder.categoriesPlacement !== Placement.UNSET) {
            checkedPlacementItems.add(this.toPlacementItem(binder.categoriesPlacement));
        }
        if (binder.seriesNamePlacement !== Placement.UNSET) {
            checkedPlacementItems.add(this.toPlacementItem(binder.seriesNamePlacement));
        }

        const categoriesPlacementLabel = this.getPlacementLabel(binder.categoriesPlacement);
        const seriesNamePlacementLabel = this.getPlacementLabel(binder.seriesNamePlacement);

        const className = classnames("data-binder-form", this.props.className);

        return (
            <div className={className}>
                <div className="area">
                    <label className="field data">
                        <span className="field-name">区域</span>
                        <UnionValueEdit
                            value={binder.data}
                            modes={["formula"]}
                            afterChange={this.handleAreaChange} />
                    </label>
                </div>
                <div className="field orientation">
                    <span className="field-name">方向</span>
                    <label>
                        <input
                            type="radio"
                            name="orientation"
                            value={Orientation.HORIZONTAL}
                            checked={binder.orientation === Orientation.HORIZONTAL}
                            onChange={this.updateOrientation} />
                        <span>横向</span>
                    </label>
                    <label>
                        <input
                            type="radio"
                            name="orientation"
                            value={Orientation.VERTICAL}
                            checked={binder.orientation === Orientation.VERTICAL}
                            onChange={this.updateOrientation} />
                        <span>纵向</span>
                    </label>
                </div>
                <div className="placement">
                    <div className="summary">
                        <label>选择标签行/列：</label>
                        <p>分类标签：{categoriesPlacementLabel}</p>
                        <p>序列标签：{seriesNamePlacementLabel}</p>
                    </div>
                    <PlacementSelector
                        availableItems={availablePlacementItems}
                        checkedItems={checkedPlacementItems}
                        onToggle={this.togglePlacementSelection} />
                </div>
            </div>
        );
    }
}

export default DataBinderForm;
export { DataBinderFormProps };
