import React, { Component } from "react";
import { VariantType } from "../../src/model/Variant";
import Values from "../../src/model/Values";
import SmartField from "../../src/form/SmartField";
import FormField from "../../src/model/FormField";

const testValues = {
    singleDecimal: new FormField("singleDecimal",
        VariantType.DECIMAL,
        Values.dec(3.14),
        false,
        false,
        "PI",
        "Input PI.")
    ,
    singleBoolean: new FormField("singleBoolean",
        VariantType.BOOL,
        Values.bool(false),
        false,
        false,
        "Flag",
        "Input flag.")
    ,
    singleDate: new FormField("singleDate",
        VariantType.DATE,
        Values.date(new Date()),
        false,
        false,
        "Date",
        "Input date.")
    ,
    singleString: new FormField("singleString",
        VariantType.STRING,
        Values.str("foobar"),
        false,
        false,
        "Field 1",
        "Input field 1.")
    ,
    singleDecimalSelect: new FormField("singleDecimalSelect",
        VariantType.DECIMAL,
        Values.dec(3.14),
        false,
        false,
        "Numbers",
        "Input numbers.",
        Values.list([
            Values.dec(3.14),
            Values.dec(2.718),
        ]))
    ,
    singleStringSelect: new FormField("singleStringSelect",
        VariantType.STRING,
        Values.str("foo"),
        false,
        false,
        "Names",
        "Input names.",
        Values.list([
            Values.str("foo"),
            Values.str("bar"),
        ]))
    ,
    multipleInput: new FormField("multipleInput",
        VariantType.STRING,
        Values.list([Values.str("foo"), Values.str("bar")]),
        false,
        true,
        "Something",
        "Input something.",
        undefined)
    ,
    multipleDecimalSelect: new FormField("multipleDecimalSelect",
        VariantType.DECIMAL,
        Values.list([Values.dec(3.14)]),
        false,
        true,
        "Numbers",
        "Input numbers.",
        Values.list([
            Values.dec(3.14),
            Values.dec(2.718),
        ]))
    ,
    multipleStringSelect: new FormField("multipleStringSelect",
        VariantType.STRING,
        Values.list([Values.str("foo")]),
        false,
        true,
        "Names",
        "Input names.",
        Values.list([
            Values.str("foo"),
            Values.str("bar"),
        ]))
    ,
};

class SmartFieldStories extends Component {
    constructor(props) {
        super(props);
        this.afterChange = this.afterChange.bind(this);
    }

    afterChange(newValue, name) {
        testValues[name].value = newValue;
        this.forceUpdate();
        console.log("afterChange", name, newValue, testValues[name]);
    }

    render() {
        return (
            <div>
                <div>
                    <SmartField
                        field={testValues.singleDecimal}
                        afterChange={this.afterChange} />
                    <p>Single Decimal</p>
                </div>
                <div>
                    <SmartField
                        field={testValues.singleBoolean}
                        afterChange={this.afterChange} />
                    <p>Single Boolean</p>
                </div>
                <div>
                    <SmartField
                        field={testValues.singleDate}
                        afterChange={this.afterChange} />
                    <p>Single Date</p>
                </div>
                <div>
                    <SmartField
                        field={testValues.singleString}
                        afterChange={this.afterChange} />
                    <p>Single String</p>
                </div>
                <div>
                    <SmartField
                        field={testValues.singleDecimalSelect}
                        afterChange={this.afterChange} />
                    <p>Single Decimal Select</p>
                </div>
                <div>
                    <SmartField
                        field={testValues.singleStringSelect}
                        afterChange={this.afterChange} />
                    <p>Single String Select</p>
                </div>
                <div>
                    <SmartField
                        field={testValues.multipleInput}
                        afterChange={this.afterChange} />
                    <p>Multiple Input</p>
                </div>
                <div>
                    <SmartField
                        field={testValues.multipleDecimalSelect}
                        afterChange={this.afterChange} />
                    <p>Multiple Decimal Select</p>
                </div>
                <div>
                    <SmartField
                        field={testValues.multipleStringSelect}
                        afterChange={this.afterChange} />
                    <p>Multiple String Select</p>
                </div>
            </div>
        );
    }
}

export default SmartFieldStories;
