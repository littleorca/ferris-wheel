import React, { Component } from 'react';
import { ParamRuleForm, ParamRule, VariantType } from '../../src';
import { action } from '@storybook/addon-actions';

class ParamRuleFormStories extends Component {
    rule = new ParamRule('testRule', VariantType.STRING, true, []);

    constructor(props) {
        super(props);
        this.afterChange = this.afterChange.bind(this);
    }

    afterChange(rule) {
        action('ParamRuleForm afterChange')(rule);
    }

    render() {
        return (
            <div>
                <h3>ParamRuleForm</h3>
                <ParamRuleForm
                    rule={this.rule}
                    afterChange={this.afterChange} />
            </div>
        );
    }
}

export default ParamRuleFormStories;