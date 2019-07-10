import React, { Component } from 'react';
import { WorkbookPresenter } from '../../src';
import Text from "../../src/model/Text"
import Chart from "../../src/model/Chart"
import Table from "../../src/model/Table"
import Values from "../../src/model/Values"
import Row from "../../src/model/Row"
import Cell from "../../src/model/Cell"
import { VariantType } from "../../src/model/Variant"
import Series from "../../src/model/Series"
import Axis from "../../src/model/Axis"
import Layout from "../../src/model/Layout"
import Binder from "../../src/model/Binder"
import Placement from "../../src/model/Placement"
import Interval from "../../src/model/Interval"
import AxisBand from "../../src/model/AxisBand"
import Sheet from "../../src/model/Sheet"
import SheetAsset from "../../src/model/SheetAsset"
import Workbook from "../../src/model/Workbook"
import Version from "../../src/model/Version"
import Color from "../../src/model/Color"
import QueryAutomaton from "../../src/model/QueryAutomaton"
import EditResponse from "../../src/action/EditResponse"
import ChangeList from "../../src/action/ChangeList"
import Grid from "../../src/model/Grid"
import Span from "../../src/model/Span"
import Display from "../../src/model/Display"
import Form from "../../src/model/Form";
import FormField from "../../src/model/FormField";
import FormFieldBinding from "../../src/model/FormFieldBinding";
import { action } from '@storybook/addon-actions';
import GridCellImpl from '../../src/model/GridCellImpl';

const form = new Form("test_form",
    [
        new FormField("f1", VariantType.STRING, Values.str("foo"),
            false, false, "Foo", "Input foo!",
            undefined,
            [
                new FormFieldBinding("t1!'param1'")
            ]),
        new FormField("f2", VariantType.STRING, Values.list([Values.str("bar1"), Values.str("bar2")]),
            false, true, "Bar", "Input bar!",
            Values.list([
                Values.str("bar0"),
                Values.str("bar1"),
                Values.str("bar2"),
                Values.str("bar3"),
            ]),
            [
                new FormFieldBinding("t1!'param1'")
            ]),
    ],
    new Layout(undefined, undefined, undefined, undefined, undefined, new Grid(undefined, undefined, new Span(1, 13), new Span(1, 3))));
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
        ],
    ]);

const queryAutomaton = new QueryAutomaton();
// queryAutomaton.template.userParamRules.push(new ParamRule('bool', VariantType.BOOL, false, []));
// queryAutomaton.template.userParamRules.push(new ParamRule('decimal', VariantType.DECIMAL, false, []));
// queryAutomaton.template.userParamRules.push(new ParamRule('string', VariantType.STRING, false, []));
// queryAutomaton.template.userParamRules.push(new ParamRule('date', VariantType.DATE, false, []));
// queryAutomaton.template.userParamRules.push(new ParamRule('list', VariantType.LIST, false, []));
// queryAutomaton.template.userParamRules.push(new ParamRule('select str', VariantType.STRING, false, [
//     Values.str('foo'),
//     Values.str('bar')
// ]));
// queryAutomaton.template.userParamRules.push(new ParamRule('select dec', VariantType.DECIMAL, false, [
//     Values.dec(1024),
//     Values.dec(4096)
// ]));

const tableWithQueryAutomaton = new Table(
    'test_table_with_query_automaton',
    [
        [
            new GridCellImpl(new Cell(0, Values.str('foo'))),
            new GridCellImpl(new Cell(1, Values.str('bar'))),
        ], [
            new GridCellImpl(new Cell(0, Values.dec(10))),
            new GridCellImpl(new Cell(1, Values.dec(24))),
        ]
    ],
    undefined, undefined,
    { queryAutomaton });

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

const sheet = new Sheet("test_sheet", undefined, new Layout(Display.GRID, 800, undefined, undefined, undefined, new Grid(12)));
sheet.assets.push(new SheetAsset(undefined, undefined, undefined, form));
sheet.assets.push(new SheetAsset(undefined, undefined, text));
sheet.assets.push(new SheetAsset(table));
sheet.assets.push(new SheetAsset(tableWithQueryAutomaton));
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
        action('executeQuery')(request);
        okCallback(new EditResponse(request.txId, 0, 'Ok', new ChangeList([request.action])));
    },

    isBusy: () => false
};

class WorkbookPresenterStories extends Component {
    render() {
        return (
            <div>
                <h3>WorkbookPresenter</h3>
                <WorkbookPresenter
                    workbook={workbook}
                    className="testWorkbook"
                    service={fakeService} />
            </div>
        );
    }
}

export default WorkbookPresenterStories;