import * as React from 'react';
import { SortableContainer, SortableElement, SortEnd, SortEvent } from 'react-sortable-hoc';
import './ManipulableList.css';

interface ManipulableListProps<T> extends React.ClassAttributes<ManipulableList<T>> {
    list: T[];
    className?: string;
    sortHelperClass?: string;
    horizontal?: boolean; // display items horizontally
    tabIndex?: number;
    selectable?: boolean; // allow one item among the list has selected state
    sortable?: boolean; // items sortable
    removable?: boolean; // items can be removed
    appendable?: boolean; // new items can be appended
    itemRenderer?: React.SFC<ItemRendererProps<T>>;
    onSelect?: (item: T | null, index: number) => void;
    beforeMoveItem?: (item: T, oldIndex: number, newIndex: number) => boolean;
    onItemMoved?: (item: T, oldIndex: number, newIndex: number) => void;
    beforeRemoveItem?: (item: T, index: number) => boolean;
    onItemRemoved?: (item: T, index: number) => void;
    beforeAppendItem?: (item: T, index: number) => boolean;
    onItemAppended?: (item: T, index: number) => void;
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
    selected: number,
}

class ManipulableList<T> extends React.Component<ManipulableListProps<T>, ManipulableListState<T>> {

    protected static defaultProps: Partial<ManipulableListProps<any>> = {
        tabIndex: 0,
        selectable: true,
        sortable: true,
        removable: true,
        appendable: true,
    };

    protected oListRef: React.RefObject<HTMLOListElement>;
    protected sequence: number = 0;

    constructor(props: ManipulableListProps<T>) {
        super(props);

        this.oListRef = React.createRef();

        this.handleKeyDown = this.handleKeyDown.bind(this);
        this.handleClick = this.handleClick.bind(this);
        this.onSortEnd = this.onSortEnd.bind(this);

        const selected = props.list.length > 0 ? 0 : -1;
        this.state = { selected };
        if (typeof props.onSelect !== 'undefined') {
            props.onSelect(selected === -1 ? null : props.list[selected], selected);
        }
    }

    public componentDidUpdate(prevProps: ManipulableListProps<T>) {
        const selected = this.getSelectedIndex();
        if (selected !== this.state.selected ||
            this.props.list[selected] !== prevProps.list[selected]) {
            this.selectItem(selected, false);
        }
    }

    public getSelectedIndex(): number {
        let selected = this.state.selected;
        if (selected >= this.props.list.length) {
            selected = this.props.list.length - 1;
        } else if (selected < 0 && this.props.list.length > 0) {
            selected = 0;
        }
        return selected;
    }

    public getSelectedItem(): T | null {
        const selected = this.getSelectedIndex();
        if (selected === -1) {
            return null;
        }
        return this.props.list[selected];
    }

    public addItem(item: T): void {
        if (!this.props.appendable) {
            throw new Error('List not appendable.');
        }
        if (typeof this.props.beforeAppendItem !== 'undefined' &&
            !this.props.beforeAppendItem(item, this.props.list.length)) {
            return;
        }
        this.props.list.push(item);
        this.selectItem(this.props.list.length - 1);
        if (typeof this.props.onItemAppended !== 'undefined') {
            this.props.onItemAppended(item, this.props.list.length - 1);
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
        return item.toString();
    }

    protected handleKeyDown(event: React.KeyboardEvent) {
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
                if (this.props.appendable &&
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

    public tryRemoveSelected() {
        const list = this.props.list;
        const index = this.state.selected;
        if (index === -1) {
            return;
        }
        const item = list[index];
        if (typeof this.props.beforeRemoveItem !== 'undefined' &&
            !this.props.beforeRemoveItem(item, index)) {
            return;
        }
        list.splice(index, 1);
        const selected = (index >= list.length) ? index - 1 : index;
        if (typeof this.props.onItemRemoved !== 'undefined') {
            this.props.onItemRemoved(item, index);
        }
        this.selectItem(selected);
    }

    protected trySelectPrevious() {
        const list = this.props.list;
        if (list.length === 0) {
            return;
        }
        let selected = this.state.selected - 1;
        if (selected < 0) {
            selected = list.length - 1;
        }
        this.selectItem(selected);
    }

    protected trySelectNext() {
        const list = this.props.list;
        if (list.length === 0) {
            return;
        }
        let selected = this.state.selected + 1;
        if (selected >= list.length) {
            selected = 0;
        }
        this.selectItem(selected);
    }

    public selectItem(index: number, focus: boolean = true) {
        const list = this.props.list;
        if (index !== -1 && (index < 0 || index >= list.length)) {
            throw new Error('Selected index is out of range.');
        }
        this.setState({ selected: index });
        // TODO scroll selected item into view if needed!
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
        if (this.state.selected !== index) {
            this.selectItem(index);
        }
    }

    protected SortableItem = SortableElement((props: ManipulableElementProps<T>) => {
        const idx = props.idx;
        const selected = this.getSelectedIndex();

        const className = "manipulable-item" +
            (idx === selected ? " selected" : "");

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
        const className = "manipulable-list" +
            (props.horizontal ? " horizontal" : " vertical") +
            (typeof props.className !== 'undefined' ?
                " " + props.className : "");

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

        let selected = this.state.selected;
        if (selected === sort.oldIndex) {
            selected = sort.newIndex;
        } else if (selected >= sort.newIndex && selected < sort.oldIndex) {
            selected++;
        } else if (selected <= sort.newIndex && selected > sort.oldIndex) {
            selected--;
        }

        const selectIdxChanged = selected !== this.state.selected;
        if (selectIdxChanged) {
            this.setState({ selected });
        }

        if (typeof this.props.onItemMoved !== 'undefined') {
            this.props.onItemMoved(item, sort.oldIndex, sort.newIndex);
        }
        if (selectIdxChanged && typeof this.props.onSelect !== 'undefined') {
            this.props.onSelect(this.props.list[selected], selected);
        }
    }

    public render() {
        const helperClass = "manipulable-list-sort-helper" +
            (typeof this.props.sortHelperClass !== 'undefined' ?
                " " + this.props.sortHelperClass : "");

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
