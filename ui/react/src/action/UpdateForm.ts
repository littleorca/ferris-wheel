import SheetAction from "./SheetAction";
import Form from "../model/Form";
import Action from "./Action";

class UpdateForm extends SheetAction {
    public form: Form;

    public static deserialize(input: any): UpdateForm {
        const sheetName = input.sheetName;
        const form = Form.deserialize(input.form);
        return new UpdateForm(sheetName, form);
    }

    constructor(sheetName: string, form: Form) {
        super(sheetName);
        this.form = form;
    }

    public isAssetAction(): boolean {
        return true;
    }

    public targetAsset(): string | undefined {
        return this.form.name;
    }

    public wrapper(): Action {
        const action = new Action();
        action.updateForm = this;
        return action;
    }
}

export default UpdateForm;
