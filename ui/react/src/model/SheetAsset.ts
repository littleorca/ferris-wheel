import Table from "./Table";
import Chart from "./Chart";
import Text from './Text';

class SheetAsset {
    public table?: Table;
    public chart?: Chart;
    public text?: Text;

    public static deserialize(input: any): SheetAsset {
        const table = typeof input.table === 'undefined' ?
            undefined : Table.deserialize(input.table);
        const chart = typeof input.chart === 'undefined' ?
            undefined : Chart.deserialize(input.chart);
        const text = typeof input.text === 'undefined' ?
            undefined : Text.deserialize(input.text);
        return new SheetAsset(table, chart, text);
    }

    constructor(table?: Table, chart?: Chart, text?: Text) {
        this.table = table;
        this.chart = chart;
        this.text = text;
    }

    public assetType(): "table" | "chart" | "text" {
        if (typeof this.table !== 'undefined') {
            return "table";

        } else if (typeof this.chart !== 'undefined') {
            return "chart";

        } else if (typeof this.text !== 'undefined') {
            return "text";
        }

        throw new Error('Illegal asset.');
    }

    public specific(): Table | Chart | Text {
        if (typeof this.table !== 'undefined') {
            return this.table;

        } else if (typeof this.chart !== 'undefined') {
            return this.chart;

        } else if (typeof this.text !== 'undefined') {
            return this.text;
        }

        throw new Error('Illegal asset.');
    }
}

export default SheetAsset;
