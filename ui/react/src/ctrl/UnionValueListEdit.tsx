import * as React from 'react';
import UnionValue from '../model/UnionValue';
import ManipulableList, { ItemRendererProps } from './ManipulableList';
import Values from '../model/Values';
import EditableUnionValue from './EditableUnionValue';
import { UnionValueEditMode } from './UnionValueEdit';
import classnames from "classnames";
import './UnionValueListEdit.css';

interface UnionValueListEditProps extends React.ClassAttributes<UnionValueListEdit> {
    list: UnionValue[],
    modes?: UnionValueEditMode[];
    readOnly?: boolean,
    className?: string,
    hideActions?: boolean,
    sortable?: boolean,
    addible?: boolean,
    removable?: boolean,
    afterChange?(list: UnionValue[]): void,
}

class UnionValueListEdit extends React.Component<UnionValueListEditProps> {
    protected static defaultProps: Partial<UnionValueListEditProps> = {
        readOnly: false,
        hideActions: false,
        sortable: true,
        addible: true,
        removable: true,
    };

    private manipulableListRef: React.RefObject<ManipulableList<UnionValue>>;
    private pendingNewItem: UnionValue | null = null;

    constructor(props: UnionValueListEditProps) {
        super(props);

        this.manipulableListRef = React.createRef();

        this.itemRenderer = this.itemRenderer.bind(this);
        this.addNewItem = this.addNewItem.bind(this);
        this.removeItem = this.removeItem.bind(this);
        this.onItemMoved = this.onItemMoved.bind(this);
        this.onItemRemoved = this.onItemRemoved.bind(this);
        this.onItemAppended = this.onItemAppended.bind(this);
        this.onItemUpdated = this.onItemUpdated.bind(this);
    }

    protected addNewItem() {
        const mlist: ManipulableList<UnionValue> | null = this.manipulableListRef.current;
        if (mlist === null) {
            return;
        }
        this.pendingNewItem = Values.blank();
        mlist.addItem(this.pendingNewItem);
    }

    protected removeItem() {
        const mlist: ManipulableList<UnionValue> | null = this.manipulableListRef.current;
        if (mlist === null) {
            return;
        }
        mlist.removeSelected();
    }

    protected onItemMoved(item: UnionValue, oldIndex: number, newIndex: number) {
        this.onUpdate();
    }

    protected onItemRemoved(item: UnionValue, index: number) {
        this.onUpdate();
    }

    protected onItemAppended(item: UnionValue, index: number) {
        this.pendingNewItem = item;
        this.onUpdate();
    }

    protected onItemUpdated(oldItem: UnionValue, newItem: UnionValue, index: number) {
        this.onUpdate();
    }

    protected onUpdate() {
        this.forceUpdate();
        if (typeof this.props.afterChange !== 'undefined') {
            this.props.afterChange(this.props.list);
        }
    }

    protected itemRenderer(props: ItemRendererProps<UnionValue>) {
        const afterChange = (value: UnionValue) => {
            props.updateItem(value);
        };

        let editMode = false;
        if (this.pendingNewItem === props.value) {
            editMode = !this.props.readOnly;
            this.pendingNewItem = null;
        }

        const filteredModes: UnionValueEditMode[] = [];
        if (typeof this.props.modes !== "undefined") {
            this.props.modes.forEach(m => {
                if (m !== "formula" && m !== "list") {
                    filteredModes.push(m);
                }
            });
        } else {
            filteredModes.push("decimal");
            filteredModes.push("boolean");
            filteredModes.push("date");
            filteredModes.push("string");
        }

        if (filteredModes.length === 0) {
            throw new Error("Invalid mode setting.");
        }

        return (
            <EditableUnionValue
                value={props.value}
                modes={filteredModes}
                aux="none"
                editMode={editMode}
                afterChange={afterChange} />
        );
    }

    public render() {
        const list = this.props.list;

        const className = classnames("union-value-list-edit", this.props.className);

        return (
            <div className={className}>
                <ManipulableList<UnionValue>
                    ref={this.manipulableListRef}
                    list={list}
                    itemRenderer={this.itemRenderer}
                    sortable={this.props.sortable}
                    addible={this.props.addible}
                    removable={this.props.removable}
                    onItemMoved={this.onItemMoved}
                    onItemRemoved={this.onItemRemoved}
                    onItemAdded={this.onItemAppended}
                    onItemUpdated={this.onItemUpdated}
                    createItem={Values.blank} />
                {this.props.hideActions || (
                    <div className="actions">
                        {this.props.addible && <button
                            type="button"
                            className="add-item"
                            onClick={this.addNewItem}>添加</button>}
                        {this.props.removable && <button
                            type="button"
                            className="remove-item"
                            onClick={this.removeItem}>删除</button>}
                    </div>
                )}
            </div>
        );
    }
}

export default UnionValueListEdit;
export { UnionValueListEditProps };
