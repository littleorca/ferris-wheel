import React, { Component } from "react";
import Form from "../../src/model/Form";
import Layout from "../../src/model/Layout";
import FormField from "../../src/model/FormField";
import { VariantType } from "../../src/model/Variant";
import Values from "../../src/model/Values";
import FormFieldBinding from "../../src/model/FormFieldBinding";
import FormView from "../../src/view/FormView";

const form = new Form(
    "test-form",
    [
        new FormField("f1", VariantType.STRING, Values.str("foo"),
            false, false, "Foo", undefined,
            undefined,
            [new FormFieldBinding("t1!'param1'")]),
        new FormField("f2", VariantType.STRING, Values.str("bar"),
            false, false, "Bar", undefined,
            Values.list([Values.str("Bar 1"), Values.str("Bar 2")]),
            [new FormFieldBinding("t1!'param2'")]),
        new FormField("f3", VariantType.STRING, Values.list([Values.str("foo"), Values.str("bar")]),
            false, true, "Foobar", undefined,
            undefined,
            [new FormFieldBinding("t1!'param3'")]),
        new FormField("f4", VariantType.DATE,
            Values.date(new Date()),
            false, false, "Date", undefined,
            undefined,
            [new FormFieldBinding("t1!'param4'")]),
    ],
    new Layout()
);

class FormViewStories extends Component {
    render() {
        return (
            <div>
                <FormView
                    form={form} />
            </div>
        );
    }
}

export default FormViewStories;