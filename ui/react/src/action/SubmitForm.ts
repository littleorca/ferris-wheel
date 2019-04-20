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
            for (let i = 0; i < input.params.length; i++) {
                params.push(Parameter.deserialize(input.params[i]));
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
