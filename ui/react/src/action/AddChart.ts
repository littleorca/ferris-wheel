import Chart from '../model/Chart';
import SheetAction from './SheetAction';
import Action from './Action';

class AddChart extends SheetAction {
    public chart: Chart;

    public static deserialize(input: any) {
        const sheetName: string = input.sheetName;
        const chart: Chart = Chart.deserialize(input.chart);
        return new AddChart(sheetName, chart);
    }

    constructor(sheetName: string, chart: Chart) {
        super(sheetName);
        this.chart = chart;
    }

    public wrapper(): Action {
        const action = new Action();
        action.addChart = this;
        return action;
    }

}

export default AddChart;