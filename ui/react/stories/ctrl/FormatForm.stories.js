import React, { Component } from "react";
import FormatForm from "../../src/form/FormatForm";
import Values from "../../src/model/Values";

class FormatFormStories extends Component {
    format = "";
    decimalSample = Values.dec("1324.5768");
    dateSample = Values.date(new Date());
    render() {
        return (
            <div>
                <h2>FormatForm</h2>
                <FormatForm
                    format={this.format}
                    onChange={format => {
                        console.log("onChange=>", format);
                        this.format = format;
                        this.forceUpdate();
                    }}
                />
                <h2>FormatForm with decimal sample</h2>
                <FormatForm
                    format={this.format}
                    sample={this.decimalSample}
                    onChange={format => {
                        console.log("onChange=>", format);
                        this.format = format;
                        this.forceUpdate();
                    }}
                />
                <h2>FormatForm with date sample</h2>
                <FormatForm
                    format={this.format}
                    sample={this.dateSample}
                    onChange={format => {
                        console.log("onChange=>", format);
                        this.format = format;
                        this.forceUpdate();
                    }}
                />
            </div>
        );
    }
}

export default FormatFormStories;
