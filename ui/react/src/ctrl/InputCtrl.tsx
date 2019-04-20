import * as React from "react";

interface InputCtrlProps<V> extends React.ClassAttributes<any> {
    className?: string;
    style?: React.CSSProperties;
    name?: string;
    value: V;
    afterChange: (value: V, name?: string) => void;
}

abstract class InputCtrl<V, P extends InputCtrlProps<V>, S = any> extends React.PureComponent<P, S> {
    constructor(props: P) {
        super(props);
        this.afterChange = this.afterChange.bind(this);
    }

    protected afterChange(newValue: V) {
        if (typeof this.props.afterChange === "function") {
            this.props.afterChange(newValue, this.props.name);
        }
    }
}

export default InputCtrl;
export { InputCtrlProps };
