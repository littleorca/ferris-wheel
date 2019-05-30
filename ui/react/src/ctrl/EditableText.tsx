import * as React from 'react';
import InlineEditable, { InlineEditorProps } from './InlineEditable';
import EditBox from './EditBox';
import ValueChange from "./ValueChange";

export interface EditableTextProps extends React.ClassAttributes<EditableText> {
    value: string,
    multiline?: boolean,
    readOnly?: boolean,
    className?: string,
    displayClassName?: string,
    editClassName?: string,
    onClick?: React.MouseEventHandler,
    beforeEdit?: (value: string) => string,
    afterEdit?: (value: string) => string,
    afterChange?: (value: string) => void,
};

const echoBack = (value: string) => value;

class EditableText extends React.Component<EditableTextProps> {
    protected static defaultProps: Partial<EditableTextProps> = {
        multiline: false,
        readOnly: false,
    }

    constructor(props: EditableTextProps) {
        super(props);

        this.inlineEditor = this.inlineEditor.bind(this);
    }

    protected inlineEditor(props: InlineEditorProps<string>) {
        const afterChange = (change: ValueChange<string>) => {
            if (change.type === 'commit') {
                props.onSubmit(change.toValue);
            } else if (change.type === 'rollback') {
                // no need to deal the value after rollback as the dirty value 
                // is not commited. here we leave edit mode after rollback.
                afterEndEdit();
            }
        };

        const afterEndEdit = () => {
            props.onCancel();
        };

        return (
            <EditBox
                value={props.value}
                multiline={this.props.multiline}
                className={props.className}
                style={props.style}
                focusByDefault={true}
                afterChange={afterChange}
                afterEndEdit={afterEndEdit} />
        );
    }

    public render() {
        return (
            <InlineEditable<string>
                value={this.props.value}
                displayable={echoBack}
                editor={this.inlineEditor}
                readOnly={this.props.readOnly}
                className={this.props.className}
                displayClassName={this.props.displayClassName}
                editClassName={this.props.editClassName}
                onClick={this.props.onClick}
                afterChange={this.props.afterChange} />
        );
    }
}

export default EditableText;