import UnionValue from "./UnionValue";
import Layout from "./Layout";
import Values from "./Values";

class Text {
    public name: string;
    public content: UnionValue;
    public layout: Layout;

    public static deserialize(input: any): Text {
        const name = input.name;
        const content = typeof input.content !== 'undefined' ?
            Values.deserialize(input.content) : undefined;
        const layout = typeof input.layout !== 'undefined' ?
            Layout.deserialize(input.layout) : undefined;

        return new Text(name, content, layout);
    }

    constructor(
        name: string = '',
        content: UnionValue = Values.blank(),
        layout: Layout = new Layout()) {

        this.name = name;
        this.content = content;
        this.layout = layout;
    }

    public clone() {
        return Text.deserialize(this);
    }
}

export default Text;
