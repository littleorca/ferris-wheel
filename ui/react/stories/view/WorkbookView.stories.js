import React, { Component } from 'react';
import Text from "../../src/model/Text";
import Table from "../../src/model/Table";
import Row from "../../src/model/Row";
import Cell from "../../src/model/Cell";
import GridData from "../../src/model/GridData";
import GridCell from "../../src/model/GridCell";
import Chart from "../../src/model/Chart";
import Values from "../../src/model/Values";
import { VariantType } from "../../src/model/Variant";
import Series from "../../src/model/Series";
import Layout from "../../src/model/Layout";
import Binder from "../../src/model/Binder";
import Axis from "../../src/model/Axis";
import Placement from "../../src/model/Placement";
import Interval from "../../src/model/Interval";
import AxisBand from "../../src/model/AxisBand";
import Color from "../../src/model/Color";
import SheetAsset from "../../src/model/SheetAsset";
import Sheet from "../../src/model/Sheet";
import Workbook from "../../src/model/Workbook";
import Version from "../../src/model/Version";
import WorkbookView from "../../src/view/WorkbookView";
import GridCellImpl from '../../src/model/GridCellImpl';

const text = new Text('test_text', Values.str('hello\n\tworld!'));
const table = new Table('test_table',
    [
        [
            new GridCellImpl(new Cell(0, Values.str('hello'))),
            new GridCellImpl(new Cell(1, Values.str('world'))),
            new GridCellImpl(new Cell(2, Values.str('~!'))),
        ], [
            new GridCellImpl(new Cell(0, Values.dec(10))),
            new GridCellImpl(new Cell(1, Values.dec(15))),
            new GridCellImpl(new Cell(2, Values.withType(VariantType.DECIMAL, 25, "A2+B2"))),
        ]
    ]);

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

class WorkbookViewStories extends Component {
    render() {
        return (
            <div>
                <h3>WorkbookView</h3>
                <WorkbookView
                    workbook={workbook}
                    className="testWorkbook" />
            </div>
        );
    }
}

export default WorkbookViewStories;