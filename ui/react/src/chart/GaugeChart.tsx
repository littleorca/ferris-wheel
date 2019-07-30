import * as React from "react";
import { ChartRendererProps } from "./ChartRenderer";
import Formatter from "../util/Formatter";
import Values from "../model/Values";
import Color, { floatToInt8 } from "../model/Color";
import numbro from "numbro";

function GaugeChart(props: ChartRendererProps) {
    if (props.data.series.length < 1 ||
        props.data.series[0].yValues.length < 1) {
        return null;
    }
    const series = props.data.series[0];
    const value = series.yValues[0];
    const formattedVal = _format(value, props.data.yAxis.format);
    let min = Number.MAX_VALUE;
    let max = Number.MIN_VALUE;
    let color = null;
    let minColor = null;
    let maxColor = null;
    props.data.yAxis.bands.forEach(band => {
        if (band.interval.from < min) {
            min = band.interval.from;
            minColor = band.color;
        }
        if (band.interval.to > max) {
            max = band.interval.to;
            maxColor = band.color;
        }
        if (value >= band.interval.from && value <= band.interval.to) {
            color = band.color;
        }
    });
    let arcLen = 212 * value / (max - min);
    if (arcLen < 0) {
        arcLen = 0;
        if (color == null) {
            color = minColor;
        }
    }
    if (arcLen > 212) {
        arcLen = 212;
        if (color == null) {
            color = maxColor;
        }
    }
    if (color == null) {
        color = new Color(16, 142, 233);
    }
    const red = floatToInt8(color.red);
    const green = floatToInt8(color.green);
    const blue = floatToInt8(color.blue);
    const alpha = floatToInt8(color.alpha);
    const formattedMin = _format(min, props.data.yAxis.format);
    const formattedMax = _format(max, props.data.yAxis.format);

    const axisTitle = props.data.yAxis.title;

    return (
        <svg
            className={props.className}
            viewBox="-50 -50 100 105"
            style={props.style}>
            <path
                d="M -45,0 A 45,45 0 1 1 0,45"
                strokeLinecap="round"
                stroke="#eee"
                strokeWidth="9"
                fillOpacity="0"
                transform="rotate(-45,0,0)"
            />
            <path
                d="M -45,0 A 45,45 0 1 1 0,45"
                strokeLinecap="round"
                stroke={`rgba(${red}, ${green}, ${blue}, ${alpha})`}
                strokeWidth="9"
                fillOpacity="0"
                transform="rotate(-45,0,0)"
                style={{
                    strokeDasharray: `${arcLen}, 213`,
                    strokeDashoffset: "0",
                    transition: "stroke-dashoffset 0.3s ease 0s, stroke 0.3s ease"
                }}
            />
            <text
                x="0"
                y="0"
                dominantBaseline="middle"
                textAnchor="middle"
                fontSize="18"
                fill="#333">
                {formattedVal}
            </text>
            <text
                x="-30"
                y="42"
                dominantBaseline="middle"
                textAnchor="middle"
                fontSize="9"
                fill={value >= min ? "#666" : "#e00"}>
                {formattedMin}
            </text>
            <text
                x="30"
                y="42"
                dominantBaseline="middle"
                textAnchor="middle"
                fontSize="9"
                fill={value <= max ? "#666" : "#e00"}>
                {formattedMax}
            </text>
            <text
                x="0"
                y="18"
                dominantBaseline="middle"
                textAnchor="middle"
                fontSize="9"
                fill="#666">
                {axisTitle}
            </text>
        </svg>
    );
}

function _format(value: number, format: string) {
    return format === "" ?
        numbro(value).format({ average: true, totalLength: 3 }) :
        Formatter.format(Values.auto(value), format);
}

export default GaugeChart;