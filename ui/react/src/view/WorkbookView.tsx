import * as React from 'react';
import Workbook from '../model/Workbook';
import SharedViewProps from './SharedViewProps';
import Sheet from '../model/Sheet';
import SheetView from './SheetView';
import ManipulableList from '../ctrl/ManipulableList';
import EditableText from '../ctrl/EditableText';
import { ItemRendererProps } from '../ctrl/ManipulableList';
import Action from '../action/Action';
import ActionHandler from '../action/ActionHandler';
import ActionHerald from '../action/ActionHerald';
import SelectAsset from '../action/SelectAsset';
import RenameSheet from '../action/RenameSheet';
import MoveSheet from '../action/MoveSheet';
import RemoveSheet from '../action/RemoveSheet';
import AddSheet from '../action/AddSheet';
import TransferAsset from '../action/TransferAsset';
import Button from '../ctrl/Button';
import './WorkbookView.css';

interface WorkbookViewProps extends SharedViewProps<WorkbookView> {
    workbook: Workbook,
    className?: string,
}

interface WorkbookViewState {
    selected?: Sheet;
}

class WorkbookView extends React.Component<WorkbookViewProps, WorkbookViewState> implements ActionHerald {
    protected static defaultProps: Partial<WorkbookViewProps> = {
        editable: false,
    };
    private tabsRef: React.RefObject<ManipulableList<Sheet>> = React.createRef();
    private listeners: Set<ActionHandler> = new Set();

    constructor(props: WorkbookViewProps) {
        super(props);

        this.state = {
            selected: props.workbook.sheets[0],
        };
        this.sendSelectSheetAction(props.workbook.sheets[0]);

        this.onApplyAction = this.onApplyAction.bind(this);
        this.getTabItemLabel = this.getTabItemLabel.bind(this);
        this.handleClickAddSheet = this.handleClickAddSheet.bind(this);
        this.handleAddSheet = this.handleAddSheet.bind(this);
        this.handleRenameSheet = this.handleRenameSheet.bind(this);
        this.handleRemoveSheet = this.handleRemoveSheet.bind(this);
        this.handleMoveSheet = this.handleMoveSheet.bind(this);
        this.handleSelectSheet = this.handleSelectSheet.bind(this);
    }

    public componentDidUpdate(prevProps: WorkbookViewProps) {
        if (this.props.workbook !== prevProps.workbook) {
            this.onSelectSheet(this.props.workbook.sheets[0]);
        }
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

    public subscribe(handler: ActionHandler) {
        this.listeners.add(handler);
    }

    public unsubscribe(handler: ActionHandler) {
        this.listeners.delete(handler);
    }

    protected onApplyAction(action: Action) {
        if (typeof action.addSheet !== 'undefined') {
            this.applyAddSheet(action.addSheet);
        } else if (typeof action.moveSheet !== 'undefined') {
            this.applyMoveSheet(action.moveSheet);
        } else if (typeof action.removeSheet !== 'undefined') {
            this.applyRemoveSheet(action.removeSheet);
        } else if (typeof action.renameSheet !== 'undefined') {
            this.applyRenameSheet(action.renameSheet);
        } else if (typeof action.transferAsset !== 'undefined') {
            this.applyTransferAsset(action.transferAsset);
        } else if (typeof action.createWorkbook !== 'undefined') {
        } else if (typeof action.openWorkbook !== 'undefined') {
        } else if (typeof action.saveWorkbook !== 'undefined') {
        } else if (typeof action.closeWorkbook !== 'undefined') {
        } else {
            this.listeners.forEach(handler => handler(action));
        }
    }

    protected applyAddSheet(addSheet: AddSheet) {
        const sheets = this.props.workbook.sheets;
        const newSheet = new Sheet(addSheet.sheetName);
        sheets.splice(addSheet.index, 0, newSheet);
        if (this.tabsRef.current !== null) {
            this.tabsRef.current.selectItem(addSheet.index);
        } else {
            throw new Error("Tabs reference not available!");
        }
    }

    protected applyMoveSheet(moveSheet: MoveSheet) {
        const sheets = this.props.workbook.sheets;
        let fromIndex: number = -1;
        for (let i = 0; i < sheets.length; i++) {
            if (sheets[i].name === moveSheet.sheetName) {
                fromIndex = i;
                break;
            }
        }
        if (fromIndex === -1) {
            throw new Error("There is no sheet with name " + moveSheet.sheetName);
        }
        const sheet = sheets[fromIndex];
        sheets.splice(fromIndex, 1);
        sheets.splice(moveSheet.targetIndex, 0, sheet);
        if (this.tabsRef.current !== null) {
            const selectedIndex = this.tabsRef.current.getSelectedIndex();
            if (selectedIndex === fromIndex) {
                this.tabsRef.current.selectItem(moveSheet.targetIndex);
            } else if (selectedIndex >= moveSheet.targetIndex &&
                selectedIndex < fromIndex) {
                this.tabsRef.current.selectItem(selectedIndex + 1);
            } else if (selectedIndex > fromIndex &&
                selectedIndex <= moveSheet.targetIndex) {
                this.tabsRef.current.selectItem(selectedIndex - 1);
            } else {
                // any thing todo?
            }
        } else {
            throw new Error("Tabs reference not available!");
        }
    }

    protected applyRemoveSheet(removeSheet: RemoveSheet) {
        const sheets = this.props.workbook.sheets;
        let index: number = -1;
        for (let i = 0; i < sheets.length; i++) {
            if (sheets[i].name === removeSheet.sheetName) {
                index = i;
                break;
            }
        }
        if (index === -1) {
            throw new Error("There is no sheet with name " + removeSheet.sheetName);
        }
        const removed = sheets[index];
        sheets.splice(index, 1);
        if (removed === this.state.selected) {
            if (index >= sheets.length) {
                index--;
            }
            if (this.tabsRef.current !== null) {
                this.tabsRef.current.selectItem(index);
            }
        } else {
            this.forceUpdate();
        }
    }

    protected applyRenameSheet(renameSheet: RenameSheet) {
        for (const sheet of this.props.workbook.sheets) {
            if (sheet.name === renameSheet.oldSheetName) {
                sheet.name = renameSheet.newSheetName;
                this.forceUpdate();
                break;
            }
        }
    }

    protected applyTransferAsset(transferAsset: TransferAsset) {
        // TODO
    }

    protected getTabItemLabel(sheet: Sheet) {
        return sheet.name;
    }

    protected SheetTabRenderer = (props: ItemRendererProps<Sheet>) => {
        const afterChange = (name: string) => {
            // avoid update sheet name directly
            const sheet = new Sheet(name);
            props.updateItem(sheet);
        };

        const onRemove = (buttonName: string, event: React.MouseEvent<HTMLButtonElement>) => {
            this.handleRemoveSheet(props.value);
            // default behavior will active the tab as the button is inside the tab.
            event.stopPropagation();
            event.preventDefault();
        };

        return (
            <span
                className="sheet-tab">
                <EditableText
                    value={props.value.name}
                    readOnly={!this.props.editable}
                    afterChange={afterChange} />
                {this.props.editable && (
                    <Button
                        name={"remove-sheet" + props.value.name}
                        label="x"
                        tips="删除该 Sheet"
                        onClick={onRemove} />
                )}
            </span>
        );
    };

    protected handleSelectSheet(sheet: Sheet | null, index: number) {
        this.onSelectSheet(sheet || undefined);
    }

    protected onSelectSheet(sheet: Sheet | undefined) {
        this.setState({
            selected: sheet,
        });
        this.sendSelectSheetAction(sheet);
    }

    protected sendSelectSheetAction(sheet?: Sheet) {
        if (typeof this.props.onAction !== 'undefined') {
            const selectAsset = new SelectAsset(sheet);
            this.props.onAction(selectAsset.wrapper());
        }
    }

    protected handleClickAddSheet() {
        if (typeof this.props.onAction === 'undefined') {
            throw new Error('Add sheet action occurred unexpectedly.');
        }

        const namePrefix = "sheet";
        let i = 1;
        let name;

        const isUnique = (pendingName: string) => {
            for (const sheet of this.props.workbook.sheets) {
                if (sheet.name === pendingName) {
                    return false;
                }
            }
            return true;
        };

        while (true) {
            name = namePrefix + i;
            if (isUnique(name)) {
                break;
            } else {
                i++;
            }
        }

        const addSheet = new AddSheet(name, this.props.workbook.sheets.length);
        this.props.onAction(addSheet.wrapper());
    }

    protected handleAddSheet(sheet: Sheet, index: number) {
        if (typeof this.props.onAction !== 'undefined') {
            const addSheet = new AddSheet(sheet.name, index);
            this.props.onAction(addSheet.wrapper());
        }
        return false;
    }

    protected handleRenameSheet(oldSheet: Sheet, newSheet: Sheet, index: number) {
        if (typeof this.props.onAction !== 'undefined') {
            const renameSheet = new RenameSheet(oldSheet.name, newSheet.name);
            this.props.onAction(renameSheet.wrapper());
        }
        return false; // always stop renaming directly and wait for server side notification
    }

    protected handleRemoveSheet(sheet: Sheet) {
        if (typeof this.props.onAction !== 'undefined') {
            const removeSheet = new RemoveSheet(sheet.name);
            this.props.onAction(removeSheet.wrapper());
        }
    }

    protected handleMoveSheet(sheet: Sheet, oldIndex: number, newIndex: number) {
        if (typeof this.props.onAction !== 'undefined') {
            const moveSheet = new MoveSheet(sheet.name, newIndex);
            this.props.onAction(moveSheet.wrapper());
        }
        return false;
    }

    public render() {
        const className = "workbook-view" +
            (this.props.editable ? " editable" : " presentation") +
            (typeof this.props.className !== 'undefined' ?
                " " + this.props.className : "");

        const sheets = this.props.workbook.sheets;

        return (
            <div className={className}>
                <div className="sheet-tabs">
                    {this.props.editable &&
                        typeof this.props.onAction !== 'undefined' && (
                            <Button
                                name="add-sheet"
                                label="+"
                                tips="添加新的 Sheet"
                                onClick={this.handleClickAddSheet} />
                        )}
                    <ManipulableList<Sheet>
                        ref={this.tabsRef}
                        sortHelperClass="sheet-tab-sort-helper"
                        horizontal={true}
                        list={sheets}
                        appendable={false} // disable ManipulableList's default behavior
                        sortable={this.props.editable}
                        removable={false} // disable ManipulableList's default behavior
                        getItemLabel={this.getTabItemLabel}
                        itemRenderer={this.SheetTabRenderer}
                        // beforeAppendItem={this.handleAddSheet}
                        beforeUpdateItem={this.handleRenameSheet}
                        // beforeRemoveItem={this.handleRemoveSheet}
                        beforeMoveItem={this.handleMoveSheet}
                        onSelect={this.handleSelectSheet} />
                </div>

                {this.state.selected && (
                    <SheetView
                        key={this.state.selected.name}
                        sheet={this.state.selected}
                        editable={this.props.editable}
                        onAction={this.props.onAction}
                        herald={this} />
                )}
            </div>
        );
    }
}

export default WorkbookView;
export { WorkbookViewProps };
