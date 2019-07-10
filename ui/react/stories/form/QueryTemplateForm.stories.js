import React, { Component } from 'react';
import QueryTemplateForm from "../../src/form/QueryTemplateForm";
import QueryTemplate from '../../src/model/QueryTemplate';
import { action } from '@storybook/addon-actions';

class QueryTemplateFormStories extends Component {
    template = new QueryTemplate('scheme', [], []);

    constructor(props) {
        super(props);
        this.afterChange = this.afterChange.bind(this);
    }

    afterChange(template) {
        action('QueryTemplateForm afterChange')(template);
    }

    render() {
        return (
            <div>
                <h3>QueryTemplateForm</h3>
                <QueryTemplateForm
                    queryTemplate={this.template}
                    afterChange={this.afterChange} />
            </div>
        );
    }
}

export default QueryTemplateFormStories;