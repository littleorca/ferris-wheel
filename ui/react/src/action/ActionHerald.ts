import ActionHandler from "./ActionHandler";

interface ActionHerald {
    subscribe(handler: ActionHandler): void;
    unsubscribe(handler: ActionHandler): void;
}

export default ActionHerald;
