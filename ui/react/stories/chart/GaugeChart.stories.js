import * as React from "react";
import GaugeChart from "../../src/chart/GaugeChart";
import Axis from "../../src/model/Axis";
import AxisBand from "../../src/model/AxisBand";
import Interval from "../../src/model/Interval";
import Color from "../../src/model/Color";

function createGauge(title, value) {
    return {
        type: "Gauge",
        name: title,
        title: title,
        categories: [],
        series: [{
            name: "s1",
            xValues: [],
            yValues: [value],
            zValues: []
        }],
        xAxis: new Axis(),
        yAxis: new Axis(undefined, undefined, undefined, undefined, undefined, [
            new AxisBand(new Interval(0, 50), "green", new Color(0, .7, 0)),
            new AxisBand(new Interval(50, 75), "yellow", new Color(.7, .6, 0)),
            new AxisBand(new Interval(75, 100), "red", new Color(.8, .2, 0)),
        ]),
        zAxis: new Axis()
    };
}

const gauges = [
    {
        type: "Gauge",
        name: "gauge-0",
        title: "Gauge 0 (empty)",
        categories: [],
        series: [],
        xAxis: new Axis(),
        yAxis: new Axis(),
        zAxis: new Axis()
    },
    createGauge("Gauge 1 (normal)", 10),
    createGauge("Gauge 2 (normal)", 60),
    createGauge("Gauge 3 (normal)", 80),
    createGauge("Gauge 4 (<min)", -10),
    createGauge("Gauge 5 (min)", 0),
    createGauge("Gauge 6 (max)", 100),
    createGauge("Gauge 7 (>max)", 210),
    {
        type: "Gauge",
        name: "gauge-8",
        title: "Gauge 8 (#,##0.00%)",
        categories: [],
        series: [{
            name: "s1",
            xValues: [],
            yValues: [0.12345],
            zValues: []
        }],
        xAxis: new Axis(),
        yAxis: new Axis(undefined, undefined, undefined, undefined, undefined, [
            new AxisBand(new Interval(0, .5), "green", new Color(0, .7, 0)),
            new AxisBand(new Interval(.5, .75), "yellow", new Color(.7, .6, 0)),
            new AxisBand(new Interval(.75, 1), "red", new Color(.8, .2, 0)),
        ], undefined, "#,##0.00%"),
        zAxis: new Axis()
    },
    {
        type: "Gauge",
        name: "gauge-9",
        title: "Gauge 9 (default fmt)",
        categories: [],
        series: [{
            name: "s1",
            xValues: [],
            yValues: [123],
            zValues: []
        }],
        xAxis: new Axis(),
        yAxis: new Axis(undefined, undefined, undefined, undefined, undefined, [
            new AxisBand(new Interval(0, 50), "green", new Color(0, .7, 0)),
            new AxisBand(new Interval(50, 75), "yellow", new Color(.7, .6, 0)),
            new AxisBand(new Interval(75, 150), "red", new Color(.8, .2, 0)),
        ]),
        zAxis: new Axis()
    },
    {
        type: "Gauge",
        name: "gauge-10",
        title: "Gauge 10 (default fmt)",
        categories: [],
        series: [{
            name: "s1",
            xValues: [],
            yValues: [12345],
            zValues: []
        }],
        xAxis: new Axis(),
        yAxis: new Axis(undefined, undefined, undefined, undefined, undefined, [
            new AxisBand(new Interval(0, 5000), "green", new Color(0, .7, 0)),
            new AxisBand(new Interval(5000, 7500), "yellow", new Color(.7, .6, 0)),
            new AxisBand(new Interval(7500, 15000), "red", new Color(.8, .2, 0)),
        ]),
        zAxis: new Axis()
    },
    {
        type: "Gauge",
        name: "gauge-11",
        title: "Gauge 11 (huge)",
        categories: [],
        series: [{
            name: "s1",
            xValues: [],
            yValues: [12345678900000000],
            zValues: []
        }],
        xAxis: new Axis(),
        yAxis: new Axis(undefined, undefined, undefined, undefined, undefined, [
            new AxisBand(new Interval(0, 10000000000000000), "green", new Color(0, .7, 0)),
            new AxisBand(new Interval(10000000000000000, 20000000000000000), "yellow", new Color(.7, .6, 0)),
            new AxisBand(new Interval(20000000000000000, 30000000000000000), "red", new Color(.8, .2, 0)),
        ]),
        zAxis: new Axis()
    }
];

const containerStyle = {
    display: "flex",
    flexFlow: "row wrap",
}

const paneStyle = {
    width: "180px",
    padding: "20px",
}

class GaugeChartStories extends React.Component {
    render() {
        return (
            <div style={containerStyle}>
                {gauges.map((g, i) => (
                    <div key={i} style={paneStyle}>
                        <h3>{g.title}</h3>
                        <div>
                            <GaugeChart data={g} />
                        </div>
                    </div>
                ))}
            </div>
        );
    }
}

export default GaugeChartStories;