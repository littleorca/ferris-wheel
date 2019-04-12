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

function getCommonStyle() {
    return {
        width: "100%",
        height: "100%",
        boxSizing: "border-box"
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
        let categories = chartData.categories;
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
        data={plotData}
        config={plotConfig}
        layout={layout}
        style={getCommonStyle()}
        useResizeHandler
    />
}

export function PlotlyBarChart(props: ChartRendererProps) {
    const chartData = props.data;
    const plotData: any = [];

    const isHorizontal = (chartData.yAxis.placement === Placement.BOTTOM);

    chartData.series.forEach(s => {
        let categories = chartData.categories;
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
        data={plotData}
        config={plotConfig}
        layout={layout}
        style={getCommonStyle()}
        useResizeHandler
    />
}

function _internalPlotlyPieChart(props: ChartRendererProps, hole?: number) {
    const chartData = props.data;
    const plotData: any = [];

    const pieCount = chartData.series.length;
    const columns = Math.min(pieCount, 4);
    const rows = Math.ceil(pieCount / columns);

    chartData.series.forEach((s, i) => {
        let categories = chartData.categories;
        if (categories.length === 0) {
            s.yValues.forEach((v, i) => {
                categories.push(String(i));
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
        data={plotData}
        config={plotConfig}
        layout={layout}
        style={getCommonStyle()}
        useResizeHandler
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
        let categories = chartData.categories;
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
        data={plotData}
        config={plotConfig}
        layout={layout}
        style={getCommonStyle()}
        useResizeHandler
    />
}

export function PlotlyScatterChart(props: ChartRendererProps) {
    const chartData = props.data;
    const plotData: any = [];

    chartData.series.forEach(s => {
        let categories = chartData.categories;
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
        data={plotData}
        config={plotConfig}
        layout={layout}
        style={getCommonStyle()}
        useResizeHandler
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
        let categories = chartData.categories;
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
        data={plotData}
        config={plotConfig}
        layout={layout}
        style={getCommonStyle()}
        useResizeHandler
    />
}

export function PlotlyGaugeChart(props: ChartRendererProps) {
    // Enter a speed between 0 and 180
    const level = 175;

    // Trig to calc meter point
    const degrees = 180 - level,
        radius = .5;
    const radians = degrees * Math.PI / 180;
    const x = radius * Math.cos(radians);
    const y = radius * Math.sin(radians);

    // Path: may have to change to create a better triangle
    const mainPath = 'M -.0 -0.025 L .0 0.025 L ',
        pathX = String(x),
        space = ' ',
        pathY = String(y),
        pathEnd = ' Z';
    const path = mainPath.concat(pathX, space, pathY, pathEnd);

    const data = [{
        type: 'scatter',
        x: [0], y: [0],
        marker: { size: 28, color: '850000' },
        showlegend: false,
        name: 'speed',
        text: level,
        hoverinfo: 'text+name'
    },
    {
        values: [50 / 6, 50 / 6, 50 / 6, 50 / 6, 50 / 6, 50 / 6, 50],
        rotation: 90,
        text: ['TOO FAST!', 'Pretty Fast', 'Fast', 'Average',
            'Slow', 'Super Slow', ''],
        textinfo: 'text',
        textposition: 'inside',
        marker: {
            colors: ['rgba(14, 127, 0, .5)', 'rgba(110, 154, 22, .5)',
                'rgba(170, 202, 42, .5)', 'rgba(202, 209, 95, .5)',
                'rgba(210, 206, 145, .5)', 'rgba(232, 226, 202, .5)',
                'rgba(255, 255, 255, 0)']
        },
        labels: ['151-180', '121-150', '91-120', '61-90', '31-60', '0-30', ''],
        hoverinfo: 'label',
        hole: .5,
        type: 'pie',
        showlegend: false
    }];

    const layout = Object.assign({}, getCommonLayout(), {
        shapes: [{
            type: 'path',
            path: path,
            fillcolor: '850000',
            line: {
                color: '850000'
            }
        }],
        title: 'Gauge Speed 0-100',
        height: 1000,
        width: 1000,
        xaxis: {
            zeroline: false, showticklabels: false,
            showgrid: false, range: [-1, 1]
        },
        yaxis: {
            zeroline: false, showticklabels: false,
            showgrid: false, range: [-1, 1]
        }
    });

    return <Plot
        data={data}
        layout={layout}
        style={getCommonStyle()}
        useResizeHandler
    />
}

