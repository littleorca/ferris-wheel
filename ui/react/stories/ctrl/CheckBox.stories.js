import React, { Component } from "react";
import CheckBox from "../../src/ctrl/CheckBox";
import CheckBoxGroup from "../../src/ctrl/CheckBoxGroup";

let testValue = false;
const options = [{
    label: "Option 1",
    value: "Opt-1",
}, {
    label: "Option 2",
    value: "Opt-2",
}];
const selected = [];

class CheckBoxStories extends Component {
    render() {
        return (
            <div>
                <h3>Single Check Box (for boolean  value)</h3>
                <CheckBox
                    value={testValue}
                    afterChange={newValue => {
                        testValue = newValue;
                        this.forceUpdate();
                    }} />
                <h3>Single Check Box (initial as intermediate)</h3>
                <CheckBox
                    value={testValue}
                    indeterminate={true}
                    afterChange={newValue => {
                        testValue = newValue;
                        this.forceUpdate();
                    }} />
                <h3>Single Check Box (readonly)</h3>
                <CheckBox
                    value={true}
                    readOnly
                    afterChange={newValue => {
                        testValue = newValue;
                        this.forceUpdate();
                    }} />
                <h3>Check Box Group (for multiple select)</h3>
                <CheckBoxGroup
                    options={options}
                    value={selected}
                    afterChange={newValue => {
                        this.forceUpdate();
                    }} />
            </div>
        );
    }
}

export default CheckBoxStories;