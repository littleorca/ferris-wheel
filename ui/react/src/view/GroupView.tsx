import * as React from 'react';
import './GroupView.css';

interface GroupItemProps extends React.ClassAttributes<any> {
    name: string,
    title: string,
    children?: React.ReactNode,
}

const GroupItem = (props: GroupItemProps) => {
    return null;
};

type GroupItemNode = React.ReactElement<GroupItemProps> | undefined | false;

interface GroupViewProps extends React.ClassAttributes<GroupView> {
    children: GroupItemNode | GroupItemNode[],
    className?: string,
    mode?: 'tab' | 'fold';
}

interface GroupViewState {
    displayStates: Map<string, boolean>;
}

class GroupView extends React.Component<GroupViewProps, GroupViewState> {
    constructor(props: GroupViewProps) {
        super(props);
        this.state = {
            displayStates: new Map(),
        }
    }

    public render() {
        const mode = typeof this.props.mode !== 'undefined' ?
            this.props.mode : "fold";
        const className = "group-view " + mode +
            (typeof this.props.className !== 'undefined' ?
                " " + this.props.className : "");

        const displayStates = this.state.displayStates;

        return (
            <div className={className}>
                {React.Children.map(this.props.children, child => {
                    if (child === null) {
                        return;
                    }

                    const item = child as React.ReactElement<GroupItemProps>;

                    const isShow = displayStates.get(item.props.name) !== false;
                    const itemClassName = "group-view-item" +
                        (isShow ? " active" : " inactive");
                    const titleClassName = "group-view-title";
                    const contentClassName = "group-view-content" +
                        (isShow ? " show" : " hide");

                    const handleClick = (event: React.MouseEvent) => {
                        displayStates.set(item.props.name, !isShow);
                        this.setState({ displayStates });
                    };

                    return (
                        <div
                            key={item.props.name}
                            className={itemClassName}>
                            <div
                                className={titleClassName}
                                onClick={handleClick}>
                                <span>{item.props.title}</span>
                            </div>
                            <div
                                className={contentClassName}>
                                {item.props.children}
                            </div>
                        </div>
                    );
                })}
            </div>
        );
    }
}

export default GroupView;
export { GroupItemProps, GroupItem, GroupViewProps };
