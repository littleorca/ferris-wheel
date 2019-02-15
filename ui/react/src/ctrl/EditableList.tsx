import * as React from 'react';
import ManipulableList from './ManipulableList';
import classnames from 'classnames';
import './EditableList.css';

interface EditorProps<T> extends React.ClassAttributes<any> {
    value: T,
    disabled?: boolean,
    onSubmit(newVal: T): void
}

interface EditableListProps<T> extends React.ClassAttributes<EditableList<T>> {
    list: T[],
    initialSelect?: number,
    editor: React.SFC<EditorProps<T>>,
    readOnly?: boolean,
    className?: string,
    horizontal?: boolean,
    hideActions?: boolean,
    fixedItems?: boolean,
    getKey?(item: T, index: number): string,
    getLabel?(item: T, index: number): string,
    createItem?(): T,
    afterChange?(list: T[]): void,
    onSelect?(item: T, index: number): void
}

interface EditableListState<T> {
    selectItem: T | null,
    selectIndex: number,
}

class EditableList<T> extends React.Component<EditableListProps<T>, EditableListState<T>> {
    protected static defaultProps: Partial<EditableListProps<any>> = {};

    private listRef: React.RefObject<ManipulableList<T>>;

    constructor(props: EditableListProps<T>) {
        super(props);

        this.listRef = React.createRef();

        this.state = {
            selectItem: null,
            selectIndex: -1
        };

        this.handleSelect = this.handleSelect.bind(this);
        this.handleAddItem = this.handleAddItem.bind(this);
        this.handleRemoveItem = this.handleRemoveItem.bind(this);
        this.onSubmit = this.onSubmit.bind(this);
        this.onMoved = this.onMoved.bind(this);
        this.onRemoved = this.onRemoved.bind(this);
        this.onAppended = this.onAppended.bind(this);
    }

    public addItem(item: T, index: number = -1, select: boolean = true) {
        const mlist: ManipulableList<T> | null = this.listRef.current;
        if (mlist === null) {
            return;
        }
        mlist.addItem(item, index, select);
    }

    public removeItem() {
        this.handleRemoveItem();
    }

    public selectItem(index: number, focus: boolean = true) {
        const mlist: ManipulableList<T> | null = this.listRef.current;
        if (mlist === null) {
            return;
        }
        mlist.selectItem(index, focus);
    }

    protected handleSelect(selectItem: T, selectIndex: number) {
        this.setState({
            selectItem,
            selectIndex
        });
        if (typeof this.props.onSelect !== "undefined") {
            this.props.onSelect(selectItem, selectIndex);
        }
    }

    protected handleAddItem() {
        if (typeof this.props.createItem === "undefined") {
            return;
        }
        const mlist: ManipulableList<T> | null = this.listRef.current;
        if (mlist === null) {
            return;
        }
        mlist.addItem(this.props.createItem());
    }

    protected handleRemoveItem() {
        const mlist: ManipulableList<T> | null = this.listRef.current;
        if (mlist === null) {
            return;
        }
        mlist.removeSelected();
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
        if (typeof this.props.afterChange !== "undefined") {
            this.props.afterChange(this.props.list);
        }
    }

    public render() {
        const list = this.props.list;
        const selectItem = this.state.selectItem;
        const selectIndex = this.state.selectIndex;

        const onSubmit = (newVal: T) => {
            this.onSubmit(newVal, selectIndex);
        };

        const className = classnames(
            "editable-list",
            this.props.horizontal ? "horizontal" : "vertical",
            this.props.className);

        const hideActions = this.props.fixedItems || this.props.hideActions;
        return (
            <div className={className}>
                <div className="list-container">
                    <ManipulableList<T>
                        ref={this.listRef}
                        list={list}
                        initialSelect={this.props.initialSelect}
                        horizontal={this.props.horizontal}
                        sortable={!this.props.fixedItems}
                        addible={!this.props.fixedItems}
                        removable={!this.props.fixedItems}
                        onSelect={this.handleSelect}
                        onItemMoved={this.onMoved}
                        onItemRemoved={this.onRemoved}
                        onItemAdded={this.onAppended}
                        getItemKey={this.props.getKey}
                        getItemLabel={this.props.getLabel}
                        createItem={this.props.createItem}
                    />
                    {hideActions || (
                        <div className="actions">
                            <button
                                type="button"
                                className="add-item"
                                onClick={this.handleAddItem}
                            >
                                添加
                            </button>
                            <button
                                type="button"
                                className="remove-item"
                                onClick={this.handleRemoveItem}
                            >
                                删除
                            </button>
                        </div>
                    )}
                </div>
                {this.props.editor && (
                    <div className="editor-container">
                        {selectItem !== null && (
                            <this.props.editor
                                value={selectItem}
                                onSubmit={onSubmit}
                            />
                        )}
                    </div>
                )}
            </div>
        );
    }
}

export default EditableList;
export { EditorProps, EditableListProps };
