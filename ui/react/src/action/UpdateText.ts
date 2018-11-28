import Text from '../model/Text';
import SheetAction from './SheetAction';
import Action from './Action';

class UpdateText extends SheetAction {
    public text: Text;

    public static deserialize(input: any) {
        const sheetName: string = input.sheetName;
        const text = Text.deserialize(input.text);
        return new UpdateText(sheetName, text);
    }

    constructor(sheetName: string, text: Text) {
        super(sheetName);
        this.text = text;
    }

    public isAssetAction(): boolean {
        return true;
    }

    public targetAsset(): string | undefined {
        return this.text.name;
    }

    public wrapper(): Action {
        const action = new Action();
        action.updateText = this;
        return action;
    }

}

export default UpdateText;
