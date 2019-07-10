import React, { Component } from 'react';
import UnionValueEdit from "../../src/ctrl/UnionValueEdit";
import Values from '../../src/model/Values';
import { action } from '@storybook/addon-actions';

class UnionValueEditStories extends Component {
    value = Values.str("hello world");

    constructor(props) {
        super(props);

        this.handleChange = this.handleChange.bind(this);
    }

    handleChange(change) {
        if (change.type === "commit") {
            this.value = change.toValue;
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