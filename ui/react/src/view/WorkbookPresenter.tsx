import * as React from 'react';
import { Workbook } from '../model';
import {
    Service, ActionHandler, Action, ActionHerald, EditRequest, EditResponse
} from '../action';
import WorkbookView from './WorkbookView';

interface WorkbookPresenterProps extends React.ClassAttributes<WorkbookPresenter> {
    workbook: Workbook;
    service: Service;
    className?: string;
}

class WorkbookPresenter extends React.Component<WorkbookPresenterProps> implements ActionHerald {
    protected listeners: Set<ActionHandler> = new Set();

    constructor(props: WorkbookPresenterProps) {
        super(props);

        this.handleAction = this.handleAction.bind(this);
        this.onServiceOk = this.onServiceOk.bind(this);
        this.onServiceError = this.onServiceError.bind(this);
    }

    public subscribe(handler: ActionHandler) {
        this.listeners.add(handler);
    }

    public unsubscribe(handler: ActionHandler) {
        this.listeners.delete(handler);
    }

    protected handleAction(action: Action) {
        // console.log('handleAction', action);
        // ignore local actions such as asset been selected.
        if (action.isLocalAction()) {
            return;
        }
        // Workbook presenter only deal with execute query action.
        // And there supposed to be no other remote actions from WorkbookView.
        if (typeof action.executeQuery === 'undefined') {
            // throw new Error('Illegal action.');
            return;
        }

        const request = new EditRequest(0, action);
        this.setState({
            txId: request.txId,
            message: `服务请求中，txId=${request.txId}…`,
            serviceStatus: "忙碌…"
        });
        this.props.service.call(
            request,
            this.onServiceOk,
            this.onServiceError,
        );
    }

    protected onServiceOk(resp: EditResponse) {
        // make sure resp is an EditResponse instance.
        resp = EditResponse.deserialize(resp);
        if (resp.statusCode === 0) {
            if (typeof resp.changes !== 'undefined') {
                this.applyChanges(resp.changes.actions);
            }

        } else {
            // TODO
        }
    }

    protected applyChanges(actions: Action[]) {
        // console.log('applyChanges', actions);
        for (const action of actions) {
            this.listeners.forEach(handler => handler(action));
        }
    }

    protected onServiceError() {
        // TODO
    }

    render() {
        return (
            <WorkbookView
                workbook={this.props.workbook}
                editable={false}
                className={this.props.className}
                onAction={this.handleAction}
                herald={this} />
        );
    }
}

export default WorkbookPresenter;