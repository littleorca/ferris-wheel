import Text from '../model/Text';
import SheetAction from './SheetAction';
import Action from './Action';

class AddText extends SheetAction {
    public text: Text;

    public static deserialize(input: any) {
        const sheetName: string = input.sheetName;
        const text = Text.deserialize(input.text);
        return new AddText(sheetName, text);
    }

    constructor(sheetName: string, text: Text) {
        super(sheetName);
        this.text = text;
    }

    public wrapper(): Action {
        const action = new Action();
        action.addText = this;
        return action;
    }

}

export default AddText;
