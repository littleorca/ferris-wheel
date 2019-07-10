import React, { Component } from 'react';
import EditBox from '../../src/ctrl/EditBox';
import EditableList from '../../src/ctrl/EditableList';
import { action } from '@storybook/addon-actions';

const editableListValues = ["foo", "bar", "demo"];
const createEditableListItem = () => "new one";
const textEditor = (props) => {
    const afterChange = (change) => {
        if (change.type === 'commit') {
            props.onSubmit(change.toValue);
        }
    };
    return (
        <EditBox value={props.value}
            afterChange={afterChange} />
    );
};

class EditableListStories extends Component {
    listRef = React.createRef();

    constructor(props) {
        super(props);
        this.state = { input: "-1" };
    }

    render() {
        return (
            <div>
                <h3>EditableList</h3>
                <EditableList
                    list={editableListValues}
                    editor={textEditor}
                    createItem={createEditableListItem}
                    afterChange={action('EditableList changed')} />
                <h3>EditableList (manual action)</h3>
                <EditableList
                    ref={this.listRef}
                    list={editableListValues}
                    initialSelect={1}
                    hideActions={true}
                    editor={textEditor}
                    afterChange={action('EditableList changed')} />
                <div>
                    <label>位置索引</label>
                    <input type="text" value={this.state.input} onChange={e => {
                        this.setState({ input: e.currentTarget.value });
                    }} />
                    <button onClick={() => {
                        let index = Number(this.state.input);
                        if (Number.isNaN(index)) {
                            index = -1;
                        }
                        this.listRef.current.addItem("new one", index);
                    }}>添加</button>
                    <button onClick={() => {
                        this.listRef.current.removeItem();
                    }}>删除</button>
                    <button onClick={() => {
                        let index = Number(this.state.input);
                        if (Number.isNaN(index)) {
                            index = -1;
                        }
                        this.listRef.current.selectItem(index);
                    }}>选中</button>
                </div>
            </div>
        );
    }
}

export default EditableListStories;