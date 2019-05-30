import * as React from 'react';
import UnionValue from '../model/UnionValue';
import InlineEditable, { InlineEditorProps } from './InlineEditable';
import UnionValueEdit, { UnionValueEditMode, UnionValueEditAux } from './UnionValueEdit';
import ValueChange from "./ValueChange";

export interface EditableUnionValueProps extends React.ClassAttributes<EditableUnionValue> {
    value: UnionValue,
    id?: string;
    name?: string;
    placeholder?: string;
    className?: string;
    style?: React.CSSProperties;
    disableCommitOnEnter?: boolean;
    autoExpand?: boolean;
    disabled?: boolean;
    modes?: UnionValueEditMode[];
    aux?: UnionValueEditAux;
    readOnly?: boolean,
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
        readOnly: false,
    }

    constructor(props: EditableUnionValueProps) {
        super(props);

        this.inlineEditor = this.inlineEditor.bind(this);
    }

    protected inlineEditor(props: InlineEditorProps<UnionValue>) {
        const afterChange = (change: ValueChange<UnionValue>) => {
            if (change.type === 'commit') {
                props.onSubmit(change.toValue);
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
                className={props.className}
                style={props.style}
                id={this.props.id}
                name={this.props.name}
                placeholder={this.props.placeholder}
                disableCommitOnEnter={this.props.disableCommitOnEnter}
                autoExpand={this.props.autoExpand}
                aux={this.props.aux}
                focusByDefault={true}
                disabled={this.props.disabled}
                modes={this.props.modes}
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