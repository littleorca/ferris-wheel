import React, { Component } from "react";
import CheckBox from "../../src/ctrl/CheckBox";
import Field from "../../src/form/Field";

let testValue = true;

class FieldStories extends Component {
    render() {
        return (
            <div>
                <Field
                    name="test1"
                    label="Test!"
                    tips="Input test value!"
                    error="Empty value not allowed">
                    <CheckBox
                        value={testValue}
                        label="Test!" />
                </Field>
            </div>
        );
    }
}

export default FieldStories;
