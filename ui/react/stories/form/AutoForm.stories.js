import React, { Component } from 'react';
import { AutoForm, ParamRule, VariantType, Values } from '../../src';
import { action } from '@storybook/addon-actions';

class AutoFormStories extends Component {
    userParamRules = [
        new ParamRule('bool', VariantType.BOOL, false, []),
        new ParamRule('decimal', VariantType.DECIMAL, false, []),
        new ParamRule('string', VariantType.STRING, false, []),
        new ParamRule('date', VariantType.DATE, false, []),
        new ParamRule('list', VariantType.LIST, false, []),
        new ParamRule('select str', VariantType.STRING, false, [
            Values.str('foo'),
            Values.str('bar')
        ]),
        new ParamRule('select dec', VariantType.DECIMAL, false, [
            Values.dec(1024),
            Values.dec(4096)
        ])
    ];

    constructor(props) {
        super(props);

        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    handleChange(param) {
        action('AutoForm change')(param);
    }

    handleSubmit(params) {
        action('AutoForm submit')(params);
    }

    render() {
        return (
            <div>
                <h3>AutoForm</h3>
                <AutoForm
                    rules={this.userParamRules}
                    afterChange={this.handleChange}
                    onSumbit={this.handleSubmit} />
            </div>
        );
    }
}

export default AutoFormStories;