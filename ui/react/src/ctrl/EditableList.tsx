import * as React from 'react';
import ManipulableList from './ManipulableList';
import './EditableList.css';

interface EditorProps<T> extends React.ClassAttributes<any> {
    value: T,
    disabled?: boolean,
    onSubmit(newVal: T): void
}

interface EditableListProps<T> extends React.ClassAttributes<EditableList<T>> {
    list: T[],
    editor: React.SFC<EditorProps<T>>,
    readOnly?: boolean,
    className?: string,
    horizontal?: boolean,
    fixedItems?: boolean,
    getKey?(item: T, index: number): string,
    getLabel?(item: T, index: number): string,
    createItem?(): T,
    afterChange?(list: T[]): void,
}

interface EditableListState<T> {
    selectedItem: T | null,
    selectedIndex: number,
}

class EditableList<T> extends React.Component<EditableListProps<T>, EditableListState<T>> {
    protected static defaultProps: Partial<EditableListProps<any>> = {
    };

    private manipulableListRef: React.RefObject<ManipulableList<T>>;

    constructor(props: EditableListProps<T>) {
        super(props);

        this.manipulableListRef = React.createRef();

        this.state = {
            selectedItem: null,
            selectedIndex: -1,
        };

        this.onSelected = this.onSelected.bind(this);
        this.addNewItem = this.addNewItem.bind(this);
        this.removeItem = this.removeItem.bind(this);
        this.onSubmit = this.onSubmit.bind(this);
        this.onMoved = this.onMoved.bind(this);
        this.onRemoved = this.onRemoved.bind(this);
        this.onAppended = this.onAppended.bind(this);
    }

    protected onSelected(selectedItem: T, selectedIndex: number) {
        this.setState({
            selectedItem,
            selectedIndex
        });
    }

    protected addNewItem() {
        if (typeof this.props.createItem === 'undefined') {
            return;
        }
        const mlist: ManipulableList<T> | null = this.manipulableListRef.current;
        if (mlist === null) {
            return;
        }
        mlist.addItem(this.props.createItem());
    }

    protected removeItem() {
        const mlist: ManipulableList<T> | null = this.manipulableListRef.current;
        if (mlist === null) {
            return;
        }
        mlist.tryRemoveSelected();
    }

    protected onSubmit(newVal: T, index: number) {
        this.props.list[index] = newVal;
        this.onUpdate();
    }

    protected onMoved(item: T, oldIndex: number, newIndex: number) {
        this.onUpdate();
    }

    protected onRemoved(item: T, index: number) {
        this.onUpdate();
    }

    protected onAppended(item: T, index: number) {
        this.onUpdate();
    }

    protected onUpdate() {
        this.forceUpdate();
        if (typeof this.props.afterChange !== 'undefined') {
            this.props.afterChange(this.props.list);
        }
    }

    public render() {
        const list = this.props.list;
        const selectedItem = this.state.selectedItem;
        const selectedIndex = this.state.selectedIndex;

        const onSubmit = (newVal: T) => {
            this.onSubmit(newVal, selectedIndex);
        }

        const className = "editable-list" +
            (this.props.horizontal ? " horizontal" : " vertical") +
            (typeof this.props.className !== 'undefined' ?
                " " + this.props.className : "");

        return (
            <div className={className}>
                <div className="list-container">
                    <ManipulableList<T>
                        ref={this.manipulableListRef}
                        list={list}
                        horizontal={this.props.horizontal}
                        sortable={!this.props.fixedItems}
                        appendable={!this.props.fixedItems}
                        removable={!this.props.fixedItems}
                        onSelect={this.onSelected}
                        onItemMoved={this.onMoved}
                        onItemRemoved={this.onRemoved}
                        onItemAppended={this.onAppended}
                        getItemKey={this.props.getKey}
                        getItemLabel={this.props.getLabel}
                        createItem={this.props.createItem} />
                    {this.props.fixedItems || (
                        <div className="actions">
                            <button
                                type="button"
                                className="add-item"
                                onClick={this.addNewItem}>添加</button>
                            <button
                                type="button"
                                className="remove-item"
                                onClick={this.removeItem}>删除</button>
                        </div>
                    )}
                </div>
                {this.props.editor && (
                    <div className="editor-container">
                        {selectedItem !== null && (
                            <this.props.editor
                                value={selectedItem}
                                onSubmit={onSubmit} />
                        )}
                    </div>
                )}
            </div>
        );
    }
}

export default EditableList;
export { EditorProps, EditableListProps };
