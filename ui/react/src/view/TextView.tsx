import * as React from 'react';
import * as ReactDOM from 'react-dom';
import Text from '../model/Text';
import EditableUnionValue from '../ctrl/EditableUnionValue';
import SharedViewProps from './SharedViewProps';
import UnionValue from '../model/UnionValue';
import UpdateText from '../action/UpdateText';
import Action from '../action/Action';
import RenameAsset from '../action/RenameAsset';
import GroupView, { GroupItem } from './GroupView';
import LayoutForm from '../form/LayoutForm';
import Layout from '../model/Layout';
import LayoutAsset from '../action/LayoutAsset';
import classnames from "classnames";
import './TextView.css';

interface TextViewProps extends SharedViewProps<TextView> {
    text: Text,
    className?: string;
}

class TextView extends React.Component<TextViewProps> {
    constructor(props: TextViewProps) {
        super(props);

        this.afterChange = this.afterChange.bind(this);
        this.handleLayoutChange = this.handleLayoutChange.bind(this);
        this.handleAction = this.handleAction.bind(this);
        this.applyAction = this.applyAction.bind(this);

        if (typeof this.props.herald !== 'undefined') {
            this.props.herald.subscribe(this.applyAction);
        }
    }

    public componentWillUnmount() {
        if (typeof this.props.herald !== 'undefined') {
            this.props.herald.unsubscribe(this.applyAction);
        }
    }

    protected afterChange(value: UnionValue) {
        if (typeof this.props.onAction === 'undefined') {
            return;
        }
        const updateText = new UpdateText('', new Text(this.props.text.name, value, this.props.text.layout));
        this.props.onAction(updateText.wrapper());
    }

    protected handleLayoutChange(layout: Layout) {
        const layoutAsset = new LayoutAsset("", this.props.text.name, layout);
        this.handleAction(layoutAsset.wrapper());
    };

    protected handleAction(action: Action) {
        if (typeof this.props.onAction === "function") {
            this.props.onAction(action);
        }
    }

    protected applyAction(action: Action) {
        if (!action.isAssetAction() ||
            action.targetAsset() !== this.props.text.name) {
            return;
        }
        if (typeof action.renameAsset !== 'undefined') {
            this.applyRenameAsset(action.renameAsset);
        } else if (typeof action.updateText !== 'undefined') {
            this.applyUpdateText(action.updateText);
        }
    }

    protected applyRenameAsset(renameAsset: RenameAsset) {
        this.props.text.name = renameAsset.newAssetName;
        this.forceUpdate();
    }

    protected applyUpdateText(updateText: UpdateText) {
        this.props.text.content = updateText.text.content;
        this.props.text.layout = updateText.text.layout;
        this.forceUpdate();
    }

    public render() {
        const text = this.props.text;

        const className = classnames(
            "text-view",
            { "editable": this.props.editable },
            this.props.className);

        return (
            <div className={className}>
                <EditableUnionValue
                    value={text.content}
                    disableCommitOnEnter={true}
                    readOnly={!this.props.editable}
                    afterChange={this.afterChange} />
                <>
                    {this.props.controlPortal &&
                        ReactDOM.createPortal(this.renderControl(), this.props.controlPortal)}
                </>
            </div>
        );
    }

    protected renderControl() {
        return (
            <GroupView
                className="realtime-edit text-option">
                <GroupItem
                    name="layout"
                    title="布局">
                    <form onSubmit={e => e.preventDefault()}>
                        <LayoutForm
                            layout={this.props.text.layout}
                            afterChange={this.handleLayoutChange} />
                    </form>
                </GroupItem>
            </GroupView>
        );
    }

}

export default TextView;
export { TextViewProps };
