import React, { Component } from 'react';
import PivotFieldForm from '../../src/form/PivotFieldForm';
import { action } from '@storybook/addon-actions';

class PivotFieldFormStories extends Component {
    fields = [];

    constructor(props) {
        super(props);
        this.afterChange = this.afterChange.bind(this);
    }

    afterChange(fields) {
        action('PivotFieldForm afterChange')(fields);
    }

    render() {
        return (
            <div>
                <h3>PivotFieldForm</h3>
                <PivotFieldForm
                    fields={this.fields}
                    afterChange={this.afterChange} />
            </div>
        );
    }
}

export default PivotFieldFormStories;