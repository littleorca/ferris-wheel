import React, { Component } from 'react';
import TableView from '../../src/view/TableView';
import { Table, Row, Cell, Values, VariantType, QueryAutomaton, ParamRule } from '../../src/model';
import { Button } from '../../src/ctrl';
import { RefreshCellValue, SetCellValue, SetCellFormula } from '../../src/action';

const table = new Table('test_table', []);
table.rows.push(new Row(0,
    [
        new Cell(0, Values.str('hello')),
        new Cell(1, Values.str('world')),
        new Cell(2, Values.str('~!')),
    ]));
table.rows.push(new Row(1,
    [
        new Cell(0, Values.dec(10)),
        new Cell(1, Values.dec(15)),
        new Cell(2, Values.withType(VariantType.DECIMAL, 25, "A2+B2")),
    ]));

const queryAutomaton = new QueryAutomaton();
queryAutomaton.template.userParamRules.push(new ParamRule('bool', VariantType.BOOL, false, []));
queryAutomaton.template.userParamRules.push(new ParamRule('decimal', VariantType.DECIMAL, false, []));
queryAutomaton.template.userParamRules.push(new ParamRule('string', VariantType.STRING, false, []));
queryAutomaton.template.userParamRules.push(new ParamRule('date', VariantType.DATE, false, []));
queryAutomaton.template.userParamRules.push(new ParamRule('list', VariantType.LIST, false, []));
queryAutomaton.template.userParamRules.push(new ParamRule('select str', VariantType.STRING, false, [
    Values.str('foo'),
    Values.str('bar')
]));
queryAutomaton.template.userParamRules.push(new ParamRule('select dec', VariantType.DECIMAL, false, [
    Values.dec(1024),
    Values.dec(4096)
]));

const tableWithQueryAutomaton = new Table(
    'test_table_with_query_automaton',
    [],
    { queryAutomaton });
tableWithQueryAutomaton.rows.push(new Row(0,
    [
        new Cell(0, Values.str('foo')),
        new Cell(1, Values.str('bar')),
    ]));
tableWithQueryAutomaton.rows.push(new Row(1,
    [
        new Cell(0, Values.dec(10)),
        new Cell(1, Values.dec(24)),
    ]));

class TableViewStories extends Component {
    render() {
        const listeners = new Set();
        const fakeHerald = {
            subscribe: (listener) => {
                listeners.add(listener);
            },
            unsubscribe: (listener) => {
                listeners.delete(listener);
            }
        };

        const setCellValue = () => {
            const setCellValue = new SetCellValue(
                '', 'test_table', 1, 0,
                Values.dec(Math.random()));
            listeners.forEach(handler => handler(setCellValue.wrapper()));
        };

        const setCellFormula = () => {
            const setCellFormula = new SetCellFormula(
                '', 'test_table', 1, 1,
                'A1+' + Math.random());
            listeners.forEach(handler => handler(setCellFormula.wrapper()));
        };

        const refreshCellValue = () => {
            const refreshCellValue = new RefreshCellValue(
                '', 'test_table', 1, 2,
                Values.dec(Math.random()));
            listeners.forEach(handler => handler(refreshCellValue.wrapper()));
        };

        return (
            <div>
                <h3>TableView</h3>
                <div>
                    <h4>Editable Table</h4>
                    <div>
                        <Button
                            name="SetCellValue (A2)"
                            onClick={setCellValue} />
                        <Button
                            name="SetCellFormula (B2)"
                            onClick={setCellFormula} />
                        <Button
                            name="RefreshCellValue (C2)"
                            onClick={refreshCellValue} />
                    </div>
                    <div>
                        <TableView
                            style={{
                                height: 150
                            }}
                            table={table}
                            editable={true}
                            herald={fakeHerald} />
                    </div>
                </div>
                <div>
                    <h4>With query automaton</h4>
                    <TableView
                        style={{
                            height: 200,
                        }}
                        table={tableWithQueryAutomaton}
                        editable={false} />
                </div>
            </div >
        );
    }
}

export default TableViewStories;