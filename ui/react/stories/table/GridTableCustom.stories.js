import * as React from "react";
import { exampleColumnHeaders, exampleRowHeaders } from "./GridData.stories";
import GridTable from "../../src/table/GridTable";
import GridCellImpl from "../../src/model/GridCellImpl";
import GridDataImpl from "../../src/model/GridDataImpl";
import Values from "../../src/model/Values";
import Formatter from "../../src/util/Formatter";
import { VariantType } from "../../src/model/Variant";
import UnionValueEdit, { fromEditableString } from "../../src/ctrl/UnionValueEdit";

const data = new GridDataImpl(
    [[
        new GridCellImpl(Values.str("hello world"), "left"),
        new GridCellImpl(Values.str("the beautiful"), "left"),
        new GridCellImpl(Values.str("world !!!!"), "left"),
    ], [
        new GridCellImpl(Values.bool(true), "left"),
        new GridCellImpl(Values.date(new Date()), "left"),
        new GridCellImpl(Values.dec(512), "right"),
    ], [
        new GridCellImpl(Values.bool(false), "left"),
        new GridCellImpl(Values.date(new Date()), "left"),
        new GridCellImpl(Values.withType(VariantType.DECIMAL, 1024, "C2*2"), "center"),
    ]]
);

const colors = ["#fed", "#efe", "#def"];
let colorIndex = 0;
function customRenderer(props) {
    return (
        <span style={{
            backgroundColor: colors[colorIndex++ % colors.length]
        }}>{Formatter.format(props.data, "")}</span>
    );
}

function customEditor(props) {
    const initialUpdate = typeof props.initialInput !== "undefined" ?
        fromEditableString(props.initialInput) : undefined;
    return (
        <UnionValueEdit
            style={{
                border: "0 none"
            }}
            value={props.data}
            initialUpdate={initialUpdate}
            focusByDefault={true}
            afterChange={valueChange => {
                if (valueChange.type === "rollback") {
                    props.onCancel();
                } else if (valueChange.type === "commit") {
                    props.onOk(valueChange.toValue);
                }
            }}
            afterEndEdit={() => {
                props.onCancel();
            }} />
    );
}

class GridTableCustomStories extends React.Component {

    render() {
        return (
            <div>
                <h3>
                    Custom Renderer and Editor
                </h3>
                <GridTable
                    style={{
                        width: 600,
                        height: 300
                    }}
                    data={data}
                    editable={true}
                    newValue={() => Values.blank()}
                    forDisplay={v => v.toString()}
                    forEdit={v => toEditableString(v)}
                    fromEditableString={s => fromEditableString(s)}
                    customRenderer={customRenderer}
                    customEditor={customEditor} />
            </div>
        )
    }
}

export default GridTableCustomStories;