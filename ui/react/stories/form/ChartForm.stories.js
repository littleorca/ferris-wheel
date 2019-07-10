import React, { Component } from 'react';
import ChartForm from '../../src/form/ChartForm';
import Chart from '../../src/model/Chart';
import { action } from '@storybook/addon-actions';

class ChartFormStories extends Component {
    chart = new Chart();

    constructor(props) {
        super(props);

        this.afterChange = this.afterChange.bind(this);
    }

    afterChange(chart) {
        action('ChartForm afterChange')(chart);
    }

    render() {
        return (
            <div>
                <h3>ChartForm</h3>
                <form>
                    <ChartForm
                        chart={this.chart}
                        afterChange={this.afterChange} />
                </form>
            </div>
        );
    }
}

export default ChartFormStories;