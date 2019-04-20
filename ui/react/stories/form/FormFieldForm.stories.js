import React, { Component } from "react";
import { FormFieldForm, Values, VariantType } from "../../src";
import FormField from "../../src/model/FormField";
import FormFieldBinding from "../../src/model/FormFieldBinding";

const field = new FormField(
    "test-field",             // name: string = '',
    VariantType.STRING,       // type: VariantType = VariantType.BLANK,
    Values.blank(),           // value: UnionValue = Values.blank(),
    false,                    // mandatory: boolean = false,
    false,                    // multiple
    "Test Field",             // label: string = '',
    "Input test field!",      // tips: string = '',
    // "",                       // error: string = '',
    Values.blank(),           // candidates: UnionValue = Values.blank(),
    [                         // bindings: FormFieldBinding[] = [],
        new FormFieldBinding("table1!A1"),
        new FormFieldBinding("table2!'foo'"),
    ],
    // undefined,                // decimalInput?: DecimalInput,
    // undefined,                // booleanInput?: BooleanInput,
    // undefined,                // dateInput?: DateInput,
    // new StringInput(          // stringInput?: StringInput,
    //     "来输入啊！",               // placeholder: string = '',
    //     ".+",                     // regexConstraint: string = '',
    //     false,                    // password: boolean = false,
    //     false,                    // multiline: boolean = false
    // ),
    // undefined,                // listInput?: ListInput,
    // undefined,                // selectInput?: SelectInput
);

class FormFieldFormStories extends Component {
    render() {
        return <div>
            <div>
                <h3>Form Field Form: string</h3>
                <FormFieldForm field={field} />
            </div>
        </div>
    }
}

export default FormFieldFormStories;
