import SheetAction from "./SheetAction";
import Parameter from "../model/Parameter";
import Action from "./Action";

class SubmitForm extends SheetAction {
    public formName: string;
    public params: Parameter[];

    public static deserialize(input: any): SubmitForm {
        const sheetName = input.sheetName;
        const formName = input.formName;
        const params: Parameter[] = [];
        if (Array.isArray(input.params)) {
            for (const param of input.params) {
                params.push(Parameter.deserialize(param));
            }
        }
        return new SubmitForm(sheetName, formName, params);
    }

    constructor(sheetName: string, formName: string, params: Parameter[]) {
        super(sheetName);
        this.formName = formName;
        this.params = params;
    }

    public wrapper(): Action {
        const action = new Action();
        action.submitForm = this;
        return action;
    }
}

export default SubmitForm;
