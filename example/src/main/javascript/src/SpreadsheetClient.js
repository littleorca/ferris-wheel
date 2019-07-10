import EditResponse from "@littleorca/ferris-wheel/lib/action/EditResponse";
import Values from '@littleorca/ferris-wheel/lib/model/Values';

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
        if (resp.changes) {
            for (let i = 0; i < resp.changes.actions.length; i++) {
                const act = resp.changes.actions[i];
                for (let k in act) {
                    if (!act.hasOwnProperty(k)) {
                        continue;
                    }
                    const listeners = this.registeredChangeListeners[k];
                    if (typeof listeners !== 'undefined' && Array.isArray(listeners)) {
                        for (let j = 0; j < listeners.length; j++) {
                            console.log("notify", act[k]);
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
            var msg = EditResponse.deserialize(JSON.parse(evt.data));
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
