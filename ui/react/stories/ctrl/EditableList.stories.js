import React, { Component } from 'react';
import EditBox from '../../src/ctrl/EditBox';
import EditableList from '../../src/ctrl/EditableList';
import { action } from '@storybook/addon-actions';

const editableListValues = ["foo", "bar", "demo"];
const createEditableListItem = () => "new one";
const textEditor = (props) => {
    const afterChange = (change) => {
        if (change.type === 'rollback') {
            props.onSubmit(change.originValue);
        } else {
            props.onSubmit(change.nextValue);
        }
    };
    return (
        <EditBox value={props.value}
            afterChange={afterChange} />
    );
};

class EditableListStories extends Component {
    render() {
        return (
            <div>
                <h3>EditableList</h3>
                <EditableList
                    list={editableListValues}
                    editor={textEditor}
                    createItem={createEditableListItem}
                    afterChange={action('EditableList changed')} />
            </div>
        );
    }
}

export default EditableListStories;