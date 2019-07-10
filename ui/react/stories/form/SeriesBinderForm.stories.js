import React, { Component } from 'react';
import SeriesBinderForm from '../../src/form/SeriesBinderForm';
import { action } from '@storybook/addon-actions';

class SeriesBinderFormStories extends Component {
    series = [];

    constructor(props) {
        super(props);
        this.afterChange = this.afterChange.bind(this);
    }

    afterChange(series) {
        action('SeriesBinderForm afterChange')(series);
    }

    render() {
        return (
            <div>
                <h3>SeriesBinderForm</h3>
                <SeriesBinderForm
                    series={this.series}
                    afterChange={this.afterChange} />
            </div>
        );
    }
}

export default SeriesBinderFormStories;