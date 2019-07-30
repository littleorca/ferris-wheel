import * as React from 'react';
import classnames from 'classnames';
import './Button.css';

interface ButtonProps extends React.ClassAttributes<any> {
    name: string;
    type?: "button" | "submit" | "reset";
    onClick?: (name: string, event: React.MouseEvent<HTMLButtonElement>) => void;
    label?: string;
    tips?: string;
    className?: string;
    disabled?: boolean;
    tabIndex?: number;
    style?: React.CSSProperties;
    href?: string;
}

function Button(props: ButtonProps) {
    const label = typeof props.label !== 'undefined' ? props.label : props.name;
    const tips = typeof props.tips !== 'undefined' ? props.tips : label;

    const className = classnames("button", props.className);

    const handleClick = (event: React.MouseEvent<HTMLButtonElement>) => {
        const target = event.currentTarget;
        const name = target.name;
        if (typeof props.onClick !== 'undefined') {
            props.onClick(name, event);
        }
    };

    if (typeof props.href === 'string' && props.href !== '') {
        return (
            <a
                title={tips}
                className={className}
                tabIndex={props.tabIndex}
                style={props.style}
                href={props.href}>
                <span>{label}</span>
            </a>
        );

    } else {
        return (
            <button
                type={props.type || "button"}
                name={props.name}
                title={tips}
                onClick={handleClick}
                className={className}
                disabled={props.disabled}
                tabIndex={props.tabIndex}
                style={props.style}>
                <span>{label}</span>
            </button>
        );
    }
}

export default Button;
export { ButtonProps };
