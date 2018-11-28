import * as React from 'react';
import UnionValue from '../model/UnionValue';
import InlineEditable, { InlineEditorProps } from './InlineEditable';
import UnionValueEdit, { UnionValueChange } from './UnionValueEdit';

export interface EditableUnionValueProps extends React.ClassAttributes<EditableUnionValue> {
    value: UnionValue,
    multiline?: boolean,
    readOnly?: boolean,
    className?: string,
    displayClassName?: string,
    editClassName?: string,
    editMode?: boolean,
    onClick?: React.MouseEventHandler,
    afterChange?: (value: UnionValue) => void,
};

const unionValueToDisplayText = (value: UnionValue) => {
    return value.toString();
};

class EditableUnionValue extends React.Component<EditableUnionValueProps> {
    protected static defaultProps: Partial<EditableUnionValueProps> = {
        multiline: false,
        readOnly: false,
    }

    constructor(props: EditableUnionValueProps) {
        super(props);

        this.inlineEditor = this.inlineEditor.bind(this);
    }

    protected inlineEditor(props: InlineEditorProps<UnionValue>) {
        const afterChange = (change: UnionValueChange) => {
            if (change.type === 'commit') {
                props.onSubmit(change.newValue);
            } else if (change.type === 'rollback') {
                props.onCancel();
            }
        };

        const afterEndEdit = () => {
            props.onCancel();
        }

        return (
            <UnionValueEdit
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
            <InlineEditable<UnionValue>
                value={this.props.value}
                displayable={unionValueToDisplayText}
                editor={this.inlineEditor}
                readOnly={this.props.readOnly}
                className={this.props.className}
                displayClassName={this.props.displayClassName}
                editClassName={this.props.editClassName}
                editMode={this.props.editMode}
                onClick={this.props.onClick}
                afterChange={this.props.afterChange} />
        );
    }
}

export default EditableUnionValue;