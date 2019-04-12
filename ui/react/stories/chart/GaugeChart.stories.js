import * as React from "react";
import {
    GaugeChart,
    Axis,
    AxisBand,
    Interval,
    Color,
} from "../../src";

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

const gauge0 = {
    type: "Gauge",
    name: "gauge-0",
    title: "Gauge 0 (empty)",
    categories: [],
    series: [],
    xAxis: new Axis(),
    yAxis: new Axis(),
    zAxis: new Axis()
};

const gauge1 = createGauge("Gauge 1 (normal)", 10);
const gauge2 = createGauge("Gauge 2 (normal)", 60);
const gauge3 = createGauge("Gauge 3 (normal)", 80);
const gauge4 = createGauge("Gauge 4 (less than min)", -10);
const gauge5 = createGauge("Gauge 5 (min)", 0);
const gauge6 = createGauge("Gauge 6 (max)", 100);
const gauge7 = createGauge("Gauge 7 (greater than max)", 210);

const containerStyle = {
    display: "flex"
}

const paneStyle = {
    minWidth: "100px",
    maxWidth: "400px",
    padding: "20px",
}

class GaugeChartStories extends React.Component {
    render() {
        return (
            <div style={containerStyle}>
                <div style={paneStyle}>
                    <h3>Gauge 0 (empty)</h3>
                    <div>
                        <GaugeChart data={gauge0} />
                    </div>
                </div>
                <div style={paneStyle}>
                    <h3>Gauge 1 (normal)</h3>
                    <div>
                        <GaugeChart data={gauge1} />
                    </div>
                </div>
                <div style={paneStyle}>
                    <h3>Guague 2 (normal)</h3>
                    <div>
                        <GaugeChart data={gauge2} />
                    </div>
                </div>
                <div style={paneStyle}>
                    <h3>Gauge 3 (normal)</h3>
                    <div>
                        <GaugeChart data={gauge3} />
                    </div>
                </div>
                <div style={paneStyle}>
                    <h3>Gauge 4 (normal)</h3>
                    <div>
                        <GaugeChart data={gauge4} />
                    </div>
                </div>
                <div style={paneStyle}>
                    <h3>Gauge 5 (normal)</h3>
                    <div>
                        <GaugeChart data={gauge5} />
                    </div>
                </div>
                <div style={paneStyle}>
                    <h3>Gauge 6 (normal)</h3>
                    <div>
                        <GaugeChart data={gauge6} />
                    </div>
                </div>
                <div style={paneStyle}>
                    <h3>Gauge 7 (normal)</h3>
                    <div>
                        <GaugeChart data={gauge7} />
                    </div>
                </div>
            </div>
        );
    }
}

export default GaugeChartStories;