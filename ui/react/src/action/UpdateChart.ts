import Chart from '../model/Chart';
import SheetAction from './SheetAction';
import Action from './Action';

class UpdateChart extends SheetAction {
    public chart: Chart;

    public static deserialize(input: any) {
        const sheetName: string = input.sheetName;
        const chart = Chart.deserialize(input.chart);
        return new UpdateChart(sheetName, chart);
    }

    constructor(sheetName: string, chart: Chart) {
        super(sheetName);
        this.chart = chart;
    }

    public isAssetAction(): boolean {
        return true;
    }

    public targetAsset(): string | undefined {
        return this.chart.name;
    }

    public wrapper(): Action {
        const action = new Action();
        action.updateChart = this;
        return action;
    }

}

export default UpdateChart;
