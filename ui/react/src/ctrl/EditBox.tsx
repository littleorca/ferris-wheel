import * as React from "react";
import ValueChange from "./ValueChange";
import classnames from "classnames";
import "./EditBox.css";

export interface EditBoxProps extends React.ClassAttributes<EditBox> {
    value: string;
    /**
     * Initial update value, treated as non-committed, and purposed for eidt suggestion.
     */
    initialUpdate?: string; // Initial update
    id?: string;
    name?: string;
    placeholder?: string;
    multiline?: boolean;
    className?: string;
    style?: React.CSSProperties;
    readOnly?: boolean;
    disabled?: boolean;
    focusByDefault?: boolean;
    selectOnFocus?: boolean;
    forceCommitOnEnter?: boolean;
    autoExpand?: boolean;
    afterBeginEdit?: () => void;
    beforeChange?: (change: ValueChange<string>) => boolean;
    afterChange?: (change: ValueChange<string>) => void;
    afterEndEdit?: () => void;
    onClick?: (event: React.MouseEvent<HTMLElement>) => void;
}

interface EditBoxState {
    originValue: string;
    currentValue: string;
}

class EditBox extends React.Component<EditBoxProps, EditBoxState> {
    protected textareaRef = React.createRef<HTMLTextAreaElement>();

    protected static getDerivedStateFromProps(nextProps: EditBoxProps, prevState: EditBoxState) {
        if (nextProps.value === prevState.originValue) {
            return null;
        }
        return {
            originValue: nextProps.value,
            currentValue: nextProps.value,
        };
    }

    constructor(props: EditBoxProps) {
        super(props);

        const value = this.props.value;
        const initialUpdate = this.props.initialUpdate;
        this.state = {
            originValue: value,
            currentValue: typeof initialUpdate === "string" ? initialUpdate : value,
        }

        this.handleFocus = this.handleFocus.bind(this);
        this.handleChange = this.handleChange.bind(this);
        this.handleKeyDown = this.handleKeyDown.bind(this);
        this.handleBlur = this.handleBlur.bind(this);
        this.handleClick = this.handleClick.bind(this);
    }

    public componentDidMount() {
        if (this.props.focusByDefault) {
            this.focus();
        }
        if (this.textareaRef.current) {
            this.resizeIfNeeded(this.textareaRef.current);
        }
    }

    public componentDidUpdate(prevProps: EditBoxProps, prevState: EditBoxState) {
        if (prevState.currentValue !== this.state.currentValue) {
            if (this.textareaRef.current) {
                this.resizeIfNeeded(this.textareaRef.current);
            }
        }
    }

    protected handleFocus(event: React.FocusEvent) {
        if (this.props.readOnly || this.props.disabled) {
            return;
        }
        if (this.props.selectOnFocus) {
            if (this.textareaRef.current) {
                this.textareaRef.current.select();
            }
        }
        if (typeof this.props.afterBeginEdit !== 'undefined') {
            this.props.afterBeginEdit();
        }
    }

    protected handleChange(event: React.ChangeEvent<HTMLTextAreaElement>) {
        const target = event.currentTarget;
        this.resizeIfNeeded(target);
        this.updateValue(target.value);
    }

    protected resizeIfNeeded(target: HTMLTextAreaElement) {
        if (this.props.autoExpand) {
            target.style.removeProperty("height");
            target.style.height = target.scrollHeight + "px";

        } else { // no auto expand
            if (!this.props.multiline) {
                const computedStyle = window.getComputedStyle(target);
                const lineHeight = target.clientHeight
                    - parseFloat(computedStyle.getPropertyValue("padding-top"))
                    - parseFloat(computedStyle.getPropertyValue("padding-bottom"));
                target.style.lineHeight = lineHeight + "px";
            }
        }
    }

    public updateValue(newValue: string) {
        const change: ValueChange<string> = {
            id: this.props.id,
            name: this.props.name,
            fromValue: this.state.currentValue,
            toValue: newValue,
            type: 'edit',
        }
        let accepted = true;
        if (typeof this.props.beforeChange !== 'undefined') {
            accepted = this.props.beforeChange(change);
        }
        if (accepted && change.fromValue !== change.toValue) {
            this.setState({
                currentValue: change.toValue,
            });
            if (typeof this.props.afterChange !== 'undefined') {
                this.props.afterChange(change);
            }
        }
    }

    public getCurrentValue() {
        return this.state.currentValue;
    }

    protected handleKeyDown(event: React.KeyboardEvent<HTMLTextAreaElement>) {
        const target = event.currentTarget;
        if (event.key === 'Enter') {
            if (this.props.multiline && this.props.forceCommitOnEnter && event.altKey) {
                const start = target.selectionStart;
                const stop = target.selectionEnd;
                if (start === null || stop === null) {
                    return;
                }
                target.setRangeText("\n", start, stop, "end");
                this.resizeIfNeeded(target);
                this.updateValue(target.value);

            } else if (!this.props.multiline || this.props.forceCommitOnEnter || event.ctrlKey || event.metaKey) {
                event.preventDefault();
                this.commit();
            }

        } else if (event.key === 'Escape') {
            event.nativeEvent.stopImmediatePropagation();
            this.rollback();
        }
    }

    protected handleBlur(event: React.FocusEvent) {
        if (this.props.readOnly || this.props.disabled) {
            return;
        }
        if (this.props.value !== this.state.currentValue) {
            if (!this.commit()) {
                this.rollback();
            }
        }
        // reset current value. after commit, if this.props.value changes,
        // the state will be update to new this.props.value, if this.props.value
        // won't change, let's suppose the new value is rejected.
        this.setState({
            currentValue: this.props.value
        });
        if (typeof this.props.afterEndEdit !== 'undefined') {
            this.props.afterEndEdit();
        }
    }

    protected handleClick(event: React.MouseEvent<HTMLElement>) {
        if (typeof this.props.onClick !== "undefined") {
            this.props.onClick(event);
        }
    }

    public commit() {
        const change: ValueChange<string> = {
            id: this.props.id,
            name: this.props.name,
            fromValue: this.props.value,
            toValue: this.state.currentValue,
            type: 'commit',
        }
        let accepted = true;
        if (typeof this.props.beforeChange !== 'undefined') {
            accepted = this.props.beforeChange(change);
        }
        if (!accepted) {
            // this.rollback(); 
            // skip rollback, leave it to user.
            // this gives user a chance to continue edit.
            return false;
        }

        // do callback even nothing changed, treat 'commit' itself
        // as an action which may mean something.
        if (typeof this.props.afterChange !== 'undefined') {
            this.props.afterChange(change);
        }

        return true;
    }

    public rollback() {
        const change: ValueChange<string> = {
            id: this.props.id,
            name: this.props.name,
            fromValue: this.state.currentValue,
            toValue: this.props.value,
            type: 'rollback',
        }
        let accepted = true;
        if (typeof this.props.beforeChange !== 'undefined') {
            accepted = this.props.beforeChange(change);
        }
        if (!accepted) {
            return;
        }
        if (this.state.currentValue !== change.toValue) {
            this.setState({
                currentValue: change.toValue,
            });
        }
        // maybe nothing changed, but the rollback action itself 
        // may mean something, so just do the callback.
        if (typeof this.props.afterChange !== 'undefined') {
            this.props.afterChange(change);
        }
    }

    public focus() {
        if (this.textareaRef.current) {
            this.textareaRef.current.focus();
        }
    }

    public render() {
        const className = classnames(
            "edit-box",
            this.props.className,
            this.props.multiline ? "multiline" : "singleline",
            { "auto-expand": this.props.autoExpand }
        );

        return (
            <textarea
                ref={this.textareaRef}
                value={this.state.currentValue}
                id={this.props.id}
                name={this.props.name}
                placeholder={this.props.placeholder}
                className={className}
                style={this.props.style}
                rows={this.props.multiline ? undefined : 1}
                onFocus={this.handleFocus}
                onChange={this.handleChange}
                onKeyDown={this.handleKeyDown}
                onBlur={this.handleBlur}
                onClick={this.handleClick}
                readOnly={this.props.readOnly}
                disabled={this.props.disabled} />
        );
    }
}

export default EditBox;