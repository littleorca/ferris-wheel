import ChangeList from "./ChangeList";
import Workbook from "../model/Workbook";
import Chart from "../model/Chart";

class EditResponse {
    public txId: number;
    public statusCode: number;
    public message: string;
    public changes?: ChangeList;
    public workbook?: Workbook;
    public suggestedChart?: Chart;

    public static deserialize(input: any) {
        const txId: number = Number(input.txId);
        const statusCode: number = Number(input.statusCode);
        const message: string = input.message;
        const changes: ChangeList | undefined = typeof input.changes !== 'undefined' ? ChangeList.deserialize(input.changes) : undefined;
        const workbook: Workbook | undefined = typeof input.workbook !== 'undefined' ?
            Workbook.deserialize(input.workbook) : undefined;
        const suggestedChart: Chart | undefined = typeof input.suggestedChart !== 'undefined' ?
            Chart.deserialize(input.suggestedChart) : undefined;
        return new EditResponse(txId, statusCode, message, changes, workbook, suggestedChart);
    }

    constructor(
        txId: number,
        statusCode: number,
        message: string,
        changes?: ChangeList,
        workbook?: Workbook,
        suggestedChart?: Chart) {

        this.txId = txId;
        this.statusCode = statusCode;
        this.message = message;
        this.changes = changes;
        this.workbook = workbook;
        this.suggestedChart = suggestedChart;
    }
}

export default EditResponse;
