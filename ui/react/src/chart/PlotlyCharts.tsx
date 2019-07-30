import * as React from 'react';
import Stacking from '../model/Stacking';
import Placement from '../model/Placement';
import { ChartRendererProps } from './ChartRenderer';
import createPlotlyComponent from 'react-plotly.js/factory';

const Plot = createPlotlyComponent(Plotly);

function getGlobalConfig() {
    return {
        displayModeBar: false,
        responsive: true
    };
}

function getCommonLayout() {
    return {
        autosize: true,
        title: false,
        margin: {
            l: 60, // default: 80
            t: 20, // default: 100
            b: 60, // default: 80
        }
    };
}

function getHiddenAxisConfig() {
    return {
        zeroline: false,
        showticklabels: false,
        showgrid: false
    };
}

export function PlotlyLineChart(props: ChartRendererProps) {
    const chartData = props.data;
    const plotData: any = [];

    chartData.series.forEach(s => {
        const categories = chartData.categories;
        if (categories.length === 0) {
            s.yValues.forEach((v, i) => {
                categories.push(String(i));
            })
        }
        const trace: any = {
            type: "scatter",
            mode: "lines+points",
            name: s.name,
            x: categories,
            y: s.yValues,
            line: {
                shape: "spline"
            }
        };
        if (chartData.yAxis.stacking === Stacking.ABSOLUTE) {
            trace.stackgroup = "one";
        }
        plotData.push(trace);
    });

    const plotConfig = Object.assign({}, getGlobalConfig());
    const layout = Object.assign({}, getCommonLayout(), {
        autosize: true,
        xaxis: {
            title: chartData.xAxis.title
        },
        yaxis: {
            title: chartData.yAxis.title
        }
    });

    return <Plot
        className={props.className}
        style={props.style}
        data={plotData}
        config={plotConfig}
        layout={layout}
        useResizeHandler={true}
    />
}

export function PlotlyBarChart(props: ChartRendererProps) {
    const chartData = props.data;
    const plotData: any = [];

    const isHorizontal = (chartData.yAxis.placement === Placement.BOTTOM);

    chartData.series.forEach(s => {
        const categories = chartData.categories;
        if (categories.length === 0) {
            s.yValues.forEach((v, i) => {
                categories.push(String(i));
            })
        }

        const trace: any = {
            type: "bar",
            name: s.name,
            x: categories,
            y: s.yValues,
            orientation: "v"
        };

        if (isHorizontal) {
            trace.x = s.yValues;
            trace.y = categories;
            trace.orientation = "h";
        }
        plotData.push(trace);
    });

    const plotConfig = Object.assign({}, getGlobalConfig());
    const layout: any = Object.assign({}, getCommonLayout(), {
        autosize: true,
        xaxis: {
            title: isHorizontal ? chartData.yAxis.title : chartData.xAxis.title
        },
        yaxis: {
            title: isHorizontal ? chartData.xAxis.title : chartData.yAxis.title
        }
    });
    if (chartData.yAxis.stacking === Stacking.ABSOLUTE) {
        layout.barmode = "stack";
    }

    return <Plot
        className={props.className}
        style={props.style}
        data={plotData}
        config={plotConfig}
        layout={layout}
        useResizeHandler={true}
    />
}

function _internalPlotlyPieChart(props: ChartRendererProps, hole?: number) {
    const chartData = props.data;
    const plotData: any = [];

    const pieCount = chartData.series.length;
    const columns = Math.min(pieCount, 4);
    const rows = Math.ceil(pieCount / columns);

    chartData.series.forEach((s, i) => {
        const categories = chartData.categories;
        if (categories.length === 0) {
            s.yValues.forEach((v, j) => {
                categories.push(String(j));
            })
        }
        const trace: any = {
            type: "pie",
            name: s.name,
            labels: categories,
            values: s.yValues,
            domain: {
                rows: Math.floor(i / columns),
                column: i % columns
            }
        };
        if (typeof hole !== "undefined") {
            trace.hole = hole;
        }
        plotData.push(trace);
    });

    const plotConfig = Object.assign({}, getGlobalConfig());
    const layout = Object.assign({}, getCommonLayout(), {
        autosize: true,
        grid: {
            rows,
            columns
        },
        xaxis: getHiddenAxisConfig(),
        yaxis: getHiddenAxisConfig()
    });
    layout.margin.b = 20;

    return <Plot
        className={props.className}
        style={props.style}
        data={plotData}
        config={plotConfig}
        layout={layout}
        useResizeHandler={true}
    />
}

export function PlotlyPieChart(props: ChartRendererProps) {
    return _internalPlotlyPieChart(props);
}

export function PlotlyDoughnutChart(props: ChartRendererProps) {
    return _internalPlotlyPieChart(props, .4);
}

export function PlotlyRadarChart(props: ChartRendererProps) {
    const chartData = props.data;
    const plotData: any = [];

    chartData.series.forEach(s => {
        const categories = chartData.categories;
        if (categories.length === 0) {
            s.yValues.forEach((v, i) => {
                categories.push(String(i));
            })
        }
        const trace = {
            type: "scatterpolar",
            name: s.name,
            theta: categories,
            r: s.yValues,
            fill: "toself"
        };
        plotData.push(trace);
    });

    const plotConfig = Object.assign({}, getGlobalConfig());
    const layout = Object.assign({}, getCommonLayout(), {
        autosize: true,
        polar: {
            radialaxis: {
                visible: true
            }
        },
        xaxis: getHiddenAxisConfig(),
        yaxis: getHiddenAxisConfig()
    });
    layout.margin.b = 20;

    return <Plot
        className={props.className}
        style={props.style}
        data={plotData}
        config={plotConfig}
        layout={layout}
        useResizeHandler={true}
    />
}

export function PlotlyScatterChart(props: ChartRendererProps) {
    const chartData = props.data;
    const plotData: any = [];

    chartData.series.forEach(s => {
        const categories = chartData.categories;
        if (categories.length === 0) {
            s.yValues.forEach((v, i) => {
                categories.push(String(i));
            })
        }
        const trace = {
            type: "scatter",
            mode: "markers",
            name: s.name,
            text: categories,
            x: s.xValues,
            y: s.yValues,
            marker: {
                size: 10
            }
        };
        plotData.push(trace);
    });

    const plotConfig = Object.assign({}, getGlobalConfig());
    const layout = Object.assign({}, getCommonLayout(), {
        autosize: true,
        xaxis: {
            title: chartData.xAxis.title
        },
        yaxis: {
            title: chartData.yAxis.title
        }
    });

    return <Plot
        className={props.className}
        style={props.style}
        data={plotData}
        config={plotConfig}
        layout={layout}
        useResizeHandler={true}
    />
}

export function PlotlyBubbleChart(props: ChartRendererProps) {
    const chartData = props.data;
    const plotData: any = [];

    const maxMarkerSize = 50;
    let maxSize = 0;

    chartData.series.forEach(s => {
        s.zValues.forEach(v => {
            if (v > maxSize) {
                maxSize = v;
            }
        })
    })

    chartData.series.forEach(s => {
        const categories = chartData.categories;
        if (categories.length === 0) {
            s.yValues.forEach((v, i) => {
                categories.push(String(i));
            })
        }
        const trace = {
            type: "scatter",
            mode: "markers",
            name: s.name,
            text: categories,
            x: s.xValues,
            y: s.yValues,
            marker: {
                size: s.zValues,
                sizeref: 2.0 * maxSize / (maxMarkerSize ** 2),
                sizemode: "area"
            }
        };
        plotData.push(trace);
    });

    const plotConfig = Object.assign({}, getGlobalConfig());
    const layout = Object.assign({}, getCommonLayout(), {
        autosize: true,
        xaxis: {
            title: chartData.xAxis.title
        },
        yaxis: {
            title: chartData.yAxis.title
        }
    });

    return <Plot
        className={props.className}
        style={props.style}
        data={plotData}
        config={plotConfig}
        layout={layout}
        useResizeHandler={true}
    />
}
