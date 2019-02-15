import * as React from 'react';
import classnames from 'classnames';
import "./InlineEditable.css";

interface InlineEditableProps<T> extends React.ClassAttributes<InlineEditable<T>> {
    value: T,
    displayable: (value: T) => string,
    editor: React.SFC<InlineEditorProps<T>>,
    readOnly?: boolean,
    className?: string,
    displayClassName?: string,
    editClassName?: string,
    editMode?: boolean,
    onClick?: React.MouseEventHandler,
    afterChange?: (value: T) => void,
};

interface InlineEditorProps<T> extends React.ClassAttributes<any> {
    value: T,
    onSubmit: (value: T) => void,
    onCancel: () => void,
    id?: string,
    name?: string,
    placeholder?: string,
    className?: string,
    style?: React.CSSProperties,
}

interface InlineEditableState<T> {
    isEdit: boolean;
    width: number;
    height: number;
}

const editorStyle: Partial<React.CSSProperties> = {
    margin: 0,
    padding: 0,
    width: '100%',
    height: '100%',
    minWidth: '2em',
    boxSizing: 'border-box',
    fontSize: 'inherit',
};

class InlineEditable<T> extends React.Component<InlineEditableProps<T>, InlineEditableState<T>> {
    protected static defaultProps: Partial<InlineEditableProps<any>> = {
        readOnly: false,
        editMode: false,
    }

    private displayElementRef = React.createRef<HTMLSpanElement>();

    constructor(props: InlineEditableProps<T>) {
        super(props);

        this.state = {
            isEdit: !props.readOnly && props.editMode === true,
            width: 0,
            height: 0,
        };

        this.beginEdit = this.beginEdit.bind(this);
        this.onSubmit = this.onSubmit.bind(this);
        this.onCancel = this.onCancel.bind(this);
    }

    public componentDidUpdate(prevProps: InlineEditableProps<T>) {
        if (this.props.value !== prevProps.value) {
            this.setState({
                isEdit: false,
            });
        }
    }

    protected beginEdit() {
        if (this.props.readOnly) {
            return;
        }
        let width = 0;
        let height = 0;
        if (this.displayElementRef.current) {
            width = this.displayElementRef.current.offsetWidth;
            height = this.displayElementRef.current.offsetHeight;
        }
        this.setState({
            isEdit: true,
            width,
            height,
        });
    }

    protected onSubmit(value: T) {
        this.endEdit();
        if (typeof this.props.afterChange !== 'undefined') {
            this.props.afterChange(value);
        }
    }

    protected onCancel() {
        this.endEdit();
    }

    protected endEdit() {
        this.setState({
            isEdit: false,
        });
    }

    protected getDisplayText() {
        return this.props.displayable(this.props.value);
    }

    public render() {
        if (this.state.isEdit) {
            const className = classnames(
                "inline-editable",
                this.props.className,
                "editing",
                this.props.editClassName);

            const style = { ...editorStyle };
            if (this.state.width !== 0) {
                style.width = this.state.width;
            }
            if (this.state.height !== 0) {
                style.height = this.state.height;
            }

            return (
                <this.props.editor
                    className={className}
                    style={style}
                    value={this.props.value}
                    onSubmit={this.onSubmit}
                    onCancel={this.onCancel} />
            );

        } else {
            const className = classnames(
                "inline-editable",
                this.props.className,
                "display",
                this.props.displayClassName);

            return (
                <span
                    ref={this.displayElementRef}
                    className={className}
                    onClick={this.props.onClick}
                    onDoubleClick={this.beginEdit}>
                    {this.getDisplayText()}
                </span>
            );
        }
    }
}

export default InlineEditable;
export { InlineEditableProps, InlineEditorProps };
