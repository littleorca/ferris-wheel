import React, { Component } from 'react';
import Interval from '../../src/model/Interval';
import IntervalInput from '../../src/ctrl/IntervalInput';
import { action } from '@storybook/addon-actions';

class IntervalInputStories extends Component {
    intvl = new Interval(0, 10);

    constructor(props) {
        super(props);

        this.handleChange = this.handleChange.bind(this);
    }

    handleChange(val) {
        this.intvl = val;
        action('IntervalInput afterChange')(val);
        this.forceUpdate();
    }

    render() {
        return (
            <div>
                <h3>IntervalInput</h3>
                <IntervalInput
                    value={this.intvl}
                    afterChange={this.handleChange} />
            </div>
        );
    }
}

export default IntervalInputStories;