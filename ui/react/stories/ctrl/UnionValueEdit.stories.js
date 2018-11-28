import React, { Component } from 'react';
import UnionValueEdit from '../../src/ctrl/UnionValueEdit';
import Values from '../../src/model/Values';
import { action } from '@storybook/addon-actions';

class UnionValueEditStories extends Component {
    value = Values.str("hello world");

    constructor(props) {
        super(props);

        this.handleChange = this.handleChange.bind(this);
    }

    handleChange(change) {
        this.value = change.newValue;
        action('UnionValueEdit afterChange')(change);
    }

    render() {
        return (
            <div>
                <h3>UnionValueEdit</h3>
                <UnionValueEdit
                    value={this.value}
                    afterChange={this.handleChange} />
            </div>
        );
    }
}

export default UnionValueEditStories;