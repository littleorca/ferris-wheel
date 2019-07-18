import * as React from 'react';
import UnionValueEdit from "../../src/ctrl/UnionValueEdit";
import Values from '../../src/model/Values';
import { action } from '@storybook/addon-actions';
import ValueChange from '../../src/ctrl/ValueChange';
import UnionValue from '../../src/model/UnionValue';

class UnionValueEditStories extends React.Component {
    private value: UnionValue = Values.str("hello world");
    private valueB: UnionValue = Values.str("foobar");
    private switchableValue = this.value;

    constructor(props: any) {
        super(props);

        this.handleChange = this.handleChange.bind(this);
        this.handleSwitchValue = this.handleSwitchValue.bind(this);
        this.handleSwitchableValueChange = this.handleSwitchableValueChange.bind(this);
    }

    handleChange(change: ValueChange<UnionValue>) {
        if (change.type === "commit") {
            this.value = change.toValue;
            this.forceUpdate();
        }
        action('UnionValueEdit afterChange')(change);
    }

    handleSwitchValue() {
        this.switchableValue = (this.switchableValue === this.value) ?
            this.valueB : this.value;
        this.forceUpdate();
    }

    handleSwitchableValueChange(change: ValueChange<UnionValue>) {
        if (change.type === "commit") {
            this.switchableValue = change.toValue;
            this.forceUpdate();
        }
        action('UnionValueEdit afterChange')(change);
    }

    render() {
        return (
            <div>
                <div>
                    <h3>UnionValueEdit</h3>
                    <UnionValueEdit
                        value={this.value}
                        afterChange={this.handleChange} />
                </div><div>
                    <h3>UnionValueEdit test switch value</h3>
                    <button onClick={this.handleSwitchValue}>Switch Value</button>
                    <UnionValueEdit
                        value={this.switchableValue}
                        afterChange={this.handleChange} />
                </div><div>
                    <h3>UnionValueEdit mode=formula</h3>
                    <UnionValueEdit
                        value={this.value}
                        modes={["formula"]}
                        afterChange={this.handleChange} />
                </div><div>
                    <h3>UnionValueEdit mode=decimal</h3>
                    <UnionValueEdit
                        value={this.value}
                        modes={["decimal"]}
                        afterChange={this.handleChange} />
                </div><div>
                    <h3>UnionValueEdit mode=boolean</h3>
                    <UnionValueEdit
                        value={this.value}
                        modes={["boolean"]}
                        afterChange={this.handleChange} />
                </div><div>
                    <h3>UnionValueEdit mode=date</h3>
                    <UnionValueEdit
                        value={this.value}
                        modes={["date"]}
                        afterChange={this.handleChange} />
                </div><div>
                    <h3>UnionValueEdit mode=string</h3>
                    <UnionValueEdit
                        value={this.value}
                        modes={["string"]}
                        afterChange={this.handleChange} />
                </div><div>
                    <h3>UnionValueEdit autoExpand</h3>
                    <UnionValueEdit
                        value={this.value}
                        autoExpand={true}
                        afterChange={this.handleChange} />
                </div><div>
                    <h3>UnionValueEdit aux=none</h3>
                    <UnionValueEdit
                        value={this.value}
                        aux="none"
                        afterChange={this.handleChange} />
                </div>
            </div>
        );
    }
}

export default UnionValueEditStories;