type ValueChangeType = "edit" | "commit" | "rollback";

interface ValueChange<V> {
    id?: string;
    name?: string;
    fromValue?: V;
    toValue: V;
    type: ValueChangeType;
}

export default ValueChange;
export { ValueChangeType };
