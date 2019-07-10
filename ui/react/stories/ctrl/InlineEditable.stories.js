import React, { Component } from 'react';
import InlineEditable from '../../src/ctrl/InlineEditable';
import EditBox from '../../src/ctrl/EditBox';
import { action } from '@storybook/addon-actions';

class InlineEditableStories extends Component {
    text = "Inline editable text";

    constructor(props) {
        super(props);

        this.handleClick = this.handleClick.bind(this);
        this.handleChange = this.handleChange.bind(this);
        this.echoBack = this.echoBack.bind(this);
    }

    handleClick(e) {
        action('InlineEditable clicked')(e);
    }

    handleChange(val) {
        this.text = val;
        action('InlinEditable afterChange')(val);
        this.forceUpdate();
    }

    echoBack(val) {
        return val;
    }

    render() {
        return (
            <div>
                <h3>InlineEditable</h3>
                <div>
                    <InlineEditable
                        value={this.text}
                        editor={InlineEditor}
                        displayable={this.echoBack}
                        onClick={this.handleClick}
                        afterChange={this.handleChange} />
                </div>
            </div>
        );
    }
}

function InlineEditor(props) {
    const afterChange = (change) => {
        if (change.type === 'commit') {
            props.onSubmit(change.toValue);
        } else if (change.type === 'rollback') {
            endEdit();
        }
    }
    const endEdit = () => {
        props.onCancel();
    }
    return (
        <EditBox
            {...props}
            value={props.value}
            focused={true}
            afterChange={afterChange}
            afterEndEdit={endEdit} />
    );
}

export default InlineEditableStories;