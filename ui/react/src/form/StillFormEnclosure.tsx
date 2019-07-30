import * as React from "react";
import Button from "../ctrl/Button";
import * as classnames from "classnames";
import "./StillFormEnclosure.css"

interface StillFormEnclosureProps extends React.ClassAttributes<StillFormEnclosure> {
    id?: string;
    name?: string;
    className?: string;
    style?: React.CSSProperties;
    noSubmitButton?: boolean;
    noResetButton?: boolean;
    submitLabel?: string;
    resetLabel?: string;
    onSubmit?: () => void;
    onReset?: () => void;
    onDestroy?: () => void;
}

class StillFormEnclosure extends React.Component<StillFormEnclosureProps> {

    constructor(props: StillFormEnclosureProps) {
        super(props);

        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleReset = this.handleReset.bind(this);
    }

    public componentWillUnmount() {
        if (typeof this.props.onDestroy === "function") {
            this.props.onDestroy();
        }
    }

    protected handleSubmit(e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault();
        if (typeof this.props.onSubmit === "function") {
            this.props.onSubmit();
        }
    }

    protected handleReset(e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault();
        if (typeof this.props.onReset === "function") {
            this.props.onReset();
        }
    }

    public render() {
        const className = classnames(this.props.className, "still-form-enclosure");
        const submitLabel = typeof this.props.submitLabel === "string" ?
            this.props.submitLabel : "Submit";
        const resetLabel = typeof this.props.resetLabel === "string" ?
            this.props.resetLabel : "Reset";

        return (
            <form
                id={this.props.id}
                name={this.props.name}
                className={className}
                style={this.props.style}
                onSubmit={this.handleSubmit}
                onReset={this.handleReset}>
                <div className="still-form-content">
                    {this.props.children}
                </div>
                <div className="still-form-action">
                    {this.props.noSubmitButton || (
                        <Button
                            name="form-submit-button"
                            type="submit"
                            label={submitLabel} />
                    )}
                    {this.props.noResetButton || (
                        <Button
                            name="form-reset-button"
                            type="reset"
                            label={resetLabel} />
                    )}
                </div>
            </form>
        );
    }
}

export default StillFormEnclosure;
