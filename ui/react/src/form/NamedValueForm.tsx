import * as React from 'react';
import UnionValueEdit, { UnionValueChange } from '../ctrl/UnionValueEdit';
import NamedValue from '../model/NamedValue';
import EditBox, { EditBoxChange } from '../ctrl/EditBox';

interface NamedValueFormProps extends React.ClassAttributes<NamedValueForm> {
    namedValue: NamedValue,
    afterChange?: (namedValue: NamedValue) => void,
}

class NamedValueForm extends React.Component<NamedValueFormProps> {

    constructor(props: NamedValueFormProps) {
        super(props);

        this.handleNameChange = this.handleNameChange.bind(this);
        this.handleValueChange = this.handleValueChange.bind(this);
    }

    protected handleNameChange(change: EditBoxChange) {
        if (change.type !== 'commit') {
            return;
        }
        const param = this.props.namedValue;
        const value = change.nextValue;
        param.name = value;
        this.forceUpdate();
        this.onUpdate();
    }

    protected handleValueChange(change: UnionValueChange) {
        if (change.type !== 'commit') {
            return;
        }
        const param = this.props.namedValue;
        param.value = change.newValue;
        this.onUpdate();
    }

    protected onUpdate() {
        if (typeof this.props.afterChange !== 'undefined') {
            this.props.afterChange(this.props.namedValue);
        }
    }

    public render() {
        const namedValue = this.props.namedValue;
        return (
            <div className="named-value-form">
                <label className="field name">
                    <span className="field-name">名称</span>
                    <EditBox
                        name="name"
                        value={namedValue.name}
                        afterChange={this.handleNameChange} />
                </label>
                <label className="field value">
                    <span className="field-name">值</span>
                    <UnionValueEdit
                        value={namedValue.value}
                        afterChange={this.handleValueChange} />
                </label>
            </div>
        );
    }
}

export default NamedValueForm;
export { NamedValueFormProps };
