import Table from "./Table";
import Chart from "./Chart";
import Text from './Text';
import Form from "./Form";

class SheetAsset {
    public table?: Table;
    public chart?: Chart;
    public text?: Text;
    public form?: Form;

    public static deserialize(input: any): SheetAsset {
        const table = typeof input.table === 'undefined' ?
            undefined : Table.deserialize(input.table);
        const chart = typeof input.chart === 'undefined' ?
            undefined : Chart.deserialize(input.chart);
        const text = typeof input.text === 'undefined' ?
            undefined : Text.deserialize(input.text);
        const form = typeof input.form === 'undefined' ?
            undefined : Form.deserialize(input.form);
        return new SheetAsset(table, chart, text, form);
    }

    constructor(table?: Table, chart?: Chart, text?: Text, form?: Form) {
        this.table = table;
        this.chart = chart;
        this.text = text;
        this.form = form;
    }

    public assetType(): "table" | "chart" | "text" | "form" {
        if (typeof this.table !== 'undefined') {
            return "table";

        } else if (typeof this.chart !== 'undefined') {
            return "chart";

        } else if (typeof this.text !== 'undefined') {
            return "text";

        } else if (typeof this.form !== 'undefined') {
            return "form";
        }

        throw new Error('Illegal asset.');
    }

    public specific(): Table | Chart | Text | Form {
        if (typeof this.table !== 'undefined') {
            return this.table;

        } else if (typeof this.chart !== 'undefined') {
            return this.chart;

        } else if (typeof this.text !== 'undefined') {
            return this.text;

        } else if (typeof this.form !== 'undefined') {
            return this.form;
        }

        throw new Error('Illegal asset.');
    }

    public clone() {
        return new SheetAsset(
            this.table ? this.table.clone() : undefined,
            this.chart ? this.chart.clone() : undefined,
            this.text ? this.text.clone() : undefined,
            this.form ? this.form.clone() : undefined);
    }
}

export default SheetAsset;
