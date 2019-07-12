import * as React from "react";
import Select from "../../src/ctrl/Select";

const options = [{
    value: "foo",
    label: "Foo!"
}, {
    value: "bar",
    label: "Bar!"
}];

let testValue = "foo";
// const selection = [];

class SelectStories extends React.Component {
    render() {
        return (
            <div>
                <h3>Single Select</h3>
                <Select
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

export default SelectStories;