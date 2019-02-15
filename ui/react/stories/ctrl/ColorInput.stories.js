import React, { Component } from 'react';
import {
    ColorInput,
    Color
} from '../../src';
import { action } from '@storybook/addon-actions';

class ColorInputStories extends Component {
    color = new Color();

    constructor(props) {
        super(props);

        this.handleChange = this.handleChange.bind(this);
    }

    handleChange(color) {
        this.color = color;
        action('ColorInput afterChange')(color);
        this.forceUpdate();
    }

    render() {
        return (
            <div>
                <h3>ColorInput</h3>
                <ColorInput
                    value={this.color}
                    afterChange={this.handleChange} />
            </div>
        );
    }
}

export default ColorInputStories;