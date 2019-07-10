import React, { Component } from 'react';
import PivotValueForm from '../../src/form/PivotValueForm';
import { action } from '@storybook/addon-actions';

class PivotValueFormStories extends Component {
    values = [];

    constructor(props) {
        super(props);
        this.afterChange = this.afterChange.bind(this);
    }

    afterChange(values) {
        action('PivotValueForm afterChange')(values);
    }

    render() {
        return (
            <div>
                <h3>PivotValueForm</h3>
                <PivotValueForm
                    values={this.values}
                    afterChange={this.afterChange} />
            </div>
        );
    }
}

export default PivotValueFormStories;