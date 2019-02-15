import React, { Component } from 'react';
import { LayoutForm, Layout } from '../../src';
import { action } from '@storybook/addon-actions';

class LayoutFormStories extends Component {
    layout = new Layout();

    constructor(props) {
        super(props);

        this.afterChange = this.afterChange.bind(this);
    }

    afterChange(layout) {
        action('LayoutForm afterChange')(layout);
    }

    render() {
        return (
            <div>
                <h3>LayoutForm</h3>
                <LayoutForm
                    layout={this.layout}
                    afterChange={this.afterChange} />
            </div>
        );
    }
}

export default LayoutFormStories;