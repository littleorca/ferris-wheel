import React, { Component } from 'react';
import PivotFilterForm from '../../src/form/PivotFilterForm';
import { action } from '@storybook/addon-actions';

class PivotFilterFormStories extends Component {
    filters = [];

    constructor(props) {
        super(props);
        this.afterChange = this.afterChange.bind(this);
    }

    afterChange(filters) {
        action('PivotFilterForm afterChange')(filters);
    }

    render() {
        return (
            <div>
                <h3>PivotFilterForm</h3>
                <PivotFilterForm
                    filters={this.filters}
                    afterChange={this.afterChange} />
            </div>
        );
    }
}

export default PivotFilterFormStories;