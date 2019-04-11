import * as React from "react";
import Axis from "../model/Axis";

interface ChartSeriesData {
    name: string,
    xValues: number[],
    yValues: number[],
    zValues: number[],
}

interface ChartData {
    type: string,
    name: string,
    title: string,
    categories: (string | Date)[];
    series: ChartSeriesData[];
    xAxis: Axis;
    yAxis: Axis;
    zAxis: Axis;
}

interface ChartRendererProps extends React.ClassAttributes<any> {
    data: ChartData;
    className?: string;
}

type ChartRenderer = React.SFC<ChartRendererProps>;

export default ChartRenderer;
export { ChartSeriesData, ChartData, ChartRendererProps }