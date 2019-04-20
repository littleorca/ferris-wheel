import SheetAction from "./SheetAction";
import Form from "../model/Form";
import Action from "./Action";

class AddForm extends SheetAction {
    public form: Form;

    public static deserialize(input: any): AddForm {
        const sheetName = input.sheetName;
        const form = Form.deserialize(input.form);
        return new AddForm(sheetName, form);
    }

    constructor(sheetName: string, form: Form) {
        super(sheetName);
        this.form = form;
    }

    public wrapper(): Action {
        const action = new Action();
        action.addForm = this;
        return action;
    }
}

export default AddForm;