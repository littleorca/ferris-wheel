import React, { Component } from 'react';
import Values from '../../src/model/Values';
import EditableUnionValue from '../../src/ctrl/EditableUnionValue';
import { action } from '@storybook/addon-actions';

class EditableUnionValueStories extends Component {
    unionValue = Values.str('hello world');

    constructor(props) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
    }

    handleChange(val) {
        this.unionValue = val;
        action('EditableUnionValue changed')(val);
        this.forceUpdate();
    }

    render() {
        return (
            <div>
                <h3>EditableUnionValue</h3>
                <EditableUnionValue
                    value={this.unionValue}
                    afterChange={this.handleChange} />
            </div>
        );
    }
}

export default EditableUnionValueStories;