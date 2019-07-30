import * as React from 'react';
import * as ReactDOM from 'react-dom';
import UnionValue from '../model/UnionValue';
import EditBox from './EditBox';
import Values from '../model/Values';
import { VariantType } from '../model/Variant';
import * as Popover from "react-popover";
import DatePicker from "react-datepicker";
import * as moment from 'moment';
import Toolbar, { Group } from './Toolbar';
import Button from './Button';
import classnames from "classnames";
import UnionValueListEdit from './UnionValueListEdit';
import ValueChange from './ValueChange';
import './UnionValueEdit.css';

type UnionValueEditMode = "formula" | "decimal" | "boolean" | "date" | "string" | "list";

type UnionValueEditAux = "outside" | "none"; // TODO not really implemented.

interface UnionValueEditProps extends React.ClassAttributes<UnionValueEdit> {
    value: UnionValue;
    initialUpdate?: UnionValue;
    id?: string;
    name?: string;
    placeholder?: string;
    className?: string;
    style?: React.CSSProperties;
    disableCommitOnEnter?: boolean;
    autoExpand?: boolean;
    focusByDefault?: boolean;
    readOnly?: boolean;
    disabled?: boolean;
    modes?: UnionValueEditMode[];
    aux?: UnionValueEditAux;
    afterChange: (change: ValueChange<UnionValue>) => void;
    afterEndEdit?: () => void;
}

interface UnionValueEditState {
    active: boolean;
    originValue: UnionValue;
    originModes?: UnionValueEditMode[];
    allowedModes: Set<UnionValueEditMode>;
    currentMode?: UnionValueEditMode;
    currentValue: UnionValue;
    valid: boolean;
}

class UnionValueEdit extends React.Component<UnionValueEditProps, UnionValueEditState> {
    private editBoxRef = React.createRef<EditBox>();
    private toolbarRef = React.createRef<Toolbar>();
    private auxRef = React.createRef<HTMLDivElement>();

    protected static getDerivedStateFromProps(nextProps: UnionValueEditProps,
        prevState: UnionValueEditState): UnionValueEditState | null {
        if (nextProps.value !== prevState.originValue || nextProps.modes !== prevState.originModes) {
            const allowedModes = UnionValueEdit.getAllowedModes(nextProps.modes);
            let currentValue = prevState.currentValue;
            if (nextProps.value !== prevState.originValue) {
                currentValue = (typeof nextProps.initialUpdate !== "undefined") ?
                    nextProps.initialUpdate : nextProps.value;
            }
            const currentMode = UnionValueEdit.getLegalEditMode(currentValue, allowedModes);
            return {
                ...prevState,
                originValue: nextProps.value,
                originModes: nextProps.modes,
                allowedModes,
                currentMode,
                currentValue,
                valid: typeof currentMode !== "undefined",
            };

        } else {
            return null;
        }
    }

    protected static getAllowedModes(modes?: UnionValueEditMode[]) {
        const allowedModes = new Set<UnionValueEditMode>();
        if (typeof modes !== "undefined") {
            modes.forEach(m => allowedModes.add(m));
        } else {
            allowedModes.add("formula");
            allowedModes.add("decimal");
            allowedModes.add("boolean");
            allowedModes.add("date");
            allowedModes.add("string");
            allowedModes.add("list");
        }
        return allowedModes;
    }

    protected static getLegalEditMode(value: UnionValue, allowedModes: Set<UnionValueEditMode>): UnionValueEditMode | undefined {
        const mode = UnionValueEdit.getEditModeByValue(value);
        if (typeof mode === "undefined" || !allowedModes.has(mode)) {
            return undefined;
        }
        return mode;
    }

    protected static getEditModeByValue(value: UnionValue): UnionValueEditMode | undefined {
        if (value.isFormula()) {
            return "formula";
        }

        switch (value.valueType()) {
            case VariantType.LIST: return "list";
            case VariantType.DECIMAL: return "decimal";
            case VariantType.BOOL: return "boolean";
            case VariantType.DATE: return "date";
            case VariantType.STRING: return "string";
            default: return undefined;
        }
    }

    constructor(props: UnionValueEditProps) {
        super(props);
        const allowedModes = UnionValueEdit.getAllowedModes(props.modes);
        const currentValue = (typeof props.initialUpdate !== "undefined") ?
            props.initialUpdate : props.value;
        const currentMode = UnionValueEdit.getLegalEditMode(currentValue, allowedModes);
        this.state = {
            active: false,
            originValue: props.value,
            originModes: props.modes,
            allowedModes,
            currentMode,
            currentValue,
            valid: typeof currentMode !== "undefined",
        };

        this.handleGlobalMouseDown = this.handleGlobalMouseDown.bind(this);
        this.handleGlobalKeyDown = this.handleGlobalKeyDown.bind(this);
        this.handleEditBoxBeginEdit = this.handleEditBoxBeginEdit.bind(this);
        this.handleEditBoxEndEdit = this.handleEditBoxEndEdit.bind(this);
        this.beforeEditBoxChange = this.beforeEditBoxChange.bind(this);
        this.afterEditBoxChange = this.afterEditBoxChange.bind(this);
        this.handleEditBoxClick = this.handleEditBoxClick.bind(this);
        this.handleDateChange = this.handleDateChange.bind(this);
        this.handleListChange = this.handleListChange.bind(this);
        this.handleModeClick = this.handleModeClick.bind(this);
        this.handleCloseAux = this.handleCloseAux.bind(this);
    }

    public componentDidMount() {
        document.addEventListener("mousedown", this.handleGlobalMouseDown);
        document.addEventListener("keydown", this.handleGlobalKeyDown);
    }

    protected handleGlobalMouseDown(event: MouseEvent) {
        const target = event.target;
        if (!this.state.active || !target) {
            return;
        }
        const editBox = ReactDOM.findDOMNode(this.editBoxRef.current);
        if (editBox && editBox.contains(target as Element)) {
            return;
        }
        const tollbar = ReactDOM.findDOMNode(this.toolbarRef.current);
        if (tollbar && tollbar.contains(target as Element)) {
            event.preventDefault();
            return;
        }
        const aux = ReactDOM.findDOMNode(this.auxRef.current);
        if (aux && aux.contains(target as Element)) {
            if (this.state.currentMode === "date") {
                event.preventDefault();
            }
            return;
        }
    }

    protected handleGlobalKeyDown(event: KeyboardEvent) {
        if (event.key !== "Escape" || !this.state.active) {
            return;
        }
        if (this.state.currentMode === "list") {
            event.stopImmediatePropagation();
            if (this.props.value === this.state.currentValue) {
                this.setState({ active: false });
            } else {
                const change: ValueChange<UnionValue> = {
                    id: this.props.id,
                    name: this.props.name,
                    fromValue: this.state.currentValue,
                    toValue: this.props.value,
                    type: "rollback",
                };
                this.processChange(change, true);
            }
        }
    }

    public componentWillUnmount() {
        document.removeEventListener("mousedown", this.handleGlobalMouseDown);
        document.removeEventListener("keydown", this.handleGlobalKeyDown);
    }

    protected handleEditBoxBeginEdit() {
        // let showCalendar = false;
        // if (this.state.allowedModes.has("date") && this.state.currentMode === "date") {
        //     showCalendar = true;
        // }
        this.setState({
            active: true,
            // showCalendar
        });
    }

    protected handleEditBoxEndEdit() {
        this.setState({ active: false });
        // since we keep sync pending value to EditBox, EditBox won't fire the final(after blur)
        // commit/rollback as it is not dirty. UnionValueEdit should deal this.
        if (this.props.value !== this.state.currentValue) {
            let change: ValueChange<UnionValue>;
            if (this.state.valid) {
                change = {
                    id: this.props.id,
                    name: this.props.name,
                    fromValue: this.props.value,
                    toValue: this.state.currentValue,
                    type: "commit",
                };
            } else {
                change = {
                    id: this.props.id,
                    name: this.props.name,
                    fromValue: this.state.currentValue,
                    toValue: this.props.value,
                    type: "rollback",
                };
            }
            this.processChange(change);
        }
        if (typeof this.props.afterEndEdit !== "undefined") {
            this.props.afterEndEdit();
        }
    }

    protected beforeEditBoxChange(change: ValueChange<string>) {
        if (change.type === "rollback") {
            return true;
        }
        const newValue = fromEditableString(change.toValue);
        const mode = UnionValueEdit.getLegalEditMode(newValue, this.state.allowedModes);
        if (typeof mode !== "undefined") {
            return true;
        }
        if (change.type === "commit") {
            return false;
        }
        // try to fix
        let targetMode = this.state.currentMode;
        if (!targetMode && this.state.allowedModes.size === 1) {
            targetMode = this.state.allowedModes.keys().next().value;
        }
        switch (targetMode) {
            case "formula":
                if (change.toValue === "") {
                    change.toValue = "=";
                }
                break;
            case "decimal":
                // if (change.toValue.startsWith("'") && !isNaN(Number(change.toValue.substring(1)))) {
                //     change.toValue = change.toValue.substring(1);
                //     return true;
                // }
                break;
            case "boolean":
                // if (change.toValue.toLowerCase() === "'true") {
                //     change.toValue = "true";
                //     return true;
                // } else if (change.toValue.toLowerCase() === "'false") {
                //     change.toValue = "false";
                //     return true;
                // }
                break;
            case "date":
                // if (change.toValue.startsWith("'")) {
                //     const m = moment(change.toValue.substring(1), moment.ISO_8601);
                //     if (m.isValid()) {
                //         change.toValue = change.toValue.substring(1);
                //         return true;
                //     }
                // }
                break;
            case "string":
                change.toValue = "'" + change.toValue;
                break;
        }
        return true;
    }

    protected afterEditBoxChange(change: ValueChange<string>) {
        let fromValue = this.state.currentValue;
        let toValue = fromEditableString(change.toValue);
        if (change.type === "rollback") {
            toValue = this.props.value;
        } else if (change.type === "commit") {
            fromValue = this.props.value;
        }
        this.processChange({
            id: this.props.id,
            name: this.props.name,
            fromValue,
            toValue,
            type: change.type,
        });
    }

    protected handleEditBoxClick() {
        if (this.state.currentMode === "list" && !this.state.active) {
            this.setState({
                active: true
            })
        }
    }

    protected handleModeSwitch(mode: UnionValueEditMode) {
        const editBox = this.editBoxRef.current;
        if (!editBox) {
            return;
        }
        const change: ValueChange<UnionValue> = {
            id: this.props.id,
            name: this.props.name,
            toValue: Values.blank(),
            type: "edit"
        };

        if (mode === this.state.currentMode) {
            if (mode === "boolean") {
                // for boolean value, we can toggle true/false
                change.fromValue = fromEditableString(editBox.getCurrentValue());
                change.toValue = Values.bool(!change.fromValue.booleanValue());
                this.processChange(change);
            }
            return;
        }

        change.toValue = this.createDefaultValue(mode);
        this.processChange(change, true);
    }

    protected createDefaultValue(mode: UnionValueEditMode): UnionValue {
        switch (mode) {
            case "formula": return Values.formula("");
            case "decimal": return Values.dec(0);
            case "boolean": return Values.bool(false);
            case "date": return Values.date(new Date());
            case "string": return Values.str("");
            case "list": return Values.list([]);
            default: return Values.blank();
        }
    }

    protected createDefaultEditableString(value: UnionValue, mode: UnionValueEditMode | undefined) {
        if (typeof mode === "undefined" && this.state.allowedModes.size === 1) {
            mode = this.state.allowedModes.keys().next().value;
        }
        let editableString: string;
        // FIXME actually list value cannot be edit at present, should either raise exception or make it editable.
        if (!value.isFormula() && value.valueType() === VariantType.LIST) {
            editableString = "[";
            value.listValue().forEach(v => {
                if (editableString.length > 1) {
                    editableString += "; ";
                }
                editableString += toEditableString(v);
            });
            editableString += "]";
        } else {
            editableString = toEditableString(value);
            if (editableString === "" && mode === "formula") {
                editableString = "=";
            }
        }
        return editableString;
    }

    protected handleDateChange(date: Date | null, event: React.SyntheticEvent<any> | undefined) {
        const editBox = this.editBoxRef.current
        if (!editBox || date === null) {
            return;
        }
        const dateValue = Values.date(date);
        this.processChange({
            id: this.props.id,
            name: this.props.name,
            fromValue: this.props.value,
            toValue: dateValue,
            type: "edit"
        });
    }

    protected handleListChange(list: UnionValue[]) {
        const listValue = Values.list(list);
        this.processChange({
            id: this.props.id,
            name: this.props.name,
            fromValue: this.props.value,
            toValue: listValue,
            type: "edit"
        });
    }

    protected handleModeClick(name: string) {
        switch (name) {
            case "formula":
            case "decimal":
            case "boolean":
            case "date":
            case "string":
            case "list":
                this.handleModeSwitch(name);
                break;
            default: // should never happen
                throw new Error("Unknown mode name: " + name);
        }
    }

    protected handleCloseAux(event: Event) {
        this.setState({ active: false });
        if (this.state.currentValue === this.props.value) {
            return;
        }
        const change: ValueChange<UnionValue> = {
            id: this.props.id,
            name: this.props.name,
            fromValue: this.props.value,
            toValue: this.state.currentValue,
            type: "commit"
        };
        this.processChange(change);
    }

    protected processChange(change: ValueChange<UnionValue>, forceFocus: boolean = false) {
        const mode = UnionValueEdit.getLegalEditMode(change.toValue, this.state.allowedModes);
        const valid = typeof mode !== "undefined";
        this.setState({
            currentMode: mode,
            currentValue: change.toValue,
            valid
        }, () => {
            if (forceFocus && mode !== "list" && this.editBoxRef.current) {
                this.editBoxRef.current.focus();
            }
        });

        if (!valid) {
            return;
        }

        if (typeof this.props.afterChange !== 'undefined') {
            this.props.afterChange(change);
        }
    }

    public render() {
        const className = classnames(
            "union-value-edit",
            this.props.className,
            "aux-" + (this.props.aux || "outside"),
            {
                "active": this.state.active,
                "invalid": !this.state.valid,
                "readOnly": this.props.readOnly,
                "disabled": this.props.disabled,
            }
        );
        const modeClassName = classnames(
            "union-value-edit-mode",
            "mode-" + this.state.currentMode || "blank"
        );
        const popoverClassName = classnames(
            "union-value-edit-popover",
            "mode-" + this.state.currentMode || "blank",
            {
                "active": this.state.active,
                "invalid": !this.state.valid,
                "readOnly": this.props.readOnly,
                "disabled": this.props.disabled,
            }
        );
        const value = this.state.currentValue;
        const editableString = this.createDefaultEditableString(value, this.state.currentMode);
        const editable = !this.props.readOnly && !this.props.disabled;
        // const allowMultiline = typeof this.props.modes === "undefined" ||
        //     this.props.modes.indexOf("formula") !== -1 ||
        //     this.props.modes.indexOf("string") !== -1;
        const allowMultiline = this.state.currentMode !== "decimal" &&
            this.state.currentMode !== "boolean" &&
            this.state.currentMode !== "date";
        const showCalendar = this.state.active && editable && this.state.allowedModes.has("date") && this.state.currentMode === "date";
        const showListEdit = this.state.active && this.state.allowedModes.has("list") && this.state.currentMode === "list";
        const openPopover = showCalendar || showListEdit;

        return (
            <Popover
                className={popoverClassName}
                isOpen={openPopover}
                onOuterAction={this.handleCloseAux}
                preferPlace="below"
                tipSize={0.01}
                enterExitTransitionDurationMs={0}
                body={(
                    <div
                        ref={this.auxRef}
                        className="union-value-edit-aux">
                        {showCalendar && (
                            <DatePicker
                                inline={true}
                                dateFormat={"YYYY-MM-DD HH:mm:ss"}
                                selected={value.valueType() === VariantType.DATE ?
                                    value.dateValue() : new Date()}
                                showTimeSelect={true}
                                onChange={this.handleDateChange} />
                        )}
                        {showListEdit && (
                            <UnionValueListEdit
                                list={value.valueType() === VariantType.LIST ? value.listValue() : []}
                                modes={this.props.modes}
                                addible={editable}
                                removable={editable}
                                sortable={editable}
                                afterChange={this.handleListChange}
                            />
                        )}
                    </div>
                )}>
                <div
                    className={className}
                    style={this.props.style}>
                    <EditBox
                        ref={this.editBoxRef}
                        id={this.props.id}
                        name={this.props.name}
                        placeholder={this.props.placeholder}
                        value={editableString}
                        multiline={allowMultiline}
                        forceCommitOnEnter={!this.props.disableCommitOnEnter}
                        autoExpand={this.props.autoExpand}
                        focusByDefault={this.props.focusByDefault || this.state.active}
                        afterBeginEdit={this.handleEditBoxBeginEdit}
                        beforeChange={this.beforeEditBoxChange}
                        afterChange={this.afterEditBoxChange}
                        afterEndEdit={this.handleEditBoxEndEdit}
                        onClick={this.handleEditBoxClick}
                        readOnly={this.state.currentMode === "list"}
                        disabled={this.props.disabled}
                    />
                    {this.props.aux !== "none" && (
                        <Toolbar
                            ref={this.toolbarRef}
                            className={modeClassName}>
                            <Group>
                                {this.state.allowedModes.has("formula") && (
                                    <Button
                                        name="formula"
                                        label="𝑓"
                                        tips="formula"
                                        className={classnames({
                                            "mode-current": "formula" === this.state.currentMode
                                        })}
                                        disabled={!editable}
                                        onClick={this.handleModeClick} />
                                )}
                                {this.state.allowedModes.has("decimal") && (
                                    <Button
                                        name="decimal"
                                        label="½"
                                        tips="decimal"
                                        className={classnames({
                                            "mode-current": "decimal" === this.state.currentMode
                                        })}
                                        disabled={!editable}
                                        onClick={this.handleModeClick} />
                                )}
                                {this.state.allowedModes.has("boolean") && (
                                    <Button
                                        name="boolean"
                                        label="𝔹"
                                        tips="boolean"
                                        className={classnames({
                                            "mode-current": "boolean" === this.state.currentMode
                                        })}
                                        disabled={!editable}
                                        onClick={this.handleModeClick} />
                                )}
                                {this.state.allowedModes.has("date") && (
                                    <Button
                                        name="date"
                                        label="☀"
                                        tips="date"
                                        className={classnames({
                                            "mode-current": "date" === this.state.currentMode
                                        })}
                                        disabled={!editable}
                                        onClick={this.handleModeClick} />
                                )}
                                {this.state.allowedModes.has("string") && (
                                    <Button
                                        name="string"
                                        label="“"
                                        tips="string"
                                        className={classnames({
                                            "mode-current": "string" === this.state.currentMode
                                        })}
                                        disabled={!editable}
                                        onClick={this.handleModeClick} />
                                )}
                                {this.state.allowedModes.has("list") && (
                                    <Button
                                        name="list"
                                        label="≡"
                                        tips="list"
                                        className={classnames({
                                            "mode-current": "list" === this.state.currentMode
                                        })}
                                        onClick={this.handleModeClick} />
                                )}
                            </Group>
                        </Toolbar>
                    )}
                </div>
            </Popover>
        );
    }
}

function fromEditableString(editableString: string): UnionValue {
    if (editableString.startsWith('=')) {
        return Values.formula(editableString.substring(1));
    } else if (editableString.startsWith('\'')) {
        return Values.str(editableString.substring(1));
    } else if (editableString === '') {
        return Values.blank();
    } else if (!isNaN(Number(editableString))) {
        return Values.dec(editableString);
    } else if ('true' === editableString.toLowerCase()) {
        return Values.bool(true);
    } else if ('false' === editableString.toLowerCase()) {
        return Values.bool(false);
    } else {
        const m = moment(editableString, moment.ISO_8601);
        if (m.isValid()) {
            return Values.date(m.toDate());
        } else {
            return Values.str(editableString);
        }
    }
}

function toEditableString(value: UnionValue): string {
    if (value.isFormula()) {
        return '=' + value.getFormulaString();
    } else if (value.isBlank()) {
        return '';
    } else {
        switch (value.valueType()) {
            case VariantType.BLANK:
            case VariantType.DECIMAL:
            case VariantType.BOOL:
            case VariantType.DATE:
                return value.toString() || '';
            case VariantType.STRING:
                const s = value.strValue();
                if (s === null || s === '') {
                    return '';
                } else if (s.startsWith("'") ||
                    s.startsWith("=") ||
                    'true' === s.toLowerCase() ||
                    'false' === s.toLowerCase() ||
                    !isNaN(Number(s)) ||
                    moment(s, moment.ISO_8601).isValid()) {
                    return '\'' + s;
                } else {
                    return s;
                }
            case VariantType.ERROR:
            case VariantType.LIST:
            default:
                throw new Error('Not editable!');
        }
    }
}

export default UnionValueEdit;
export {
    UnionValueEditMode,
    UnionValueEditAux,
    UnionValueEditProps,
    fromEditableString,
    toEditableString
}
