import React, { Component } from 'react';
import { BandForm, AxisBand } from '../../src';
import { action } from '@storybook/addon-actions';

class BandFormStroies extends Component {
    band = new AxisBand();

    constructor(props) {
        super(props);

        this.afterChange = this.afterChange.bind(this);
    }

    afterChange(band) {
        action('BandForm afterChange')(band);
    }

    render() {
        return (
            <div>
                <h3>BandForm</h3>
                <BandForm
                    band={this.band}
                    afterChange={this.afterChange} />
            </div>
        );
    }
}

export default BandFormStroies;