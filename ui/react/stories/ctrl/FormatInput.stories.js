import React from "react";
import FormatInput from "../../src/ctrl/FormatInput";

let format = "";

class FormatInputStories extends React.Component {
    render() {
        return <div>
            <div>
                <FormatInput
                    format={format}
                    afterChange={fmt => {
                        format = fmt;
                        this.forceUpdate();
                    }} />
            </div>
        </div>
    }
}

export default FormatInputStories;