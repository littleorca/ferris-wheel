import * as React from 'react';
import { SortableContainer, SortableElement, SortEnd, SortEvent } from 'react-sortable-hoc';
import classnames from 'classnames';
import './ManipulableList.css';

interface ManipulableListProps<T> extends React.ClassAttributes<ManipulableList<T>> {
    list: T[];
    initialSelect?: number;
    className?: string;
    sortHelperClass?: string;
    horizontal?: boolean; // display items horizontally
    tabIndex?: number;
    selectable?: boolean; // allow one item among the list has selected state
    sortable?: boolean; // items sortable
    removable?: boolean; // items can be removed
    addible?: boolean; // new items can be added
    itemRenderer?: React.SFC<ItemRendererProps<T>>;
    onSelect?: (item: T | null, index: number) => void;
    beforeMoveItem?: (item: T, oldIndex: number, newIndex: number) => boolean;
    onItemMoved?: (item: T, oldIndex: number, newIndex: number) => void;
    beforeRemoveItem?: (item: T, index: number) => boolean;
    onItemRemoved?: (item: T, index: number) => void;
    beforeAddItem?: (item: T, index: number) => boolean;
    onItemAdded?: (item: T, index: number) => void;
    beforeUpdateItem?: (oldItem: T, newItem: T, index: number) => boolean;
    onItemUpdated?: (oldItem: T, newItem: T, index: number) => void;
    getItemKey?: (item: T, index: number) => string;
    getItemLabel?: (item: T, index: number) => string;
    createItem?: () => T;
}

interface ItemRendererProps<T> extends React.ClassAttributes<any> {
    value: T,
    updateItem: (value: T) => void,
}

interface ManipulableElementProps<T> extends React.ClassAttributes<any> {
    item: T,
    idx: number,
}

interface ManipulableListState<T> {
    selectIndex: number,
}

class ManipulableList<T> extends React.Component<ManipulableListProps<T>, ManipulableListState<T>> {

    protected static defaultProps: Partial<ManipulableListProps<any>> = {
        tabIndex: 0,
        selectable: true,
        sortable: true,
        removable: true,
        addible: true,
    };

    protected oListRef: React.RefObject<HTMLOListElement>;
    protected sequence: number = 0;

    constructor(props: ManipulableListProps<T>) {
        super(props);

        this.oListRef = React.createRef();

        this.handleKeyDown = this.handleKeyDown.bind(this);
        this.handleClick = this.handleClick.bind(this);
        this.onSortEnd = this.onSortEnd.bind(this);

        const selectIndex = typeof props.initialSelect !== "undefined"
            && props.initialSelect >= 0
            && props.initialSelect < props.list.length ?
            props.initialSelect : (props.list.length > 0 ? 0 : -1);
        this.state = { selectIndex: selectIndex };
        if (props.initialSelect !== selectIndex && typeof props.onSelect !== 'undefined') {
            props.onSelect(selectIndex === -1 ? null : props.list[selectIndex], selectIndex);
        }
    }

    public componentDidUpdate(prevProps: ManipulableListProps<T>) {
        const selectIndex = this.getSelectIndex();
        if (selectIndex !== this.state.selectIndex ||
            this.props.list[selectIndex] !== prevProps.list[selectIndex]) {
            this.selectItem(selectIndex, false);
        }
    }

    public getSelectIndex(): number {
        let selectIndex = this.state.selectIndex;
        if (selectIndex >= this.props.list.length) {
            selectIndex = this.props.list.length - 1;
        } else if (selectIndex < 0 && this.props.list.length > 0) {
            selectIndex = 0;
        }
        return selectIndex;
    }

    public getSelectItem(): T | null {
        const selectIndex = this.getSelectIndex();
        if (selectIndex === -1) {
            return null;
        }
        return this.props.list[selectIndex];
    }

    public addItem(item: T, index: number = -1, select: boolean = true): void {
        if (!this.props.addible) {
            throw new Error('List not addible.');
        }
        if (index < 0 || index > this.props.list.length) {
            index = this.props.list.length;
        }
        if (typeof this.props.beforeAddItem !== 'undefined' &&
            !this.props.beforeAddItem(item, index)) {
            return;
        }
        this.props.list.splice(index, 0, item);
        if (select) {
            this.selectItem(index);
        }
        if (typeof this.props.onItemAdded !== "undefined") {
            this.props.onItemAdded(item, index);
        }
    }

    protected getKey(item: T, index: number) {
        if (typeof this.props.getItemKey !== 'undefined') {
            return this.props.getItemKey(item, index);
        }
        return this.sequence + "$" + index;
    }

    protected getLabel(item: T, index: number) {
        if (typeof this.props.getItemLabel !== 'undefined') {
            return this.props.getItemLabel(item, index);
        }
        return String(item);
    }

    protected handleKeyDown(event: React.KeyboardEvent) {
        if (event.target !== event.currentTarget) {
            return; // ignore child events
        }
        switch (event.key) {
            case 'ArrowUp':
                if (this.props.selectable && !this.props.horizontal) {
                    this.trySelectPrevious();
                    event.preventDefault();
                }
                break;
            case 'ArrowDown':
                if (this.props.selectable && !this.props.horizontal) {
                    this.trySelectNext();
                    event.preventDefault();
                }
                break;
            case 'ArrowLeft':
                if (this.props.selectable && this.props.horizontal) {
                    this.trySelectPrevious();
                    event.preventDefault();
                }
                break;
            case 'ArrowRight':
                if (this.props.selectable && this.props.horizontal) {
                    this.trySelectNext();
                    event.preventDefault();
                }
                break;
            case 'N':
            case 'n':
                if (this.props.addible &&
                    typeof this.props.createItem !== 'undefined' &&
                    (event.ctrlKey || event.metaKey)) {
                    this.addItem(this.props.createItem());
                    event.preventDefault();
                }
                break;
            case 'Backspace':
                if (this.props.removable && event.metaKey) {
                    this.tryRemoveSelected();
                    event.preventDefault();
                }
                break;
            case 'Delete':
                if (this.props.removable) {
                    this.tryRemoveSelected();
                    event.preventDefault();
                }
                break;
        }
    }

    public removeSelected() {
        this.tryRemoveSelected();
    }

    protected tryRemoveSelected() {
        const list = this.props.list;
        const index = this.state.selectIndex;
        if (index === -1) {
            return;
        }
        const item = list[index];
        if (typeof this.props.beforeRemoveItem !== 'undefined' &&
            !this.props.beforeRemoveItem(item, index)) {
            return;
        }
        list.splice(index, 1);
        const selectIndex = (index >= list.length) ? index - 1 : index;
        if (typeof this.props.onItemRemoved !== 'undefined') {
            this.props.onItemRemoved(item, index);
        }
        this.selectItem(selectIndex);
    }

    protected trySelectPrevious() {
        const list = this.props.list;
        if (list.length === 0) {
            return;
        }
        let selectIndex = this.state.selectIndex - 1;
        if (selectIndex < 0) {
            selectIndex = list.length - 1;
        }
        this.selectItem(selectIndex);
    }

    protected trySelectNext() {
        const list = this.props.list;
        if (list.length === 0) {
            return;
        }
        let selectIndex = this.state.selectIndex + 1;
        if (selectIndex >= list.length) {
            selectIndex = 0;
        }
        this.selectItem(selectIndex);
    }

    public selectItem(index: number, focus: boolean = true) {
        const list = this.props.list;
        if (index !== -1 && (index < 0 || index >= list.length)) {
            throw new Error('Select index is out of range.');
        }
        this.setState({ selectIndex: index });
        // TODO scroll select item into view if needed!
        if (focus && this.oListRef.current) {
            this.oListRef.current.focus();
        }
        if (typeof this.props.onSelect !== 'undefined') {
            this.props.onSelect(index === -1 ? null : list[index], index);
        }
    }

    protected handleClick(event: React.MouseEvent) {
        if (!this.props.selectable) {
            return;
        }
        const dataIndex = event.currentTarget.getAttribute('data-index');
        if (dataIndex === null) {
            return;
        }
        const index = parseInt(dataIndex, 10);
        if (this.state.selectIndex !== index) {
            this.selectItem(index);
        }
    }

    protected SortableItem = SortableElement((props: ManipulableElementProps<T>) => {
        const idx = props.idx;
        const selectIndex = this.getSelectIndex();

        const className = classnames(
            "manipulable-item",
            { "selected": idx === selectIndex });

        const updateItem = (value: T) => {
            const list = this.props.list;
            const oldValue = list[idx];
            if (typeof this.props.beforeUpdateItem !== 'undefined' &&
                !this.props.beforeUpdateItem(oldValue, value, idx)) {
                return;
            }
            list[idx] = value;
            this.forceUpdate();
            if (this.oListRef.current) {
                this.oListRef.current.focus();
            }
            if (typeof this.props.onItemUpdated !== 'undefined') {
                this.props.onItemUpdated(oldValue, value, idx);
            }
        };

        return (
            <li
                className={className}
                // tabIndex={0}
                data-index={idx}
                title={this.getLabel(props.item, idx)}
                onClick={this.handleClick}>
                {typeof this.props.itemRenderer === 'undefined' ?
                    (
                        <span>{this.getLabel(props.item, idx)}</span>
                    )
                    :
                    (
                        <this.props.itemRenderer
                            value={props.item}
                            updateItem={updateItem} />
                    )
                }
            </li>
        );
    });

    protected SortableList = SortableContainer((props: ManipulableListProps<T>) => {
        const className = classnames(
            "manipulable-list",
            props.horizontal ? "horizontal" : "vertical",
            props.className);

        const list = this.props.list;
        return (
            <ol
                ref={this.oListRef}
                className={className}
                tabIndex={this.props.tabIndex}
                onKeyDown={this.handleKeyDown}>
                {list.map((item: T, index: number) =>
                    <this.SortableItem
                        key={this.getKey(item, index)}
                        disabled={!this.props.sortable}
                        index={index}
                        item={item}
                        idx={index} />
                )}
            </ol>
        );
    });

    protected onSortEnd(sort: SortEnd, event: SortEvent) {
        if (sort.oldIndex === sort.newIndex) {
            return;
        }
        const list = this.props.list;
        if (typeof this.props.beforeMoveItem !== 'undefined' &&
            !this.props.beforeMoveItem(list[sort.oldIndex], sort.oldIndex, sort.newIndex)) {
            return;
        }
        const item = list.splice(sort.oldIndex, 1)[0];
        list.splice(sort.newIndex, 0, item);

        let selectIndex = this.state.selectIndex;
        if (selectIndex === sort.oldIndex) {
            selectIndex = sort.newIndex;
        } else if (selectIndex >= sort.newIndex && selectIndex < sort.oldIndex) {
            selectIndex++;
        } else if (selectIndex <= sort.newIndex && selectIndex > sort.oldIndex) {
            selectIndex--;
        }

        const selectIdxChanged = selectIndex !== this.state.selectIndex;
        if (selectIdxChanged) {
            this.setState({ selectIndex: selectIndex });
        }

        if (typeof this.props.onItemMoved !== 'undefined') {
            this.props.onItemMoved(item, sort.oldIndex, sort.newIndex);
        }
        if (selectIdxChanged && typeof this.props.onSelect !== 'undefined') {
            this.props.onSelect(this.props.list[selectIndex], selectIndex);
        }
    }

    public render() {
        const helperClass = classnames(
            "manipulable-list-sort-helper",
            this.props.sortHelperClass);

        return (
            <this.SortableList {...this.props}
                axis={this.props.horizontal ? "x" : "y"}
                lockAxis={this.props.horizontal ? "x" : "y"}
                pressDelay={200}
                lockToContainerEdges={true}
                helperClass={helperClass}
                onSortEnd={this.onSortEnd} />
        );
    }
}

export default ManipulableList;
export { ManipulableListProps, ItemRendererProps };
