import Action from "./Action";

class EditRequest {
    public txId: number;
    public action: Action;

    public static deserialize(input: any) {
        const txId = input.txId;
        const action = Action.deserialize(input.action);
        return new EditRequest(txId, action);
    }

    constructor(txId: number, action: Action) {
        this.txId = txId;
        this.action = action;
    }
}

export default EditRequest;
