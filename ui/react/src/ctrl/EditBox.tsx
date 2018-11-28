import * as React from 'react';

export interface EditBoxProps extends React.ClassAttributes<EditBox> {
    value: string,
    id?: string,
    name?: string,
    placeholder?: string,
    multiline?: boolean,
    className?: string,
    style?: React.CSSProperties,
    disabled?: boolean,
    focused?: boolean,
    afterBeginEdit?: () => void,
    beforeChange?: (change: EditBoxChange) => boolean,
    afterChange?: (change: EditBoxChange) => void,
    afterEndEdit?: () => void,
}

export interface EditBoxChange {
    id?: string,
    name?: string,
    originValue: string,
    prevValue: string,
    nextValue: string,
    type: 'edit' | 'commit' | 'rollback',
}

interface EditBoxState {
    currentValue: string,
}

class EditBox extends React.Component<EditBoxProps, EditBoxState> {

    protected inputElement: React.RefObject<HTMLInputElement>;
    protected textareaElement: React.RefObject<HTMLTextAreaElement>;

    constructor(props: EditBoxProps) {
        super(props);

        const value = this.props.value;
        this.state = {
            currentValue: value,
        }

        this.inputElement = React.createRef();
        this.textareaElement = React.createRef();

        this.handleFocus = this.handleFocus.bind(this);
        this.handleChange = this.handleChange.bind(this);
        this.handleKeyDown = this.handleKeyDown.bind(this);
        this.handleBlur = this.handleBlur.bind(this);
    }

    public componentDidMount() {
        if (this.props.focused) {
            this.focus();
        }
    }

    public componentDidUpdate(prevProps: EditBoxProps, prevState: EditBoxState) {
        if (prevProps.value !== this.props.value) {
            this.setState({
                currentValue: this.props.value,
            });
        }
    }

    protected handleFocus(event: React.FocusEvent) {
        if (typeof this.props.afterBeginEdit !== 'undefined') {
            this.props.afterBeginEdit();
        }
    }

    protected handleChange(event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
        const change: EditBoxChange = {
            id: this.props.id,
            name: this.props.name,
            originValue: this.props.value,
            prevValue: this.state.currentValue,
            nextValue: event.target.value,
            type: 'edit',
        }
        let accepted = true;
        if (typeof this.props.beforeChange !== 'undefined') {
            accepted = this.props.beforeChange(change);
        }
        if (accepted && change.prevValue !== change.nextValue) {
            this.setState({
                currentValue: change.nextValue,
            });
            if (typeof this.props.afterChange !== 'undefined') {
                this.props.afterChange(change);
            }
        }
    }

    protected handleKeyDown(event: React.KeyboardEvent) {
        if (event.key === 'Enter'
            && (!this.props.multiline || event.ctrlKey || event.metaKey)) {
            this.commit();
        } else if (event.key === 'Escape') {
            this.rollback();
        }
    }

    protected handleBlur(event: React.FocusEvent) {
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

    public commit() {
        const change: EditBoxChange = {
            id: this.props.id,
            name: this.props.name,
            originValue: this.props.value,
            prevValue: this.state.currentValue,
            nextValue: this.state.currentValue,
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
        const change: EditBoxChange = {
            id: this.props.id,
            name: this.props.name,
            originValue: this.props.value,
            prevValue: this.state.currentValue,
            nextValue: this.state.currentValue,
            type: 'rollback',
        }
        let accepted = true;
        if (typeof this.props.beforeChange !== 'undefined') {
            accepted = this.props.beforeChange(change);
        }
        if (!accepted) {
            return;
        }
        if (this.state.currentValue !== change.originValue) {
            this.setState({
                currentValue: change.originValue,
            });
        }
        // maybe nothing changed, but the rollback action itself 
        // may mean something, so just do the callback.
        if (typeof this.props.afterChange !== 'undefined') {
            this.props.afterChange(change);
        }
    }

    public focus() {
        if (this.props.multiline) {
            if (this.textareaElement.current !== null) {
                this.textareaElement.current.focus();
            }

        } else {
            if (this.inputElement.current !== null) {
                this.inputElement.current.focus();
            }
        }
    }

    public render() {
        const commonProps = {
            value: this.state.currentValue,
            id: this.props.id,
            name: this.props.name,
            placeholder: this.props.placeholder,
            className: this.props.className,
            style: this.props.style,
            onFocus: this.handleFocus,
            onChange: this.handleChange,
            onKeyDown: this.handleKeyDown,
            onBlur: this.handleBlur,
            disabled: this.props.disabled,
        };

        if (this.props.multiline) {
            return <textarea ref={this.textareaElement} {...commonProps} />;
        } else {
            return <input type="text" ref={this.inputElement} {...commonProps} />;
        }
    }
}

export default EditBox;