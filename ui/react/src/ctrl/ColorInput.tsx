import * as React from 'react';
import Color, { floatToInt8, int8ToFloat } from '../model/Color';
import * as Popover from 'react-popover';
import { SketchPicker, ColorResult, RGBColor } from 'react-color';
import './ColorInput.css';

interface ColorInputProps extends React.ClassAttributes<ColorInput> {
    value: Color;
    className?: string;
    disabled?: boolean;
    popoverPlacement?: 'top' | 'right' | 'bottom' | 'left';
    afterChange?(color: Color): void;
}

interface ColorInputState {
    isOpen: boolean,
    pendingColor: Color,
}

class ColorInput extends React.Component<ColorInputProps, ColorInputState> {
    protected static defaultProps: Partial<ColorInputProps> = {
        value: new Color(),
    }

    constructor(props: ColorInputProps) {
        super(props);

        this.state = {
            isOpen: false,
            pendingColor: props.value
        };

        this.handleClick = this.handleClick.bind(this);
        this.handleClose = this.handleClose.bind(this);
        this.handleChange = this.handleChange.bind(this);
    }

    public componentDidUpdate(prevProps: ColorInputProps) {
        if (this.props.value !== prevProps.value) {
            this.setState({
                pendingColor: this.props.value,
            });
        }
    }

    protected handleClick() {
        if (!this.state.isOpen) {
            this.setState({
                isOpen: true
            });
        } else {
            this.handleClose();
        }
    }

    protected handleClose() {
        const value = this.props.value;
        const pending = this.state.pendingColor;
        value.red = pending.red;
        value.green = pending.green;
        value.blue = pending.blue;
        value.alpha = pending.alpha;
        this.setState({
            isOpen: false,
        });
        if (typeof this.props.afterChange === 'function') {
            this.props.afterChange(value);
        }
    }

    protected handleChange(color: ColorResult) {
        this.setState({
            pendingColor: {
                red: int8ToFloat(color.rgb.r),
                green: int8ToFloat(color.rgb.g),
                blue: int8ToFloat(color.rgb.b),
                alpha: (typeof color.rgb.a !== 'undefined' &&
                    color.rgb.a !== null) ? color.rgb.a : 1,
            }
        });
    }

    public render() {
        const color = this.state.pendingColor;
        const r = floatToInt8(color.red);
        const g = floatToInt8(color.green);
        const b = floatToInt8(color.blue);
        const a = color.alpha;
        const bgColor = `rgba(${r}, ${g}, ${b}, ${a})`;
        const fgColor = `rgba(${255 - r}, ${255 - g}, ${255 - b}, ${0.5 + a / 2.0})`;
        const sketchColor: RGBColor = { r, g, b, a };

        return (
            <Popover
                className="color-input-popover"
                isOpen={this.state.isOpen}
                preferPlace="above"
                onOuterAction={this.handleClose}
                body={<SketchPicker
                    color={sketchColor}
                    onChange={this.handleChange} />}>
                <span
                    className="color-input"
                    style={{
                        color: fgColor,
                        backgroundColor: bgColor,
                    }}
                    onClick={this.handleClick}>
                    <span>{bgColor}</span>
                </span>
            </Popover>
        );
    }
}

export default ColorInput;
export { ColorInputProps };
