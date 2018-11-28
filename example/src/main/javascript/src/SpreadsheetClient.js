import { Values } from '@littleorca/ferris-wheel';

export default class SpreadsheetClient {

    /**
     * Protobuf client via websocket.
     * @param {string} wsUri 
     */
    constructor(wsUri) {

        this.onConnected = this.onConnected.bind(this);
        this.onDisconnected = this.onDisconnected.bind(this);
        this.beforeRequest = this.beforeRequest.bind(this);
        this.afterResponse = this.afterResponse.bind(this);
        this.onError = this.onError.bind(this);

        this.listener = {
            onConnected: this.onConnected,
            onDisconnected: this.onDisconnected,
            beforeRequest: this.beforeRequest,
            afterResponse: this.afterResponse,
            onError: this.onError,
        };

        this.wsClient = this.createWebSocketClient(wsUri, this.listener);
        this.registeredListeners = [];
        this.registeredChangeListeners = {};
    }

    /**
     * Implements Service interface.
     *
     * type OkCallback = (resp: EditResponse) => void;
     * type ErrorCallback = () => void;
     *
     * interface Service {
     * call: (
     *    request: EditRequest,
     *    onOk: OkCallback,
     *    onError: ErrorCallback
     * ) => void;
     * }
     */
    call(request, onOk, onError) {
        this.wsClient.sendRequest(request, (response) => {
            onOk(response);
        });
    }

    isBusy() {
        return !this.isReady;
    }

    isReady() {
        return this.wsClient.isReady();
    }

    static errorName(errorCode) {
        const errors = [
            "#OK!",
            "#UNKNOWN!",
            "#REF!",
            "#VALUE!",
            "#DIV/0!"
        ];
        return errors[errorCode];
    }

    static orientationName(orientationOrdinal) {
        const o = [
            'UNSET',
            'HORIZONTAL',
            'VERTICAL'
        ];
        return o[orientationOrdinal];
    }

    static placementName(placementOrdinal) {
        const p = [
            'UNSET',
            'LEFT',
            'TOP',
            'RIGHT',
            'BOTTOM',
            'CENTER',
        ];
        return p[placementOrdinal];
    }

    /**
     * Listener
     */

    addListener(listener) {
        this.registeredListeners.push(listener);
    }

    removeListener(listener) {
        for (let i = 0; i < this.registeredListeners.length; i++) {
            if (this.registeredListeners[i] === listener) {
                this.registeredListeners.splice(i, 1);
                break;
            }
        }
    }

    clearListeners() {
        this.registeredListeners = [];
    }

    addChangeListener(actionName, changeListener) {
        if (!this.registeredChangeListeners.hasOwnProperty(actionName)) {
            this.registeredChangeListeners[actionName] = [];
        }
        this.registeredChangeListeners[actionName].push(changeListener);
    }

    removeChangeListener(actionName, changeListener) {
        if (!this.registeredChangeListeners.hasOwnProperty(actionName)) {
            return;
        }
        const changeListeners = this.registeredChangeListeners[actionName];
        for (let i = 0; i < changeListeners.length; i++) {
            if (changeListeners[i] === changeListener) {
                changeListeners.splice(i, 1);
                break;
            }
        }
    }

    clearChangeListeners() {
        this.registeredChangeListeners = {};
    }

    onConnected() {
        console.log('listener: onConnected');
        this.registeredListeners.forEach(l => l.onConnected());
    }

    onDisconnected() {
        console.log('listener: onDisconnected');
        this.registeredListeners.forEach(l => l.onDisconnected());
    }

    beforeRequest(txId) {
        // console.log('listener: beforeRequest', txId);
        this.registeredListeners.forEach(l => l.beforeRequest(txId));
    }

    afterResponse(resp) {
        // console.log('listener: afterResponse', resp);
        this.registeredListeners.forEach(l => l.afterResponse(resp));
        // distribute changes if any
        if (resp.hasOwnProperty('changes')) {
            for (let i = 0; i < resp.changes.actions.length; i++) {
                const act = resp.changes.actions[i];
                for (let k in act) {
                    if (!act.hasOwnProperty(k)) {
                        continue;
                    }
                    const listeners = this.registeredChangeListeners[k];
                    if (typeof listeners !== 'undefined' && Array.isArray(listeners)) {
                        for (let j = 0; j < listeners.length; j++) {
                            listeners[j](act[k]);
                        }
                    }
                }
            }
        }
    }

    onError(msg) {
        console.log('listener: onError', msg);
        this.registeredListeners.forEach(l => { l.onError(msg) });
    }

    /**
     * 
     * @param {string} sheetName 
     * @param {*} chart
     * chart: {
     *   name: string,
     *   type: string,
     *   title: UnionValue,
     *   categories: UnionValue,
     *   series:[{
     *     name: UnionValue,
     *     xValues: UnionValue,
     *     yValues: UnionValue,
     *   }],
     *   xAxis: {},
     *   yAxis: {},
     *   zAxis: {}
     * }
     */
    addChart(sheetName, chartSettings, callback) {
        // var c = this.createChartBySettings(chartSettings);
        this.wsClient.send({
            addChart: {
                sheetName: sheetName,
                chart: chartSettings,
            }
        }, callback);
    }

    addSheet(sheetName, callback) {
        this.wsClient.send({
            addSheet: {
                sheetName: sheetName,
            }
        }, callback);
    }

    addTable(sheetName, tableName, callback) {
        this.wsClient.send({
            addTable: {
                sheetName: sheetName,
                table: {
                    name: tableName,
                }
            }
        }, callback);
    }

    addText(sheetName, textName, textFormula, callback) {
        var text = {
            name: textName,
        }
        if (textFormula) {
            text.content = Values.formula(textFormula).toObject();
        }
        this.wsClient.send({
            addText: {
                sheetName: sheetName,
                text: text,
            }
        }, callback);
    }

    /**
     * 
     * @param {string} sheetName 
     * @param {string} tableName 
     * @param {*} query
     * {
     *     schema: schema,
     *     builtinParams: {
     *         p1: {
     *             formulaString: formulaString
     *             // decimalValue: '111', etc
     *         }
     *     },
     *     userParamRules: {
     *         p2: {
     *             allowedTypes: [types...],
     *             nullable: true/false,
     *             allowedValues: [values...]
     *         }
     *     }
     * }
     */
    automateTableByQuery(sheetName, tableName, query, callback) {

        const template = query;

        this.wsClient.send({
            automateTable: {
                sheetName: sheetName,
                tableName: tableName,
                automaton: {
                    queryAutomaton: {
                        template: template,
                    },
                },
            }
        }, callback);
    }

    /**
     * {
     *     data: UnionValue,
     *     filters: [TBD...],
     *     rows: [string...],
     *     columns: [string...],
     *     values: [{
     *         field: ,
     *         aggregateType: ,
     *         label: ,
     *     }]
     * }
     * 
     * @param {string} sheetName 
     * @param {string} tableName 
     * @param {*} pivot 
     */
    automateTableByPivot(sheetName, tableName, pivot, callback) {
        // TODO not implemented yet.
        var auto = {
            sheetName: sheetName,
            tableName: tableName,
            automaton: {
                pivotAutomaton: pivot,
            },
        }
        this.wsClient.send({
            automateTable: auto
        }, callback);
    }

    setCellValue(sheetName, tableName, rowIndex, columnIndex, value, callback) {
        var set = {
            sheetName: sheetName,
            tableName: tableName,
            rowIndex: rowIndex,
            columnIndex: columnIndex,
            value: value
        }
        this.wsClient.send({
            setCellValue: set
        }, callback);
    }

    setCellFormula(sheetName, tableName, rowIndex, columnIndex, formulaString, callback) {
        var set = {
            sheetName: sheetName,
            tableName: tableName,
            rowIndex: rowIndex,
            columnIndex: columnIndex,
            formulaString: formulaString,
        }
        this.wsClient.send({
            setCellFormula: set
        }, callback);
    }

    /**
     * Should not be really used since RefreshCellValue is an internal event other than user operation.
     * @param {string} sheetName 
     * @param {string} tableName 
     * @param {number} rowIndex 
     * @param {number} columnIndex 
     * @param {*} value 
     */
    refreshCellValue(sheetName, tableName, rowIndex, columnIndex, value, callback) {
        var refresh = {
            sheetName: sheetName,
            tableName: tableName,
            rowIndex: rowIndex,
            columnIndex: columnIndex,
            value: value
        }
        this.wsClient.send({
            refreshCellValue: refresh
        }, callback);
    }

    chartConsult(sheetName, tableName, chartType, left, top, right, bottom, callback) {
        var cc = {
            sheetName: sheetName,
            tableName: tableName,
            type: chartType,
            left: left,
            top: top,
            right: right,
            bottom: bottom,
        }
        this.wsClient.send({
            chartConsult: cc
        }, callback);
    }

    eraseColumns(sheetName, tableName, columnIndex, nColumns, callback) {
        var erase = {
            sheetName: sheetName,
            tableName: tableName,
            columnIndex: columnIndex,
            nColumns: nColumns,
        }
        this.wsClient.send({
            eraseColumns: erase
        }, callback);
    }

    eraseRows(sheetName, tableName, rowIndex, nRows, callback) {
        var erase = {
            sheetName: sheetName,
            tableName: tableName,
            rowIndex: rowIndex,
            nRows: nRows,
        }
        this.wsClient.send({
            eraseRows: erase
        }, callback);
    }

    fillUp(sheetName, tableName, rowIndex, firstColumn, lastColumn, nRows, callback) {
        var fill = {
            sheetName: sheetName,
            tableName: tableName,
            rowIndex: rowIndex,
            firstColumn: firstColumn,
            lastColumn: lastColumn,
            nRows: nRows,
        }
        this.wsClient.send({
            fillUp: fill
        }, callback);
    }

    fillRight(sheetName, tableName, columnIndex, firstRow, lastRow, nColumns, callback) {
        var fill = {
            sheetName: sheetName,
            tableName: tableName,
            columnIndex: columnIndex,
            firstRow: firstRow,
            lastRow: lastRow,
            nColumns: nColumns,
        }
        this.wsClient.send({
            fillRight: fill
        }, callback);
    }

    fillDown(sheetName, tableName, rowIndex, firstColumn, lastColumn, nRows, callback) {
        var fill = {
            sheetName: sheetName,
            tableName: tableName,
            rowIndex: rowIndex,
            firstColumn: firstColumn,
            lastColumn: lastColumn,
            nRows: nRows,
        }
        this.wsClient.send({
            fillDown: fill
        }, callback);
    }

    fillLeft(sheetName, tableName, columnIndex, firstRow, lastRow, nColumns, callback) {
        var fill = {
            sheetName: sheetName,
            tableName: tableName,
            columnIndex: columnIndex,
            firstRow: firstRow,
            lastRow: lastRow,
            nColumns: nColumns,
        }
        this.wsClient.send({
            fillLeft: fill
        }, callback);
    }

    insertColumns(sheetName, tableName, columnIndex, nColumns, callback) {
        var insert = {
            sheetName: sheetName,
            tableName: tableName,
            columnIndex: columnIndex,
            nColumns: nColumns,
        }
        this.wsClient.send({ insertColumns: insert }, callback);
    }

    insertRows(sheetName, tableName, rowIndex, nRows, callback) {
        var insert = {
            sheetName: sheetName,
            tableName: tableName,
            rowIndex: rowIndex,
            nRows: nRows,
        }
        this.wsClient.send({ insertRows: insert }, callback);
    }

    moveSheet(sheetName, targetIndex, callback) {
        var move = {
            sheetName: sheetName,
            targetIndex: targetIndex,
        }
        this.wsClient.send({ moveSheet: move }, callback);
    }

    removeAsset(sheetName, assetName, callback) {
        var remove = {
            sheetName: sheetName,
            assetName: assetName,
        }
        this.wsClient.send({ removeAsset: remove }, callback);
    }

    removeColumns(sheetName, tableName, columnIndex, nColumns, callback) {
        var remove = {
            sheetName: sheetName,
            tableName: tableName,
            columnIndex: columnIndex,
            nColumns: nColumns,
        }
        this.wsClient.send({ removeColumns: remove }, callback)
    }

    removeRows(sheetName, tableName, rowIndex, nRows, callback) {
        var remove = {
            sheetName: sheetName,
            tableName: tableName,
            rowIndex: rowIndex,
            nRows: nRows,
        }
        this.wsClient.send({ removeRows: remove }, callback);
    }

    removeSheet(sheetName, callback) {
        var remove = {
            sheetName: sheetName,
        }
        this.wsClient.send({ removeSheet: remove }, callback);
    }

    renameAsset(sheetName, oldAssetName, newAssetName, callback) {
        var rename = {
            sheetName: sheetName,
            oldAssetName: oldAssetName,
            newAssetName: newAssetName,
        }
        this.wsClient.send({ renameAsset: rename }, callback);
    }

    renameSheet(oldSheetName, newSheetName, callback) {
        var rename = {
            oldSheetName: oldSheetName,
            newSheetName: newSheetName,
        }
        this.wsClient.send({ renameSheet: rename }, callback);
    }

    updateAutomaton(callback) {
        // TODO
        var action = {};
        this.wsClient.send(action, callback);
    }

    updateChart(sheetName, chartSettings, callback) {
        // var c = this.createChartBySettings(chartSettings);
        var update = {
            sheetName: sheetName,
            chart: chartSettings,
        }
        this.wsClient.send({ updateChart: update }, callback);
    }

    createWorkbook(pathname, callback) {
        var createWorkbook = {
            pathname: pathname,
        }
        this.wsClient.send({ createWorkbook: createWorkbook }, callback);
    }

    openWorkbook(pathname, callback) {
        var openWorkbook = {
            pathname: pathname,
        }
        this.wsClient.send({ openWorkbook: openWorkbook }, callback);
    }

    saveWorkbook(callback) {
        var saveWorkbook = {}
        this.wsClient.send({ saveWorkbook: saveWorkbook }, callback);
    }

    closeWorkbook(callback) {
        var closeWorkbook = {};
        this.wsClient.send({ closeWorkbook: closeWorkbook }, callback);
    }

    layoutAsset(sheetName, assetName, layout, callback) {
        var layoutAsset = {
            sheetName: sheetName,
            assetName: assetName,
            layout: layout,
        }
        this.wsClient.send({ layoutAsset: layoutAsset }, callback);
    }

    updateText(sheetName, textName, textValue, callback) {
        var text = {
            name: textName,
        }
        if (textValue) {
            text.content = textValue;
        }
        var update = {
            sheetName: sheetName,
            text: text,
        }
        this.wsClient.send({ updateText: update }, callback);
    }

    executeQuery(sheetName, tableName, userParams, callback) {
        let execQuery = {
            sheetName: sheetName,
            tableName: tableName,
            params: userParams,
        }
        this.wsClient.send({ executeQuery: execQuery }, callback);
    }

    /**
     * Spreadsheet protobuf client.
     * @param {string} wsUri 
     */
    createWebSocketClient(wsUri, listener) {

        var _wsUri = wsUri;

        var websocket;
        var isOpen = false;

        var _txId = 1;
        var msgCallbacks = {};

        var timer;

        websocket = new WebSocket(_wsUri);

        websocket.onopen = function (evt) {
            isOpen = true;
            timer = setInterval(ping, 15000);
            onOpen(evt);
        };

        websocket.onclose = function (evt) {
            isOpen = false;
            clearInterval(timer);
            onClose(evt);
        };

        websocket.onmessage = function (evt) {
            onMessage(evt)
        };

        websocket.onerror = function (evt) {
            onError(evt)
        };

        var ping = function () {
            if (isReady()) {
                msgCallbacks['txid-0'] = function () { };
                websocket.send('{txId:0}');
            }
        }

        var onOpen = function (evt) {
            console.log("Connected.");
            if (typeof listener !== 'undefined'
                && typeof listener.onConnected === 'function') {
                listener.onConnected();
            }
        };

        var onClose = function (evt) {
            console.log("Connection closed.");
            if (typeof listener !== 'undefined'
                && typeof listener.onDisconnected === 'function') {
                listener.onDisconnected();
            }
        };

        var onMessage = function (evt) {
            var msg = JSON.parse(evt.data);
            console.log('onMessage', msg);
            var key = 'txid-' + msg.txId;
            var msgCallback = msgCallbacks[key];
            if (typeof msgCallback === 'function') {
                msgCallback(msg);
                delete msgCallbacks[key];
            } else {
                console.log('WARN: msgCallback not defined!');
            }

            if (typeof listener !== 'undefined'
                && typeof listener.afterResponse === 'function') {
                listener.afterResponse(msg);
            }
        };

        var onError = function (evt) {
            console.log("WebSocket error occurred.");
            msgCallbacks = {}; // reset callback functions
            // FIXME there could be some problems.
            if (typeof listener !== 'undefined'
                && typeof listener.onError === 'function') {
                listener.onError('WebSocket error!');
            }
        };

        var isReady = function () {
            return websocket && isOpen;
        };

        var send = function (action, callback) {
            if (!isReady()) {
                throw new Error("WebSocket not ready.");
            }
            var txId = _txId++;
            var req = {
                txId: txId,
                action: action
            }

            if (typeof callback === 'function') {
                msgCallbacks['txid-' + txId] = callback;
            }

            if (typeof listener !== 'undefined'
                && typeof listener.beforeRequest === 'function') {
                listener.beforeRequest(txId);
            }

            websocket.send(JSON.stringify(req));
        };

        var sendRequest = function (request, callback) {
            if (!isReady()) {
                throw new Error("WebSocket not ready.");
            }
            if (typeof callback === 'function') {
                msgCallbacks['txid-' + request.txId] = callback;
            }
            if (typeof listener !== 'undefined'
                && typeof listener.beforeRequest === 'function') {
                listener.beforeRequest(request.txId);
            }
            websocket.send(JSON.stringify(request));
        }

        return {
            isReady: isReady,
            send: send,
            sendRequest: sendRequest,
        };

    }
}

export class PbHelper {
    static assetType(asset) {
        for (let k in asset) {
            if (asset.hasOwnProperty(k)
                && typeof asset[k] === 'object'
                && asset[k].hasOwnProperty('name')
                && typeof asset[k].name === 'string') {
                return k;
            }
        }
    }
    static assetName(asset) {
        for (let k in asset) {
            if (asset.hasOwnProperty(k)
                && typeof asset[k] === 'object'
                && asset[k].hasOwnProperty('name')
                && typeof asset[k].name === 'string') {
                return asset[k].name;
            }
        }
    }
}