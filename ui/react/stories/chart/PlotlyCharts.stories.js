import * as React from "react";
import {
    Axis,
    PlotlyPieChart, PlotlyRadarChart
} from "../../src";

const emptyPie = {
    type: "Pie",
    name: "empty-pie",
    title: "Empty Pie",
    categories: [],
    series: [],
    xAxis: new Axis(),
    yAxis: new Axis(),
    zAxis: new Axis()
};

const emptyRadar = {
    type: "Radar",
    name: "empty-radar",
    title: "Empty radar",
    categories: [],
    series: [],
    xAxis: new Axis(),
    yAxis: new Axis(),
    zAxis: new Axis()
};

class PlotlyChartsStories extends React.Component {
    render() {
        return (
            <div>
                <div>
                    <h3>empty pie</h3>
                    <PlotlyPieChart data={emptyPie} />
                </div>
                <div>
                    <h3>empty radar</h3>
                    <PlotlyRadarChart data={emptyRadar} />
                </div>
            </div>);
    }
}

export default PlotlyChartsStories;