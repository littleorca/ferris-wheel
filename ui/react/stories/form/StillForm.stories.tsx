import * as React from "react";
import StillFormEnclosure from "../../src/form/StillFormEnclosure";
import Values from "../../src/model/Values";
import UnionValueEdit from "../../src/ctrl/UnionValueEdit";
import ValueChange from "../../src/ctrl/ValueChange";
import UnionValue from "../../src/model/UnionValue";

class StillFormStories extends React.Component {
    private unionValue = Values.str("foobar");

    constructor(props: any) {
        super(props);

        this.handleUnionValueChange = this.handleUnionValueChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleReset = this.handleReset.bind(this);
    }

    protected handleUnionValueChange(change: ValueChange<UnionValue>) {
        console.log("change", change);
    }

    protected handleSubmit() {
        console.log("submit");
    }

    protected handleReset() {
        console.log("reset");
    }

    public render() {
        return (
            <div>
                <StillFormEnclosure
                    onSubmit={this.handleSubmit}
                    onReset={this.handleReset}>
                    <div>
                        <input type="text" name="test" defaultValue="test" />
                    </div>
                    <div>
                        <UnionValueEdit
                            value={this.unionValue}
                            afterChange={this.handleUnionValueChange} />
                    </div>
                </StillFormEnclosure>
            </div>
        );
    }
}

export default StillFormStories;
