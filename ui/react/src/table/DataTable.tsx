import * as React from "react";
import classnames from "classnames";
import "./DataTable.css";

interface DataTableProps extends React.ClassAttributes<DataTable> {
    className?: string;
    style?: React.CSSProperties;
}

class DataTable extends React.Component<DataTableProps> {

    render() {
        const className = classnames("fw-data-table", this.props.className);
        const style = this.props.style;

        return (
            <div className={className}
                style={style}>
                <table>
                    <thead>
                        <tr>
                            <th></th>
                            <th>h1</th>
                            <th>h2</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <th>1</th>
                            <td>value1</td>
                            <td>value2</td>
                        </tr>
                        <tr>
                            <th>2</th>
                            <td>value3</td>
                            <td>value4</td>
                        </tr>
                    </tbody>
                </table>
            </div>
        );
    }
}

export default DataTable;