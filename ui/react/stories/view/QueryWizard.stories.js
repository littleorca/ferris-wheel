import React, { Component } from 'react';
import { WorkbookEditor } from '../../src';
import Sheet from "../../src/model/Sheet"
import Workbook from "../../src/model/Workbook"
import Version from "../../src/model/Version"
import Values from "../../src/model/Values"
import EditResponse from "../../src/action/EditResponse"
import ChangeList from "../../src/action/ChangeList"


const sheet = new Sheet();
sheet.name = 'test_sheet';

const workbook = new Workbook(
    new Version(0, 0, 1),
    0,
    'test-workbook',
    [
        sheet,
    ],
);

const fakeService = {
    call: (request, okCallback, errorCallback) => {
        okCallback(new EditResponse(request.txId, 0, 'Ok', new ChangeList([request.action])));
    },

    isBusy: () => false
};

class QueryWizardStories extends Component {
    render() {
        return (
            <div style={{
                height: 500
            }}>
                <h3>WorkbookEditor</h3>
                <div style={{
                    border: "5px solid #000"
                }}>
                    <WorkbookEditor
                        workbook={workbook}
                        service={fakeService}
                        className="testWorkbookEditor"
                        extensions={[{
                            queryWizard: {
                                name: 'test-query-1',
                                title: '查询测试1',
                                accepts: (q) => {
                                    console.log("#Q1", q);
                                    return q.scheme === "provider-1"
                                },
                                component: (props) => <div>
                                    <h3>查询测试1</h3>
                                    <p>Demo 1</p>
                                    <button onClick={() => props.onOk({
                                        scheme: 'provider-1',
                                        builtinParams: [{
                                            name: 'var1',
                                            value: Values.formula('TODAY()')
                                        }]
                                    })}>Ok</button>
                                    <button onClick={() => props.onCancel()}>Cancel</button>
                                </div>
                            }
                        }, {
                            queryWizard: {
                                name: 'test-query-2',
                                title: '查询测试2',
                                accepts: (q) => {
                                    console.log('#Q2', q);
                                    return q.scheme === "provider-2"
                                },
                                component: (props) => <div>
                                    <h1>hello world</h1>
                                    <p>Demo 2</p>
                                    <button onClick={() => props.onOk({
                                        scheme: 'provider-2',
                                        builtinParams: [{
                                            name: 'p1',
                                            value: Values.str('hello world!')
                                        }, {
                                            name: 'type',
                                            value: Values.dec('3.14')
                                        }]
                                    })}>Ok</button>
                                    <button onClick={() => props.onCancel()}>Cancel</button>
                                </div>
                            }
                        }]} />
                </div>
            </div>
        );
    }
}

export default QueryWizardStories;