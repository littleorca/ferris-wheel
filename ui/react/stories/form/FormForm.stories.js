import React, { Component } from "react";
import Form from "../../src/model/Form";
import FormForm from "../../src/form/FormForm";
import { Layout } from "../../src";

const form = new Form("test-form", [], new Layout());

class FormFormStories extends Component {
    render() {
        return <div>
            <div>
                <h3>Form Form</h3>
                <FormForm
                    form={form} />
            </div>
        </div>
    }
}

export default FormFormStories;
