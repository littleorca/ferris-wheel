import React, { Component } from "react";
import RadioGroup from "../../src/ctrl/RadioGroup";

let options = [{
    label: "Option 1",
    value: "opt1",
}, {
    label: "Option 2",
    value: "opt2",
}];
let testValue = "opt2";

class RadioGroupStories extends Component {
    render() {
        return (
            <div>
                <RadioGroup
                    options={options}
                    value={testValue}
                    afterChange={newValue => {
                        testValue = newValue;
                        this.forceUpdate();
                    }} />
            </div>
        );
    }
}

export default RadioGroupStories;