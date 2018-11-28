import React, { Component } from 'react';
import EditableText from '../../src/ctrl/EditableText';
import { action } from '@storybook/addon-actions';

class EditableTextStories extends Component {
    text = "hello world!";

    constructor(props) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
    }

    handleChange(val) {
        this.text = val;
        action('EditableText changed')(val);
        this.forceUpdate();
    }

    render() {
        return (
            <div>
                <h3>EditableText</h3>
                <EditableText
                    value={this.text}
                    afterChange={this.handleChange} />
            </div>
        );
    }
}

export default EditableTextStories;