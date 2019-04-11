import React, { Component } from 'react';
import {
    WorkbookEditor,
    Text, Chart, Table, Values, Row, Cell, VariantType, Series, Axis, Layout, Binder, Placement, Interval, AxisBand, Sheet, SheetAsset, Workbook, Version, Color,
    EditResponse, ChangeList
} from '../../src';


const text = new Text('test_text', Values.str('hello\n\tworld!'));
const table = new Table('test_table', []);
table.rows.push(new Row(0,
    [
        new Cell(0, Values.str('hello')),
        new Cell(1, Values.str('world')),
        new Cell(2, Values.str('~!')),
    ]));
table.rows.push(new Row(1,
    [
        new Cell(0, Values.dec(123.456), "#,##0.00"),
        new Cell(1, Values.dec(3.141592), "#,##0.000"),
        new Cell(2, Values.withType(VariantType.DECIMAL, 39.3171974522, "A2/B2"), "#,##0.00%"),
    ]));

const lineChart = new Chart(
    'c1',
    'Line',
    Values.str('Chart'),
    Values.auto(['apple', 'boy', 'cat', 'dog']),
    [
        new Series(
            Values.str('s1'),
            Values.blank(),
            Values.auto([19, 13, 21, 9]),
        ),
        new Series(
            Values.str('s2'),
            Values.blank(),
            Values.auto([11, 8, 12, 15]),
        ),
        new Series(
            Values.str('s3'),
            Values.blank(),
            Values.auto([10, 16, 17, 14]),
        ),
    ],
);

const barChart = Chart.deserialize(lineChart);
barChart.type = "Bar";

const radarChart = Chart.deserialize(lineChart);
radarChart.type = "Radar";

const gaugeChart = new Chart(
    "c4",
    "Gauge",
    Values.str("Chart"),
    Values.auto(['foo', 'bar']),
    [
        new Series(
            Values.str('s1'),
            Values.blank(),
            Values.auto([80.1568376]),
        ),
    ],
    new Layout(),
    new Binder(),
    new Axis(),
    new Axis(
        '速度',
        'km/h',
        Placement.UNSET,
        false,
        new Interval(0, 100),
        [
            new AxisBand(
                new Interval(0, 45),
                'normal',
                new Color(0.3, 0.7, 0.3, 1),
            ),
            new AxisBand(
                new Interval(45, 75),
                'dangerous',
                new Color(0.8, 0.6, 0, 1),
            ),
            new AxisBand(
                new Interval(75, 100),
                'horrible',
                new Color(0.8, 0, 0, 1),
            ),
        ],
        undefined,
        '#,##0.000'
    ),
);

const sheetAsset = new SheetAsset(undefined, undefined, text);
const sheet = new Sheet();
sheet.name = 'test_sheet';
sheet.assets.push(sheetAsset);
sheet.assets.push(new SheetAsset(table));
sheet.assets.push(new SheetAsset(undefined, undefined,
    new Text('test_text_2',
        Values.str('foo\nbar'))));
sheet.assets.push(new SheetAsset(undefined, lineChart));
sheet.assets.push(new SheetAsset(undefined, gaugeChart));

const sheet2 = new Sheet(
    'sheet2',
    [
        new SheetAsset(
            undefined,
            barChart
        )
    ]
);

const workbook = new Workbook(
    new Version(0, 0, 1),
    0,
    'test-workbook',
    [
        sheet,
        sheet2,
    ],
);

const fakeService = {
    call: (request, okCallback, errorCallback) => {
        console.log("fakeService:", request);
        okCallback(new EditResponse(request.txId, 0, 'Ok', new ChangeList([request.action])));
    },

    isBusy: () => false
};

class WorkbookEditorStories extends Component {
    render() {
        return (
            <div style={{
                height: 500
            }}>
                <h3>WorkbookEditor</h3>
                <div style={{
                    border: "5px solid #000"
                }}>
                    <WorkbookEditor
                        workbook={workbook}
                        service={fakeService}
                        className="testWorkbookEditor" />
                </div>
            </div>
        );
    }
}

export default WorkbookEditorStories;