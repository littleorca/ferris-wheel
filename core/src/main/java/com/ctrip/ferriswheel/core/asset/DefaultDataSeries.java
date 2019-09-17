package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.common.chart.DataSeries;

class DefaultDataSeries extends AssetNode implements DataSeries {
    private ValueNode name;
    private ValueNode xValues;
    private ValueNode yValues;

    DefaultDataSeries(AssetManager assetManager) {
        super(assetManager);
    }

    @Override
    protected EvaluationState doEvaluate(EvaluationContext context) {
        // leave the job to chart
        return EvaluationState.DONE;
    }

    public DefaultChart getChart() {
        return (DefaultChart) getParent();
    }

    void setChart(DefaultChart chartModel) {
        setParent(chartModel);
    }

    @Override
    public ValueNode getName() {
        return name;
    }

    void setName(ValueNode name) {
        if (this.name != null) {
            unbindChild(this.name);
        }
        this.name = name;
        if (this.name != null) {
            bindChild(this.name);
        }
    }

    @Override
    public ValueNode getxValues() {
        return xValues;
    }

    void setxValues(ValueNode xValues) {
        if (this.xValues != null) {
            unbindChild(this.xValues);
        }
        this.xValues = xValues;
        if (this.xValues != null) {
            bindChild(this.xValues);
        }
    }

    @Override
    public ValueNode getyValues() {
        return yValues;
    }

    void setyValues(ValueNode yValues) {
        if (this.yValues != null) {
            unbindChild(this.yValues);
        }
        this.yValues = yValues;
        if (this.yValues != null) {
            bindChild(this.yValues);
        }
    }
}
