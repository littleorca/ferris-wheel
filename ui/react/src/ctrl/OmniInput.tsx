import * as React from "react";
import classnames from "classnames";
import SelectOption from "./SelectOption";
import CheckBox from "./CheckBox";
import "./OmniInput.css";

interface OmniInputProps extends React.ClassAttributes<OmniInput> {
    className?: string;
    style?: React.CSSProperties;
    name?: string;
    label?: string;
    tips?: string;
    value: string[];
    options?: SelectOption[];
    allowManualInput?: boolean;
    afterChange: (value: string[]) => void;
}

interface OmniInputState {
    showSelectedOnly: boolean;
    pendingInput: string;
}

class OmniInput extends React.Component<OmniInputProps, OmniInputState> {
    private listRef: React.RefObject<HTMLUListElement> = React.createRef();

    constructor(props: OmniInputProps) {
        super(props);
        this.state = {
            showSelectedOnly: false,
            pendingInput: "",
        };

        this.handleFilterChange = this.handleFilterChange.bind(this);
        this.handleInput = this.handleInput.bind(this);
        this.handleToggleSelectAll = this.handleToggleSelectAll.bind(this);
        this.handleToggleShowSelectedOnly = this.handleToggleShowSelectedOnly.bind(this);
        this.handleFalseClick = this.handleFalseClick.bind(this);
    }

    protected handleFilterChange(e: React.ChangeEvent<HTMLInputElement>) {
        const filter = e.currentTarget.value;
        this.setState({ pendingInput: filter });
        if (this.listRef.current) {
            this.listRef.current.scroll({
                top: 0,
            });
        }
    }

    protected handleInput(e: React.KeyboardEvent<HTMLInputElement>) {
        if (e.keyCode !== 13) {
            return;
        }
        const value = e.currentTarget.value;
        if (this.isAllowManualInput() && value !== "") {
            if (this.props.value.indexOf(value) === -1) {
                this.setState({ pendingInput: "" });
                this.afterChange(this.props.value.concat(value));
            }
        }
    }

    protected handleToggleSelectAll(newVal: boolean) {
        let newSelection: string[] = [];
        if (newVal && this.props.options) { // select all
            newSelection = this.props.options.map(o => o.value);
        }
        this.afterChange(newSelection);
    }

    protected handleToggleShowSelectedOnly(newVal: boolean) {
        this.setState({ showSelectedOnly: newVal });
    }

    protected handleFalseClick() {
        return false;
    }

    protected checkItem(value: string, checked: boolean) {
        const newValue = [];
        let done = false;
        for (const item of this.props.value) {
            if (item !== value || checked) {
                newValue.push(item);
                if (item === value/* && checked */) {
                    done = true;
                }
            }
        }
        if (checked && !done) {
            newValue.push(value);
        }
        this.afterChange(newValue);
    }

    protected afterChange(newValue: string[]) {
        this.props.afterChange(newValue);
        this.forceUpdate();
    }

    protected sortOptionsByFilter(options: SelectOption[], filter: string) {
        const lowerCaseFilter = filter.toLowerCase();
        const matchesA: SelectOption[] = []; // prefix matches
        const matchesB: SelectOption[] = []; // contains
        const matchesC: SelectOption[] = []; // label contains
        const matchSet = new Set<string>();
        options.forEach(o => {
            const lowerCaseValue = o.value.toLowerCase();
            const pos = lowerCaseValue.indexOf(lowerCaseFilter);
            if (pos === 0) {
                matchesA.push(o);
                matchSet.add(o.value);
            } else if (pos > 0) {
                matchesB.push(o);
                matchSet.add(o.value);
            } else if (typeof o.label !== "undefined" && o.label.toLowerCase().indexOf(lowerCaseFilter) !== -1) {
                matchesC.push(o);
                matchSet.add(o.value);
            }
        });
        const beforeSort = options.splice(0, options.length);
        matchesA.forEach(o => options.push(o));
        matchesB.forEach(o => options.push(o));
        matchesC.forEach(o => options.push(o));
        beforeSort.forEach(o => {
            if (!matchSet.has(o.value)) {
                options.push(o);
            }
        });
    }

    protected isAllowManualInput() {
        return typeof this.props.allowManualInput !== "undefined" ?
            this.props.allowManualInput : typeof this.props.options === "undefined";
    }

    public render() {
        const props = this.props;
        const className = classnames("omni-input", props.className);
        const hasOptions = typeof props.options !== "undefined";
        const allowManualInput = this.isAllowManualInput();
        const originOptionSet = new Set<string>();
        if (typeof props.options !== "undefined") {
            props.options.forEach(o => originOptionSet.add(o.value));
        }
        const selectedSet = new Set<string>();
        const mixedOptions: SelectOption[] = [];
        props.value.forEach(v => {
            if (v !== "") {
                selectedSet.add(v);
                if (!originOptionSet.has(v)) {
                    // those manually input value beyond the given options go first.
                    mixedOptions.push({ value: v });
                }
            }
        });
        let allSelected = true;
        if (typeof props.options !== "undefined") {
            props.options.forEach(o => {
                if (o.value !== "") {
                    mixedOptions.push(o);
                    if (!selectedSet.has(o.value)) {
                        allSelected = false;
                    }
                }
            });
        }
        if (this.state.pendingInput !== "") {
            this.sortOptionsByFilter(mixedOptions, this.state.pendingInput);
        }

        return (
            <div
                className={className}
                style={this.props.style}>
                <div className="omni-input-title">{props.label || props.name}</div>
                <div className="omni-input-control">
                    <label>
                        <span>{!allowManualInput ? "搜索" :
                            hasOptions ? "搜索/输入" : "输入"}</span>
                        <input
                            type="text"
                            value={this.state.pendingInput}
                            onChange={this.handleFilterChange}
                            onKeyDown={this.handleInput} />
                    </label>
                </div>
                <div className="omni-input-options">
                    {hasOptions && (
                        <div className="omni-input-options-actions">
                            <CheckBox
                                name="select-all"
                                label="选择全部"
                                value={allSelected}
                                indeterminate={selectedSet.size > 0 && !allSelected}
                                afterChange={this.handleToggleSelectAll} />
                            <CheckBox
                                name="selected-only"
                                label="仅显示选中"
                                value={this.state.showSelectedOnly}
                                afterChange={this.handleToggleShowSelectedOnly} />
                        </div>
                    )}
                    <ul
                        className="omni-input-options-list"
                        ref={this.listRef}>
                        {mixedOptions.map((o, i) => {
                            const label = typeof o.label !== "undefined" ? o.label : o.value;
                            const selected = selectedSet.has(o.value);
                            if (this.state.showSelectedOnly && !selected) {
                                return null;
                            }
                            const handleClickItem = () => {
                                this.checkItem(o.value, !selected);
                            }
                            return (
                                <li
                                    key={i + ":" + o.value}
                                    className={classnames("omni-input-options-item", { "selected": selected })}
                                    title={label}
                                    onClick={handleClickItem}>
                                    <input
                                        type="checkbox"
                                        defaultChecked={true}
                                        onClick={this.handleFalseClick} />
                                    <span>{label}</span>
                                </li>
                            );
                        })}
                    </ul>
                </div>
                <div className="omni-input-tips">
                    {props.tips}
                </div>
            </div>
        );
    }
}

export default OmniInput;
export { OmniInputProps };
