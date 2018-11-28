import React, { Component } from 'react';
import AxisForm from '../../src/form/AxisForm';
import Axis from '../../src/model/Axis';
import { action } from '@storybook/addon-actions';

class AxisFormStories extends Component {
    axis = new Axis();

    constructor(props) {
        super(props);

        this.afterChange = this.afterChange.bind(this);
    }

    afterChange(axis) {
        action('AxisForm afterChange')(axis);
    }

    render() {
        return (
            <div>
                <h3>AxisForm</h3>
                <AxisForm
                    axis={this.axis}
                    afterChange={this.afterChange} />
            </div>
        );
    }
}

export default AxisFormStories;