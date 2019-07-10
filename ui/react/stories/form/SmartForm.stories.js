import React, { Component } from "react";
import { VariantType } from "../../src/model/Variant";
import Values from "../../src/model/Values";
import SmartForm from "../../src/form/SmartForm";
import FormField from "../../src/model/FormField";

const testFields = [
    new FormField("singleDecimal",
        VariantType.DECIMAL,
        Values.dec(3.14),
        false,
        false,
        "PI",
        "Input PI.")
    ,
    new FormField("singleBoolean",
        VariantType.BOOL,
        Values.bool(false),
        false,
        false,
        "Flag",
        "Input flag.")
    ,
    new FormField("singleDate",
        VariantType.DATE,
        Values.date(new Date()),
        false,
        false,
        "Date",
        "Input date.")
    ,
    new FormField("singleString",
        VariantType.STRING,
        Values.str("foobar"),
        false,
        false,
        "Field 1",
        "Input field 1.")
    ,
    new FormField("singleDecimalSelect",
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
    new FormField("singleStringSelect",
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
    new FormField("multipleInput",
        VariantType.STRING,
        Values.list([Values.str("foo"), Values.str("bar")]),
        false,
        true,
        "Numbers",
        "Input numbers.",
        undefined)
    ,
    new FormField("multipleDecimalSelect",
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
    new FormField("multipleStringSelect",
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
];

class SmartFormStories extends Component {
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
            <div style={{
                width: "100%"
            }}>
                <SmartForm
                    fields={testFields}
                    afterChange={(field) => {
                        console.log('change', field.name, field.value.toString());
                    }}
                    onSubmit={(fields) => {
                        const params = {};
                        fields.forEach(f => params[f.name] = f.value.toString());
                        console.log('submit', params);
                    }} />
            </div>
        );
    }
}

export default SmartFormStories;
