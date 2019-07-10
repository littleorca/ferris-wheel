import React, { Component } from "react";
import Form from "../../src/model/Form";
import AddFormForm from "../../src/form/AddFormForm";
import Layout from "../../src/model/Layout";
import { VariantType } from "../../src/model/Variant";

const form = new Form("test-add-form", [], new Layout());
const pendingFields = [{
    sheetName: "s1",
    assetName: "asset 1",
    paramName: "param-1",
    paramType: VariantType.STRING,
}, {
    sheetName: "s 2",
    assetName: "asset '2'",
    paramName: "参数2",
    paramType: VariantType.STRING,
}];

class AddFormFormStories extends Component {
    render() {
        return <div>
            <div>
                <h3>AddForm Form</h3>
                <AddFormForm
                    style={{
                        width: 640,
                        height: 400,
                        padding: 15,
                        border: "2px solid #999"
                    }}
                    pendingFields={pendingFields}
                    onSubmit={f => {
                        console.log("onSubmit", f);
                    }}
                    onCancel={() => {
                        console.log("cancel");
                    }} />
            </div>
        </div>
    }
}

export default AddFormFormStories;
