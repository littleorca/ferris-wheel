import React, { Component } from 'react';
import {
    ChartView,
    Chart, Series, AxisBand, Values, Layout, Binder, Axis, Placement, Interval, Color, Stacking
} from '../../src';

const lineChart = new Chart(
    'c0',
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
    undefined,
    undefined,
    new Axis('X-title', 'X-label'),
    new Axis('Y-title', 'Y-label')
);

const lineChartWithDateAxis = new Chart(
    'c0',
    'Line',
    Values.str('Chart'),
    Values.auto([new Date('2019/04/07'), new Date('2019/04/08'), new Date('2019/04/09'), new Date('2019/04/10')]),
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
    undefined,
    undefined,
    new Axis('X-title', 'X-label'),
    new Axis('Y-title', 'Y-label')
);

const lineChartMissingCategories = new Chart(
    'c1',
    'Line',
    Values.str('Chart'),
    Values.blank(),
    [
        new Series(
            Values.str('s1'),
            Values.blank(),
            Values.auto([19, 13, 21, 9]),
        )
    ],
);

const stackedLineChart = Chart.deserialize(lineChart);
stackedLineChart.yAxis.stacking = Stacking.ABSOLUTE;

const barChart = Chart.deserialize(lineChart);
barChart.type = "Bar";

const horizontalBarChart = Chart.deserialize(barChart);
horizontalBarChart.yAxis.placement = Placement.BOTTOM;

const stackedBarChart = Chart.deserialize(barChart);
stackedBarChart.yAxis.stacking = Stacking.ABSOLUTE;

const pieChart = new Chart(
    'c2',
    'Pie',
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
    ],
);

const doughnutChart = Chart.deserialize(pieChart);
doughnutChart.type = 'Doughnut';

const scatterChart = new Chart(
    'c3',
    'Scatter',
    Values.str('Chart'),
    Values.auto(['apple', 'boy', 'cat', 'dog']),
    [
        new Series(
            Values.str('s1'),
            Values.auto([1, 2, 3, 4]),
            Values.auto([19, 13, 21, 9]),
        ),
        new Series(
            Values.str('s2'),
            Values.auto([3, 8, 6, 2]),
            Values.auto([11, 8, 12, 15]),
        ),
    ],
    undefined,
    undefined,
    new Axis('X-title', 'X-label'),
    new Axis('Y-title', 'Y-label')
);

const bubbleChart = new Chart(
    'c3',
    'Bubble',
    Values.str('Chart'),
    Values.auto(['apple', 'boy', 'cat', 'dog']),
    [
        new Series(
            Values.str('s1'),
            Values.auto([1, 2, 3, 4]),
            Values.auto([19, 13, 21, 9]),
            Values.auto([100, 103, 201, 199]),
        ),
        new Series(
            Values.str('s2'),
            Values.auto([3, 8, 6, 2]),
            Values.auto([11, 8, 12, 15]),
            Values.auto([110, 80, 120, 150]),
        ),
    ],
    undefined,
    undefined,
    new Axis('X-title', 'X-label'),
    new Axis('Y-title', 'Y-label')
);

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


class ChartViewStories extends Component {

    render() {
        return (
            <div>
                <h3>ChartView</h3>
                <div>
                    <h4>Line</h4>
                    <ChartView
                        chart={lineChart} />
                </div>
                <div>
                    <h4>Line (date categories)</h4>
                    <ChartView
                        chart={lineChartWithDateAxis} />
                </div>
                <div>
                    <h4>Line (missing categories)</h4>
                    <ChartView
                        chart={lineChartMissingCategories} />
                </div>
                <div>
                    <h4>Stacked Line Chart</h4>
                    <ChartView
                        chart={stackedLineChart} />
                </div>
                <div>
                    <h4>Bar</h4>
                    <ChartView
                        chart={barChart} />
                </div>
                <div>
                    <h4>Horizontal Bar</h4>
                    <ChartView
                        chart={horizontalBarChart} />
                </div>
                <div>
                    <h4>Stacked Bar</h4>
                    <ChartView
                        chart={stackedBarChart} />
                </div>
                <div>
                    <h4>Pie</h4>
                    <ChartView
                        chart={pieChart} />
                </div>
                <div>
                    <h4>Doughnut</h4>
                    <ChartView
                        chart={doughnutChart} />
                </div>
                <div>
                    <h4>Radar</h4>
                    <ChartView
                        chart={radarChart} />
                </div>
                <div>
                    <h4>Scatter</h4>
                    <ChartView
                        chart={scatterChart} />
                </div>
                <div>
                    <h4>Bubble</h4>
                    <ChartView
                        chart={bubbleChart} />
                </div>
                <div>
                    <h4>Gauge</h4>
                    <ChartView
                        chart={gaugeChart} />
                </div>
            </div>
        );
    }
}

export default ChartViewStories;