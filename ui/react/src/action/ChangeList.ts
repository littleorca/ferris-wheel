import Action from "./Action";

class ChangeList {
    public actions: Action[];

    public static deserialize(input: any): ChangeList {
        const actions = [];
        if (typeof input.actions !== 'undefined') {
            for (const action of input.actions) {
                actions.push(Action.deserialize(action));
            }
        }
        return new ChangeList(actions);
    }

    constructor(actions: Action[]) {
        this.actions = actions;
    }
}

export default ChangeList;
