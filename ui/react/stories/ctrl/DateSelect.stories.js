import React, { Component } from "react";
import DateSelect from "../../src/ctrl/DateSelect";

let date = new Date();

class DateSelectStories extends Component {
    render() {
        return (
            <div>
                <DateSelect
                    value={date}
                    afterChange={newVal => {
                        date = newVal;
                        this.forceUpdate();
                    }} />
            </div>
        );
    }
}

export default DateSelectStories;
