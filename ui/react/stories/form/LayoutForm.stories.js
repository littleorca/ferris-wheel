import React, { Component } from 'react';
import LayoutForm from '../../src/form/LayoutForm';
import Layout from '../../src/model/Layout';
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