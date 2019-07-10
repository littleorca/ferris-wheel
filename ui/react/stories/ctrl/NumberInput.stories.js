import React, { Component } from 'react';
import NumberInput from '../../src/ctrl/NumberInput';
import { action } from '@storybook/addon-actions';

class NumberInputStories extends Component {
    num = NaN;

    constructor(props) {
        super(props);

        this.handleChange = this.handleChange.bind(this);
    }

    handleChange(val) {
        this.num = val;
        action('NumberInput afterChange')(val);
        this.forceUpdate();
    }

    render() {
        return (
            <div>
                <h3>NumberInput</h3>
                <NumberInput
                    value={this.num}
                    afterChange={this.handleChange} />
            </div>
        );
    }
}

export default NumberInputStories;