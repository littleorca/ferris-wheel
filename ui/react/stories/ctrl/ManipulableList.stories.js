import React, { Component } from 'react';
import { ManipulableList, InlineEditable, EditableText, EditBox } from '../../src';
import { action } from '@storybook/addon-actions';

class ManipulableListStories extends Component {

    constructor(props) {
        super(props);

        this.onItemUpdated = this.onItemUpdated.bind(this);
    }

    onItemUpdated(oldItem, newItem, index) {
        action('ManipulableList onItemUpdated')(oldItem, newItem, index);
    }

    render() {
        return (
            <div>
                <h3>ManipulableList</h3>
                <div>
                    <h4>with inline editor</h4>
                    <ManipulableList
                        list={['hello', 'world']}
                        createItem={createItem}
                        itemRenderer={InlineEditorRenderer}
                        onItemUpdated={this.onItemUpdated} />
                </div>
                <div>
                    <ManipulableList
                        list={["hello", "world", "~!"]}
                        itemRenderer={itemRenderer}
                        createItem={createItem}
                        onItemUpdated={this.onItemUpdated} />
                </div>
                <div>
                    <h4>disable sort</h4>
                    <ManipulableList
                        list={["hello*", "world*", "~!*"]}
                        itemRenderer={itemRenderer}
                        sortable={false}
                        onItemUpdated={this.onItemUpdated} />
                </div>
                <div>
                    <h4>initialSelect 1</h4>
                    <ManipulableList
                        list={["hello", "world*", "~!"]}
                        itemRenderer={itemRenderer}
                        initialSelect={1}
                        onItemUpdated={this.onItemUpdated} />
                </div>
                <div>
                    <h4>initialSelect -1</h4>
                    <ManipulableList
                        list={["hello", "world", "~!"]}
                        itemRenderer={itemRenderer}
                        initialSelect={-1}
                        onItemUpdated={this.onItemUpdated} />
                </div>
                <div>
                    <h4>initialSelect 999</h4>
                    <ManipulableList
                        list={["hello", "world", "~!"]}
                        itemRenderer={itemRenderer}
                        initialSelect={999}
                        onItemUpdated={this.onItemUpdated} />
                </div>
                <div>
                    <h4>initialSelect NaN</h4>
                    <ManipulableList
                        list={["hello", "world", "~!"]}
                        itemRenderer={itemRenderer}
                        initialSelect={NaN}
                        onItemUpdated={this.onItemUpdated} />
                </div>
            </div>
        );
    }
}

function createItem() {
    return "new-one";
}

function InlineEditor(props) {
    const afterChange = (change) => {
        if (change.type === 'commit') {
            props.onSubmit(change.nextValue);
        } else if (change.type === 'rollback') {
            props.onSubmit(change.originValue);
        }
    }
    return (
        <EditBox
            {...props}
            value={props.value}
            focused={true}
            afterChange={afterChange} />
    );
};

function InlineEditorRenderer(props) {
    const afterChange = (value) => {
        props.updateItem(value);
    };

    return (
        <InlineEditable
            value={props.value}
            editor={InlineEditor}
            displayable={echoBack}
            afterChange={afterChange} />
    );
}

function itemRenderer(props) {
    return <EditableText
        value={props.value}
        afterChange={props.updateItem} />
}

function echoBack(val) {
    return val;
}

export default ManipulableListStories;