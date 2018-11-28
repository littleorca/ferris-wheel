import ActionMeta from "./ActionMeta";
import Action from "./Action";

// deprecated
class WorkbookOperation extends ActionMeta {
    public pathname: string;

    public static deserialize(input: any) {
        const pathname: string = input.pathname;
        return new WorkbookOperation(pathname);
    }

    constructor(pathname: string) {
        super();
        this.pathname = pathname;
    }

    public wrapAsSaveWorkbook() {
        const action = new Action();
        action.saveWorkbook = this;
        return action;
    }
}

export default WorkbookOperation;
