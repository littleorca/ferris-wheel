import React, { Component } from 'react';
import UnionValueListEdit from '../../src/ctrl/UnionValueListEdit';
import { action } from '@storybook/addon-actions';

class UnionValueListEditStories extends Component {
    list = [];

    constructor(props) {
        super(props);

        this.handleChange = this.handleChange.bind(this);
    }

    handleChange(list) {
        action('UnionValueListEdit afterChange')(list);
    }

    render() {
        return (
            <div>
                <h3>UnionValueListEdit</h3>
                <UnionValueListEdit
                    list={this.list}
                    afterChange={this.handleChange} />
            </div>
        );
    }
}

export default UnionValueListEditStories;