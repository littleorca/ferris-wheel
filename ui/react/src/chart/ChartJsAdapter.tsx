import * as React from "react";
import { Axis, Interval, Placement, Stacking } from "../model";
import { ChartRendererProps, ChartData } from "../view/ChartView";
import * as ChartJS from 'chart.js';
import {
    Line, HorizontalBar, Bar, Pie, Radar, Scatter, Bubble, Doughnut
} from "react-chartjs-2";
import { floatToInt8 } from "../model/Color";

/* global settings */

ChartJS.defaults.global.maintainAspectRatio = false;
if (typeof ChartJS.defaults.global.legend === 'undefined') {
    ChartJS.defaults.global.legend = {};
}
if (typeof ChartJS.defaults.global.legend.labels == 'undefined') {
    ChartJS.defaults.global.legend.labels = {};
}
ChartJS.defaults.global.legend.labels.boxWidth = 12;

ChartJS.defaults.line.scales.xAxes[0].gridLines = false;
ChartJS.defaults.line.spanGaps = true;

ChartJS.defaults.bar.scales.xAxes[0].gridLines = false;

interface RgbaColor {
    red: number,
    green: number,
    blue: number,
    alpha: number,
}

const RgbaColors: RgbaColor[] = Object.freeze([
    { red: 0x7c, green: 0xb5, blue: 0xec, alpha: 1 },
    { red: 0x43, green: 0x43, blue: 0x48, alpha: 1 },
    { red: 0x90, green: 0xed, blue: 0x7d, alpha: 1 },
    { red: 0xf7, green: 0xa3, blue: 0x5c, alpha: 1 },
    { red: 0x80, green: 0x85, blue: 0xe9, alpha: 1 },
    { red: 0xf1, green: 0x5c, blue: 0x80, alpha: 1 },
    { red: 0xe4, green: 0xd3, blue: 0x54, alpha: 1 },
    { red: 0x2b, green: 0x90, blue: 0x8f, alpha: 1 },
    { red: 0xf4, green: 0x5b, blue: 0x5b, alpha: 1 },
    { red: 0x91, green: 0xe8, blue: 0xe1, alpha: 1 }
]) as RgbaColor[];

let colorIndex = 0;

function resetColorIndex() {
    colorIndex = 0;
}

function getColors(amount: number, alpha: number = 1): RgbaColor[] {
    const colors = [];
    for (let i = 0; i < amount; i++) {
        colors.push(RgbaColors[colorIndex]);
        colorIndex = (colorIndex + 1) % RgbaColors.length;
    }
    return colors;
}

function toCssColors(rgbaColors: RgbaColor[], newAlpha?: number) {
    const cssColors = [];
    for (const rgba of rgbaColors) {
        const alpha = typeof newAlpha !== 'undefined' ? newAlpha : rgba.alpha;
        cssColors.push(`rgba(${rgba.red}, ${rgba.green}, ${rgba.blue}, ${alpha})`);
    }
    return cssColors;
}

function getInterval(data: number[]): Interval {
    if (data.length === 0) {
        throw new Error('Data is empty.');
    }
    let from = data[0];
    let to = data[0];
    for (const n of data) {
        from = Math.min(from, n);
        to = Math.max(to, n);
    }
    return { from, to };
}

function mergeInterval(intvl1: Interval, intvl2: Interval): Interval {
    return {
        from: Math.min(intvl1.from, intvl2.from),
        to: Math.max(intvl1.to, intvl2.to),
    };
}

let sequence: number = 0;

function datasetKeyProvider(e: any) {
    if (typeof e.label === 'string' && e.label !== '') {
        return e.label;
    }
    return '$' + (sequence++);
}

function getOrCreateAxesOpt(options: any, axis: 'x' | 'y' | 'z') {
    if (typeof options.scales === 'undefined') {
        options.scales = {};
    }
    const key = axis + 'Axes';
    if (typeof options.scales[key] === 'undefined') {
        options.scales[key] = [];
    }
    if (options.scales[key].length === 0) {
        options.scales[key].push({});
    }
    return options.scales[key][0];
}

function fillAxisTitle(axisOpt: any, title: string) {
    if (typeof axisOpt.scaleLabel === 'undefined') {
        axisOpt.scaleLabel = {};
    }
    Object.assign(axisOpt.scaleLabel, {
        display: true,
        labelString: title,
    });
}

function fillAxisOptions(chartjsAxisOpt: any, axis: Axis) {
    if (axis.title !== '') {
        fillAxisTitle(chartjsAxisOpt, axis.title);
    }
    switch (axis.stacking) {
        case Stacking.UNSET:
            break;
        case Stacking.ABSOLUTE:
            chartjsAxisOpt.stacked = true;
            break;
        case Stacking.PERCENT:
            // WARN: not supported.
            break;
    }
}

function fillAxesOptions(options: any, chart: ChartData) {
    const xAxis = getOrCreateAxesOpt(options, 'x');
    fillAxisOptions(xAxis, chart.xAxis);

    const yAxis = getOrCreateAxesOpt(options, 'y');
    fillAxisOptions(yAxis, chart.yAxis);

    const zAxis = getOrCreateAxesOpt(options, 'z');
    fillAxisOptions(zAxis, chart.zAxis);
}

function fixCategories(data: any) {
    let maxPoints = 0;
    for (const ds of data.datasets) {
        if (ds.data.length > maxPoints) {
            maxPoints = ds.data.length;
        }
    }
    for (let i = 0; i < maxPoints; i++) {
        data.labels[i] = i + '';
    }
}

function LineRenderer(props: ChartRendererProps) {
    const convertedData = {
        labels: props.data.categories,
        datasets: [],
    };

    resetColorIndex();

    const fill = props.data.yAxis.stacking === Stacking.ABSOLUTE ?
        'origin' : false;

    for (const ser of props.data.series) {
        const colors = getColors(1);
        const [color] = toCssColors(colors);
        const [bgColor] = toCssColors(colors, 0.25);
        const ds = {
            label: ser.name,
            data: ser.yValues,
            backgroundColor: bgColor,
            borderColor: color,
            fill: fill,
        };
        convertedData.datasets.push(ds as never);
    }

    if (convertedData.labels.length === 0) {
        fixCategories(convertedData);
    }

    const options = {};

    fillAxesOptions(options, props.data);

    return (
        <Line
            datasetKeyProvider={datasetKeyProvider}
            data={convertedData}
            options={options} />
    );
}

function BarRenderer(props: ChartRendererProps) {
    const convertedData = {
        labels: props.data.categories,
        datasets: [],
    };

    resetColorIndex();

    let range: Interval = { from: NaN, to: NaN };
    for (const ser of props.data.series) {
        const colors = getColors(1);
        const [color] = toCssColors(colors);
        // const [bgColor] = toCssColors(colors, .3);
        const ds = {
            label: ser.name,
            data: ser.yValues,
            backgroundColor: color,
            borderColor: color,
        };
        convertedData.datasets.push(ds as never);
        const intvl = getInterval(ser.yValues);
        if (isNaN(range.from) || isNaN(range.to)) {
            range = intvl;
        } else {
            range = mergeInterval(range, intvl);
        }
    }

    const rangeDelta = range.to - range.from;
    range.from = range.from - rangeDelta * 0.05;
    if (rangeDelta > 5) {
        range.from = Math.floor(range.from);
    }

    if (convertedData.labels.length === 0) {
        fixCategories(convertedData);
    }

    const options = {
        scales: {
            xAxes: [{}],
            yAxes: [{
                ticks: {
                    min: range.from,
                    // max: range.to,
                }
            }]
        }
    };

    fillAxesOptions(options, props.data);

    if (props.data.yAxis.stacking === Stacking.ABSOLUTE) {
        Object.assign(options.scales.xAxes[0], { stacked: true });
    }

    const isHorizontal = props.data.yAxis.placement === Placement.TOP ||
        props.data.yAxis.placement === Placement.BOTTOM;

    if (isHorizontal) {
        return (
            <HorizontalBar
                datasetKeyProvider={datasetKeyProvider}
                data={convertedData}
                options={options} />
        )
    } else {
        return (
            <Bar
                datasetKeyProvider={datasetKeyProvider}
                data={convertedData}
                options={options} />
        )
    }
}

function PieRenderer(props: ChartRendererProps) {
    return CommonPieRenderer(props);
}

function DoughnutRenderer(props: ChartRendererProps) {
    return CommonPieRenderer(props, true);
}

function CommonPieRenderer(props: ChartRendererProps, isDoughnut: boolean = false) {
    const convertedData = {
        labels: props.data.categories,
        datasets: [],
    };

    resetColorIndex();

    for (const ser of props.data.series) {
        const colors = toCssColors(getColors(ser.yValues.length));
        const ds = {
            label: ser.name,
            data: ser.yValues,
            backgroundColor: colors,
        };
        convertedData.datasets.push(ds as never);
    }

    if (convertedData.labels.length === 0) {
        fixCategories(convertedData);
    }

    const options = {};

    // fillAxesOptions(options, props.data);

    if (isDoughnut) {
        return (
            <Doughnut
                datasetKeyProvider={datasetKeyProvider}
                data={convertedData}
                options={options} />
        );

    } else {
        return (
            <Pie
                datasetKeyProvider={datasetKeyProvider}
                data={convertedData}
                options={options} />
        );
    }
}

function RadarRenderer(props: ChartRendererProps) {
    const convertedData = {
        labels: props.data.categories,
        datasets: [],
    };

    resetColorIndex();

    let range: Interval = { from: NaN, to: NaN };
    for (const ser of props.data.series) {
        const colors = getColors(1);
        const [backgroundColor] = toCssColors(colors, 0.2);
        const [borderColor] = toCssColors(colors);
        const ds = {
            label: ser.name,
            data: ser.yValues,
            backgroundColor,
            borderColor,
        };
        convertedData.datasets.push(ds as never);

        const intvl = getInterval(ser.yValues);
        if (isNaN(range.from) || isNaN(range.to)) {
            range = intvl;
        } else {
            range = mergeInterval(range, intvl);
        }
    }

    const rangeDelta = range.to - range.from;
    range.from = range.from - rangeDelta * 0.5;
    if (rangeDelta > 5) {
        range.from = Math.floor(range.from);
    }

    if (convertedData.labels.length === 0) {
        fixCategories(convertedData);
    }

    const options: any = {
        scale: {
            ticks: {
                // beginAtZero: true,
                min: range.from,
                // max: range.to,
            }
        }
    };

    // fillAxesOptions(options, props.data);

    return (
        <Radar
            datasetKeyProvider={datasetKeyProvider}
            data={convertedData}
            options={options} />
    );
}

function ScatterRenderer(props: ChartRendererProps) {
    const convertedData = {
        labels: props.data.categories,
        datasets: [],
    };

    resetColorIndex();

    for (const ser of props.data.series) {
        const len = Math.min(ser.xValues.length, ser.yValues.length);
        const dots = [];
        for (let i = 0; i < len; i++) {
            dots.push({
                x: ser.xValues[i],
                y: ser.yValues[i],
            });
        }
        const colors = getColors(1);
        const [backgroundColor] = toCssColors(colors, 0.2);
        const [borderColor] = toCssColors(colors);
        const ds = {
            label: ser.name,
            data: dots,
            backgroundColor,
            borderColor,
        };
        convertedData.datasets.push(ds as never);
    }

    if (convertedData.labels.length === 0) {
        fixCategories(convertedData);
    }

    const options = {};

    fillAxesOptions(options, props.data);

    return (
        <Scatter
            datasetKeyProvider={datasetKeyProvider}
            data={convertedData}
            options={options} />
    );
}

function BubbleRenderer(props: ChartRendererProps) {
    const convertedData = {
        labels: props.data.categories,
        datasets: [],
    };

    resetColorIndex();
    let range = { from: NaN, to: NaN };

    for (const ser of props.data.series) {
        const len = Math.min(ser.xValues.length, ser.yValues.length);
        const dots = [];
        for (let i = 0; i < len; i++) {
            dots.push({
                x: ser.xValues[i],
                y: ser.yValues[i],
                z: ser.zValues[i],
            });
        }
        const colors = getColors(1);
        const [backgroundColor] = toCssColors(colors, 0.2);
        const [borderColor] = toCssColors(colors);
        const ds = {
            label: ser.name,
            data: dots,
            backgroundColor,
            borderColor,
        };
        convertedData.datasets.push(ds as never);

        const intvl = getInterval(ser.zValues);
        if (isNaN(range.from) || isNaN(range.to)) {
            range = intvl;
        } else {
            range = mergeInterval(range, intvl);
        }
    }

    const rangeDelta = range.to - range.from;

    const radius = (context: any) => {
        const value = context.dataset.data[context.dataIndex];
        const size = context.chart.width;
        return 3 + (value.z - range.from) / rangeDelta * size / 40;
    }

    const label = (tooltipItem: any, data: any) => {
        const dataset = data.datasets[tooltipItem.datasetIndex];
        const value = dataset.data[tooltipItem.index];
        return dataset.label +
            ": (" + value.x + ", " + value.y + ", " + value.z + ")";
    }

    if (convertedData.labels.length === 0) {
        fixCategories(convertedData);
    }

    const options: any = {
        elements: {
            point: { radius },
        },
        tooltips: {
            callbacks: { label }
        }
    };

    fillAxesOptions(options, props.data);

    return (
        <Bubble
            datasetKeyProvider={datasetKeyProvider}
            data={convertedData}
            options={options} />
    );
}

function GaugeRenderer(props: ChartRendererProps) {
    resetColorIndex();
    if (props.data.series.length === 0 ||
        props.data.series[0].yValues.length === 0) {
        return <div className="alert">Invalid data!</div>;
    }

    const ser = props.data.series[0];
    const val = ser.yValues[0];

    const convertedData: any = {
        labels: props.data.categories,
        datasets: [{
            label: '',
            data: [],
            backgroundColor: [],
        }],
    };

    const bgSet = convertedData.datasets[0];
    let from = NaN;
    let to = NaN;
    for (const band of props.data.yAxis.bands) {
        bgSet.data.push(band.interval.to - band.interval.from);
        const r = floatToInt8(band.color.red);
        const g = floatToInt8(band.color.green);
        const b = floatToInt8(band.color.blue);
        const a = band.color.alpha;
        bgSet.backgroundColor.push(`rgba(${r}, ${g}, ${b}, ${a})`);
        if (isNaN(from)) {
            from = band.interval.from;
        }
        to = band.interval.to;
    }

    const startAngle = .75 * Math.PI;
    const sweepAngle = 1.5 * Math.PI;

    const drawNeedle = (chart: any) => {
        const canvas = chart.chart.canvas;
        const ctx = canvas.getContext('2d');
        const cw = canvas.offsetWidth;
        const ch = canvas.offsetHeight;
        let radius;
        if (cw < ch * 1.17157287525381) {
            radius = cw / 2;
        } else {
            radius = ch / 1.7071067811865475;
        }
        const cx = cw / 2;
        const cy = radius;

        const radianAngle = startAngle + val / (to - from) * sweepAngle;

        ctx.save();
        ctx.translate(cx, cy);
        ctx.rotate(radianAngle);
        ctx.beginPath();
        ctx.moveTo(0, -radius / 20);
        ctx.lineTo(cy * .8, 0);
        ctx.lineTo(0, radius / 20);
        ctx.fillStyle = 'rgba(76, 76, 76, 0.8)';
        ctx.fill();
        ctx.rotate(-radianAngle);
        ctx.translate(-cx, -cy);
        ctx.beginPath();
        ctx.fillStyle = 'rgba(204, 204, 204, 1)';
        ctx.arc(cx, cy, radius / 6, 0, Math.PI * 2);
        ctx.fill();
        ctx.font = Math.floor(radius / 4) + "px georgia";
        ctx.fillStyle = "black";
        ctx.textAlign = "center";
        ctx.shadowColor = "white";
        ctx.shadowOffsetX = 0;
        ctx.shadowOffsetY = 0;
        ctx.shadowBlur = Math.floor(radius / 20);
        ctx.fillText(val.toString(), cx, cy + radius / 1.75);
        ctx.restore();
    };

    if (convertedData.labels.length === 0) {
        fixCategories(convertedData);
    }

    const options: any = {
        cutoutPercentage: 80,
        rotation: startAngle,
        circumference: sweepAngle,
        legend: {
            display: false,
        },
        tooltips: {
            enabled: false,
        },
        hover: {
            mode: null
        },
        animation: {
            animateRotate: false,
            animateScale: false,
            onProgress: drawNeedle,
            onComplete: drawNeedle,
        }
    };

    // fillAxesOptions(options, props.data);

    return (
        <Doughnut
            datasetKeyProvider={datasetKeyProvider}
            data={convertedData}
            options={options} />
    );
}

export {
    LineRenderer,
    BarRenderer,
    PieRenderer,
    DoughnutRenderer,
    RadarRenderer,
    ScatterRenderer,
    BubbleRenderer,
    GaugeRenderer
};
