import React, { Component } from 'react';
import TextView from "../../src/view/TextView";
import Text from "../../src/model/Text";
import Values from '../../src/model/Values';
import { action } from '@storybook/addon-actions';

class TextViewStories extends Component {
    text = new Text('test_text', Values.str('hello\n\tworld!'));

    constructor(props) {
        super(props);
        this.handleAction = this.handleAction.bind(this);
    }

    handleAction(act) {
        action('TextView onAction')(act);
    }

    render() {
        return (
            <div>
                <h3>TextView</h3>
                <div>
                    <h4>readonly</h4>
                    <TextView
                        text={this.text} />
                </div>
                <div>
                    <h4>editable</h4>
                    <TextView
                        editable={true}
                        text={this.text}
                        onAction={this.handleAction} />
                </div>
            </div>
        );
    }
}

export default TextViewStories;