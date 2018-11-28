import { VariantType } from "./Variant";
import UnionValue from "./UnionValue";

class ListValue extends UnionValue {
    public list: { items: UnionValue[] };

    constructor(items: UnionValue[] = [], formula?: string) {
        super(formula);
        this.list = { items };
    }

    public valueType() {
        return VariantType.LIST;
    }

    public listValue() {
        return this.list.items;
    }

    public itemCount() {
        return this.list.items.length;
    }

    public item(i: number) {
        return this.list.items[i];
    }
}

export default ListValue;
