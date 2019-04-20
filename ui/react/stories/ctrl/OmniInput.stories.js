import React, { Component } from "react";
import OmniInput from "../../src/ctrl/OmniInput";

const options = [{
    label: "Option 1",
    value: "Opt-1",
}, {
    label: "Option 2",
    value: "Opt-2",
}, {
    label: "Option 3",
    value: "Opt-3",
}, {
    label: "Option 4",
    value: "Opt-4",
}, {
    label: "Option 55555555",
    value: "Opt-5",
}, {
    label: "Option 666",
    value: "Opt-6",
}];

let testValue = ["Opt-1", "Opt-3"];

class OmniInputStories extends Component {
    render() {
        return (
            <div>
                <h3>Omni Input</h3>
                <div style={{
                    display: "flex"
                }}>
                    <div style={{
                        marginLeft: 15
                    }}>
                        <h3>-option</h3>
                        <OmniInput
                            style={{
                                width: "200px",
                                // height: "300px",
                            }}
                            value={testValue}
                            allowManualInput={true}
                            tips="多值无选项"
                            afterChange={newValue => {
                                console.log(newValue);
                                testValue = newValue;
                                this.forceUpdate();
                            }} />
                    </div>
                    <div style={{
                        marginLeft: 15
                    }}>
                        <h3>+options +manual</h3>
                        <OmniInput
                            style={{
                                width: "200px",
                                // height: "300px",
                            }}
                            value={testValue}
                            options={options}
                            allowManualInput={true}
                            tips="多值有选项，可手输"
                            afterChange={newValue => {
                                console.log(newValue);
                                testValue = newValue;
                                this.forceUpdate();
                            }} />
                    </div>
                    <div style={{
                        marginLeft: 15
                    }}>
                        <h3>+options, -manual</h3>
                        <OmniInput
                            style={{
                                width: "200px",
                                // height: "300px",
                            }}
                            value={testValue}
                            options={options}
                            allowManualInput={false}
                            tips="多值有选项，无手输"
                            afterChange={newValue => {
                                console.log(newValue);
                                testValue = newValue;
                                this.forceUpdate();
                            }} />
                    </div>
                </div>
            </div>
        );
    }
}

export default OmniInputStories;