import React, { Component } from 'react';
import { ParameterForm, Parameter, Values } from '../../src';
import { action } from '@storybook/addon-actions';

class ParameterFormStories extends Component {
    param = new Parameter('testP', Values.blank());

    constructor(props) {
        super(props);
        this.afterChange = this.afterChange.bind(this);
    }

    afterChange(param) {
        action('Parameter form afterChange')(param);
    }

    render() {
        return (
            <div>
                <h3>ParameterForm</h3>
                <ParameterForm
                    parameter={this.param}
                    afterChange={this.afterChange} />
            </div>
        );
    }
}

export default ParameterFormStories;