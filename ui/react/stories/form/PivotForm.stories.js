import React, { Component } from 'react';
import { PivotForm, PivotAutomaton } from '../../src';
import { action } from '@storybook/addon-actions';

class PivotFormStories extends Component {
    pivot = new PivotAutomaton();

    constructor(props) {
        super(props);
        this.afterChange = this.afterChange.bind(this);
    }

    afterChange(pivot) {
        action('PivotForm afterChange')(pivot);
    }

    render() {
        return (
            <div>
                <h3>PivotForm</h3>
                <PivotForm
                    pivot={this.pivot}
                    afterChange={this.afterChange} />
            </div>
        );
    }
}

export default PivotFormStories;