import React, { Component } from 'react';
import EditBox from '../../src/ctrl/EditBox';
import { action } from '@storybook/addon-actions';

class EditBoxStories extends Component {
    text = "hello world";

    constructor(props) {
        super(props);
        this.beforeChange = this.beforeChange.bind(this);
        this.afterBeginEdit = this.afterBeginEdit.bind(this);
        this.afterChange = this.afterChange.bind(this);
        this.afterEndEdit = this.afterEndEdit.bind(this);
    }

    beforeChange(change) {
        action('EditBox beforeChange')(change);
        return true;
    }

    afterBeginEdit() {
        action('EditBox afterBeginEdit')();
    }

    afterChange(change) {
        action('EditBox afterChange')(change);
        if (change.type === 'commit') {
            this.text = change.nextValue;
            this.forceUpdate();
        }
    }

    afterEndEdit() {
        action('EditBox afterEndEdit')();
    }

    render() {
        return (
            <div>
                <EditBox
                    id="e-b-id"
                    value={this.text}
                    placeholder="holder"
                    beforeChange={this.beforeChange}
                    afterBeginEdit={this.afterBeginEdit}
                    afterChange={this.afterChange}
                    afterEndEdit={this.afterEndEdit} />
            </div>
        );
    }
}

export default EditBoxStories;